package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Form mua hàng nhanh trực tiếp trên Livestream với giá độc quyền (US-61)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatHangLiveForm {

    @NotNull(message = "Mã phòng live không được để trống")
    private Long maLive;

    @NotNull(message = "Vui lòng chọn sản phẩm muốn mua")
    private Long maSanPham;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng mua tối thiểu là 1")
    @Builder.Default
    private Integer soLuong = 1;

    @NotBlank(message = "Vui lòng nhập họ tên người nhận")
    private String hoTenNguoiNhan;

    @NotBlank(message = "Vui lòng nhập số điện thoại nhận hàng")
    private String soDienThoai;

    @NotBlank(message = "Vui lòng nhập địa chỉ nhận hàng")
    private String diaChiGiaoHang;

    private String ghiChu;
}
