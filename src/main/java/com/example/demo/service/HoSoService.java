package com.example.demo.service;

import com.example.demo.dto.request.CapNhatHoSoRequest;
import com.example.demo.dto.request.DoiMatKhauRequest;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeHoSoResponse;
import com.example.demo.entity.NguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.DiaChiNguoiDungRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.security.NguoiDungPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Dịch vụ xử lý nghiệp vụ Hồ sơ cá nhân (US-04)
 * Áp dụng cho toàn bộ các vai trò người dùng (All Roles)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HoSoService {

    private final NguoiDungRepository nguoiDungRepository;
    private final DiaChiNguoiDungRepository diaChiNguoiDungRepository;
    private final XacThucService xacThucService;
    private final PasswordEncoder passwordEncoder;

    @Value("${flexshop.upload.dir:uploads/avatars}")
    private String thuMucUpload;

    private static final List<String> MIME_HOP_LE = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Trích xuất thực thể NguoiDung của người dùng đang đăng nhập từ Security Context
     */
    @Transactional(readOnly = true)
    public NguoiDung layNguoiDungHienTaiEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new NgoaiLeUngDung("Chưa xác thực danh tính người dùng! Vui lòng đăng nhập.", HttpStatus.UNAUTHORIZED);
        }

        NguoiDungPrincipal principal = (NguoiDungPrincipal) authentication.getPrincipal();
        return nguoiDungRepository.findById(principal.getMaNguoiDung())
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy thông tin tài khoản người dùng!", HttpStatus.NOT_FOUND));
    }

    /**
     * Xem thông tin hồ sơ cá nhân hiện tại
     */
    @Transactional(readOnly = true)
    public NguoiDungResponse layHoSoHienTai() {
        NguoiDung nguoiDung = layNguoiDungHienTaiEntity();
        return xacThucService.chuyenSangNguoiDungResponse(nguoiDung);
    }

    /**
     * Cập nhật thông tin hồ sơ cá nhân (Họ tên, SĐT, Avatar URL)
     */
    @Transactional
    public NguoiDungResponse capNhatHoSo(CapNhatHoSoRequest yeuCau) {
        NguoiDung nguoiDung = layNguoiDungHienTaiEntity();

        String sdtMoi = yeuCau.getSoDienThoai().trim();
        String hoVaTenMoi = yeuCau.getHoVaTen().trim();

        // 1. Kiểm tra tính duy nhất của Số điện thoại nếu người dùng đổi số khác
        if (!sdtMoi.equals(nguoiDung.getSoDienThoai())) {
            boolean daTonTai = nguoiDungRepository.existsBySoDienThoaiAndMaNguoiDungNot(sdtMoi, nguoiDung.getMaNguoiDung());
            if (daTonTai) {
                throw new NgoaiLeUngDung("Số điện thoại '" + sdtMoi + "' đã được đăng ký bởi một tài khoản khác!", HttpStatus.CONFLICT);
            }
            nguoiDung.setSoDienThoai(sdtMoi);
        }

        // 2. Cập nhật Họ và tên
        nguoiDung.setHoVaTen(hoVaTenMoi);

        // 3. Cập nhật Avatar nếu có truyền đường dẫn ảnh mới
        if (StringUtils.hasText(yeuCau.getAnhDaiDien())) {
            nguoiDung.setAnhDaiDien(yeuCau.getAnhDaiDien().trim());
        }

        nguoiDung.setNgayCapNhat(LocalDateTime.now());
        NguoiDung nguoiDungCapNhat = nguoiDungRepository.save(nguoiDung);

        log.info("Cập nhật thành công hồ sơ người dùng ID: {}, Email: {}", nguoiDung.getMaNguoiDung(), nguoiDung.getEmail());
        return xacThucService.chuyenSangNguoiDungResponse(nguoiDungCapNhat);
    }

    /**
     * Tải lên tệp ảnh đại diện mới (Upload Avatar)
     */
    @Transactional
    public NguoiDungResponse taiLenAnhDaiDien(MultipartFile teps) {
        if (teps == null || teps.isEmpty()) {
            throw new NgoaiLeUngDung("Vui lòng chọn tệp ảnh đại diện để tải lên!", HttpStatus.BAD_REQUEST);
        }

        // 1. Kiểm tra dung lượng tệp (tối đa 5MB)
        if (teps.getSize() > MAX_FILE_SIZE) {
            throw new NgoaiLeUngDung("Kích thước tệp ảnh không được vượt quá 5MB!", HttpStatus.BAD_REQUEST);
        }

        // 2. Kiểm tra định dạng MIME hợp lệ
        String contentType = teps.getContentType();
        if (contentType == null || !MIME_HOP_LE.contains(contentType.toLowerCase())) {
            throw new NgoaiLeUngDung("Định dạng ảnh không hợp lệ! Hệ thống chỉ hỗ trợ JPG, JPEG, PNG, WEBP, GIF.", HttpStatus.BAD_REQUEST);
        }

        NguoiDung nguoiDung = layNguoiDungHienTaiEntity();

        try {
            // 3. Chuẩn bị thư mục lưu trữ uploads/avatars
            Path thuMucLuu = Paths.get(thuMucUpload).toAbsolutePath().normalize();
            if (!Files.exists(thuMucLuu)) {
                Files.createDirectories(thuMucLuu);
            }

            // 4. Sinh tên tệp an toàn chống ghi đè
            String tenGoc = StringUtils.cleanPath(teps.getOriginalFilename() != null ? teps.getOriginalFilename() : "avatar.png");
            String duoiTep = "";
            int dotIndex = tenGoc.lastIndexOf(".");
            if (dotIndex > 0) {
                duoiTep = tenGoc.substring(dotIndex);
            } else {
                duoiTep = ".png";
            }

            String tenTepMoi = "avatar_user_" + nguoiDung.getMaNguoiDung() + "_" + UUID.randomUUID().toString().substring(0, 8) + duoiTep;
            Path duongDanDich = thuMucLuu.resolve(tenTepMoi);

            // 5. Ghi tệp vào đĩa cứng máy chủ
            try (InputStream inputStream = teps.getInputStream()) {
                Files.copy(inputStream, duongDanDich, StandardCopyOption.REPLACE_EXISTING);
            }

            // 6. Cập nhật đường dẫn ảnh đại diện trong Database
            String duongDanTruyCap = "/uploads/avatars/" + tenTepMoi;
            nguoiDung.setAnhDaiDien(duongDanTruyCap);
            nguoiDung.setNgayCapNhat(LocalDateTime.now());
            NguoiDung nguoiDungCapNhat = nguoiDungRepository.save(nguoiDung);

            log.info("Người dùng ID: {} đã tải lên ảnh đại diện mới thành công: {}", nguoiDung.getMaNguoiDung(), duongDanTruyCap);
            return xacThucService.chuyenSangNguoiDungResponse(nguoiDungCapNhat);

        } catch (IOException e) {
            log.error("Lỗi khi lưu trữ tệp ảnh đại diện: ", e);
            throw new NgoaiLeUngDung("Lỗi hệ thống khi lưu trữ ảnh đại diện: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Đổi mật khẩu tài khoản người dùng
     * - Kiểm tra mật khẩu hiện tại chính xác
     * - Tiêu chí bắt buộc: Mật khẩu mới KHÔNG ĐƯỢC TRÙNG mật khẩu hiện tại
     * - Xác nhận mật khẩu mới phải khớp
     */
    @Transactional
    public NguoiDungResponse doiMatKhau(DoiMatKhauRequest yeuCau) {
        NguoiDung nguoiDung = layNguoiDungHienTaiEntity();

        String matKhauHienTai = yeuCau.getMatKhauHienTai();
        String matKhauMoi = yeuCau.getMatKhauMoi();
        String xacNhanMatKhauMoi = yeuCau.getXacNhanMatKhauMoi();

        // 1. Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(matKhauHienTai, nguoiDung.getMatKhauMaHoa())) {
            throw new NgoaiLeUngDung("Mật khẩu hiện tại không chính xác!", HttpStatus.BAD_REQUEST);
        }

        // 2. Validate bắt buộc: Mật khẩu mới KHÔNG ĐƯỢC TRÙNG với mật khẩu hiện tại
        if (passwordEncoder.matches(matKhauMoi, nguoiDung.getMatKhauMaHoa())) {
            throw new NgoaiLeUngDung("Mật khẩu mới không được trùng với mật khẩu cũ!", HttpStatus.BAD_REQUEST);
        }

        // 3. Kiểm tra xác nhận mật khẩu mới
        if (!matKhauMoi.equals(xacNhanMatKhauMoi)) {
            throw new NgoaiLeUngDung("Mật khẩu xác nhận không khớp với mật khẩu mới!", HttpStatus.BAD_REQUEST);
        }

        // 4. Mã hóa mật khẩu mới bằng BCrypt và lưu vào DB
        nguoiDung.setMatKhauMaHoa(passwordEncoder.encode(matKhauMoi));
        nguoiDung.setNgayCapNhat(LocalDateTime.now());
        NguoiDung nguoiDungCapNhat = nguoiDungRepository.save(nguoiDung);

        log.info("Người dùng ID: {}, Email: {} đã đổi mật khẩu thành công", nguoiDung.getMaNguoiDung(), nguoiDung.getEmail());
        return xacThucService.chuyenSangNguoiDungResponse(nguoiDungCapNhat);
    }

    /**
     * Thống kê tổng quan hồ sơ tài khoản
     */
    @Transactional(readOnly = true)
    public ThongKeHoSoResponse layThongKeHoSo() {
        NguoiDung nguoiDung = layNguoiDungHienTaiEntity();
        NguoiDungResponse thongTinNguoiDung = xacThucService.chuyenSangNguoiDungResponse(nguoiDung);

        long tongDiaChi = diaChiNguoiDungRepository.countByMaNguoiDungAndDaXoaFalse(nguoiDung.getMaNguoiDung());

        LocalDateTime ngayTao = nguoiDung.getNgayTao() != null ? nguoiDung.getNgayTao() : LocalDateTime.now();
        long soNgayThamGia = ChronoUnit.DAYS.between(ngayTao, LocalDateTime.now());

        return ThongKeHoSoResponse.builder()
                .thongTinNguoiDung(thongTinNguoiDung)
                .tongSoDiaChi(tongDiaChi)
                .gioiHanDiaChi(20)
                .soNgayThamGia(soNgayThamGia)
                .ngayTao(ngayTao)
                .ngayCapNhat(nguoiDung.getNgayCapNhat())
                .trangThaiBaoMat("Bảo mật cao (BCrypt salt 12)")
                .build();
    }
}
