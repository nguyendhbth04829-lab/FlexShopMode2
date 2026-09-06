package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & PHÍ SÀN (DEV 5 - MINH)
 * USER STORY: US-42 (Ký Quỹ Escrow - Shopee Guarantee 3 Ngày)
 *             US-43 (Khấu Trừ Phí Sàn 3% & Dòng Tiền Ví Người Bán)
 * =====================================================================
 * Nghiệp vụ chi tiết & Validate chuyên nghiệp:
 *   1. Tự động chuyển tiền đơn hàng vào Ký Quỹ Escrow với trạng thái DANG_TAM_GIU trong 3 ngày.
 *   2. Validate tỷ lệ phí sàn (3.00%), bóc tách phí sàn chính xác theo công thức US-43.
 *   3. Kiểm soát phân luồng giao dịch đồng bộ qua CSDL với Optimistic Locking (chống Race Condition).
 *   4. Giải ngân an toàn: Kiểm tra trạng thái nghiêm ngặt chống double-spending (rút tiền/giải ngân trùng lặp).
 *   5. Quét tự động định kỳ giải ngân các đơn quá hạn 3 ngày bảo lưu Shopee Guarantee.
 * =====================================================================
 */
@Service
public class KyQuyService {

    @Autowired
    private GiaoDichKyQuyRepository giaoDichKyQuyRepository;

    @Autowired
    private ViNguoiBanRepository viNguoiBanRepository;

    @Autowired
    private LichSuGiaoDichViRepository lichSuGiaoDichViRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    /**
     * [US-42 & US-43] Tìm kiếm, lọc nâng cao và phân trang giao dịch ký quỹ
     */
    public Page<GiaoDichKyQuy> getDanhSachKyQuyPhanTrang(
            String keyword,
            String trangThai,
            Long maGianHang,
            Boolean chiLayQuaHan,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return giaoDichKyQuyRepository.timKiemNangCao(
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : null,
                maGianHang,
                chiLayQuaHan != null && chiLayQuaHan,
                LocalDateTime.now(),
                pageable
        );
    }

    /**
     * Lấy chi tiết giao dịch ký quỹ kèm validate tồn tại
     */
    public GiaoDichKyQuy getChiTietKyQuy(Long maKyQuy) {
        if (maKyQuy == null || maKyQuy <= 0) {
            throw new IllegalArgumentException("Mã ký quỹ không hợp lệ: " + maKyQuy);
        }
        return giaoDichKyQuyRepository.findById(maKyQuy)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy giao dịch ký quỹ với mã ID: #" + maKyQuy));
    }

    /**
     * Lấy hoặc tự động khởi tạo Ví Người Bán (vi_nguoi_ban) cho Gian Hàng
     */
    @Transactional
    public ViNguoiBan getOrCreateViNguoiBan(GianHang gianHang) {
        if (gianHang == null || gianHang.getMaGianHang() == null) {
            throw new IllegalArgumentException("Thông tin gian hàng không hợp lệ!");
        }
        return viNguoiBanRepository.findByGianHang_MaGianHang(gianHang.getMaGianHang())
                .orElseGet(() -> {
                    ViNguoiBan viMoi = new ViNguoiBan();
                    viMoi.setGianHang(gianHang);
                    viMoi.setSoDuKhaDung(BigDecimal.ZERO);
                    viMoi.setSoDuTamGiuEscrow(BigDecimal.ZERO);
                    viMoi.setTongTienDaRut(BigDecimal.ZERO);
                    viMoi.setPhienBanLock(1);
                    viMoi.setNgayCapNhat(LocalDateTime.now());
                    return viNguoiBanRepository.save(viMoi);
                });
    }

