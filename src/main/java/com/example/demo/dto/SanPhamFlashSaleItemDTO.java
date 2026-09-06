package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO hiển thị từng sản phẩm trong trang Flash Sale khách hàng (US-53 - PROMOTION)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamFlashSaleItemDTO {

    private Long maSanPhamFs;
    private Long maFlashSale;
    private Long maSanPham;
    private Long maBienThe;
    private String tenSanPham;
    private String tenBienThe;
    private String linkAnh;
    private String tenGianHang;

    private BigDecimal giaGoc;
    private BigDecimal giaFlashSale;
    private int phanTramGiam;

    private int soLuongGioiHan;
    private int soLuongDaBan;
    private int phanTramDaBan;
    private boolean chayHang;
}
