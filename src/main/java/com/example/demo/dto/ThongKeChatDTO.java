package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeChatDTO {

    private long tongHoiThoai;
    private long tongTinChuaDoc;
    private long tongDeXuatTraGia;
    private long deXuatChoDuyet;
    private long deXuatDaDongY;
    private long deXuatTuChoi;
    private double tyLeDongYPhanTram;
}
