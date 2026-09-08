package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * US-33: Form ghi checkpoint hanh trinh kien hang qua Hub.
 * maHub nullable: moc nhu CHO_LAY_HANG / DANG_GIAO co the chua qua Hub.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GhiHanhTrinhForm {

    @NotNull(message = "Vui lòng chọn đơn hàng shop.")
    @Min(value = 1, message = "Mã đơn hàng shop không hợp lệ.")
    private Long maDonHangShop;

    private Long maHub;

    @NotBlank(message = "Vui lòng nhập tiêu đề mốc (vd Đã rời Hub, Đang nhập Hub).")
    @Size(max = 150, message = "Tiêu đề mốc tối đa 150 ký tự.")
    private String tieuDeMoc;

    @Size(max = 255, message = "Vị trí hiện tại tối đa 255 ký tự.")
    private String viTriHienTai;
}
