package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO yêu cầu đổi mật khẩu tài khoản người dùng (US-04)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiMatKhauRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String matKhauHienTai;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 32, message = "Mật khẩu mới phải từ 8 đến 32 ký tự")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,32}$",
            message = "Mật khẩu mới phải chứa ít nhất 1 chữ thường, 1 chữ hoa, 1 chữ số và 1 ký tự đặc biệt (@$!%*?&)"
    )
    private String matKhauMoi;

    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
    private String xacNhanMatKhauMoi;
}
