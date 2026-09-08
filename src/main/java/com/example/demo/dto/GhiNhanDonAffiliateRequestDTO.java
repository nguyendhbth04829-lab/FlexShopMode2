package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - DTO Ghi Nhận Đơn Hàng Tiếp Thị (Attribution)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GhiNhanDonAffiliateRequestDTO {

    private String maLinkAffiliate;
    private Long maDonHangShop;

    public void validate() {
        if (maLinkAffiliate == null || maLinkAffiliate.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã liên kết tiếp thị (Affiliate Code) không được để trống!");
        }
        if (maDonHangShop == null || maDonHangShop <= 0) {
            throw new IllegalArgumentException("Mã đơn hàng shop không hợp lệ!");
        }
    }
}
