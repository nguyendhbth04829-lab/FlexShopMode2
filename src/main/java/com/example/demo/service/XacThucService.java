package com.example.demo.service;

import com.example.demo.dto.request.DangKyRequest;
import com.example.demo.dto.request.DangNhapRequest;
import com.example.demo.dto.request.LamMoiTokenRequest;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.XacThucResponse;
import com.example.demo.entity.GioHang;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.VaiTro;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.GioHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VaiTroRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.security.NguoiDungPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class XacThucService {

    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository;
    private final GioHangRepository gioHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    @Value("${flexshop.jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs;

    @Value("${flexshop.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    /**
     * US-01: Đăng ký tài khoản người dùng mới
     */
    @Transactional
    public NguoiDungResponse dangKy(DangKyRequest yeuCau) {
        String emailChuan = yeuCau.getEmail().trim().toLowerCase();

        // 1. Kiểm tra Email duy nhất (Case-insensitive)
        if (nguoiDungRepository.existsByEmailIgnoreCase(emailChuan)) {
            throw new NgoaiLeUngDung("Email '" + emailChuan + "' đã được đăng ký trong hệ thống. Vui lòng sử dụng email khác!", HttpStatus.CONFLICT);
        }

        // 2. Kiểm tra Số điện thoại duy nhất (nếu có cung cấp)
        String sdtChuan = null;
        if (StringUtils.hasText(yeuCau.getSoDienThoai())) {
            sdtChuan = yeuCau.getSoDienThoai().trim();
            if (nguoiDungRepository.existsBySoDienThoai(sdtChuan)) {
                throw new NgoaiLeUngDung("Số điện thoại '" + sdtChuan + "' đã được đăng ký trong hệ thống!", HttpStatus.CONFLICT);
            }
        }

        // 3. Mã hóa mật khẩu bằng BCrypt (Salt round >= 10, cấu hình là 12)
        String matKhauMaHoa = passwordEncoder.encode(yeuCau.getMatKhau());

        // 4. Gán vai trò mặc định: KHACH_HANG (Tiêu chí US-01)
        VaiTro vaiTroMacDinh = vaiTroRepository.findByTenVaiTro("KHACH_HANG")
                .orElseGet(() -> vaiTroRepository.save(new VaiTro("KHACH_HANG", "Khách hàng mua sắm mặc định")));

        Set<VaiTro> danhSachVaiTro = new HashSet<>();
        danhSachVaiTro.add(vaiTroMacDinh);

        // 5. Lưu thông tin người dùng mới (US-01: Mặc định luôn là KHACH_HANG)
        NguoiDung nguoiDung = NguoiDung.builder()
                .email(emailChuan)
                .soDienThoai(sdtChuan)
                .matKhauMaHoa(matKhauMaHoa)
                .hoVaTen(yeuCau.getHoVaTen().trim())
                .anhDaiDien("https://ui-avatars.com/api/?name=" + yeuCau.getHoVaTen().trim().replace(" ", "+") + "&background=4f46e5&color=fff")
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .danhSachVaiTro(danhSachVaiTro)
                .ngayTao(LocalDateTime.now())
                .ngayCapNhat(LocalDateTime.now())
                .build();

        NguoiDung nguoiDungDaLuu = nguoiDungRepository.save(nguoiDung);

        // 6. Tự động khởi tạo Giỏ hàng (gio_hang) cho người dùng mới
        if (!gioHangRepository.existsByMaNguoiDung(nguoiDungDaLuu.getMaNguoiDung())) {
            GioHang gioHang = GioHang.builder()
                    .maNguoiDung(nguoiDungDaLuu.getMaNguoiDung())
                    .ngayCapNhat(LocalDateTime.now())
                    .build();
            gioHangRepository.save(gioHang);
        }

        log.info("Đăng ký thành công người dùng mới ID: {}, Email: {}", nguoiDungDaLuu.getMaNguoiDung(), nguoiDungDaLuu.getEmail());
        return chuyenSangNguoiDungResponse(nguoiDungDaLuu);
    }

    /**
     * US-02: Đăng nhập hệ thống, sinh Access Token (15m) & Refresh Token (7d lưu Redis)
     */
    @Transactional(readOnly = true)
    public XacThucResponse dangNhap(DangNhapRequest yeuCau) {
        String dinhDanh = yeuCau.getTaiKhoan().trim();

        // 1. Tìm người dùng bằng Email hoặc Số điện thoại
        NguoiDung nguoiDung = nguoiDungRepository.findByIdentifier(dinhDanh)
                .orElseThrow(() -> new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác"));

        // 2. Kiểm tra trạng thái tài khoản
        if (Boolean.TRUE.equals(nguoiDung.getDaXoa())) {
            throw new NgoaiLeUngDung("Tài khoản của bạn đã bị xóa khỏi hệ thống!", HttpStatus.FORBIDDEN);
        }
        if (!"HOAT_DONG".equalsIgnoreCase(nguoiDung.getTrangThai())) {
            throw new NgoaiLeUngDung("Tài khoản của bạn đang bị khóa (" + nguoiDung.getTrangThai() + "). Vui lòng liên hệ CSKH!", HttpStatus.FORBIDDEN);
        }

        // 3. Kiểm tra mật khẩu mã hóa BCrypt
        if (!passwordEncoder.matches(yeuCau.getMatKhau(), nguoiDung.getMatKhauMaHoa())) {
            throw new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác");
        }

        // 4. Tạo NguoiDungPrincipal
        NguoiDungPrincipal nguoiDungPrincipal = NguoiDungPrincipal.tao(nguoiDung);

        // 5. Sinh Access Token (hết hạn sau 15 phút = 900,000 ms)
        String accessToken = jwtTokenProvider.generateAccessToken(nguoiDungPrincipal);

        // 6. Sinh Refresh Token (hết hạn sau 7 ngày = 604,800,000 ms)
        String tokenId = UUID.randomUUID().toString();
        String refreshToken = jwtTokenProvider.generateRefreshToken(nguoiDungPrincipal, tokenId);

        // 7. Lưu Refresh Token vào Redis với thời hạn 7 ngày
        tokenRedisService.saveRefreshToken(nguoiDung.getMaNguoiDung(), tokenId, refreshToken, refreshTokenExpirationMs);

        // 8. Chuẩn bị thông tin phản hồi và điều hướng Dashboard theo 5 Role
        NguoiDungResponse thongTinNguoiDung = chuyenSangNguoiDungResponse(nguoiDung);

        return XacThucResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .loaiToken("Bearer")
                .thoiGianHetHan(accessTokenExpirationMs / 1000)
                .thongTinNguoiDung(thongTinNguoiDung)
                .build();
    }

    /**
     * Gia hạn Access Token mới bằng Refresh Token
     */
    public XacThucResponse lamMoiToken(LamMoiTokenRequest yeuCau) {
        String refreshToken = yeuCau.getRefreshToken();

        // 1. Kiểm tra chữ ký và hạn dùng của Refresh Token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new NgoaiLeUngDung("Refresh Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED);
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            throw new NgoaiLeUngDung("Loại token không hợp lệ (yêu cầu REFRESH token)", HttpStatus.BAD_REQUEST);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String tokenId = jwtTokenProvider.getTokenId(refreshToken);

        // 2. Kiểm tra tính hợp lệ trong Redis
        if (!tokenRedisService.validateRefreshToken(userId, tokenId, refreshToken)) {
            throw new NgoaiLeUngDung("Refresh Token không tồn tại hoặc đã bị thu hồi trong Redis", HttpStatus.UNAUTHORIZED);
        }

        // 3. Tải thông tin người dùng
        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new NgoaiLeUngDung("Người dùng không tồn tại", HttpStatus.NOT_FOUND));

        if (!"HOAT_DONG".equalsIgnoreCase(nguoiDung.getTrangThai()) || Boolean.TRUE.equals(nguoiDung.getDaXoa())) {
            throw new NgoaiLeUngDung("Tài khoản người dùng không còn hoạt động", HttpStatus.FORBIDDEN);
        }

        NguoiDungPrincipal nguoiDungPrincipal = NguoiDungPrincipal.tao(nguoiDung);

        // 4. Sinh Access Token mới (15 phút)
        String newAccessToken = jwtTokenProvider.generateAccessToken(nguoiDungPrincipal);

        return XacThucResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .loaiToken("Bearer")
                .thoiGianHetHan(accessTokenExpirationMs / 1000)
                .thongTinNguoiDung(chuyenSangNguoiDungResponse(nguoiDung))
                .build();
    }

    /**
     * Đăng xuất hệ thống: Thu hồi Refresh Token và Blacklist Access Token còn hạn vào Redis
     */
    public void dangXuat(String accessToken, Long userId, String refreshToken) {
        // 1. Blacklist Access Token vào Redis (nếu còn hiệu lực)
        if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)) {
            long remainingMs = jwtTokenProvider.getRemainingExpirationMs(accessToken);
            tokenRedisService.blacklistAccessToken(accessToken, remainingMs);
        }

        // 2. Thu hồi Refresh Token khỏi Redis
        if (StringUtils.hasText(refreshToken) && jwtTokenProvider.validateToken(refreshToken)) {
            String tokenId = jwtTokenProvider.getTokenId(refreshToken);
            Long refUserId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            tokenRedisService.revokeRefreshToken(refUserId, tokenId);
        }

        // 3. Xóa ngữ cảnh bảo mật hiện tại
        SecurityContextHolder.clearContext();
        log.info("Người dùng userId: {} đã đăng xuất an toàn khỏi hệ thống", userId);
    }

    /**
     * Chuyển đổi Entity NguoiDung sang NguoiDungResponse
     */
    public NguoiDungResponse chuyenSangNguoiDungResponse(NguoiDung nguoiDung) {
        List<String> roleNames = nguoiDung.getDanhSachVaiTro().stream()
                .map(VaiTro::getTenVaiTro)
                .distinct()
                .collect(Collectors.toList());

        // Chuẩn hóa và loại bỏ lặp nếu người dùng đồng thời có cả TAI_XE và SHIPPER
        if (roleNames.contains("TAI_XE") && roleNames.contains("SHIPPER")) {
            roleNames.remove("SHIPPER");
        }

        String targetDashboardUrl = xacDinhDuongDanDashboard(roleNames);

        return NguoiDungResponse.builder()
                .maNguoiDung(nguoiDung.getMaNguoiDung())
                .email(nguoiDung.getEmail())
                .soDienThoai(nguoiDung.getSoDienThoai())
                .hoVaTen(nguoiDung.getHoVaTen())
                .anhDaiDien(nguoiDung.getAnhDaiDien())
                .trangThai(nguoiDung.getTrangThai())
                .danhSachVaiTro(roleNames)
                .duongDanDashboard(targetDashboardUrl)
                .ngayTao(nguoiDung.getNgayTao())
                .build();
    }

    /**
     * Tiêu chí US-02: Điều hướng đúng trang Dashboard theo 5 Role
     */
    public String xacDinhDuongDanDashboard(List<String> roles) {
        if (roles.contains("ADMIN") || roles.contains("ROLE_ADMIN")) {
            return "/admin/dashboard";
        }
        if (roles.contains("NGUOI_BAN") || roles.contains("ROLE_NGUOI_BAN")) {
            return "/seller/dashboard";
        }
        if (roles.contains("CSKH") || roles.contains("ROLE_CSKH")) {
            return "/cskh/dashboard";
        }
        if (roles.contains("TAI_XE") || roles.contains("ROLE_TAI_XE") || roles.contains("SHIPPER") || roles.contains("ROLE_SHIPPER")) {
            return "/shipper/dashboard";
        }
        return "/customer/dashboard";
    }
}
