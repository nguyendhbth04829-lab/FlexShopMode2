package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiOtpDangKyRequest {

    @NotBlank(message = "Email không được để trống")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Email không đúng định dạng chuẩn"
    )
    private String email;

    @Size(min = 2, max = 100, message = "Họ và tên từ 2 đến 100 ký tự")
    private String hoVaTen;

    @Pattern(
            regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
            message = "Số điện thoại không đúng định dạng (VD: 0987654321 hoặc +84987654321)"
    )
    private String soDienThoai;
}
