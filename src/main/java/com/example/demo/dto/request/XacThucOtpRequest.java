package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XacThucOtpRequest {

    @NotBlank(message = "Vui lòng nhập Email hoặc Số điện thoại")
    private String taiKhoan;

    @NotBlank(message = "Vui lòng nhập mã OTP gồm 6 chữ số")
    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP phải gồm đúng 6 chữ số ngẫu nhiên")
    private String maOtp;
}
