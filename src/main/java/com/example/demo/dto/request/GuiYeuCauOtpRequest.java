package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiYeuCauOtpRequest {

    @NotBlank(message = "Vui lòng nhập Email hoặc Số điện thoại để nhận mã OTP")
    private String taiKhoan;
}
