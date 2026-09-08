package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - DTO Kết Quả Tìm Kiếm Sản Phẩm Kèm Ads Tài Trợ
 * =====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KetQuaTimKiemAdsDTO {

    private Long maSanPham;
    private String tenSanPham;
    private BigDecimal giaCoBan;
    private String tenGianHang;
    private Long maGianHang;

    private Long maChienDich;
    private Long maTuKhoa;
    private String tuKhoaKhop;
    private BigDecimal giaThauCpc;
    private boolean laQuangCao; // true = Sản phẩm Tài Trợ (Sponsored), false = Kết quả tự nhiên

    public String getHienThiGia() {
        if (giaCoBan == null) return "0 đ";
        return String.format("%,.0f đ", giaCoBan);
    }

    public String getHienThiCpc() {
        if (giaThauCpc == null) return "0 đ";
        return String.format("%,.0f đ", giaThauCpc);
    }
}
