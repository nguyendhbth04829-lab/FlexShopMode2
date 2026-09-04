package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeKhieuNaiDTO {
    private long tongSoPhieu;
    private long soPhieuMoMoi;
    private long soPhieuDangXuLy;
    private long soPhieuDaHoanTien;
    private long soPhieuTuChoi;
    private long soPhieuDaHuy;
    private BigDecimal tongTienDaHoan;
    private BigDecimal tongTienDangKhieuNai;
}
