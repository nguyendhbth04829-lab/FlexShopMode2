package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Form gán sản phẩm chính A hoặc phụ kiện mua kèm B vào Combo (US-54 - PROMOTION)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemSanPhamComboForm {

    @NotNull(message = "Mã chương trình không được để trống")
    private Long maCombo;

    @NotNull(message = "Vui lòng chọn biến thể sản phẩm")
    private Long maBienThe;

    @NotBlank(message = "Vui lòng chọn vai trò sản phẩm (Chính A hoặc Phụ kiện B)")
    private String vaiTro = "MUA_KEM_DEAL_SOC"; // SAN_PHAM_CHINH hoặc MUA_KEM_DEAL_SOC

    /**
     * Mức giảm giá theo % (Mặc định 50% cho Deal sốc)
     */
    private BigDecimal phanTramGiam = new BigDecimal("50.00");

    /**
     * Giá bán sau khi giảm sốc (nếu nhập trực tiếp)
     */
    private BigDecimal giaUuDai;

    @Min(value = 1, message = "Giới hạn mua kèm mỗi đơn phải từ 1 trở lên")
    private Integer gioiHanMuaKemMoiDon = 1;

    @Min(value = 1, message = "Số lượng suất mở bán phải từ 1 trở lên")
    private Integer soLuongToiDa = 100;
}