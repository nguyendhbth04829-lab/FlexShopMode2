package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Form đăng ký sản phẩm giảm sốc vào khung giờ Flash Sale (US-53 - PROMOTION)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DangKyFlashSaleForm {

    @NotNull(message = "Vui lòng chọn khung giờ Flash Sale")
    private Long maFlashSale;

    @NotNull(message = "Vui lòng chọn biến thể sản phẩm tham gia Flash Sale")
    private Long maBienThe;

    @NotNull(message = "Giá Flash Sale không được để trống")
    @DecimalMin(value = "1000.00", message = "Giá Flash Sale tối thiểu phải từ 1.000 đ")
    private BigDecimal giaFlashSale;

    @NotNull(message = "Số lượng suất Flash Sale không được để trống")
    @Min(value = 1, message = "Số lượng suất Flash Sale tối thiểu là 1")
    private Integer soLuongGioiHan;

    @NotNull(message = "Giới hạn mua mỗi khách không được để trống")
    @Min(value = 1, message = "Giới hạn mua mỗi khách tối thiểu là 1")
    private Integer gioiHanMuaMoiKhach = 1;
}
