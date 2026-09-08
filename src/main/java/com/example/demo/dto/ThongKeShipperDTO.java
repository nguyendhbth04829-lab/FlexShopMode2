package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * US-39: Thong ke dashboard shipper (lich su + COD doi soat).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeShipperDTO {

    private long tongCuoc;

    private long tongThanhCong;

    private long tongDangGiao;

    private long tongChoLayHang;

    private long tongLuotThatBai;

    private BigDecimal soDuCodDangGiu = BigDecimal.ZERO;

    private BigDecimal tongCodDaThu = BigDecimal.ZERO;

    private long tongThanhCongHomNay;

    private BigDecimal codThuHomNay = BigDecimal.ZERO;
}
