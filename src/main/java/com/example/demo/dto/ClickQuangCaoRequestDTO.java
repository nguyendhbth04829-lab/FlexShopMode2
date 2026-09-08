package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - DTO Xử lý Lượt Nhấp Quảng Cáo CPC
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickQuangCaoRequestDTO {

    private Long maTuKhoa;
    private Long maSanPham;
    private String tuKhoaTimKiem;
}
