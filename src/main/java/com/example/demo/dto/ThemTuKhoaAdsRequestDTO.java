package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - DTO Thêm Từ Khóa Đấu Thầu Vào Chiến Dịch
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemTuKhoaAdsRequestDTO {

    private Long maChienDich;
    private String tuKhoa;
    private BigDecimal giaThauMoiClickCpc = new BigDecimal("1000.00");

    public void validate() {
        if (maChienDich == null) {
            throw new IllegalArgumentException("Mã chiến dịch không được để trống.");
        }
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập từ khóa cần đấu thầu.");
        }
        if (giaThauMoiClickCpc == null || giaThauMoiClickCpc.compareTo(new BigDecimal("500.00")) < 0) {
            throw new IllegalArgumentException("Giá thầu mỗi lượt click (CPC) tối thiểu là 500 VNĐ.");
        }
        if (giaThauMoiClickCpc.compareTo(new BigDecimal("50000.00")) > 0) {
            throw new IllegalArgumentException("Giá thầu mỗi lượt click (CPC) tối đa là 50,000 VNĐ.");
        }
    }
}
