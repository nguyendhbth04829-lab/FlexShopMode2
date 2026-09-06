package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheSanPhamDTO {

    private Long maSanPham;
    private String tenSanPham;
    private String duongDanSlug;
    private BigDecimal giaBan;
    private BigDecimal giaGoc;
    private String linkAnh;
    private Long maBienThe;
    private String tenBienThe;
    private Long maGianHang;
    private String tenGianHang;
}
