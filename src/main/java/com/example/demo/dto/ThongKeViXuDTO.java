package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;

/**
 * DTO Thống kê số dư và hoạt động Ví Xu (US-55 - Coin Reward System)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeViXuDTO {

    private Long soXuHienTai;
    private Long giaTriQuyDoiVnd;
    private Long tongXuDaTichLuy;
    private Long tongXuDaTieuTrongThang;
    private Long tongXuTichTrongThang;
    private boolean daDiemDanhHomNay;
    private Long xuNhanKhiDiemDanh;

    public String getDinhDangSoXuHienTai() {
        if (soXuHienTai == null) return "0";
        DecimalFormat df = new DecimalFormat("###,###,###");
        return df.format(soXuHienTai);
    }

    public String getDinhDangGiaTriQuyDoi() {
        if (giaTriQuyDoiVnd == null) return "0 đ";
        DecimalFormat df = new DecimalFormat("###,###,### đ");
        return df.format(giaTriQuyDoiVnd);
    }

    public String getDinhDangTongTichLuy() {
        if (tongXuDaTichLuy == null) return "0";
        DecimalFormat df = new DecimalFormat("###,###,###");
        return df.format(tongXuDaTichLuy);
    }

    public String getDinhDangTongTieuThang() {
        if (tongXuDaTieuTrongThang == null) return "0";
        DecimalFormat df = new DecimalFormat("###,###,###");
        return df.format(tongXuDaTieuTrongThang);
    }
}
