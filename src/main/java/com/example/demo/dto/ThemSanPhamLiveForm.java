package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Form thêm sản phẩm vào phiên Livestream kèm giá độc quyền (US-61)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemSanPhamLiveForm {

    @NotNull(message = "Mã phòng live không được để trống")
    private Long maLive;

    @NotNull(message = "Vui lòng chọn sản phẩm")
    private Long maSanPham;

    @NotNull(message = "Vui lòng nhập giá độc quyền trên live")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá độc quyền live phải lớn hơn 0")
    private BigDecimal giaDocQuyenLive;

    @Builder.Default
    private Integer soLuongGioiHan = 50;

    @Builder.Default
    private Integer thuTuHienThi = 1;
}
