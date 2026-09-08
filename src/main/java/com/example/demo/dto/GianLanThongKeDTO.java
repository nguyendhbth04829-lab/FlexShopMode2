package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & AN NINH SÀN (DEV 5 - MINH)
 * USER STORY: US-68 - DTO Thống Kê Giám Sát Gian Lận & An Ninh
 * =====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GianLanThongKeDTO {

    @Builder.Default
    private int tongCanhBao = 0;

    @Builder.Default
    private int soCaChoDieuTra = 0;

    @Builder.Default
    private int soCaNguyHiemRuiRoCao = 0;

    @Builder.Default
    private int soCaDaXuLy = 0;

    @Builder.Default
    private int soCaDaBoQua = 0;

    @Builder.Default
    private double diemRuiRoTrungBinh = 0.0;
}
