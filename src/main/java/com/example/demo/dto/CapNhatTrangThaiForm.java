package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * US-34: Form bat/tat trang thai lam viec cua shipper (mobile app).
 * GPS validate: vi do [-90,90], kinh do [-180,180].
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapNhatTrangThaiForm {

    @NotNull(message = "Vui long chon trang thai lam viec.")
    private Boolean dangTrucTuyen;

    @NotNull(message = "Vui long lay toa do GPS hien tai.")
    @DecimalMin(value = "-90.0", message = "Vi do phai trong [-90, 90].")
    @DecimalMax(value = "90.0", message = "Vi do phai trong [-90, 90].")
    private BigDecimal viDoHienTai;

    @NotNull(message = "Vui long lay toa do GPS hien tai.")
    @DecimalMin(value = "-180.0", message = "Kinh do phai trong [-180, 180].")
    @DecimalMax(value = "180.0", message = "Kinh do phai trong [-180, 180].")
    private BigDecimal kinhDoHienTai;
}