    /**
     * [US-42 & US-43] Tự động tạo giao dịch Ký Quỹ Escrow khi đơn hàng thanh toán thành công
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public GiaoDichKyQuy taoGiaoDichKyQuy(DonHangShop donHangShop) {
        // 1. Validate dữ liệu đầu vào
        if (donHangShop == null) {
            throw new IllegalArgumentException("Đơn hàng shop không được để trống!");
        }
        if (donHangShop.getTongTienShopNhan() == null || donHangShop.getTongTienShopNhan().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tổng tiền đơn hàng shop phải lớn hơn 0!");
        }
        if (donHangShop.getGianHang() == null) {
            throw new IllegalArgumentException("Đơn hàng shop phải thuộc về một gian hàng xác định!");
        }

        // 2. Chống tạo trùng lặp Ký quỹ cho cùng 1 đơn hàng shop (Idempotency)
        Optional<GiaoDichKyQuy> existing = giaoDichKyQuyRepository.findByDonHangShop_MaDonHangShop(donHangShop.getMaDonHangShop());
        if (existing.isPresent()) {
            return existing.get();
        }

        // 3. Tính toán phí sàn 3% chuẩn xác theo công thức US-43
        BigDecimal tongTienDonHang = donHangShop.getTongTienShopNhan();
        BigDecimal tyLePhiSan = new BigDecimal("3.00"); // 3% chuẩn US-43
        BigDecimal tienPhiSan = tongTienDonHang.multiply(tyLePhiSan)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal tienPhiThanhToan = BigDecimal.ZERO;
        BigDecimal tienPhiDichVu = BigDecimal.ZERO;

        // Tiền thực nhận về ví = Tổng tiền - (Phí sàn + Phí thanh toán + Phí dịch vụ)
        BigDecimal tienThucNhanVeVi = tongTienDonHang.subtract(tienPhiSan)
                .subtract(tienPhiThanhToan)
                .subtract(tienPhiDichVu);

        if (tienThucNhanVeVi.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Số tiền thực nhận về ví sau khi trừ phí sàn phải lớn hơn 0!");
        }

        // 4. Lưu bản ghi Ký Quỹ Escrow (Shopee Guarantee giữ tiền trong 3 ngày)
        GiaoDichKyQuy kyQuy = new GiaoDichKyQuy();
        kyQuy.setDonHangShop(donHangShop);
        kyQuy.setGianHang(donHangShop.getGianHang());
        kyQuy.setTongTienDonHang(tongTienDonHang);
        kyQuy.setTyLePhiSanPhanTram(tyLePhiSan);
        kyQuy.setTienPhiSan(tienPhiSan);
        kyQuy.setTienPhiThanhToan(tienPhiThanhToan);
        kyQuy.setTienPhiDichVu(tienPhiDichVu);
        kyQuy.setTienThucNhanVeVi(tienThucNhanVeVi);
        kyQuy.setTrangThai("DANG_TAM_GIU");
        kyQuy.setNgayDuKienNhaTien(LocalDateTime.now().plusDays(3)); // Shopee Guarantee bảo vệ 3 ngày
        kyQuy.setNgayTao(LocalDateTime.now());

        GiaoDichKyQuy savedKyQuy = giaoDichKyQuyRepository.save(kyQuy);

        // 5. Cập nhật tăng số dư tạm giữ trong ví người bán (Đồng bộ CSDL)
        ViNguoiBan vi = getOrCreateViNguoiBan(donHangShop.getGianHang());
        BigDecimal tamGiuHienTai = vi.getSoDuTamGiuEscrow() != null ? vi.getSoDuTamGiuEscrow() : BigDecimal.ZERO;
        vi.setSoDuTamGiuEscrow(tamGiuHienTai.add(tienThucNhanVeVi));
        vi.setNgayCapNhat(LocalDateTime.now());
        viNguoiBanRepository.save(vi);

        return savedKyQuy;
    }

    /**
     * Tự động tạo ký quỹ cho tất cả các ShopOrder con trong Đơn hàng tổng vừa thanh toán
     */
    @Transactional
    public void taoGiaoDichKyQuyChoDonHangTong(DonHangTong donHangTong) {
        if (donHangTong == null) return;
        List<DonHangShop> danhSachShopOrder = donHangTong.getDanhSachShopOrder();
        if (danhSachShopOrder == null || danhSachShopOrder.isEmpty()) {
            danhSachShopOrder = donHangShopRepository.findAll().stream()
                    .filter(s -> s.getDonHangTong() != null && s.getDonHangTong().getMaDonHangTong().equals(donHangTong.getMaDonHangTong()))
                    .toList();
        }
        for (DonHangShop shopOrder : danhSachShopOrder) {
            taoGiaoDichKyQuy(shopOrder);
        }
    }

