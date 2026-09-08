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
 * USER STORY: US-64 - DTO Thống Kê Hiệu Quả Quảng Cáo Shopee Ads
 * =====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdsThongKeDTO {

    private Long maGianHang;
    private String tenGianHang;
    private BigDecimal soDuViKhaDung;

    private long tongChienDich;
    private long chienDichDangChay;
    private long chienDichTamDung;
    private long chienDichHetNganSach;

    private BigDecimal tongNganSachNgay;
    private BigDecimal tongChiPhiDaDung;
    private int tongLuotClick;
    private BigDecimal cpcTrungBinh;
}
