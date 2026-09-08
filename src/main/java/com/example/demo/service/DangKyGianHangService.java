package com.example.demo.service;

import com.example.demo.dto.request.DangKyGianHangRequest;
import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.PhanTrangResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.entity.ChungChiGianHang;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.NguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.ChungChiGianHangRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class DangKyGianHangService {

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5MB (theo tiêu chí US-08: < 5MB)
    private static final Set<String> MIME_GIAY_PHEP_HOP_LE = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );
    private static final Set<String> EXT_GIAY_PHEP_HOP_LE = Set.of(
            ".pdf", ".jpeg", ".jpg", ".png"
    );

    private static final Set<String> MIME_ANH_HOP_LE = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private final GianHangRepository gianHangRepository;
    private final ChungChiGianHangRepository chungChiGianHangRepository;
    private final NguoiDungRepository nguoiDungRepository;

    /**
     * US-08: Gửi yêu cầu đăng ký mở Shop kèm giấy phép kinh doanh
     * - Tên gian hàng & Slug duy nhất, không chứa ký tự đặc biệt
     * - Giấy phép kinh doanh PDF/JPEG/PNG < 5MB
     * - Đơn chuyển về trạng thái CHO_DUYET
     */
    @Transactional
    public GianHangResponse dangKyGianHang(
            Long maNguoiDung,
            DangKyGianHangRequest yeuCau,
            MultipartFile fileGiayPhep,
            MultipartFile fileLogo
    ) {
        // 1. Kiểm tra tài khoản người dùng
        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy thông tin người dùng trong hệ thống!", HttpStatus.NOT_FOUND));

        if (Boolean.TRUE.equals(nguoiDung.getDaXoa()) || "BI_KHOA".equalsIgnoreCase(nguoiDung.getTrangThai())) {
            throw new NgoaiLeUngDung("Tài khoản của bạn đang bị khóa hoặc không còn hiệu lực để mở gian hàng!", HttpStatus.FORBIDDEN);
        }

        // 2. Kiểm tra hồ sơ gian hàng hiện có của người dùng
        Optional<GianHang> gianHangHienCo = gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(maNguoiDung);
        if (gianHangHienCo.isPresent()) {
            GianHang shop = gianHangHienCo.get();
            if ("HOAT_DONG".equalsIgnoreCase(shop.getTrangThai())) {
                throw new NgoaiLeUngDung(
                        "Tài khoản của bạn đã sở hữu gian hàng '" + shop.getTenGianHang() + "' đang hoạt động! Mỗi người dùng chỉ được sở hữu 1 gian hàng.",
                        HttpStatus.CONFLICT
                );
            }
            if ("CHO_DUYET".equalsIgnoreCase(shop.getTrangThai())) {
                throw new NgoaiLeUngDung(
                        "Hồ sơ đăng ký mở gian hàng '" + shop.getTenGianHang() + "' của bạn đang chờ Quản trị viên duyệt! Vui lòng kiên nhẫn chờ kết quả.",
                        HttpStatus.BAD_REQUEST
                );
            }
            // Nếu hồ sơ trước đó bị TU_CHOI -> người dùng có thể dùng chức năng cập nhật nộp lại
            if ("TU_CHOI".equalsIgnoreCase(shop.getTrangThai())) {
                return capNhatHoSoDangKy(maNguoiDung, yeuCau, fileGiayPhep, fileLogo);
            }
        }

        // 3. Kiểm tra tính duy nhất của Tên gian hàng
        String tenGianHang = yeuCau.getTenGianHang().trim();
        if (gianHangRepository.existsByTenGianHangIgnoreCaseAndDaXoaFalse(tenGianHang)) {
            throw new NgoaiLeUngDung("Tên gian hàng '" + tenGianHang + "' đã được sử dụng. Vui lòng chọn tên khác!", HttpStatus.CONFLICT, "tenGianHang");
        }

        // 4. Sinh & Kiểm tra Slug tự động
        String slug = xacDinhVaTaoSlugDuyNhat(yeuCau.getDuongDanSlug(), tenGianHang, null);

        // 5. Kiểm tra và tải lên tệp Giấy phép kinh doanh (< 5MB, PDF/JPEG/PNG)
        if (fileGiayPhep == null || fileGiayPhep.isEmpty()) {
            throw new NgoaiLeUngDung("Bắt buộc tải lên tệp Giấy phép kinh doanh (PDF/JPEG/PNG < 5MB)!", HttpStatus.BAD_REQUEST, "fileGiayPhep");
        }
        String linkGiayPhep = luuTepGiayPhepKinhDoanh(fileGiayPhep, maNguoiDung);

        // 6. Xử lý tải lên Logo gian hàng (tùy chọn)
        String linkLogo = null;
        if (fileLogo != null && !fileLogo.isEmpty()) {
            linkLogo = luuTepLogoGianHang(fileLogo, maNguoiDung);
        }

        // 7. Tạo bản ghi Gian Hàng mới với trạng thái CHO_DUYET
        GianHang gianHang = GianHang.builder()
                .maChuSoHuu(maNguoiDung)
                .tenGianHang(tenGianHang)
                .duongDanSlug(slug)
                .moTa(StringUtils.hasText(yeuCau.getMoTa()) ? yeuCau.getMoTa().trim() : null)
                .linkLogo(linkLogo)
                .diaChiKho(yeuCau.getDiaChiKho().trim())
                .sdtKho(yeuCau.getSdtKho().trim())
                .trangThai("CHO_DUYET") // Tiêu chí US-08: Đơn chuyển về trạng thái CHO_DUYET
                .hangGianHang("TIEM_NANG")
                .diemSaoQuaTa(0)
                .tongDanhGia(0)
                .tongDonHang(0)
                .daXoa(false)
                .ngayTao(LocalDateTime.now())
                .build();

        GianHang gianHangSaved = gianHangRepository.save(gianHang);

        // 8. Tạo bản ghi Giấy chứng nhận / Giấy phép kinh doanh đính kèm
        ChungChiGianHang chungChi = ChungChiGianHang.builder()
                .maGianHang(gianHangSaved.getMaGianHang())
                .loaiGiayTo(StringUtils.hasText(yeuCau.getLoaiGiayTo()) ? yeuCau.getLoaiGiayTo().trim() : "GIAY_PHEP_KINH_DOANH")
                .soGiayTo(yeuCau.getSoGiayTo().trim())
                .linkAnhGiayTo(linkGiayPhep)
                .trangThaiDuyet("CHO_DUYET")
                .ngayTao(LocalDateTime.now())
                .build();

        ChungChiGianHang chungChiSaved = chungChiGianHangRepository.save(chungChi);

        log.info("Người dùng ID: {} đã gửi đơn đăng ký mở gian hàng '{}' thành công (Mã gian hàng: {})",
                maNguoiDung, tenGianHang, gianHangSaved.getMaGianHang());

        return chuyenSangGianHangResponse(gianHangSaved, chungChiSaved, nguoiDung);
    }

    /**
     * Cập nhật hồ sơ đăng ký gian hàng (khi đang CHO_DUYET hoặc nộp lại sau khi bị TU_CHOI)
     */
    @Transactional
    public GianHangResponse capNhatHoSoDangKy(
            Long maNguoiDung,
            DangKyGianHangRequest yeuCau,
            MultipartFile fileGiayPhep,
            MultipartFile fileLogo
    ) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy thông tin người dùng!", HttpStatus.NOT_FOUND));

        GianHang gianHang = gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Bạn chưa nộp hồ sơ đăng ký gian hàng nào!", HttpStatus.NOT_FOUND));

        if ("HOAT_DONG".equalsIgnoreCase(gianHang.getTrangThai())) {
            throw new NgoaiLeUngDung("Gian hàng của bạn đã hoạt động chính thức. Vui lòng vào Cài đặt gian hàng để chỉnh sửa!", HttpStatus.BAD_REQUEST);
        }

        String tenGianHangMoi = yeuCau.getTenGianHang().trim();
        if (gianHangRepository.existsByTenGianHangIgnoreCaseAndMaGianHangNotAndDaXoaFalse(tenGianHangMoi, gianHang.getMaGianHang())) {
            throw new NgoaiLeUngDung("Tên gian hàng '" + tenGianHangMoi + "' đã được sử dụng bởi gian hàng khác!", HttpStatus.CONFLICT, "tenGianHang");
        }

        // Tự động sinh hoặc xác thực slug mới
        String slugMoi = xacDinhVaTaoSlugDuyNhat(yeuCau.getDuongDanSlug(), tenGianHangMoi, gianHang.getMaGianHang());

        // Cập nhật thông tin gian hàng
        gianHang.setTenGianHang(tenGianHangMoi);
        gianHang.setDuongDanSlug(slugMoi);
        gianHang.setDiaChiKho(yeuCau.getDiaChiKho().trim());
        gianHang.setSdtKho(yeuCau.getSdtKho().trim());
        gianHang.setMoTa(StringUtils.hasText(yeuCau.getMoTa()) ? yeuCau.getMoTa().trim() : null);
        gianHang.setTrangThai("CHO_DUYET"); // Chuyển lại về trạng thái CHO_DUYET để Admin xét duyệt
        gianHang.setLyDoTuChoi(null); // Xóa lý do từ chối cũ

        if (fileLogo != null && !fileLogo.isEmpty()) {
            gianHang.setLinkLogo(luuTepLogoGianHang(fileLogo, maNguoiDung));
        }

        gianHangRepository.save(gianHang);

        // Cập nhật hoặc tạo mới chứng chỉ giấy phép
        ChungChiGianHang chungChi = chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(gianHang.getMaGianHang())
                .orElseGet(() -> ChungChiGianHang.builder().maGianHang(gianHang.getMaGianHang()).build());

        chungChi.setSoGiayTo(yeuCau.getSoGiayTo().trim());
        chungChi.setLoaiGiayTo(StringUtils.hasText(yeuCau.getLoaiGiayTo()) ? yeuCau.getLoaiGiayTo().trim() : "GIAY_PHEP_KINH_DOANH");
        chungChi.setTrangThaiDuyet("CHO_DUYET");

        if (fileGiayPhep != null && !fileGiayPhep.isEmpty()) {
            chungChi.setLinkAnhGiayTo(luuTepGiayPhepKinhDoanh(fileGiayPhep, maNguoiDung));
        }

        chungChiGianHangRepository.save(chungChi);

        log.info("Người dùng ID: {} đã cập nhật lại hồ sơ đăng ký gian hàng ID: {}", maNguoiDung, gianHang.getMaGianHang());
        return chuyenSangGianHangResponse(gianHang, chungChi, nguoiDung);
    }

    /**
     * Lấy thông tin hồ sơ đăng ký gian hàng của người dùng hiện tại
     */
    @Transactional(readOnly = true)
    public GianHangResponse layThongTinGianHangCuaToi(Long maNguoiDung) {
        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy thông tin người dùng!", HttpStatus.NOT_FOUND));

        Optional<GianHang> gianHangOpt = gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(maNguoiDung);
        if (gianHangOpt.isEmpty()) {
            return null;
        }

        GianHang gianHang = gianHangOpt.get();
        ChungChiGianHang chungChi = chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(gianHang.getMaGianHang()).orElse(null);

        return chuyenSangGianHangResponse(gianHang, chungChi, nguoiDung);
    }

    /**
     * Kiểm tra tính khả dụng của Tên gian hàng
     */
    @Transactional(readOnly = true)
    public boolean kiemTraTenGianHangKhaDung(String tenGianHang, Long maGianHangHienTai) {
        if (!StringUtils.hasText(tenGianHang)) {
            return false;
        }
        String tenTrim = tenGianHang.trim();
        if (maGianHangHienTai != null && maGianHangHienTai > 0) {
            return !gianHangRepository.existsByTenGianHangIgnoreCaseAndMaGianHangNotAndDaXoaFalse(tenTrim, maGianHangHienTai);
        }
        return !gianHangRepository.existsByTenGianHangIgnoreCaseAndDaXoaFalse(tenTrim);
    }

    /**
     * Kiểm tra tính khả dụng của Slug
     */
    @Transactional(readOnly = true)
    public boolean kiemTraSlugKhaDung(String slug, Long maGianHangHienTai) {
        if (!StringUtils.hasText(slug)) {
            return false;
        }
        String slugTrim = slug.trim().toLowerCase(Locale.ROOT);
        if (!slugTrim.matches("^[a-z0-9-]+$")) {
            return false;
        }
        if (maGianHangHienTai != null && maGianHangHienTai > 0) {
            return !gianHangRepository.existsByDuongDanSlugIgnoreCaseAndMaGianHangNotAndDaXoaFalse(slugTrim, maGianHangHienTai);
        }
        return !gianHangRepository.existsByDuongDanSlugIgnoreCaseAndDaXoaFalse(slugTrim);
    }

    /**
     * Thuật toán tự động sinh Slug chuẩn SEO từ tên Shop:
     * - Bỏ dấu tiếng Việt
     * - Chuyển chữ thường
     * - Thay thế ký tự đặc biệt bằng dấu gạch ngang (-)
     * - Đảm bảo duy nhất trong CSDL (thêm hậu tố -1, -2 nếu trùng)
     */
    public String taoDuongDanSlugTuTenShop(String tenShop, Long maGianHangHienTai) {
        if (!StringUtils.hasText(tenShop)) {
            return "shop-" + System.currentTimeMillis();
        }

        // 1. Chuyển chữ đ/Đ thành d/D trước khi khử dấu tiếng Việt chuẩn Unicode NFD
        String text = tenShop.trim();
        text = text.replace("đ", "d").replace("Đ", "d");

        // 2. Tách dấu và loại bỏ các ký tự dấu thanh
        String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String slug = pattern.matcher(nfdNormalizedString).replaceAll("");

        // 3. Chuyển về chữ thường, thay khoảng trắng và ký tự không phải chữ số bằng dấu gạch ngang
        slug = slug.toLowerCase(Locale.ROOT);
        slug = slug.replaceAll("[^a-z0-9]+", "-");

        // 4. Cắt tỉa dấu gạch ngang dư thừa ở đầu và cuối
        slug = slug.replaceAll("^-+|-+$", "");

        if (!StringUtils.hasText(slug)) {
            slug = "shop-" + System.currentTimeMillis();
        }

        // 5. Đảm bảo độ dài không vượt quá 100 ký tự để dành chỗ cho hậu tố
        if (slug.length() > 100) {
            slug = slug.substring(0, 100).replaceAll("-+$", "");
        }

        // 6. Kiểm tra tính duy nhất trong CSDL và thêm hậu tố nếu cần
        String slugGoc = slug;
        int dem = 1;
        while (!kiemTraSlugKhaDung(slug, maGianHangHienTai)) {
            slug = slugGoc + "-" + dem;
            dem++;
        }

        return slug;
    }

    /**
     * Xác định và tạo slug duy nhất (ưu tiên slug do người dùng nhập nếu hợp lệ, hoặc tự động sinh)
     */
    private String xacDinhVaTaoSlugDuyNhat(String slugNguoiDung, String tenGianHang, Long maGianHangHienTai) {
        if (StringUtils.hasText(slugNguoiDung)) {
            String slugTrim = slugNguoiDung.trim().toLowerCase(Locale.ROOT);
            if (!slugTrim.matches("^[a-z0-9-]+$")) {
                throw new NgoaiLeUngDung("Đường dẫn slug chỉ được chứa chữ thường không dấu, số và dấu gạch ngang (-)", HttpStatus.BAD_REQUEST, "duongDanSlug");
            }
            if (!kiemTraSlugKhaDung(slugTrim, maGianHangHienTai)) {
                throw new NgoaiLeUngDung("Đường dẫn slug '" + slugTrim + "' đã tồn tại! Vui lòng chọn slug khác.", HttpStatus.CONFLICT, "duongDanSlug");
            }
            return slugTrim;
        }

        return taoDuongDanSlugTuTenShop(tenGianHang, maGianHangHienTai);
    }

    /**
     * Tải lên và lưu trữ an toàn tệp Giấy phép kinh doanh (PDF/JPEG/PNG < 5MB)
     */
    private String luuTepGiayPhepKinhDoanh(MultipartFile file, Long maNguoiDung) {
        // 1. Kiểm tra kích thước < 5MB
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new NgoaiLeUngDung("Kích thước tệp giấy phép kinh doanh vượt quá giới hạn cho phép (tối đa 5MB)!", HttpStatus.BAD_REQUEST, "fileGiayPhep");
        }

        // 2. Kiểm tra phần mở rộng tệp
        String tenGoc = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "license.pdf");
        String duoiTep = "";
        int dotIndex = tenGoc.lastIndexOf(".");
        if (dotIndex > 0) {
            duoiTep = tenGoc.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        if (!EXT_GIAY_PHEP_HOP_LE.contains(duoiTep)) {
            throw new NgoaiLeUngDung("Phần mở rộng tệp giấy phép kinh doanh không hợp lệ (" + duoiTep + ")! Chỉ chấp nhận .pdf, .jpeg, .jpg, .png.", HttpStatus.BAD_REQUEST, "fileGiayPhep");
        }

        // 3. Kiểm tra Content-Type MIME
        String contentType = file.getContentType();
        if (contentType == null || !MIME_GIAY_PHEP_HOP_LE.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new NgoaiLeUngDung("Định dạng tệp không hợp lệ! Vui lòng tải lên tệp PDF, JPEG, JPG hoặc PNG.", HttpStatus.BAD_REQUEST, "fileGiayPhep");
        }

        try {
            // 4. Tạo thư mục lưu trữ uploads/giay-phep-kd/
            Path thuMucLuu = Paths.get("uploads", "giay-phep-kd").toAbsolutePath().normalize();
            if (!Files.exists(thuMucLuu)) {
                Files.createDirectories(thuMucLuu);
            }

            // 5. Đặt tên tệp duy nhất, an toàn chống tấn công Path Traversal
            String tenTepMoi = "gpkd_" + maNguoiDung + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + duoiTep;
            Path duongDanDich = thuMucLuu.resolve(tenTepMoi);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, duongDanDich, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/giay-phep-kd/" + tenTepMoi;

        } catch (IOException e) {
            log.error("Lỗi khi lưu trữ tệp giấy phép kinh doanh: ", e);
            throw new NgoaiLeUngDung("Không thể lưu trữ tệp giấy phép kinh doanh: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Tải lên và lưu trữ logo gian hàng (tùy chọn)
     */
    private String luuTepLogoGianHang(MultipartFile file, Long maNguoiDung) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new NgoaiLeUngDung("Kích thước tệp logo không được vượt quá 5MB!", HttpStatus.BAD_REQUEST, "fileLogo");
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
     * Danh sách phân trang, tìm kiếm và lọc hồ sơ đăng ký gian hàng
     */
    @Transactional(readOnly = true)
    public PhanTrangResponse<GianHangResponse> danhSachGianHangPhanTrang(
            String keyword,
            String trangThai,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        Page<GianHang> pageGianHang = gianHangRepository.timKiemPhanTrang(keyword, trangThai, pageable);

        List<GianHangResponse> danhSach = pageGianHang.getContent().stream().map(g -> {
            ChungChiGianHang cc = chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(g.getMaGianHang()).orElse(null);
            NguoiDung chuSoHuu = nguoiDungRepository.findById(g.getMaChuSoHuu()).orElse(null);
            return chuyenSangGianHangResponse(g, cc, chuSoHuu);
        }).toList();

        return PhanTrangResponse.<GianHangResponse>builder()
                .content(danhSach)
                .number(pageGianHang.getNumber())
                .totalPages(pageGianHang.getTotalPages())
                .totalElements(pageGianHang.getTotalElements())
                .size(pageGianHang.getSize())
                .first(pageGianHang.isFirst())
                .last(pageGianHang.isLast())
                .empty(pageGianHang.isEmpty())
                .build();
    }

    /**
     * Thống kê số lượng gian hàng theo các trạng thái
     */
    @Transactional(readOnly = true)
    public ThongKeGianHangResponse layThongKeGianHang() {
        return ThongKeGianHangResponse.builder()
                .tongSo(gianHangRepository.countByDaXoaFalse())
                .choDuyet(gianHangRepository.countByTrangThaiAndDaXoaFalse("CHO_DUYET"))
                .hoatDong(gianHangRepository.countByTrangThaiAndDaXoaFalse("HOAT_DONG"))
                .tuChoi(gianHangRepository.countByTrangThaiAndDaXoaFalse("TU_CHOI"))
                .tamKhoa(gianHangRepository.countByTrangThaiAndDaXoaFalse("TAM_KHOA"))
                .build();
    }

    /**
     * Helper ánh xạ Entity -> DTO Response
     */
    public GianHangResponse chuyenSangGianHangResponse(GianHang g, ChungChiGianHang cc, NguoiDung chuSoHuu) {
        if (g == null) return null;

        GianHangResponse.GianHangResponseBuilder builder = GianHangResponse.builder()
                .maGianHang(g.getMaGianHang())
                .maChuSoHuu(g.getMaChuSoHuu())
                .tenGianHang(g.getTenGianHang())
                .duongDanSlug(g.getDuongDanSlug())
                .moTa(g.getMoTa())
                .linkLogo(g.getLinkLogo())
                .linkBanner(g.getLinkBanner())
                .diaChiKho(g.getDiaChiKho())
                .sdtKho(g.getSdtKho())
                .trangThai(g.getTrangThai())
                .lyDoTuChoi(g.getLyDoTuChoi())
                .hangGianHang(g.getHangGianHang())
                .ngayTao(g.getNgayTao());

        if (chuSoHuu != null) {
            builder.tenChuSoHuu(chuSoHuu.getHoVaTen())
                   .emailChuSoHuu(chuSoHuu.getEmail())
                   .sdtChuSoHuu(chuSoHuu.getSoDienThoai());
        }

        if (cc != null) {
            builder.maChungChi(cc.getMaChungChi())
                   .loaiGiayTo(cc.getLoaiGiayTo())
                   .soGiayTo(cc.getSoGiayTo())
                   .linkAnhGiayTo(cc.getLinkAnhGiayTo())
                   .trangThaiGiayTo(cc.getTrangThaiDuyet());
        }

        return builder.build();
    }
}
