package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - DTO Đăng ký / Kích hoạt Ví Trả Sau SPayLater
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DangKyTraSauRequestDTO {

    private Long maNguoiDung;
    private String hoVaTen;
    private String soCccd;
    private String soDienThoai;
    private BigDecimal thuNhapThang = new BigDecimal("10000000.00");
    private Boolean dongYDieuKhoan = true;

    public void validate() {
        if (maNguoiDung == null) {
            throw new IllegalArgumentException("Mã người dùng không được để trống.");
        }
        if (hoVaTen == null || hoVaTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập họ và tên chủ tài khoản.");
        }
        if (soCccd == null || !soCccd.trim().matches("^[0-9]{9,12}$")) {
            throw new IllegalArgumentException("Số CCCD/CMND phải gồm 9 đến 12 chữ số hợp lệ.");
        }
        if (soDienThoai == null || !soDienThoai.trim().matches("^(0|\\+84)[35789][0-9]{8}$")) {
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng di động Việt Nam.");
        }
        if (thuNhapThang == null || thuNhapThang.compareTo(new BigDecimal("3000000.00")) < 0) {
            throw new IllegalArgumentException("Thu nhập tối thiểu để mở ví là 3,000,000 VNĐ.");
        }
        if (dongYDieuKhoan == null || !dongYDieuKhoan) {
            throw new IllegalArgumentException("Bạn phải đồng ý với Điều khoản và Hợp đồng dịch vụ tín dụng SPayLater.");
        }
    }
}
