package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * US-37: Form xac nhan giao hang thanh cong (POD).
 * Anh POD gui qua RequestParam MultipartFile (bat buoc).
 * GPS la toa do giao hang gan watermark.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XacNhanPodForm {

    @NotNull(message = "Vui lòng lấy tọa độ GPS nơi giao hàng.")
    @DecimalMin(value = "-90.0", message = "Vĩ độ phải trong [-90, 90].")
    @DecimalMax(value = "90.0", message = "Vĩ độ phải trong [-90, 90].")
    private BigDecimal viDoGiaoHang;

    @NotNull(message = "Vui lòng lấy tọa độ GPS nơi giao hàng.")
    @DecimalMin(value = "-180.0", message = "Kinh độ phải trong [-180, 180].")
    @DecimalMax(value = "180.0", message = "Kinh độ phải trong [-180, 180].")
    private BigDecimal kinhDoGiaoHang;
}
