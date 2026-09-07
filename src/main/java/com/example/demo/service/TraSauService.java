package com.example.demo.service;

import com.example.demo.dto.DangKyTraSauRequestDTO;
import com.example.demo.dto.ThanhToanKyTraSauRequestDTO;
import com.example.demo.dto.TraSauThongKeDTO;
import com.example.demo.dto.VayTraSauRequestDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - Dịch Vụ Mua Trước Trả Sau (SPayLater / BNPL)
 * =====================================================================
 */
@Service
public class TraSauService {

    @Autowired
    private TaiKhoanTraSauRepository taiKhoanTraSauRepository;

    @Autowired
    private HopDongTraSauRepository hopDongTraSauRepository;

    @Autowired
    private KyThanhToanTraSauRepository kyThanhToanTraSauRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private KyQuyService kyQuyService;

    /**
     * Lấy tài khoản trả sau của người dùng hoặc tự động kích hoạt tài khoản ban đầu nếu chưa có.
     */
    @Transactional
    public TaiKhoanTraSau layHoacTaoTaiKhoan(Long maNguoiDung) {
        return taiKhoanTraSauRepository.findByNguoiDung_MaNguoiDung(maNguoiDung)
                .orElseGet(() -> {
                    NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với mã ID: " + maNguoiDung));

                    TaiKhoanTraSau tk = new TaiKhoanTraSau();
                    tk.setNguoiDung(nguoiDung);
                    tk.setHanMucDuocCap(new BigDecimal("5000000.00"));
                    tk.setHanMucConLai(new BigDecimal("5000000.00"));
                    tk.setDiemTinDung(650);
                    tk.setTrangThai("HOAT_DONG");
                    tk.setNgayCap(LocalDateTime.now());
                    return taiKhoanTraSauRepository.save(tk);
                });
    }

    /**
     * Kích hoạt hoặc cập nhật hồ sơ ví trả sau SPayLater
     */
    @Transactional
    public TaiKhoanTraSau kichHoatTaiKhoan(DangKyTraSauRequestDTO dto) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(dto.getMaNguoiDung())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng với ID: " + dto.getMaNguoiDung()));

        TaiKhoanTraSau tk = taiKhoanTraSauRepository.findByNguoiDung_MaNguoiDung(dto.getMaNguoiDung())
                .orElse(new TaiKhoanTraSau());

        BigDecimal hanMucCap = new BigDecimal("5000000.00");
        if (dto.getThuNhapThang() != null && dto.getThuNhapThang().compareTo(new BigDecimal("15000000.00")) >= 0) {
            hanMucCap = new BigDecimal("10000000.00");
        } else if (dto.getThuNhapThang() != null && dto.getThuNhapThang().compareTo(new BigDecimal("8000000.00")) >= 0) {
            hanMucCap = new BigDecimal("7000000.00");
        }

        if (tk.getMaTkTraSau() == null) {
            tk.setNguoiDung(nguoiDung);
            tk.setHanMucDuocCap(hanMucCap);
            tk.setHanMucConLai(hanMucCap);
            tk.setDiemTinDung(680);
            tk.setTrangThai("HOAT_DONG");
            tk.setNgayCap(LocalDateTime.now());
        } else {
            // Nâng hạn mức nếu hồ sơ mới đạt điều kiện tốt hơn
            if (hanMucCap.compareTo(tk.getHanMucDuocCap()) > 0) {
                BigDecimal tangThem = hanMucCap.subtract(tk.getHanMucDuocCap());
                tk.setHanMucDuocCap(hanMucCap);
                tk.setHanMucConLai(tk.getHanMucConLai().add(tangThem));
            }
            if (!"BI_KHOA".equalsIgnoreCase(tk.getTrangThai())) {
                tk.setTrangThai("HOAT_DONG");
            }
        }

