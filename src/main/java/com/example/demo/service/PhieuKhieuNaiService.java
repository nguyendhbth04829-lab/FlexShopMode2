package com.example.demo.service;

import com.example.demo.dto.ThongKeKhieuNaiDTO;
import com.example.demo.dto.YeuCauKhieuNaiForm;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@Transactional
public class PhieuKhieuNaiService {

    private static final Set<String> HOP_LE_LOAI_KHIEU_NAI = Set.of("HONG_VO", "GIAO_SAI", "HANG_GIA", "THIEU_HANG", "HET_HAN", "KHAC");
    private static final Set<String> HOP_LE_GIAI_PHAP = Set.of("HOAN_TIEN_TRA_HANG", "HOAN_TIEN_KHONG_TRA", "DOI_HANG");
    private static final Set<String> HOP_LE_MUC_DO = Set.of("THAP", "TRUNG_BINH", "CAO", "KHAN_CAP");

    @Autowired
    private PhieuKhieuNaiRepository phieuKhieuNaiRepository;

    @Autowired
    private BangChungKhieuNaiRepository bangChungKhieuNaiRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private LichSuTrangThaiDonRepository lichSuTrangThaiDonRepository;

    public Page<PhieuKhieuNai> layDanhSachKhieuNaiNangCao(
            Long maKhachHang,
            String tuKhoa,
            String trangThai,
            String loaiKhieuNai,
            String mucDoUuTien,
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
        return phieuKhieuNaiRepository.timKiemKhieuNaiNangCao(
                maKhachHang,
                tuKhoa,
                trangThai,
                loaiKhieuNai,
                mucDoUuTien,
                tuNgay,
                denNgay,
                pageable
        );
    }

