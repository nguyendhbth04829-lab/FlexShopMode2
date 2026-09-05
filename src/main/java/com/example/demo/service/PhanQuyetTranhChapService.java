package com.example.demo.service;

import com.example.demo.dto.PhanQuyetTranhChapForm;
import com.example.demo.dto.ThongKePhanQuyetDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
@Transactional
public class PhanQuyetTranhChapService {

    private static final Set<String> HOP_LE_QUYET_DINH = Set.of(
            "DUYET_HOAN_TIEN_KHACH",
            "BOI_THUONG_SHOP",
            "BAC_BO_KHIEU_NAI"
    );

    private static final Set<String> HOP_LE_BEN_CHIU_PHI = Set.of(
            "NGUOI_BAN",
            "SAN_FLEXSHOP",
            "DON_VI_VAN_CHUYEN"
    );

    @Autowired
    private PhieuKhieuNaiRepository phieuKhieuNaiRepository;

    @Autowired
    private LenhHoanTienBoiThuongRepository lenhHoanTienBoiThuongRepository;

    @Autowired
    private ViNguoiBanRepository viNguoiBanRepository;

    @Autowired
    private LichSuGiaoDichViRepository lichSuGiaoDichViRepository;

    @Autowired
    private LichSuTrangThaiDonRepository lichSuTrangThaiDonRepository;

