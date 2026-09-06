package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO Thống Kê KPI Chương Trình Combo & Mua Kèm Deal Sốc (US-54 - PROMOTION)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeComboDTO {

    private long tongSoCombo;
    private long soComboDangChay;
    private long soComboSapChay;
    private long tongSanPhamThamGia;
    private long tongLuotBanUuDai;
    private BigDecimal tongDoanhThuCombo;
}