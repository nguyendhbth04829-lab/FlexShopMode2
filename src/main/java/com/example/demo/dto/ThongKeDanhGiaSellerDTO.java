package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Thống kê tổng quan tình hình đánh giá & phản hồi của Gian Hàng (US-50 - Seller Dashboard)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeDanhGiaSellerDTO {

    private long tongDanhGia;
    private double diemDanhGiaTb;
    private long soLuongDaPhanHoi;
    private long soLuongChuaPhanHoi;
    private int tyLePhanHoi; // % tỷ lệ đã phản hồi

    // Phân bổ số sao
    private long soLuong5Sao;
    private long soLuong4Sao;
    private long soLuong3Sao;
    private long soLuong2Sao;
    private long soLuong1Sao;
}