    @Autowired
    private GhiChuNoiBoKhieuNaiRepository ghiChuNoiBoKhieuNaiRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    /**
     * Ra phán quyết xử lý tranh chấp (US-47) với validation đa tầng và đồng bộ tài chính ví tự động
     */
    public LenhHoanTienBoiThuong raPhanQuyet(PhanQuyetTranhChapForm form, Long maNguoiPhanQuyet) {
        // 1. Validate người ra phán quyết
        if (maNguoiPhanQuyet == null || maNguoiPhanQuyet <= 0) {
            throw new IllegalArgumentException("Mã nhân viên CSKH/Admin ra phán quyết không hợp lệ.");
        }
        NguoiDung nguoiPhanQuyet = nguoiDungRepository.findById(maNguoiPhanQuyet)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên CSKH ID: " + maNguoiPhanQuyet));

        if (Boolean.TRUE.equals(nguoiPhanQuyet.getDaXoa()) ||
                (nguoiPhanQuyet.getTrangThai() != null && !"HOAT_DONG".equalsIgnoreCase(nguoiPhanQuyet.getTrangThai()))) {
            throw new SecurityException("Tài khoản nhân viên CSKH này đang bị khóa hoặc đã ngừng hoạt động.");
        }

        // 2. Validate form
        if (form == null) {
            throw new IllegalArgumentException("Biểu mẫu phán quyết không được để trống.");
        }
        if (form.getMaPhieu() == null || form.getMaPhieu() <= 0) {
            throw new IllegalArgumentException("Mã ticket khiếu nại không hợp lệ.");
        }
        if (!HOP_LE_QUYET_DINH.contains(form.getQuyetDinh())) {
            throw new IllegalArgumentException("Quyết định xử lý không hợp lệ: " + form.getQuyetDinh());
        }

        // 3. Khử mã độc XSS và kiểm tra lý do phán quyết
        String cleanGhiChu = form.getGhiChuPhanQuyet() != null
                ? form.getGhiChuPhanQuyet().replaceAll("<[^>]*>", "").trim()
                : "";
        if (cleanGhiChu.length() < 10 || cleanGhiChu.length() > 2000) {
            throw new IllegalArgumentException("Căn cứ và lý do phán quyết phải từ 10 đến 2.000 ký tự giải trình chi tiết.");
        }

        // 4. Kiểm tra phiếu khiếu nại
        PhieuKhieuNai phieu = phieuKhieuNaiRepository.findById(form.getMaPhieu())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu khiếu nại ID: " + form.getMaPhieu()));

        // Chặn phán quyết lặp lại hoặc phán quyết trên ticket đã kết thúc
        if (Set.of("DA_HUY", "DONG_PHIEU").contains(phieu.getTrangThai())) {
            throw new IllegalStateException("Ticket [" + phieu.getMaCodePhieu() + "] đã kết thúc (" + phieu.getTrangThaiDisplay() + "), không thể ra phán quyết lại!");
        }
        if (lenhHoanTienBoiThuongRepository.existsByPhieuKhieuNai_MaPhieu(phieu.getMaPhieu())) {
            throw new IllegalStateException("Ticket [" + phieu.getMaCodePhieu() + "] đã có lệnh hoàn tiền / bồi thường trước đó. Không được tạo giao dịch trùng lặp!");
        }
        if (Set.of("CHAP_NHAN_HOAN_TIEN", "BOI_THUONG_SHOP", "TU_CHOI_KHIEU_NAI").contains(phieu.getTrangThai())) {
            throw new IllegalStateException("Ticket [" + phieu.getMaCodePhieu() + "] đã được phân xử trước đó với kết quả: " + phieu.getTrangThaiDisplay());
        }

        DonHangShop donHangShop = phieu.getDonHangShop();
        BigDecimal tongTienShopNhan = donHangShop.getTongTienShopNhan();
        LenhHoanTienBoiThuong lenhResult = null;

        // 5. Xử lý từng phương án phán quyết
        switch (form.getQuyetDinh()) {
            case "DUYET_HOAN_TIEN_KHACH": {
                // Kiểm tra số tiền
                if (form.getSoTien() == null || form.getSoTien().compareTo(BigDecimal.valueOf(1000)) < 0) {
                    throw new IllegalArgumentException("Số tiền hoàn trả cho khách hàng phải tối thiểu từ 1.000 VNĐ.");
                }
                if (form.getSoTien().compareTo(tongTienShopNhan) > 0) {
                    throw new IllegalArgumentException("Số tiền hoàn trả (" + form.getSoTien() + " đ) không được vượt quá tổng giá trị đơn hàng shop nhận (" + tongTienShopNhan + " đ).");
                }
                // Bắt buộc chọn bên chịu phí
                if (form.getBenChiuPhi() == null || !HOP_LE_BEN_CHIU_PHI.contains(form.getBenChiuPhi())) {
                    throw new IllegalArgumentException("Vui lòng chọn bên chịu chi phí hợp lệ (Shop, Sàn FlexShop, hoặc Đơn vị vận chuyển).");
                }

                // Tạo Lệnh hoàn tiền
                LenhHoanTienBoiThuong lenh = new LenhHoanTienBoiThuong();
                lenh.setPhieuKhieuNai(phieu);
                lenh.setNguoiNhanTien(phieu.getKhachHang());
                lenh.setSoTien(form.getSoTien());
                lenh.setBenChiuPhi(form.getBenChiuPhi());
                lenh.setTrangThai("DA_CHUYEN_TIEN");
                lenh.setNgayThucHien(LocalDateTime.now());
                lenhResult = lenhHoanTienBoiThuongRepository.save(lenh);

                // Đồng bộ ví người bán nếu bên chịu phí là Shop
                if ("NGUOI_BAN".equals(form.getBenChiuPhi())) {
                    dongBoViNguoiBanTruTien(phieu.getGianHang(), form.getSoTien(), phieu.getMaCodePhieu(),
                            "Khấu trừ hoàn tiền khiếu nại " + phieu.getMaCodePhieu() + " cho khách hàng");
                }

                // Cập nhật phiếu
                phieu.setTrangThai("CHAP_NHAN_HOAN_TIEN");
                phieu.setSoTienHoanTra(form.getSoTien());
                phieu.setNguoiPhanQuyet(nguoiPhanQuyet);
                phieu.setGhiChuPhanQuyet(cleanGhiChu);
                phieuKhieuNaiRepository.save(phieu);

                // Cập nhật Dòng thời gian đơn hàng (Timeline)
                ghiNhanLichSuDon(donHangShop, donHangShop.getTrangThai(), "DA_HOAN_TIEN", nguoiPhanQuyet,
                        "Phán quyết duyệt hoàn tiền " + String.format("%,.0f", form.getSoTien()) + " đ cho khách (" + lenh.getBenChiuPhiDisplay() + "). Căn cứ: " + cleanGhiChu);

                // Thêm ghi chú điều tra nội bộ
                ghiNhanGhiChuNoiBo(phieu, nguoiPhanQuyet,
                        "[Phán quyết - Duyệt hoàn tiền]: Đã duyệt hoàn tiền " + String.format("%,.0f", form.getSoTien()) + " đ cho khách hàng " + phieu.getKhachHang().getHoVaTen() +
                                ". Bên chịu chi phí: " + lenh.getBenChiuPhiDisplay() + ". Căn cứ: " + cleanGhiChu);
                break;
            }

            case "BOI_THUONG_SHOP": {
                // Kiểm tra số tiền
                if (form.getSoTien() == null || form.getSoTien().compareTo(BigDecimal.valueOf(1000)) < 0) {
                    throw new IllegalArgumentException("Số tiền bồi thường cho Shop phải tối thiểu từ 1.000 VNĐ.");
                }
                if (form.getSoTien().compareTo(tongTienShopNhan) > 0) {
                    throw new IllegalArgumentException("Số tiền bồi thường (" + form.getSoTien() + " đ) không được vượt quá tổng giá trị đơn hàng (" + tongTienShopNhan + " đ).");
                }
                // Bồi thường Shop thì bên chịu phí KHÔNG THỂ là chính Shop
                if ("NGUOI_BAN".equals(form.getBenChiuPhi())) {
                    throw new IllegalArgumentException("Bồi thường cho Shop thì bên chịu chi phí phải là Sàn FlexShop hoặc Đơn vị vận chuyển (Shop không thể tự bồi thường cho chính mình).");
                }
                if (form.getBenChiuPhi() == null || (!"SAN_FLEXSHOP".equals(form.getBenChiuPhi()) && !"DON_VI_VAN_CHUYEN".equals(form.getBenChiuPhi()))) {
                    throw new IllegalArgumentException("Vui lòng chọn nguồn bồi thường hợp lệ (Sàn FlexShop hoặc Đơn vị vận chuyển).");
                }

                NguoiDung chuShop = phieu.getGianHang().getChuSoHuu();
                if (chuShop == null) {
                    throw new IllegalStateException("Không tìm thấy chủ sở hữu gian hàng để chi trả tiền bồi thường.");
                }

                // Tạo Lệnh bồi thường
                LenhHoanTienBoiThuong lenh = new LenhHoanTienBoiThuong();
                lenh.setPhieuKhieuNai(phieu);
                lenh.setNguoiNhanTien(chuShop);
                lenh.setSoTien(form.getSoTien());
                lenh.setBenChiuPhi(form.getBenChiuPhi());
                lenh.setTrangThai("DA_CHUYEN_TIEN");
                lenh.setNgayThucHien(LocalDateTime.now());
                lenhResult = lenhHoanTienBoiThuongRepository.save(lenh);

                // Cộng tiền bồi thường vào ví khả dụng người bán
                dongBoViNguoiBanCongTien(phieu.getGianHang(), form.getSoTien(), phieu.getMaCodePhieu(),
                        "Cộng tiền bồi thường tranh chấp " + phieu.getMaCodePhieu() + " (Nguồn chi trả: " + lenh.getBenChiuPhiDisplay() + ")");

                // Cập nhật phiếu
                phieu.setTrangThai("BOI_THUONG_SHOP");
                phieu.setSoTienHoanTra(BigDecimal.ZERO); // Khách không nhận tiền
                phieu.setNguoiPhanQuyet(nguoiPhanQuyet);
                phieu.setGhiChuPhanQuyet(cleanGhiChu);
                phieuKhieuNaiRepository.save(phieu);

                // Cập nhật Timeline
                ghiNhanLichSuDon(donHangShop, donHangShop.getTrangThai(), "BOI_THUONG_SHOP", nguoiPhanQuyet,
                        "Phán quyết bồi thường cho Shop " + String.format("%,.0f", form.getSoTien()) + " đ từ nguồn " + lenh.getBenChiuPhiDisplay() + ". Căn cứ: " + cleanGhiChu);

                // Ghi chú nội bộ
                ghiNhanGhiChuNoiBo(phieu, nguoiPhanQuyet,
                        "[Phán quyết - Bồi thường Shop]: Đã duyệt bồi thường " + String.format("%,.0f", form.getSoTien()) + " đ cho Gian hàng " + phieu.getGianHang().getTenGianHang() +
                                ". Nguồn bồi thường: " + lenh.getBenChiuPhiDisplay() + ". Căn cứ: " + cleanGhiChu);
                break;
            }

            case "BAC_BO_KHIEU_NAI": {
                // Khách khiếu nại sai hoặc thiếu bằng chứng, phán quyết bảo vệ shop
                phieu.setTrangThai("TU_CHOI_KHIEU_NAI");
                phieu.setSoTienHoanTra(BigDecimal.ZERO);
                phieu.setNguoiPhanQuyet(nguoiPhanQuyet);
                phieu.setGhiChuPhanQuyet(cleanGhiChu);
                phieuKhieuNaiRepository.save(phieu);

                // Giải phóng số dư tạm giữ escrow cho Shop (nếu có)
                giaiPhongEscrowChoShop(phieu.getGianHang(), tongTienShopNhan, phieu.getMaCodePhieu());

                // Cập nhật Timeline
                ghiNhanLichSuDon(donHangShop, donHangShop.getTrangThai(), "TU_CHOI_KHIEU_NAI", nguoiPhanQuyet,
                        "CSKH bác bỏ khiếu nại của khách hàng, bảo vệ quyền lợi gian hàng. Đơn hàng hoàn tất. Căn cứ: " + cleanGhiChu);

                // Ghi chú nội bộ
                ghiNhanGhiChuNoiBo(phieu, nguoiPhanQuyet,
                        "[Phán quyết - Bác bỏ khiếu nại]: CSKH bác bỏ yêu cầu khiếu nại của khách. Căn cứ: " + cleanGhiChu);
                break;
            }
        }

        return lenhResult;
    }

