package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - DTO Thanh toán trả nợ cho từng kỳ SPayLater
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThanhToanKyTraSauRequestDTO {

    private Long maKy;
    private Long maHopDong;
    private BigDecimal soTienThanhToan;
    private String phuongThuc = "MOCK_QR"; // MOCK_QR, THE_NOI_DIA, VI_FLEXPAY

    public void validate() {
        if (maKy == null) {
            throw new IllegalArgumentException("Mã kỳ thanh toán không được để trống.");
        }
        if (maHopDong == null) {
            throw new IllegalArgumentException("Mã hợp đồng không được để trống.");
        }
        if (soTienThanhToan == null || soTienThanhToan.compareTo(new BigDecimal("1000.00")) < 0) {
            throw new IllegalArgumentException("Số tiền thanh toán tối thiểu là 1,000 VNĐ.");
        }
        if (phuongThuc == null || phuongThuc.trim().isEmpty()) {
            throw new IllegalArgumentException("Phương thức thanh toán nợ không được để trống.");
        }
    }
}
