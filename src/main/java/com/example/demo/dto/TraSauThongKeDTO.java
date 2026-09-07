package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - DTO Tổng hợp Thống Kê Tài Chính Trả Sau SPayLater
 * =====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraSauThongKeDTO {

    private Long maTkTraSau;
    private Long maNguoiDung;
    private String tenKhachHang;

    private BigDecimal tongHanMuc;
    private BigDecimal hanMucConLai;
    private BigDecimal hanMucDaDung;
    private double tiLeSuDungPhanTram;

    private Integer diemTinDung;
    private String trangThaiTaiKhoan;
    private String badgeClassTrangThai;

    private BigDecimal tongDuNoHienTai;
    private long tongSoHopDong;
    private long soHopDongDangTra;
    private long soHopDongDaTatToan;

    private long soKyCanTraThangNay;
    private BigDecimal tongTienCanTraThangNay;
    private long soKyQuaHan;
    private BigDecimal tongTienQuaHan;

    private boolean duDieuKienVay;
}
