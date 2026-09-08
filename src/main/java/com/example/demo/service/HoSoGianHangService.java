package com.example.demo.service;

import com.example.demo.dto.request.CapNhatThongTinShopRequest;
import com.example.demo.dto.request.ThemChungChiRequest;
import com.example.demo.dto.response.ChungChiGianHangResponse;
import com.example.demo.dto.response.HoSoGianHangDayDuResponse;
import com.example.demo.entity.ChungChiGianHang;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.NguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.ChungChiGianHangRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoSoGianHangService {

    private final GianHangRepository gianHangRepository;
    private final ChungChiGianHangRepository chungChiGianHangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final DangKyGianHangService dangKyGianHangService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Set<String> EXT_GIAY_TO_HOP_LE = Set.of(
            ".pdf", ".jpeg", ".jpg", ".png"
    );

    private static final Set<String> MIME_GIAY_TO_HOP_LE = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private static final Set<String> EXT_ANH_HOP_LE = Set.of(
            ".jpg", ".jpeg", ".png", ".webp"
    );

    private static final Set<String> MIME_ANH_HOP_LE = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    /**
     * Lấy đầy đủ thông tin hồ sơ gian hàng, thống kê và danh sách chứng chỉ pháp lý của Seller
     */
    @Transactional(readOnly = true)
    public HoSoGianHangDayDuResponse layHoSoGianHangCuaToi(Long maNguoiDung) {
        GianHang gianHang = gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Bạn chưa có gian hàng nào trên hệ thống FlexShop. Vui lòng đăng ký mở gian hàng!", HttpStatus.NOT_FOUND));

        NguoiDung chuSoHuu = nguoiDungRepository.findById(gianHang.getMaChuSoHuu()).orElse(null);

        List<ChungChiGianHang> danhSachCc = chungChiGianHangRepository.findByMaGianHangOrderByMaChungChiDesc(gianHang.getMaGianHang());

        List<ChungChiGianHangResponse> danhSachCcResponse = danhSachCc.stream()
                .map(this::chuyenSangChungChiResponse)
                .collect(Collectors.toList());

        long tongCc = chungChiGianHangRepository.countByMaGianHang(gianHang.getMaGianHang());
        long daDuyet = chungChiGianHangRepository.countByMaGianHangAndTrangThaiDuyet(gianHang.getMaGianHang(), "DA_DUYET");
        long choDuyet = chungChiGianHangRepository.countByMaGianHangAndTrangThaiDuyet(gianHang.getMaGianHang(), "CHO_DUYET");
        long tuChoi = chungChiGianHangRepository.countByMaGianHangAndTrangThaiDuyet(gianHang.getMaGianHang(), "TU_CHOI");

        return HoSoGianHangDayDuResponse.builder()
                .maGianHang(gianHang.getMaGianHang())
                .maChuSoHuu(gianHang.getMaChuSoHuu())
                .tenChuSoHuu(chuSoHuu != null ? chuSoHuu.getHoVaTen() : null)
                .emailChuSoHuu(chuSoHuu != null ? chuSoHuu.getEmail() : null)
                .sdtChuSoHuu(chuSoHuu != null ? chuSoHuu.getSoDienThoai() : null)
                .tenGianHang(gianHang.getTenGianHang())
                .duongDanSlug(gianHang.getDuongDanSlug())
                .moTa(gianHang.getMoTa())
                .linkLogo(gianHang.getLinkLogo())
                .linkBanner(gianHang.getLinkBanner())
                .diaChiKho(gianHang.getDiaChiKho())
                .sdtKho(gianHang.getSdtKho())
                .trangThai(gianHang.getTrangThai())
                .tenTrangThai(HoSoGianHangDayDuResponse.chuyenDoiTenTrangThaiShop(gianHang.getTrangThai()))
                .lyDoTuChoi(gianHang.getLyDoTuChoi())
                .hangGianHang(gianHang.getHangGianHang())
                .tenHangGianHang(HoSoGianHangDayDuResponse.chuyenDoiTenHangShop(gianHang.getHangGianHang()))
                .diemSaoQuaTa(gianHang.getDiemSaoQuaTa() != null ? gianHang.getDiemSaoQuaTa() : 0)
                .diemDanhGiaTb(gianHang.getDiemDanhGiaTb())
                .tongDanhGia(gianHang.getTongDanhGia() != null ? gianHang.getTongDanhGia() : 0)
                .tongDonHang(gianHang.getTongDonHang() != null ? gianHang.getTongDonHang() : 0)
                .tyLePhanHoiChat(gianHang.getTyLePhanHoiChat())
                .ngayTao(gianHang.getNgayTao())
                .tongSoChungChi(tongCc)
                .soChungChiDaDuyet(daDuyet)
                .soChungChiChoDuyet(choDuyet)
                .soChungChiTuChoi(tuChoi)
                .danhSachChungChi(danhSachCcResponse)
                .build();
    }

    /**
     * Cập nhật thông tin gian hàng (Tên shop, Mô tả, Địa chỉ kho, SĐT kho, Logo)
     */
    @Transactional
    public HoSoGianHangDayDuResponse capNhatThongTinGianHang(
            Long maNguoiDung,
            CapNhatThongTinShopRequest yeuCau,
            MultipartFile fileLogo
    ) {
        GianHang gianHang = gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy gian hàng của bạn để cập nhật!", HttpStatus.NOT_FOUND));

        String tenMoi = yeuCau.getTenGianHang().trim();

        // 1. Kiểm tra trùng tên shop nếu tên thay đổi
        if (!gianHang.getTenGianHang().equalsIgnoreCase(tenMoi)) {
            if (gianHangRepository.existsByTenGianHangIgnoreCaseAndMaGianHangNotAndDaXoaFalse(tenMoi, gianHang.getMaGianHang())) {
                throw new NgoaiLeUngDung("Tên gian hàng '" + tenMoi + "' đã có shop khác sử dụng. Vui lòng chọn tên khác!", HttpStatus.CONFLICT, "tenGianHang");
            }
            gianHang.setTenGianHang(tenMoi);
            // Tự động sinh lại slug mới theo tên shop đã đổi
            String slugMoi = dangKyGianHangService.taoDuongDanSlugTuTenShop(tenMoi, gianHang.getMaGianHang());
            gianHang.setDuongDanSlug(slugMoi);
        }

        // 2. Cập nhật các trường thông tin cơ bản
        gianHang.setMoTa(StringUtils.hasText(yeuCau.getMoTa()) ? yeuCau.getMoTa().trim() : null);
        gianHang.setDiaChiKho(yeuCau.getDiaChiKho().trim());
        gianHang.setSdtKho(yeuCau.getSdtKho().trim());

        // 3. Xử lý tải lên Logo mới nếu có
        if (fileLogo != null && !fileLogo.isEmpty()) {
            String linkLogo = luuTepLogo(fileLogo, maNguoiDung);
            gianHang.setLinkLogo(linkLogo);
        }

        gianHangRepository.save(gianHang);
        log.info("Cập nhật thông tin gian hàng thành công: maGianHang={}, tenGianHang={}", gianHang.getMaGianHang(), gianHang.getTenGianHang());

        return layHoSoGianHangCuaToi(maNguoiDung);
    }

    /**
     * Nộp thêm / Cập nhật chứng chỉ, giấy phép kinh doanh mới cho shop
     * Giấy tờ nộp mới sẽ ở trạng thái CHO_DUYET để Admin kiểm duyệt
     */
    @Transactional
    public ChungChiGianHangResponse themChungChi(
            Long maNguoiDung,
            ThemChungChiRequest yeuCau,
            MultipartFile fileGiayTo
    ) {
        GianHang gianHang = gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy gian hàng của bạn để nộp chứng chỉ!", HttpStatus.NOT_FOUND));

        if (fileGiayTo == null || fileGiayTo.isEmpty()) {
            throw new NgoaiLeUngDung("Bắt buộc phải tải lên tệp chứng chỉ / giấy phép (PDF, JPG, PNG < 5MB)!", HttpStatus.BAD_REQUEST, "fileGiayTo");
        }

        String soGiayTo = yeuCau.getSoGiayTo().trim();

        // Kiểm tra số giấy tờ có trùng với gian hàng khác không
        if (chungChiGianHangRepository.existsBySoGiayToAndMaGianHangNot(soGiayTo, gianHang.getMaGianHang())) {
            throw new NgoaiLeUngDung("Số giấy tờ / Mã số thuế '" + soGiayTo + "' đã được đăng ký bởi gian hàng khác!", HttpStatus.CONFLICT, "soGiayTo");
        }

        // Lưu tệp an toàn
        String linkGiayTo = luuTepGiayTo(fileGiayTo, maNguoiDung);

        ChungChiGianHang chungChi = ChungChiGianHang.builder()
                .maGianHang(gianHang.getMaGianHang())
                .loaiGiayTo(yeuCau.getLoaiGiayTo().trim())
                .soGiayTo(soGiayTo)
                .linkAnhGiayTo(linkGiayTo)
                .trangThaiDuyet("CHO_DUYET")
                .ngayTao(LocalDateTime.now())
                .build();

        ChungChiGianHang daLuu = chungChiGianHangRepository.save(chungChi);
        log.info("Nộp chứng chỉ mới thành công: maChungChi={}, loaiGiayTo={}, maGianHang={}", daLuu.getMaChungChi(), daLuu.getLoaiGiayTo(), gianHang.getMaGianHang());

        return chuyenSangChungChiResponse(daLuu);
    }

    /**
     * Xóa chứng chỉ pháp lý (Chỉ cho phép xóa khi đang CHO_DUYET hoặc TU_CHOI)
     */
    @Transactional
    public void xoaChungChi(Long maNguoiDung, Long maChungChi) {
        GianHang gianHang = gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy gian hàng của bạn!", HttpStatus.NOT_FOUND));

        ChungChiGianHang chungChi = chungChiGianHangRepository.findByMaChungChiAndMaGianHang(maChungChi, gianHang.getMaGianHang())
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy chứng chỉ/giấy tờ cần xóa hoặc không thuộc quyền sở hữu của bạn!", HttpStatus.NOT_FOUND));

        if ("DA_DUYET".equalsIgnoreCase(chungChi.getTrangThaiDuyet())) {
            throw new NgoaiLeUngDung("Không thể xóa chứng chỉ/giấy phép đã được Quản trị viên phê duyệt! Vui lòng liên hệ Admin nếu có sự thay đổi pháp lý.", HttpStatus.BAD_REQUEST);
        }

        // Xóa tệp vật lý nếu tồn tại
        xoaTepVatLy(chungChi.getLinkAnhGiayTo());

        chungChiGianHangRepository.delete(chungChi);
        log.info("Đã xóa chứng chỉ: maChungChi={}, maGianHang={}", maChungChi, gianHang.getMaGianHang());
    }

    /**
     * Helper chuyển đổi Entity ChungChiGianHang -> DTO ChungChiGianHangResponse
     */
    public ChungChiGianHangResponse chuyenSangChungChiResponse(ChungChiGianHang cc) {
        if (cc == null) return null;

        boolean isPdf = cc.getLinkAnhGiayTo() != null && cc.getLinkAnhGiayTo().toLowerCase(Locale.ROOT).endsWith(".pdf");

        return ChungChiGianHangResponse.builder()
                .maChungChi(cc.getMaChungChi())
                .maGianHang(cc.getMaGianHang())
                .loaiGiayTo(cc.getLoaiGiayTo())
                .tenLoaiGiayTo(ChungChiGianHangResponse.chuyenDoiTenLoaiGiayTo(cc.getLoaiGiayTo()))
                .soGiayTo(cc.getSoGiayTo())
                .linkAnhGiayTo(cc.getLinkAnhGiayTo())
                .trangThaiDuyet(cc.getTrangThaiDuyet())
                .tenTrangThaiDuyet(ChungChiGianHangResponse.chuyenDoiTenTrangThai(cc.getTrangThaiDuyet()))
                .ngayTao(cc.getNgayTao())
                .laTepPdf(isPdf)
                .build();
    }

    /**
     * Lưu trữ tệp chứng chỉ/giấy tờ an toàn (PDF, JPEG, JPG, PNG < 5MB)
     */
    private String luuTepGiayTo(MultipartFile file, Long maNguoiDung) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new NgoaiLeUngDung("Kích thước tệp chứng chỉ vượt quá giới hạn tối đa (5MB)!", HttpStatus.BAD_REQUEST, "fileGiayTo");
        }

        String tenGoc = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");
        String duoiTep = "";
        int dotIndex = tenGoc.lastIndexOf(".");
        if (dotIndex > 0) {
            duoiTep = tenGoc.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        if (!EXT_GIAY_TO_HOP_LE.contains(duoiTep)) {
            throw new NgoaiLeUngDung("Phần mở rộng tệp không hợp lệ (" + duoiTep + ")! Chỉ chấp nhận .pdf, .jpeg, .jpg, .png.", HttpStatus.BAD_REQUEST, "fileGiayTo");
        }

        String contentType = file.getContentType();
        if (contentType == null || !MIME_GIAY_TO_HOP_LE.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new NgoaiLeUngDung("Định dạng tệp không hợp lệ! Vui lòng tải lên tệp PDF, JPEG, JPG hoặc PNG.", HttpStatus.BAD_REQUEST, "fileGiayTo");
        }

        try {
            Path thuMucLuu = Paths.get("uploads", "giay-phep-kd").toAbsolutePath().normalize();
            if (!Files.exists(thuMucLuu)) {
                Files.createDirectories(thuMucLuu);
            }

            String tenTepMoi = "cc_" + maNguoiDung + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + duoiTep;
            Path duongDanDich = thuMucLuu.resolve(tenTepMoi);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, duongDanDich, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/giay-phep-kd/" + tenTepMoi;

        } catch (IOException e) {
            log.error("Lỗi khi lưu trữ tệp chứng chỉ gian hàng: ", e);
            throw new NgoaiLeUngDung("Không thể lưu trữ tệp chứng chỉ: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Lưu trữ ảnh logo gian hàng (JPG, JPEG, PNG, WEBP < 5MB)
     */
    private String luuTepLogo(MultipartFile file, Long maNguoiDung) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new NgoaiLeUngDung("Kích thước tệp ảnh logo không được vượt quá 5MB!", HttpStatus.BAD_REQUEST, "fileLogo");
        }

        String contentType = file.getContentType();
        if (contentType == null || !MIME_ANH_HOP_LE.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new NgoaiLeUngDung("Định dạng ảnh logo không hợp lệ! Chỉ chấp nhận JPG, JPEG, PNG, WEBP.", HttpStatus.BAD_REQUEST, "fileLogo");
        }

        String tenGoc = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "logo.png");
        String duoiTep = ".png";
        int dotIndex = tenGoc.lastIndexOf(".");
        if (dotIndex > 0) {
            duoiTep = tenGoc.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        try {
            Path thuMucLuu = Paths.get("uploads", "shop-logos").toAbsolutePath().normalize();
            if (!Files.exists(thuMucLuu)) {
                Files.createDirectories(thuMucLuu);
            }

            String tenTepMoi = "logo_" + maNguoiDung + "_" + System.currentTimeMillis() + duoiTep;
            Path duongDanDich = thuMucLuu.resolve(tenTepMoi);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, duongDanDich, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/shop-logos/" + tenTepMoi;

        } catch (IOException e) {
            log.error("Lỗi khi lưu trữ ảnh logo gian hàng: ", e);
            throw new NgoaiLeUngDung("Không thể lưu trữ ảnh logo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper xóa tệp vật lý an toàn
     */
    private void xoaTepVatLy(String duongDanTuongDoi) {
        if (!StringUtils.hasText(duongDanTuongDoi)) return;
        try {
            String duongDanChuan = duongDanTuongDoi.startsWith("/") ? duongDanTuongDoi.substring(1) : duongDanTuongDoi;
            Path path = Paths.get(duongDanChuan).toAbsolutePath().normalize();
            Path rootUploads = Paths.get("uploads").toAbsolutePath().normalize();
            // Đảm bảo tệp nằm trong thư mục uploads (chống path traversal)
            if (path.startsWith(rootUploads) && Files.exists(path)) {
                Files.delete(path);
                log.info("Đã xóa tệp vật lý: {}", path);
            }
        } catch (Exception e) {
            log.warn("Không thể xóa tệp vật lý {}: {}", duongDanTuongDoi, e.getMessage());
        }
    }
}