    /**
     * Thống kê KPI tài chính phán quyết thời gian thực
     */
    @Transactional(readOnly = true)
    public ThongKePhanQuyetDTO layThongKePhanQuyet() {
        long tongSo = lenhHoanTienBoiThuongRepository.count();
        BigDecimal hoanKhach = lenhHoanTienBoiThuongRepository.sumTongTienHoanKhach();
        BigDecimal boiThuongShop = lenhHoanTienBoiThuongRepository.sumTongTienBoiThuongShop();
        long shopChiu = lenhHoanTienBoiThuongRepository.countByBenChiuPhi("NGUOI_BAN");
        long sanChiu = lenhHoanTienBoiThuongRepository.countByBenChiuPhi("SAN_FLEXSHOP");
        long vcChiu = lenhHoanTienBoiThuongRepository.countByBenChiuPhi("DON_VI_VAN_CHUYEN");
        long bacBo = phieuKhieuNaiRepository.countByTrangThai("TU_CHOI_KHIEU_NAI");

        return ThongKePhanQuyetDTO.builder()
                .tongSoLenh(tongSo)
                .tongTienHoanKhach(hoanKhach != null ? hoanKhach : BigDecimal.ZERO)
                .tongTienBoiThuongShop(boiThuongShop != null ? boiThuongShop : BigDecimal.ZERO)
                .soLenhNguoiBanChiu(shopChiu)
                .soLenhSanChiu(sanChiu)
                .soLenhVanChuyenChiu(vcChiu)
                .soKhieuNaiBacBo(bacBo)
                .build();
    }

