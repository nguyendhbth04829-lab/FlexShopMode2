package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO Thống kê tổng quan chương trình Flash Sale (US-53 - PROMOTION)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeFlashSaleDTO {

    private long tongKhungGio;
    private long tongKhungGioDangDienRa;
    private long tongKhungGioSapDienRa;
    private long tongKhungGioDaKetThuc;
    private long tongSanPhamThamGia;
    private long tongSuatDaBan;
    private BigDecimal tongDoanhThuFlashSale;
}