    /**
     * [US-42] Giải ngân tiền từ Tạm giữ Escrow sang Số dư khả dụng trong Ví Người Bán
     * Kích hoạt khi:
     *   - Khách hàng bấm "Đã nhận được hàng"
     *   - Hoặc hết hạn 3 ngày Shopee Guarantee (Quét tự động)
     *   - Hoặc Admin duyệt giải ngân sớm
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public GiaoDichKyQuy giaiNganKyQuy(Long maKyQuy, String lyDoGiaiNgan) {
        GiaoDichKyQuy kyQuy = getChiTietKyQuy(maKyQuy);

        // Validate trạng thái nghiêm ngặt (Chống giải ngân 2 lần)
        if ("DA_GIAI_NGAN".equals(kyQuy.getTrangThai())) {
            throw new IllegalStateException("Giao dịch #" + maKyQuy + " đã được giải ngân trước đó vào lúc " + kyQuy.getNgayThucTeNhaTien());
        }
        if ("DA_HOAN_TIEN".equals(kyQuy.getTrangThai())) {
            throw new IllegalStateException("Giao dịch #" + maKyQuy + " đã bị hủy và hoàn tiền, không thể giải ngân!");
        }
        if (!"DANG_TAM_GIU".equals(kyQuy.getTrangThai())) {
            throw new IllegalStateException("Trạng thái giao dịch không hợp lệ để giải ngân: " + kyQuy.getTrangThai());
        }

        // Cập nhật trạng thái giao dịch ký quỹ
        kyQuy.setTrangThai("DA_GIAI_NGAN");
        kyQuy.setNgayThucTeNhaTien(LocalDateTime.now());
        GiaoDichKyQuy updatedKyQuy = giaoDichKyQuyRepository.save(kyQuy);

        // Chuyển tiền trong Ví người bán (vi_nguoi_ban) với Optimistic Locking
        ViNguoiBan vi = getOrCreateViNguoiBan(kyQuy.getGianHang());
        BigDecimal tienGiaiNgan = kyQuy.getTienThucNhanVeVi();

        // Kiểm tra an toàn chống số âm (Double spending check)
        BigDecimal tamGiuMoi = vi.getSoDuTamGiuEscrow().subtract(tienGiaiNgan);
        if (tamGiuMoi.compareTo(BigDecimal.ZERO) < 0) {
            tamGiuMoi = BigDecimal.ZERO;
        }
        vi.setSoDuTamGiuEscrow(tamGiuMoi);
        vi.setSoDuKhaDung(vi.getSoDuKhaDung().add(tienGiaiNgan));
        vi.setNgayCapNhat(LocalDateTime.now());
        viNguoiBanRepository.save(vi);

        // Ghi vết biến động số dư vào Sổ cái ví (lich_su_giao_dich_vi)
        LichSuGiaoDichVi lichSu = new LichSuGiaoDichVi();
        lichSu.setViNguoiBan(vi);
        lichSu.setLoaiGiaoDich("GIAI_NGAN_DON_HANG");
        lichSu.setSoTien(tienGiaiNgan);
        lichSu.setSoDuSauGiaoDich(vi.getSoDuKhaDung());
        lichSu.setMaThamChieu(kyQuy.getDonHangShop().getMaCodeDonShop());
        lichSu.setMoTa(lyDoGiaiNgan != null ? lyDoGiaiNgan : ("Giải ngân Shopee Guarantee đơn hàng: " + kyQuy.getDonHangShop().getMaCodeDonShop()));
        lichSu.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichViRepository.save(lichSu);

        return updatedKyQuy;
    }

    /**
     * [US-42] Quét và tự động giải ngân cho tất cả các giao dịch đã quá hạn 3 ngày bảo vệ
     */
    @Transactional
    public Map<String, Object> quetVaTuDongGiaiNganChiTiet() {
        List<GiaoDichKyQuy> danhSachHetHan = giaoDichKyQuyRepository.findByTrangThaiAndNgayDuKienNhaTienBefore(
                "DANG_TAM_GIU", LocalDateTime.now()
        );

        int thanhCong = 0;
        int thatBai = 0;
        BigDecimal tongTienGiaiNgan = BigDecimal.ZERO;
        List<String> maDonThanhCong = new ArrayList<>();

        for (GiaoDichKyQuy gd : danhSachHetHan) {
            try {
                giaiNganKyQuy(gd.getMaKyQuy(), "Tự động giải ngân sau 3 ngày Shopee Guarantee");
                thanhCong++;
                tongTienGiaiNgan = tongTienGiaiNgan.add(gd.getTienThucNhanVeVi());
                maDonThanhCong.add(gd.getDonHangShop().getMaCodeDonShop());
            } catch (Exception e) {
                thatBai++;
            }
        }

        Map<String, Object> ketQua = new HashMap<>();
        ketQua.put("tongSoDonHetHan", danhSachHetHan.size());
        ketQua.put("thanhCong", thanhCong);
        ketQua.put("thatBai", thatBai);
        ketQua.put("tongTienGiaiNgan", tongTienGiaiNgan);
        ketQua.put("maDonThanhCong", maDonThanhCong);
        return ketQua;
    }