    /**
     * Tra cứu danh sách lệnh hoàn tiền / bồi thường có phân trang và bộ lọc nâng cao
     */
    @Transactional(readOnly = true)
    public Page<LenhHoanTienBoiThuong> layDanhSachLenhHoanTien(
            String tuKhoa,
            String benChiuPhi,
            String trangThai,
            String tuNgayStr,
            String denNgayStr,
            int page,
            int size
    ) {
        LocalDateTime tuNgay = null;
        LocalDateTime denNgay = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (tuNgayStr != null && !tuNgayStr.isBlank()) {
            tuNgay = LocalDate.parse(tuNgayStr, formatter).atStartOfDay();
        }
        if (denNgayStr != null && !denNgayStr.isBlank()) {
            denNgay = LocalDate.parse(denNgayStr, formatter).atTime(23, 59, 59);
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return lenhHoanTienBoiThuongRepository.timKiemLenhHoanTien(tuKhoa, benChiuPhi, trangThai, tuNgay, denNgay, pageable);
    }

    /**
     * Tra cứu chi tiết lệnh bồi hoàn theo ID
     */
    @Transactional(readOnly = true)
    public LenhHoanTienBoiThuong layChiTietLenh(Long maLenh) {
        return lenhHoanTienBoiThuongRepository.findById(maLenh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lệnh bồi hoàn ID: " + maLenh));
    }

    // ==================== CÁC PHƯƠNG THỨC HỖ TRỢ VÍ NGƯỜI BÁN & AUDIT ====================

    private ViNguoiBan layHoacTaoViNguoiBan(GianHang gianHang) {
        return viNguoiBanRepository.findByGianHang_MaGianHang(gianHang.getMaGianHang())
                .orElseGet(() -> {
                    ViNguoiBan viMoi = new ViNguoiBan();
                    viMoi.setGianHang(gianHang);
                    viMoi.setSoDuKhaDung(BigDecimal.ZERO);
                    viMoi.setSoDuTamGiuEscrow(BigDecimal.ZERO);
                    viMoi.setTongTienDaRut(BigDecimal.ZERO);
                    viMoi.setPhienBanLock(0);
                    viMoi.setNgayCapNhat(LocalDateTime.now());
                    return viNguoiBanRepository.save(viMoi);
                });
    }

    private void dongBoViNguoiBanTruTien(GianHang gianHang, BigDecimal soTien, String maThamChieu, String moTa) {
        ViNguoiBan vi = layHoacTaoViNguoiBan(gianHang);
        if (vi.getSoDuTamGiuEscrow() != null && vi.getSoDuTamGiuEscrow().compareTo(soTien) >= 0) {
            vi.setSoDuTamGiuEscrow(vi.getSoDuTamGiuEscrow().subtract(soTien));
        } else if (vi.getSoDuKhaDung() != null && vi.getSoDuKhaDung().compareTo(soTien) >= 0) {
            vi.setSoDuKhaDung(vi.getSoDuKhaDung().subtract(soTien));
        }
        vi.setNgayCapNhat(LocalDateTime.now());
        viNguoiBanRepository.save(vi);

        LichSuGiaoDichVi gd = new LichSuGiaoDichVi();
        gd.setViNguoiBan(vi);
        gd.setLoaiGiaoDich("TRU_TIEN_HOAN_TRA");
        gd.setSoTien(soTien.negate());
        gd.setSoDuSauGiaoDich(vi.getSoDuKhaDung().add(vi.getSoDuTamGiuEscrow() != null ? vi.getSoDuTamGiuEscrow() : BigDecimal.ZERO));
        gd.setMaThamChieu(maThamChieu);
        gd.setMoTa(moTa);
        gd.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichViRepository.save(gd);
    }

    private void dongBoViNguoiBanCongTien(GianHang gianHang, BigDecimal soTien, String maThamChieu, String moTa) {
        ViNguoiBan vi = layHoacTaoViNguoiBan(gianHang);
        BigDecimal soDuMoi = (vi.getSoDuKhaDung() != null ? vi.getSoDuKhaDung() : BigDecimal.ZERO).add(soTien);
        vi.setSoDuKhaDung(soDuMoi);
        vi.setNgayCapNhat(LocalDateTime.now());
        viNguoiBanRepository.save(vi);

        LichSuGiaoDichVi gd = new LichSuGiaoDichVi();
        gd.setViNguoiBan(vi);
        gd.setLoaiGiaoDich("CONG_TIEN_BOI_THUONG");
        gd.setSoTien(soTien);
        gd.setSoDuSauGiaoDich(soDuMoi);
        gd.setMaThamChieu(maThamChieu);
        gd.setMoTa(moTa);
        gd.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichViRepository.save(gd);
    }

    private void giaiPhongEscrowChoShop(GianHang gianHang, BigDecimal soTien, String maThamChieu) {
        ViNguoiBan vi = layHoacTaoViNguoiBan(gianHang);
        if (vi.getSoDuTamGiuEscrow() != null && vi.getSoDuTamGiuEscrow().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tienGiaiPhong = vi.getSoDuTamGiuEscrow().min(soTien);
            vi.setSoDuTamGiuEscrow(vi.getSoDuTamGiuEscrow().subtract(tienGiaiPhong));
            vi.setSoDuKhaDung((vi.getSoDuKhaDung() != null ? vi.getSoDuKhaDung() : BigDecimal.ZERO).add(tienGiaiPhong));
            vi.setNgayCapNhat(LocalDateTime.now());
            viNguoiBanRepository.save(vi);

            LichSuGiaoDichVi gd = new LichSuGiaoDichVi();
            gd.setViNguoiBan(vi);
            gd.setLoaiGiaoDich("GIAI_PHONG_ESCROW");
            gd.setSoTien(tienGiaiPhong);
            gd.setSoDuSauGiaoDich(vi.getSoDuKhaDung());
            gd.setMaThamChieu(maThamChieu);
            gd.setMoTa("Giải phóng tiền tạm giữ đơn hàng khi khiếu nại bị bác bỏ (" + maThamChieu + ")");
            gd.setNgayTao(LocalDateTime.now());
            lichSuGiaoDichViRepository.save(gd);
        }
    }

    private void ghiNhanLichSuDon(DonHangShop donHangShop, String trangThaiCu, String trangThaiMoi, NguoiDung cskh, String ghiChu) {
        LichSuTrangThaiDon ls = new LichSuTrangThaiDon();
        ls.setDonHangShop(donHangShop);
        ls.setTrangThaiCu(trangThaiCu);
        ls.setTrangThaiMoi(trangThaiMoi);
        ls.setNguoiThucHien("CSKH: " + cskh.getHoVaTen());
        ls.setGhiChu(ghiChu);
        ls.setThoiGian(LocalDateTime.now());
        lichSuTrangThaiDonRepository.save(ls);
    }

    private void ghiNhanGhiChuNoiBo(PhieuKhieuNai phieu, NguoiDung cskh, String noiDung) {
        GhiChuNoiBoKhieuNai gn = new GhiChuNoiBoKhieuNai();
        gn.setPhieuKhieuNai(phieu);
        gn.setNhanVien(cskh);
        gn.setNoiDung(noiDung);
        gn.setNgayTao(LocalDateTime.now());
        ghiChuNoiBoKhieuNaiRepository.save(gn);
    }
}
