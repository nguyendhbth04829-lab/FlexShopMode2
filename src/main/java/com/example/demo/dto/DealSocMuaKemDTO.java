package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO dữ liệu hiển thị Phụ kiện B Mua Kèm Deal Sốc (Giảm 50%) cho Khách Hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealSocMuaKemDTO {

    private Long maCombo;
    private String tenCombo;
    private Long maSanPhamCombo;
    private Long maBienThe;
    private String tenSanPham;
    private String tenBienThe;
    private String linkAnh;
    private BigDecimal giaGoc;
    private BigDecimal giaUuDai;
    private BigDecimal phanTramGiam;
    private BigDecimal tietKiem;
    private Integer gioiHanMuaKemMoiDon;
    private Integer conLai;
    private boolean isChayHang;
}