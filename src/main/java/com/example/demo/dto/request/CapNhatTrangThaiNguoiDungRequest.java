package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * DTO yêu cầu khóa hoặc mở khóa tài khoản người dùng - US-06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapNhatTrangThaiNguoiDungRequest {

    @NotBlank(message = "Trạng thái tài khoản không được để trống")
    @Pattern(
            regexp = "^(HOAT_DONG|BI_KHOA)$",
            message = "Trạng thái chỉ chấp nhận HOAT_DONG (Hoạt động) hoặc BI_KHOA (Khóa tài khoản)"
    )
    private String trangThai;

    private String lyDo;
}