package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - DTO Tạo Chiến Dịch Quảng Cáo Mới
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaoChienDichAdsRequestDTO {

    private Long maGianHang;
    private Long maSanPham;
    private String tenChienDich;
    private BigDecimal nganSachNgay = new BigDecimal("100000.00");
    private String danhSachTuKhoaStr;
    private BigDecimal giaThauMacDinh = new BigDecimal("1000.00");

    public void validate() {
        if (maGianHang == null) {
            throw new IllegalArgumentException("Mã gian hàng không được để trống.");
        }
        if (maSanPham == null) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần quảng cáo.");
        }
        if (tenChienDich == null || tenChienDich.trim().length() < 3) {
            throw new IllegalArgumentException("Tên chiến dịch quảng cáo phải có ít nhất 3 ký tự.");
        }
        if (nganSachNgay == null || nganSachNgay.compareTo(new BigDecimal("50000.00")) < 0) {
            throw new IllegalArgumentException("Ngân sách tối thiểu mỗi ngày là 50,000 VNĐ.");
        }
        if (giaThauMacDinh == null || giaThauMacDinh.compareTo(new BigDecimal("500.00")) < 0) {
            throw new IllegalArgumentException("Giá thầu mỗi lượt click (CPC) tối thiểu là 500 VNĐ.");
        }
        if (giaThauMacDinh.compareTo(new BigDecimal("50000.00")) > 0) {
            throw new IllegalArgumentException("Giá thầu mỗi lượt click (CPC) tối đa là 50,000 VNĐ.");
        }
    }
}
