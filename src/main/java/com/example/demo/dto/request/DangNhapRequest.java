package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DangNhapRequest {

    @NotBlank(message = "Tài khoản (Email hoặc Số điện thoại) không được để trống")
    private String taiKhoan;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String matKhau;

    @Builder.Default
    private Boolean ghiNho = false;
}
