package com.example.demo.controller.api;

import com.example.demo.dto.request.DatLaiMatKhauRequest;
import com.example.demo.dto.request.GuiYeuCauOtpRequest;
import com.example.demo.dto.request.XacThucOtpRequest;
import com.example.demo.dto.response.OtpResponse;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/otp")
@RequiredArgsConstructor
public class OtpApiController {

    private final OtpService otpService;

    /**
     * US-03: Gửi mã OTP khôi phục mật khẩu qua Email / SĐT
     * - Rate limit 3 lần / 1 giờ
     * - Hết hạn sau 300 giây
     */
    @PostMapping("/gui-yeu-cau")
    public ResponseEntity<PhanHoiApi<OtpResponse>> guiYeuCauOtp(@Valid @RequestBody GuiYeuCauOtpRequest yeuCau) {
        OtpResponse phanHoi = otpService.guiOtpKhoiPhuc(yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong(phanHoi.getThongBao(), phanHoi));
    }

    /**
     * US-03: Kiểm tra tính hợp lệ của mã OTP
     * - Sai quá 5 lần sẽ khóa 24 giờ
     */
    @PostMapping("/xac-thuc")
    public ResponseEntity<PhanHoiApi<Boolean>> xacThucOtp(@Valid @RequestBody XacThucOtpRequest yeuCau) {
        boolean hopLe = otpService.xacThucOtp(yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Mã OTP hợp lệ! Vui lòng nhập mật khẩu mới.", hopLe));
    }

    /**
     * US-03: Đặt lại mật khẩu mới sau khi xác thực OTP thành công
     */
    @PostMapping("/dat-lai-mat-khau")
    public ResponseEntity<PhanHoiApi<Void>> datLaiMatKhau(@Valid @RequestBody DatLaiMatKhauRequest yeuCau) {
        otpService.datLaiMatKhau(yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.", null));
    }
}
