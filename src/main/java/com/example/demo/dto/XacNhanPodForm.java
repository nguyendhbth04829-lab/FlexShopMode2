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

    @NotNull(message = "Vui long lay toa do GPS noi giao hang.")
    @DecimalMin(value = "-90.0", message = "Vi do phai trong [-90, 90].")
    @DecimalMax(value = "90.0", message = "Vi do phai trong [-90, 90].")
    private BigDecimal viDoGiaoHang;

    @NotNull(message = "Vui long lay toa do GPS noi giao hang.")
    @DecimalMin(value = "-180.0", message = "Kinh do phai trong [-180, 180].")
    @DecimalMax(value = "180.0", message = "Kinh do phai trong [-180, 180].")
    private BigDecimal kinhDoGiaoHang;
}
