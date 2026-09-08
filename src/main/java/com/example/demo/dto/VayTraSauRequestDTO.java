package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - DTO Khách hàng chọn gói vay trả sau cho Đơn hàng tổng
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VayTraSauRequestDTO {

    private Long maDonHangTong;
    private Long maNguoiDung;
    private Integer soKyTraGop = 3;
    private String ghiChu;

    public void validate() {
        if (maDonHangTong == null) {
            throw new IllegalArgumentException("Mã đơn hàng tổng không được để trống.");
        }
        if (maNguoiDung == null) {
            throw new IllegalArgumentException("Mã người dùng không được để trống.");
        }
        if (soKyTraGop == null || soKyTraGop < 1 || soKyTraGop > 12) {
            throw new IllegalArgumentException("Số kỳ trả góp không hợp lệ (chỉ hỗ trợ từ 1 đến 12 kỳ).");
        }
    }
}
