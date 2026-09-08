package com.example.demo.controller.api;

import com.example.demo.dto.request.DangKyRequest;
import com.example.demo.dto.request.DangNhapRequest;
import com.example.demo.dto.request.GuiOtpDangKyRequest;
import com.example.demo.dto.request.LamMoiTokenRequest;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.OtpResponse;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.dto.response.XacThucResponse;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.NguoiDungService;
import com.example.demo.service.OtpService;
import com.example.demo.service.XacThucService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class XacThucApiController {

    private final XacThucService xacThucService;
    private final NguoiDungService nguoiDungService;
    private final OtpService otpService;

    /**
     * US-01: Gửi mã OTP xác thực đăng ký tài khoản qua Gmail
     */
    @PostMapping("/register/gui-otp")
    public ResponseEntity<PhanHoiApi<OtpResponse>> guiOtpDangKy(@Valid @RequestBody GuiOtpDangKyRequest yeuCau) {
        OtpResponse phanHoi = otpService.guiOtpDangKy(yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong(phanHoi.getThongBao(), phanHoi));
    }

    /**
     * US-01: API Đăng ký tài khoản người dùng (kèm mã OTP Gmail)
     */
    @PostMapping("/register")
    public ResponseEntity<PhanHoiApi<NguoiDungResponse>> dangKy(@Valid @RequestBody DangKyRequest yeuCau) {
        NguoiDungResponse phanHoi = xacThucService.dangKy(yeuCau);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PhanHoiApi.thanhCong("Đăng ký tài khoản thành công! Bạn có thể đăng nhập ngay.", phanHoi));
    }

    /**
     * US-02: API Đăng nhập hệ thống (Sinh Access Token 15m & Refresh Token 7d lưu Redis)
     */
    @PostMapping("/login")
    public ResponseEntity<PhanHoiApi<XacThucResponse>> dangNhap(@Valid @RequestBody DangNhapRequest yeuCau) {
        XacThucResponse phanHoi = xacThucService.dangNhap(yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Đăng nhập thành công!", phanHoi));
    }

    /**
     * US-02: API Làm mới Access Token từ Refresh Token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<PhanHoiApi<XacThucResponse>> lamMoiToken(@Valid @RequestBody LamMoiTokenRequest yeuCau) {
        XacThucResponse phanHoi = xacThucService.lamMoiToken(yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Gia hạn Access Token thành công!", phanHoi));
    }

    /**
     * US-02: API Đăng xuất hệ thống (Thu hồi Refresh Token & Blacklist Access Token vào Redis)
     */
    @PostMapping("/logout")
    public ResponseEntity<PhanHoiApi<Void>> dangXuat(
            HttpServletRequest request,
            @RequestParam(value = "refreshToken", required = false) String refreshToken,
            @AuthenticationPrincipal NguoiDungPrincipal nguoiDungPrincipal) {

        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        Long userId = nguoiDungPrincipal != null ? nguoiDungPrincipal.getMaNguoiDung() : null;
        xacThucService.dangXuat(accessToken, userId, refreshToken);

        return ResponseEntity.ok(PhanHoiApi.thanhCong("Đăng xuất thành công khỏi hệ thống!", null));
    }

    /**
     * Lấy thông tin tài khoản đang đăng nhập
     */
    @GetMapping("/me")
    public ResponseEntity<PhanHoiApi<NguoiDungResponse>> layThongTinHienTai() {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thông tin tài khoản thành công!", nguoiDung));
    }
}
