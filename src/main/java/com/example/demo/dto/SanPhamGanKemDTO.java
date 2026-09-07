package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO đại diện Sản phẩm được gắn kèm trong biểu tượng Giỏ hàng trên Video (US-62)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamGanKemDTO {

    private Long maSanPham;
    private String tenSanPham;
    private BigDecimal giaCoBan;
    private String linkAnh;
    private BigDecimal danhGiaTb;
    private Integer tongDaBan;
    private String tenGianHang;
    private String duongDanSlug;
}
