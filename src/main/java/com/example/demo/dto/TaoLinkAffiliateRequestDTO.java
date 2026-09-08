package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - DTO Tạo Link Tiếp Thị Liên Kết Mới
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaoLinkAffiliateRequestDTO {

    private Long maKoc;
    private Long maSanPham;
    private BigDecimal tyLeHoaHongPhanTram = new BigDecimal("10.00");

    public void validate() {
        if (maKoc == null || maKoc <= 0) {
            throw new IllegalArgumentException("Mã KOC / Đối tác tiếp thị không hợp lệ!");
        }
        if (maSanPham == null || maSanPham <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần tạo liên kết tiếp thị!");
        }
        if (tyLeHoaHongPhanTram == null || tyLeHoaHongPhanTram.compareTo(BigDecimal.ZERO) <= 0 
                || tyLeHoaHongPhanTram.compareTo(new BigDecimal("50.00")) > 0) {
            throw new IllegalArgumentException("Tỷ lệ hoa hồng phải từ 1.00% đến tối đa 50.00%!");
        }
    }
}