        return taiKhoanTraSauRepository.save(tk);
    }

    /**
     * Kiểm tra điều kiện vay trả sau của khách hàng
     */
    public void kiemTraDieuKienVay(Long maNguoiDung, BigDecimal soTienDonHang) {
        TaiKhoanTraSau tk = taiKhoanTraSauRepository.findByNguoiDung_MaNguoiDung(maNguoiDung)
                .orElseThrow(() -> new RuntimeException("Quý khách chưa đăng ký kích hoạt Ví Trả Sau SPayLater! Vui lòng kích hoạt ví trước khi thanh toán."));

        if ("BI_KHOA".equalsIgnoreCase(tk.getTrangThai())) {
            throw new RuntimeException("Tài khoản SPayLater của quý khách đã bị KHÓA VĨNH VIỄN do vi phạm điều khoản tín dụng.");
        }

        if ("TAM_KHOA".equalsIgnoreCase(tk.getTrangThai())) {
            throw new RuntimeException("Tài khoản SPayLater của quý khách đang bị TẠM KHÓA do có kỳ nợ quá hạn. Vui lòng tất toán các khoản nợ quá hạn để mở khóa.");
        }

        long soKyQuaHan = kyThanhToanTraSauRepository.demSoKyQuaHanCuaTaiKhoan(tk.getMaTkTraSau(), LocalDate.now());
        if (soKyQuaHan > 0) {
            throw new RuntimeException("Quý khách hiện đang có " + soKyQuaHan + " kỳ thanh toán QUÁ HẠN. Vui lòng thanh toán nợ trước khi tiếp tục giao dịch.");
        }

        if (tk.getDiemTinDung() != null && tk.getDiemTinDung() < 500) {
            throw new RuntimeException("Điểm tín dụng của quý khách (" + tk.getDiemTinDung() + ") chưa đạt mức tối thiểu 500 điểm để thanh toán trả sau.");
        }

        if (soTienDonHang == null || soTienDonHang.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị đơn hàng không hợp lệ để thực hiện thanh toán trả sau.");
        }

        if (soTienDonHang.compareTo(tk.getHanMucConLai()) > 0) {
            throw new RuntimeException("Giá trị đơn hàng (" + String.format("%,.0f", soTienDonHang) + " VNĐ) vượt quá hạn mức khả dụng còn lại ("
                    + String.format("%,.0f", tk.getHanMucConLai()) + " VNĐ). Vui lòng chọn phương thức thanh toán khác hoặc trả bớt nợ cũ.");
        }
    }

    /**
     * Tính toán bảng dự tính trả góp (gốc + lãi + số tiền mỗi kỳ)
     */
    public List<KyThanhToanTraSau> tinhDuTinhKyTraGop(BigDecimal tongTien, int soKy) {
        List<KyThanhToanTraSau> dsKy = new ArrayList<>();
        if (tongTien == null || soKy <= 0) return dsKy;

        BigDecimal laiSuatThang = layLaiSuatTheoSoKy(soKy);
        BigDecimal gocMoiKy = tongTien.divide(new BigDecimal(soKy), 2, RoundingMode.HALF_UP);
        BigDecimal laiMoiKy = tongTien.multiply(laiSuatThang).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal tongGocDaChia = BigDecimal.ZERO;

        for (int i = 1; i <= soKy; i++) {
            KyThanhToanTraSau ky = new KyThanhToanTraSau();
            ky.setKySo(i);
            ky.setHanChotThanhToan(LocalDate.now().plusDays(30L * i));
            ky.setTrangThai("CHUA_TRA");
            ky.setSoTienDaTra(BigDecimal.ZERO);

            BigDecimal gocKyNay;
            if (i == soKy) {
                // Kỳ cuối hấp thụ phần lẻ làm tròn để tổng gốc khớp 100%
                gocKyNay = tongTien.subtract(tongGocDaChia);
            } else {
                gocKyNay = gocMoiKy;
                tongGocDaChia = tongGocDaChia.add(gocKyNay);
            }

            BigDecimal canTra = gocKyNay.add(laiMoiKy);
            ky.setSoTienCanTra(canTra);
            dsKy.add(ky);
        }
        return dsKy;
    }

    /**
     * Xác định lãi suất tháng (%/tháng) dựa trên số kỳ
     */
    public BigDecimal layLaiSuatTheoSoKy(int soKy) {
        return switch (soKy) {
            case 1 -> BigDecimal.ZERO; // 0% trong 30 ngày
            case 3 -> new BigDecimal("1.00"); // 1.0%/tháng
            case 6 -> new BigDecimal("1.50"); // 1.5%/tháng
            case 12 -> new BigDecimal("2.00"); // 2.0%/tháng
            default -> new BigDecimal("1.50");
        };
    }

    /**
     * Tạo hợp đồng trả sau, phân kỳ trả góp, trừ hạn mức và xác nhận thanh toán đơn hàng
     */
    @Transactional
    public HopDongTraSau taoHopDongVaGiaiNgan(VayTraSauRequestDTO dto) {
        DonHangTong donHang = donHangTongRepository.findById(dto.getMaDonHangTong())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đơn hàng tổng với mã ID: " + dto.getMaDonHangTong()));

        if ("DA_THANH_TOAN".equalsIgnoreCase(donHang.getTrangThaiThanhToan())) {
            throw new RuntimeException("Đơn hàng này đã được thanh toán thành công trước đó!");
        }

        // Kiểm tra hợp đồng trả sau đã tồn tại cho đơn hàng này chưa
        if (hopDongTraSauRepository.findByDonHangTong_MaDonHangTong(donHang.getMaDonHangTong()).isPresent()) {
            throw new RuntimeException("Đơn hàng này đã được liên kết với một hợp đồng trả sau khác!");
        }

        Long maKhachHang = donHang.getKhachHang().getMaNguoiDung();
        TaiKhoanTraSau taiKhoan = layHoacTaoTaiKhoan(maKhachHang);

        BigDecimal soTienVay = donHang.getTongThanhToanCuoi();
        kiemTraDieuKienVay(maKhachHang, soTienVay);

        // 1. Trừ hạn mức còn lại
        taiKhoan.setHanMucConLai(taiKhoan.getHanMucConLai().subtract(soTienVay));
        taiKhoanTraSauRepository.save(taiKhoan);

        // 2. Tạo Hợp đồng trả sau
        int soKy = dto.getSoKyTraGop() != null ? dto.getSoKyTraGop() : 3;
        BigDecimal laiSuat = layLaiSuatTheoSoKy(soKy);

        HopDongTraSau hopDong = new HopDongTraSau();
        hopDong.setTaiKhoanTraSau(taiKhoan);
        hopDong.setDonHangTong(donHang);
        hopDong.setTongSoTienVay(soTienVay);
        hopDong.setSoKyTraGop(soKy);
        hopDong.setLaiSuatThangPhanTram(laiSuat);
        hopDong.setNgayTao(LocalDateTime.now());

        HopDongTraSau savedHopDong = hopDongTraSauRepository.save(hopDong);

        // 3. Sinh các kỳ thanh toán
        List<KyThanhToanTraSau> dsKyDuTinh = tinhDuTinhKyTraGop(soTienVay, soKy);
        for (KyThanhToanTraSau k : dsKyDuTinh) {
            k.setHopDongTraSau(savedHopDong);
            kyThanhToanTraSauRepository.save(k);
        }
        savedHopDong.setDanhSachKy(dsKyDuTinh);

        // 4. Cập nhật Đơn hàng tổng sang ĐÃ THANH TOÁN
        donHang.setPhuongThucThanhToan("SPAYLATER");
        donHang.setTrangThaiThanhToan("DA_THANH_TOAN");
        donHang.setGhiChu(donHang.getGhiChu() != null ?
                donHang.getGhiChu() + " | Thanh toán SPayLater HD#" + savedHopDong.getMaHopDong() :
                "Thanh toán SPayLater HD#" + savedHopDong.getMaHopDong());
        donHangTongRepository.save(donHang);

        // 5. Kích hoạt Ký Quỹ Escrow bảo vệ người bán (US-42 & US-43)
        kyQuyService.taoGiaoDichKyQuyChoDonHangTong(donHang);

        return savedHopDong;
    }

    /**
     * Thanh toán trả nợ cho từng kỳ trả góp
     */
    @Transactional
    public KyThanhToanTraSau thanhToanKyTraGop(ThanhToanKyTraSauRequestDTO dto) {
        KyThanhToanTraSau ky = kyThanhToanTraSauRepository.findById(dto.getMaKy())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kỳ thanh toán với mã ID: " + dto.getMaKy()));

        if ("DA_TRA".equalsIgnoreCase(ky.getTrangThai())) {
            throw new RuntimeException("Kỳ thanh toán số " + ky.getKySo() + " đã được tất toán trước đó!");
        }

        HopDongTraSau hopDong = ky.getHopDongTraSau();
        TaiKhoanTraSau taiKhoan = hopDong.getTaiKhoanTraSau();

        BigDecimal soTienTra = dto.getSoTienThanhToan();
        if (soTienTra == null || soTienTra.compareTo(ky.getSoTienCanTra()) < 0) {
            throw new RuntimeException("Số tiền thanh toán (" + String.format("%,.0f", soTienTra) + " VNĐ) không đủ số tiền của kỳ này ("
                    + String.format("%,.0f", ky.getSoTienCanTra()) + " VNĐ).");
        }

        // Cập nhật kỳ thanh toán
        ky.setSoTienDaTra(soTienTra);
        ky.setTrangThai("DA_TRA");
        KyThanhToanTraSau updatedKy = kyThanhToanTraSauRepository.save(ky);

        // Hoàn trả hạn mức khả dụng tương ứng phần nợ gốc của kỳ
        BigDecimal gocMoiKy = hopDong.getTongSoTienVay()
                .divide(new BigDecimal(hopDong.getSoKyTraGop()), 2, RoundingMode.HALF_UP);
        BigDecimal hanMucMoi = taiKhoan.getHanMucConLai().add(gocMoiKy).min(taiKhoan.getHanMucDuocCap());
        taiKhoan.setHanMucConLai(hanMucMoi);

        // Tăng điểm tín dụng thưởng (+5 điểm)
        int diemMoi = Math.min(850, (taiKhoan.getDiemTinDung() != null ? taiKhoan.getDiemTinDung() : 650) + 5);
        taiKhoan.setDiemTinDung(diemMoi);

        // Nếu tài khoản đang tạm khóa và không còn kỳ quá hạn nào nữa thì tự động mở lại
        long soKyQuaHanConLai = kyThanhToanTraSauRepository.demSoKyQuaHanCuaTaiKhoan(taiKhoan.getMaTkTraSau(), LocalDate.now());
        if (soKyQuaHanConLai == 0 && "TAM_KHOA".equalsIgnoreCase(taiKhoan.getTrangThai())) {
            taiKhoan.setTrangThai("HOAT_DONG");
        }

        taiKhoanTraSauRepository.save(taiKhoan);

        return updatedKy;
    }

    /**
     * Quét tự động và cập nhật trạng thái nợ quá hạn
     */
    @Transactional
    public int kiemTraVaCapNhatNoQuaHan() {
        List<KyThanhToanTraSau> dsQuaHan = kyThanhToanTraSauRepository.timDanhSachKyQuaHan(LocalDate.now());
        int count = 0;
        for (KyThanhToanTraSau ky : dsQuaHan) {
            if (!"QUA_HAN".equalsIgnoreCase(ky.getTrangThai())) {
                ky.setTrangThai("QUA_HAN");
                kyThanhToanTraSauRepository.save(ky);

                // Tạm khóa tài khoản khách hàng và hạ điểm tín dụng
                TaiKhoanTraSau tk = ky.getHopDongTraSau().getTaiKhoanTraSau();
                tk.setTrangThai("TAM_KHOA");
                int diem = Math.max(300, (tk.getDiemTinDung() != null ? tk.getDiemTinDung() : 650) - 20);
                tk.setDiemTinDung(diem);
                taiKhoanTraSauRepository.save(tk);

                count++;
            }
        }
        return count;
    }

    /**
     * Tổng hợp báo cáo thống kê ví SPayLater của khách hàng
     */
    public TraSauThongKeDTO layThongKeTraSau(Long maNguoiDung) {
        TaiKhoanTraSau tk = layHoacTaoTaiKhoan(maNguoiDung);

        BigDecimal duNoHienTai = kyThanhToanTraSauRepository.tinhTongTienNoConLaiCuaTaiKhoan(tk.getMaTkTraSau());
        long tongHopDong = hopDongTraSauRepository.demTongSoHopDongTheoTaiKhoan(tk.getMaTkTraSau());

        LocalDate homNay = LocalDate.now();
        LocalDate cuoiThang = homNay.withDayOfMonth(homNay.lengthOfMonth());
        List<KyThanhToanTraSau> dsSapDenHan = kyThanhToanTraSauRepository.timKyDenHan(tk.getMaTkTraSau(), homNay, cuoiThang);

        BigDecimal tongCanTraThangNay = dsSapDenHan.stream()
                .map(k -> k.getSoTienCanTra().subtract(k.getSoTienDaTra()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long soKyQuaHan = kyThanhToanTraSauRepository.demSoKyQuaHanCuaTaiKhoan(tk.getMaTkTraSau(), homNay);
        BigDecimal tongTienQuaHan = kyThanhToanTraSauRepository.tinhTongTienQuaHanCuaTaiKhoan(tk.getMaTkTraSau(), homNay);

        List<HopDongTraSau> allHd = hopDongTraSauRepository.findByTaiKhoanTraSau_MaTkTraSauOrderByNgayTaoDesc(tk.getMaTkTraSau());
        long soHdDaTatToan = allHd.stream().filter(HopDongTraSau::isDaTatToan).count();
        long soHdDangTra = allHd.stream().filter(h -> !h.isDaTatToan()).count();

        return TraSauThongKeDTO.builder()
                .maTkTraSau(tk.getMaTkTraSau())
                .maNguoiDung(maNguoiDung)
                .tenKhachHang(tk.getNguoiDung().getHoVaTen())
                .tongHanMuc(tk.getHanMucDuocCap())
                .hanMucConLai(tk.getHanMucConLai())
                .hanMucDaDung(tk.getHanMucDaSuDung())
                .tiLeSuDungPhanTram(tk.getTiLeSuDungPhanTram())
                .diemTinDung(tk.getDiemTinDung())
                .trangThaiTaiKhoan(tk.getTenTrangThaiTiengViet())
                .badgeClassTrangThai(tk.getBadgeClass())
                .tongDuNoHienTai(duNoHienTai)
                .tongSoHopDong(tongHopDong)
                .soHopDongDangTra(soHdDangTra)
                .soHopDongDaTatToan(soHdDaTatToan)
                .soKyCanTraThangNay(dsSapDenHan.size())
                .tongTienCanTraThangNay(tongCanTraThangNay)
                .soKyQuaHan(soKyQuaHan)
                .tongTienQuaHan(tongTienQuaHan)
                .duDieuKienVay(tk.isDuDieuKienVay() && soKyQuaHan == 0)
                .build();
    }

    /**
     * Tìm kiếm và phân trang danh sách hợp đồng trả sau
     */
    public Page<HopDongTraSau> getDanhSachHopDongPhanTrang(String keyword, Long maTkTraSau, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        return hopDongTraSauRepository.timKiemNangCao(keyword, maTkTraSau, pageable);
    }

    public HopDongTraSau getChiTietHopDong(Long maHopDong) {
        return hopDongTraSauRepository.findById(maHopDong)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng trả sau với mã: " + maHopDong));
    }
}