    public ThongKeKhieuNaiDTO layThongKeKhieuNai(Long maKhachHang) {
        long tongSoPhieu = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDung(maKhachHang);
        long soPhieuMoMoi = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "MO_MOI");
        long dangXuLy = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "DANG_XU_LY");
        long daHoanTien = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "CHAP_NHAN_HOAN_TIEN");
        long tuChoi = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "TU_CHOI_KHIEU_NAI");
        long daHuy = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "DA_HUY");

        BigDecimal tongTienDaHoan = phieuKhieuNaiRepository.tinhTongTienTheoDanhSachTrangThai(
                maKhachHang,
                List.of("CHAP_NHAN_HOAN_TIEN")
        );

        BigDecimal tongTienDangKhieuNai = phieuKhieuNaiRepository.tinhTongTienTheoDanhSachTrangThai(
                maKhachHang,
                List.of("MO_MOI", "DANG_XU_LY", "CHO_SHOP_PHAN_HOI")
        );

        return ThongKeKhieuNaiDTO.builder()
                .tongSoPhieu(tongSoPhieu)
                .soPhieuMoMoi(soPhieuMoMoi)
                .soPhieuDangXuLy(dangXuLy)
                .soPhieuDaHoanTien(daHoanTien)
                .soPhieuTuChoi(tuChoi)
                .soPhieuDaHuy(daHuy)
                .tongTienDaHoan(tongTienDaHoan != null ? tongTienDaHoan : BigDecimal.ZERO)
                .tongTienDangKhieuNai(tongTienDangKhieuNai != null ? tongTienDangKhieuNai : BigDecimal.ZERO)
                .build();
    }

    public List<DonHangShop> layDanhSachDonHangKhaDung(Long maKhachHang) {
        return donHangShopRepository.findAllByKhachHangIdAndTrangThai(maKhachHang, "DA_GIAO");
    }

    public PhieuKhieuNai layChiTietPhieu(Long maPhieu) {
        return phieuKhieuNaiRepository.findById(maPhieu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu khiếu nại có mã ID: " + maPhieu));
    }

    public PhieuKhieuNai taoPhieuKhieuNai(
            Long maKhachHang,
            Long maDonHangShop,
            String loaiKhieuNai,
            String giaiPhapYeuCau,
            String mucDoUuTien,
            BigDecimal soTienHoanTra,
            String noiDungMoTa,
            MultipartFile[] filesBangChung
    ) throws IOException {
        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(maDonHangShop);
        form.setLoaiKhieuNai(loaiKhieuNai);
        form.setGiaiPhapYeuCau(giaiPhapYeuCau);
        form.setMucDoUuTien(mucDoUuTien);
        form.setSoTienHoanTra(soTienHoanTra);
        form.setNoiDungMoTa(noiDungMoTa);
        form.setFilesBangChung(filesBangChung);
        return taoPhieuKhieuNaiTuForm(maKhachHang, form);
    }

    @Transactional
    public PhieuKhieuNai taoPhieuKhieuNaiTuForm(Long maKhachHang, YeuCauKhieuNaiForm form) throws IOException {
        if (maKhachHang == null || maKhachHang <= 0) {
            throw new IllegalArgumentException("Mã định danh khách hàng không hợp lệ.");
        }
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu biểu mẫu yêu cầu khiếu nại không được để trống.");
        }

        // 1. Kiểm tra tài khoản khách hàng
        NguoiDung khachHang = nguoiDungRepository.findById(maKhachHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin khách hàng ID: " + maKhachHang));
        if (Boolean.TRUE.equals(khachHang.getDaXoa())) {
            throw new SecurityException("Tài khoản người dùng này đã bị xóa khỏi hệ thống.");
        }
        if (khachHang.getTrangThai() != null && !"HOAT_DONG".equalsIgnoreCase(khachHang.getTrangThai())) {
            throw new SecurityException("Tài khoản người dùng đang bị tạm khóa hoặc ngừng hoạt động.");
        }

        // 2. Validate enum nghiệp vụ
        if (!HOP_LE_LOAI_KHIEU_NAI.contains(form.getLoaiKhieuNai())) {
            throw new IllegalArgumentException("Lý do khiếu nại không hợp lệ: " + form.getLoaiKhieuNai());
        }
        if (!HOP_LE_GIAI_PHAP.contains(form.getGiaiPhapYeuCau())) {
            throw new IllegalArgumentException("Giải pháp yêu cầu không hợp lệ: " + form.getGiaiPhapYeuCau());
        }
        if (form.getMucDoUuTien() != null && !HOP_LE_MUC_DO.contains(form.getMucDoUuTien())) {
            throw new IllegalArgumentException("Mức độ ưu tiên không hợp lệ: " + form.getMucDoUuTien());
        }

        // 3. Kiểm tra đơn hàng shop
        if (form.getMaDonHangShop() == null || form.getMaDonHangShop() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn đơn hàng shop cần khiếu nại.");
        }
        DonHangShop donHangShop = donHangShopRepository.findById(form.getMaDonHangShop())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng shop ID: " + form.getMaDonHangShop()));

        // Kiểm tra quyền sở hữu đơn hàng
        if (donHangShop.getDonHangTong() == null || donHangShop.getDonHangTong().getKhachHang() == null
                || !donHangShop.getDonHangTong().getKhachHang().getMaNguoiDung().equals(maKhachHang)) {
            throw new SecurityException("Đơn hàng này không thuộc tài khoản của bạn! Thao tác bị từ chối.");
        }

        // Kiểm tra trạng thái đơn hàng: Chỉ đơn đã giao mới được khiếu nại đổi trả
        if (!"DA_GIAO".equalsIgnoreCase(donHangShop.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể khiếu nại đối với đơn hàng ở trạng thái 'Đã giao' (Trạng thái hiện tại: " + donHangShop.getTrangThai() + ").");
        }

        // 4. Kiểm tra thời hạn khiếu nại (Chính sách sàn 7 ngày bảo vệ người mua)
        if (donHangShop.getNgayTao() != null && donHangShop.getNgayTao().plusDays(30).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Đơn hàng đã hoàn tất quá thời hạn cho phép khiếu nại (30 ngày) theo chính sách bảo vệ người tiêu dùng sàn FlexShop.");
        }

        // 5. Kiểm tra xem đơn hàng đã có khiếu nại đang thụ lý hay chưa
        boolean daCoKhieuNaiDangXuLy = phieuKhieuNaiRepository.existsByDonHangShop_MaDonHangShopAndTrangThaiNotIn(
                form.getMaDonHangShop(), Arrays.asList("DA_HUY", "DONG_PHIEU", "TU_CHOI_KHIEU_NAI")
        );
        if (daCoKhieuNaiDangXuLy) {
            throw new IllegalStateException("Đơn hàng [" + donHangShop.getMaCodeDonShop() + "] đang có một phiếu khiếu nại đang được thụ lý! Vui lòng không tạo thêm.");
        }

        // 6. Ràng buộc giải pháp và số tiền hoàn
        BigDecimal soTienToiDa = donHangShop.getTongTienShopNhan();
        BigDecimal soTienHoanTra;
        if ("DOI_HANG".equals(form.getGiaiPhapYeuCau())) {
            soTienHoanTra = BigDecimal.ZERO;
        } else {
            soTienHoanTra = form.getSoTienHoanTra();
            if (soTienHoanTra == null || soTienHoanTra.compareTo(new BigDecimal("1000")) < 0) {
                soTienHoanTra = soTienToiDa;
            } else if (soTienHoanTra.compareTo(soTienToiDa) > 0) {
                throw new IllegalArgumentException("Số tiền yêu cầu hoàn (" + soTienHoanTra + " đ) không được vượt quá tổng giá trị đơn hàng (" + soTienToiDa + " đ)!");
            }
        }

        // 7. Làm sạch và kiểm tra độ dài nội dung mô tả (chống XSS)
        String moTaTho = form.getNoiDungMoTa();
        if (moTaTho == null || moTaTho.trim().isBlank()) {
            throw new IllegalArgumentException("Nội dung mô tả tình trạng lỗi không được để trống.");
        }
        String moTaLamSach = moTaTho.replaceAll("<[^>]*>", "").trim();
        if (moTaLamSach.length() < 10 || moTaLamSach.length() > 2000) {
            throw new IllegalArgumentException("Nội dung mô tả phải có độ dài từ 10 đến 2.000 ký tự hợp lệ.");
        }

        // 8. Bắt buộc bằng chứng đối với các lỗi hỏng vỡ, hàng giả, giao sai
        MultipartFile[] files = form.getFilesBangChung();
        boolean coTepTin = files != null && Arrays.stream(files).anyMatch(f -> f != null && !f.isEmpty());

        if (Set.of("HONG_VO", "HANG_GIA", "GIAO_SAI").contains(form.getLoaiKhieuNai()) && !coTepTin) {
            throw new IllegalArgumentException("Đối với sự cố '" + layTenLoaiDisplay(form.getLoaiKhieuNai()) + "', bạn bắt buộc phải tải lên ít nhất 1 hình ảnh hoặc video mở hộp làm bằng chứng đối soát.");
        }

        // 9. Kiểm tra số lượng và tính hợp lệ tệp bằng chứng đính kèm (tối đa 5 tệp)
        if (files != null && files.length > FileStorageService.MAX_FILE_COUNT) {
            throw new IllegalArgumentException("Bạn chỉ được tải lên tối đa " + FileStorageService.MAX_FILE_COUNT + " tệp hình ảnh/video bằng chứng!");
        }

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    fileStorageService.kiemTraTepTinHopLe(file);
                }
            }
        }

        // 10. Sinh mã phiếu duy nhất và lưu phiếu
        String maCodePhieu = taoMaCodePhieu();

        PhieuKhieuNai phieu = new PhieuKhieuNai();
        phieu.setMaCodePhieu(maCodePhieu);
        phieu.setKhachHang(khachHang);
        phieu.setDonHangShop(donHangShop);
        phieu.setGianHang(donHangShop.getGianHang());
        phieu.setLoaiKhieuNai(form.getLoaiKhieuNai());
        phieu.setGiaiPhapYeuCau(form.getGiaiPhapYeuCau());
        phieu.setMucDoUuTien(form.getMucDoUuTien() != null ? form.getMucDoUuTien() : "TRUNG_BINH");
        phieu.setNoiDungMoTa(moTaLamSach);
        phieu.setSoTienHoanTra(soTienHoanTra);
        phieu.setTrangThai("MO_MOI");
        phieu.setNgayTao(LocalDateTime.now());

        PhieuKhieuNai phieuDaLuu = phieuKhieuNaiRepository.save(phieu);

        // 11. Lưu các tệp bằng chứng
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    FileStorageService.FileUploadResult result = fileStorageService.luuTepTin(file);
                    if (result != null) {
                        BangChungKhieuNai bangChung = new BangChungKhieuNai();
                        bangChung.setPhieuKhieuNai(phieuDaLuu);
                        bangChung.setLinkTepTin(result.getFileUrl());
                        bangChung.setLoaiTepTin(result.getFileType());
                        bangChung.setVaiTroTaiLen("KHACH_HANG");
                        bangChung.setNgayTao(LocalDateTime.now());
                        bangChungKhieuNaiRepository.save(bangChung);
                        phieuDaLuu.getDanhSachBangChung().add(bangChung);
                    }
                }
            }
        }

        // 12. Ghi nhận mốc dòng thời gian đơn hàng
        LichSuTrangThaiDon ls = new LichSuTrangThaiDon();
        ls.setDonHangShop(donHangShop);
        ls.setTrangThaiCu(donHangShop.getTrangThai());
        ls.setTrangThaiMoi("KHACH_KHIEU_NAI");
        ls.setNguoiThucHien("Khách hàng: " + khachHang.getHoVaTen());
        ls.setGhiChu("Mở phiếu khiếu nại " + phieuDaLuu.getMaCodePhieu() + " (" + layTenLoaiDisplay(phieuDaLuu.getLoaiKhieuNai()) + ")");
        ls.setThoiGian(LocalDateTime.now());
        lichSuTrangThaiDonRepository.save(ls);

        return phieuDaLuu;
    }

    @Transactional
    public boolean huyKhieuNai(Long maPhieu, Long maKhachHang) {
        PhieuKhieuNai phieu = layChiTietPhieu(maPhieu);
        if (!phieu.getKhachHang().getMaNguoiDung().equals(maKhachHang)) {
            throw new SecurityException("Bạn không có quyền thao tác trên phiếu khiếu nại này!");
        }

        if (Set.of("CHAP_NHAN_HOAN_TIEN", "TU_CHOI_KHIEU_NAI", "DONG_PHIEU").contains(phieu.getTrangThai())) {
            throw new IllegalStateException("Không thể hủy khiếu nại này vì phiếu đã có phán quyết xử lý (" + phieu.getTrangThaiDisplay() + ")!");
        }

        if ("MO_MOI".equals(phieu.getTrangThai()) || "DANG_XU_LY".equals(phieu.getTrangThai())) {
            phieu.setTrangThai("DA_HUY");
            phieuKhieuNaiRepository.save(phieu);

            LichSuTrangThaiDon ls = new LichSuTrangThaiDon();
            ls.setDonHangShop(phieu.getDonHangShop());
            ls.setTrangThaiCu(phieu.getDonHangShop().getTrangThai());
            ls.setTrangThaiMoi(phieu.getDonHangShop().getTrangThai());
            ls.setNguoiThucHien("Khách hàng: " + phieu.getKhachHang().getHoVaTen());
            ls.setGhiChu("Khách hàng chủ động hủy yêu cầu khiếu nại " + phieu.getMaCodePhieu());
            ls.setThoiGian(LocalDateTime.now());
            lichSuTrangThaiDonRepository.save(ls);

            return true;
        }
        return false;
    }

    private String layTenLoaiDisplay(String loai) {
        if (loai == null) return "Không xác định";
        switch (loai) {
            case "HONG_VO": return "Hàng bị hỏng vỡ / Móp méo";
            case "GIAO_SAI": return "Giao sai mẫu mã / Kích thước";
            case "HANG_GIA": return "Hàng giả / Nhái / Kém chất lượng";
            case "THIEU_HANG": return "Giao thiếu sản phẩm / Phụ kiện";
            case "HET_HAN": return "Sản phẩm hết hạn sử dụng";
            default: return loai;
        }
    }

    private String taoMaCodePhieu() {
        String prefix = "KN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int randomSuffix = new Random().nextInt(9000) + 1000;
        return prefix + "-" + randomSuffix;
    }
}
