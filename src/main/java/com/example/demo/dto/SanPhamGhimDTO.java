package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO đại diện cho sản phẩm được ghim / hiển thị trong danh sách livestream (US-61)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamGhimDTO {

    private Long maSpLive;
    private Long maSanPham;
    private String tenSanPham;
    private String linkAnh;
    private BigDecimal giaGoc;
    private BigDecimal giaDocQuyenLive;
    private Integer phanTramGiamGia;
    private Integer soLuongGioiHan;
    private Integer soLuongDaBan;
    private Integer soLuongConLai;
    private Boolean laSanPhamDangGhim;
    private Integer thuTuHienThi;
}