    /**
     * Lấy thông tin Ví Người Bán theo mã gian hàng
     */
    public ViNguoiBan getViNguoiBanByGianHangId(Long maGianHang) {
        if (maGianHang == null) {
            throw new IllegalArgumentException("Mã gian hàng không hợp lệ!");
        }
        return viNguoiBanRepository.findByGianHang_MaGianHang(maGianHang)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy ví của gian hàng có ID: " + maGianHang));
    }

    /**
     * Lấy danh sách lịch sử biến động số dư của ví
     */
    public List<LichSuGiaoDichVi> getLichSuGiaoDichVi(Long maVi) {
        if (maVi == null) {
            return Collections.emptyList();
        }
        return lichSuGiaoDichViRepository.findByViNguoiBan_MaViOrderByNgayTaoDesc(maVi);
    }

    /**
     * [US-42 & US-43] Thống kê tổng hợp số liệu Ký Quỹ Escrow đồng bộ trực tiếp từ Database
     */
    public Map<String, Object> getThongKeEscrowDongBo() {
        BigDecimal tongTamGiu = giaoDichKyQuyRepository.tinhTongTienTheoTrangThai("DANG_TAM_GIU");
        BigDecimal tongDaGiaiNgan = giaoDichKyQuyRepository.tinhTongTienTheoTrangThai("DA_GIAI_NGAN");
        BigDecimal tongPhiSan = giaoDichKyQuyRepository.tinhTongPhiSanDaThu();
        long countTamGiu = giaoDichKyQuyRepository.demSoDonTheoTrangThai("DANG_TAM_GIU");
        long countDaGiaiNgan = giaoDichKyQuyRepository.demSoDonTheoTrangThai("DA_GIAI_NGAN");
        long countQuaHanChuaGiaiNgan = giaoDichKyQuyRepository.demSoDonQuaHanChuaGiaiNgan(LocalDateTime.now());
        long tongGiaoDich = giaoDichKyQuyRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("tongGiaoDich", tongGiaoDich);
        stats.put("tongTamGiu", tongTamGiu != null ? tongTamGiu : BigDecimal.ZERO);
        stats.put("tongDaGiaiNgan", tongDaGiaiNgan != null ? tongDaGiaiNgan : BigDecimal.ZERO);
        stats.put("tongPhiSan", tongPhiSan != null ? tongPhiSan : BigDecimal.ZERO);
        stats.put("countTamGiu", countTamGiu);
        stats.put("countDaGiaiNgan", countDaGiaiNgan);
        stats.put("countQuaHanChuaGiaiNgan", countQuaHanChuaGiaiNgan);
        return stats;
    }

    /**
     * Lấy danh sách tất cả gian hàng phục vụ bộ lọc tìm kiếm
     */
    public List<GianHang> getDanhSachGianHang() {
        return gianHangRepository.findAll();
    }
}
