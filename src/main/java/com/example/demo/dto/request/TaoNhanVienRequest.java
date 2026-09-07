package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO yêu cầu cấp tài khoản nhân viên nội bộ (Shipper / CSKH) - US-06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaoNhanVienRequest {

    @NotBlank(message = "Họ và tên nhân viên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên nhân viên từ 2 đến 100 ký tự")
    private String hoVaTen;

    @NotBlank(message = "Email công việc không được để trống")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Email không đúng định dạng chuẩn quốc tế"
    )
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
            message = "Số điện thoại phải gồm 10 số di động Việt Nam (VD: 0901234567)"
    )
    private String soDienThoai;

    @NotBlank(message = "Mật khẩu khởi tạo không được để trống")
    @Size(min = 8, message = "Mật khẩu khởi tạo phải có tối thiểu 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]).{8,}$",
            message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt"
    )
    private String matKhau;

    @NotBlank(message = "Vui lòng chọn vai trò nội bộ cho nhân viên")
    @Pattern(
            regexp = "^(TAI_XE|CSKH)$",
            message = "Vai trò nội bộ chỉ chấp nhận TAI_XE (Tài xế giao hàng) hoặc CSKH (Chăm sóc khách hàng)"
    )
    private String vaiTro;

    private String ghiChu;
}