package com.example.demo.controller.api;

import com.example.demo.dto.request.DatLaiMatKhauRequest;
import com.example.demo.dto.request.GuiYeuCauOtpRequest;
import com.example.demo.dto.request.XacThucOtpRequest;
import com.example.demo.dto.response.OtpResponse;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.exception.XuLyNgoaiLeToanCuc;
import com.example.demo.service.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OtpApiControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OtpService otpService;

    @InjectMocks
    private OtpApiController otpApiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(otpApiController)
                .setControllerAdvice(new XuLyNgoaiLeToanCuc())
                .build();
    }

    @Test
    @DisplayName("API POST /api/v1/auth/otp/gui-yeu-cau - Thành công")
    void testApiGuiYeuCauOtp_ThanhCong() throws Exception {
        GuiYeuCauOtpRequest req = new GuiYeuCauOtpRequest("customer@flexshop.vn");

        OtpResponse resp = OtpResponse.builder()
                .thongBao("Mã OTP đã được gửi đến email customer@flexshop.vn")
                .nguoiNhan("customer@flexshop.vn")
                .soGiayHieuLuc(300)
                .soLanGuiConLai(2L)
                .maOtpDemo("654321")
                .build();

        when(otpService.guiOtpKhoiPhuc(any(GuiYeuCauOtpRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/auth/otp/gui-yeu-cau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.soGiayHieuLuc").value(300))
                .andExpect(jsonPath("$.duLieu.soLanGuiConLai").value(2))
                .andExpect(jsonPath("$.duLieu.maOtpDemo").value("654321"));
    }

    @Test
    @DisplayName("API POST /api/v1/auth/otp/gui-yeu-cau - Vượt quá rate limit trả về 429")
    void testApiGuiYeuCauOtp_RateLimit() throws Exception {
        GuiYeuCauOtpRequest req = new GuiYeuCauOtpRequest("customer@flexshop.vn");

        when(otpService.guiOtpKhoiPhuc(any(GuiYeuCauOtpRequest.class)))
                .thenThrow(new NgoaiLeUngDung("Bạn đã yêu cầu gửi mã OTP quá 3 lần trong vòng 1 giờ. Vui lòng thử lại sau 1 giờ!", HttpStatus.TOO_MANY_REQUESTS));

        mockMvc.perform(post("/api/v1/auth/otp/gui-yeu-cau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.thanhCong").value(false))
                .andExpect(jsonPath("$.thongBao").value("Bạn đã yêu cầu gửi mã OTP quá 3 lần trong vòng 1 giờ. Vui lòng thử lại sau 1 giờ!"));
    }

    @Test
    @DisplayName("API POST /api/v1/auth/otp/xac-thuc - Thành công")
    void testApiXacThucOtp_ThanhCong() throws Exception {
        XacThucOtpRequest req = new XacThucOtpRequest("customer@flexshop.vn", "123456");

        when(otpService.xacThucOtp(any(XacThucOtpRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/otp/xac-thuc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.thongBao").value("Mã OTP hợp lệ! Vui lòng nhập mật khẩu mới."));
    }

    @Test
    @DisplayName("API POST /api/v1/auth/otp/xac-thuc - Nhập sai quá 5 lần trả về 403 Forbidden")
    void testApiXacThucOtp_Khoa24h() throws Exception {
        XacThucOtpRequest req = new XacThucOtpRequest("customer@flexshop.vn", "999999");

        when(otpService.xacThucOtp(any(XacThucOtpRequest.class)))
                .thenThrow(new NgoaiLeUngDung("Bạn đã nhập sai mã OTP quá 5 lần! Chức năng khôi phục mật khẩu đã bị tạm khóa trong 24 giờ vì lý do bảo mật.", HttpStatus.FORBIDDEN));

        mockMvc.perform(post("/api/v1/auth/otp/xac-thuc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.thanhCong").value(false))
                .andExpect(jsonPath("$.thongBao").value("Bạn đã nhập sai mã OTP quá 5 lần! Chức năng khôi phục mật khẩu đã bị tạm khóa trong 24 giờ vì lý do bảo mật."));
    }

    @Test
    @DisplayName("API POST /api/v1/auth/otp/dat-lai-mat-khau - Thành công")
    void testApiDatLaiMatKhau_ThanhCong() throws Exception {
        DatLaiMatKhauRequest req = DatLaiMatKhauRequest.builder()
                .taiKhoan("customer@flexshop.vn")
                .maOtp("123456")
                .matKhauMoi("FlexShop@2026")
                .xacNhanMatKhau("FlexShop@2026")
                .build();

        doNothing().when(otpService).datLaiMatKhau(any(DatLaiMatKhauRequest.class));

        mockMvc.perform(post("/api/v1/auth/otp/dat-lai-mat-khau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.thongBao").value("Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới."));
    }
}
