package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeThietLapShopResponse {

    private long tongSoLanCapNhat;
    private int phanTramHoanThien;
    private String trangThaiGianHang;
    private Boolean dangMoCua;
    private String gioHoatDongHienTai;
    private String tenGianHang;
    private String hangGianHang;
}
