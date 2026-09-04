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

@Service
@Transactional
public class PhieuKhieuNaiService {

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

        Pageable pageable = PageRequest.of(page, size);
        return phieuKhieuNaiRepository.timKiemKhieuNaiNangCao(
                maKhachHang, tuKhoa, trangThai, loaiKhieuNai, mucDoUuTien, tuNgay, denNgay, pageable
        );
    }

    public ThongKeKhieuNaiDTO layThongKeKhieuNai(Long maKhachHang) {
        long tongSoPhieu = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDung(maKhachHang);
        long soPhieuMoMoi = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "MO_MOI");
        long soPhieuDangXuLy = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "DANG_XU_LY")
                + phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "CHO_SHOP_PHAN_HOI");
        long soPhieuDaHoanTien = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "CHAP_NHAN_HOAN_TIEN");
        long soPhieuTuChoi = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "TU_CHOI_KHIEU_NAI");
        long soPhieuDaHuy = phieuKhieuNaiRepository.countByKhachHang_MaNguoiDungAndTrangThai(maKhachHang, "DA_HUY");

        BigDecimal tongTienDaHoan = phieuKhieuNaiRepository.tinhTongTienTheoDanhSachTrangThai(
                maKhachHang, List.of("CHAP_NHAN_HOAN_TIEN")
        );
        BigDecimal tongTienDangKhieuNai = phieuKhieuNaiRepository.tinhTongTienTheoDanhSachTrangThai(
                maKhachHang, Arrays.asList("MO_MOI", "DANG_XU_LY", "CHO_SHOP_PHAN_HOI")
        );

        return ThongKeKhieuNaiDTO.builder()
                .tongSoPhieu(tongSoPhieu)
                .soPhieuMoMoi(soPhieuMoMoi)
                .soPhieuDangXuLy(soPhieuDangXuLy)
                .soPhieuDaHoanTien(soPhieuDaHoanTien)
                .soPhieuTuChoi(soPhieuTuChoi)
                .soPhieuDaHuy(soPhieuDaHuy)
                .tongTienDaHoan(tongTienDaHoan != null ? tongTienDaHoan : BigDecimal.ZERO)
                .tongTienDangKhieuNai(tongTienDangKhieuNai != null ? tongTienDangKhieuNai : BigDecimal.ZERO)
                .build();
    }

    public PhieuKhieuNai layChiTietPhieu(Long maPhieu) {
        return phieuKhieuNaiRepository.findById(maPhieu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu khiếu nại #" + maPhieu));
    }

    public List<DonHangShop> layDanhSachDonHangKhaDung(Long maKhachHang) {
        return donHangShopRepository.findAllByKhachHangId(maKhachHang);
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
        NguoiDung khachHang = nguoiDungRepository.findById(maKhachHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin khách hàng ID: " + maKhachHang));

        DonHangShop donHangShop = donHangShopRepository.findById(form.getMaDonHangShop())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng shop ID: " + form.getMaDonHangShop()));

        // Kiểm tra quyền sở hữu đơn hàng
        if (!donHangShop.getDonHangTong().getKhachHang().getMaNguoiDung().equals(maKhachHang)) {
            throw new SecurityException("Đơn hàng này không thuộc tài khoản của bạn!");
        }

        // Kiểm tra trạng thái đơn hàng: Chỉ đơn đã giao mới được khiếu nại đổi trả
        if (!"DA_GIAO".equalsIgnoreCase(donHangShop.getTrangThai())) {
            throw new IllegalStateException("Chỉ có thể khiếu nại đối với đơn hàng ở trạng thái 'Đã giao' (Hiện tại: " + donHangShop.getTrangThai() + ")");
        }

        // Kiểm tra xem đơn hàng đã có khiếu nại đang thụ lý hay chưa
        boolean daCoKhieuNaiDangXuLy = phieuKhieuNaiRepository.existsByDonHangShop_MaDonHangShopAndTrangThaiNotIn(
                form.getMaDonHangShop(), Arrays.asList("DA_HUY", "DONG_PHIEU", "TU_CHOI_KHIEU_NAI")
        );
        if (daCoKhieuNaiDangXuLy) {
            throw new IllegalStateException("Đơn hàng này đang có một phiếu khiếu nại đang được thụ lý! Vui lòng không tạo thêm.");
        }

        // Kiểm tra số tiền hoàn không vượt quá tổng tiền shop nhận
        BigDecimal soTienToiDa = donHangShop.getTongTienShopNhan();
        BigDecimal soTienHoanTra = form.getSoTienHoanTra();
        if (soTienHoanTra == null || soTienHoanTra.compareTo(BigDecimal.ZERO) <= 0) {
            soTienHoanTra = soTienToiDa;
        } else if (soTienHoanTra.compareTo(soTienToiDa) > 0) {
            throw new IllegalArgumentException("Số tiền yêu cầu hoàn (" + soTienHoanTra + " đ) không được vượt quá tổng giá trị đơn hàng (" + soTienToiDa + " đ)!");
        }

        // Kiểm tra số lượng tệp bằng chứng đính kèm (tối đa 5 tệp)
        MultipartFile[] files = form.getFilesBangChung();
        if (files != null && files.length > 5) {
            throw new IllegalArgumentException("Bạn chỉ được tải lên tối đa 5 tệp hình ảnh/video bằng chứng!");
        }

        // Validate trước tất cả các file tải lên
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    fileStorageService.kiemTraTepTinHopLe(file);
                }
            }
        }

        // Sinh mã phiếu duy nhất
        String maCodePhieu = taoMaCodePhieu();

        PhieuKhieuNai phieu = new PhieuKhieuNai();
        phieu.setMaCodePhieu(maCodePhieu);
        phieu.setKhachHang(khachHang);
        phieu.setDonHangShop(donHangShop);
        phieu.setGianHang(donHangShop.getGianHang());
        phieu.setLoaiKhieuNai(form.getLoaiKhieuNai());
        phieu.setGiaiPhapYeuCau(form.getGiaiPhapYeuCau());
        phieu.setMucDoUuTien(form.getMucDoUuTien());
        phieu.setNoiDungMoTa(form.getNoiDungMoTa().trim());
        phieu.setSoTienHoanTra(soTienHoanTra);
        phieu.setTrangThai("MO_MOI");
        phieu.setNgayTao(LocalDateTime.now());

        PhieuKhieuNai phieuDaLuu = phieuKhieuNaiRepository.save(phieu);

        // Lưu các tệp bằng chứng
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

        return phieuDaLuu;
    }

    @Transactional
    public boolean huyKhieuNai(Long maPhieu, Long maKhachHang) {
        PhieuKhieuNai phieu = layChiTietPhieu(maPhieu);
        if (!phieu.getKhachHang().getMaNguoiDung().equals(maKhachHang)) {
            throw new SecurityException("Bạn không có quyền thao tác trên phiếu khiếu nại này!");
        }

        if ("MO_MOI".equals(phieu.getTrangThai())) {
            phieu.setTrangThai("DA_HUY");
            phieuKhieuNaiRepository.save(phieu);
            return true;
        }
        return false;
    }

    private String taoMaCodePhieu() {
        String prefix = "KN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int randomSuffix = new Random().nextInt(9000) + 1000;
        return prefix + "-" + randomSuffix;
    }
}
