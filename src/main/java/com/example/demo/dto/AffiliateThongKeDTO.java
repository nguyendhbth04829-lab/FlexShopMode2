package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - DTO Thống Kê Hiệu Suất Tiếp Thị KOC
 * =====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateThongKeDTO {

    @Builder.Default
    private int tongSoLink = 0;

    @Builder.Default
    private int tongDonHang = 0;

    @Builder.Default
    private BigDecimal tongHoaHongChoDoiSoat = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal tongHoaHongDaDuyet = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal tongDoanhThuDonHang = BigDecimal.ZERO;
}
