package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKePhanQuyetDTO {

    private long tongSoLenh;
    private BigDecimal tongTienHoanKhach;
    private BigDecimal tongTienBoiThuongShop;
    private long soLenhNguoiBanChiu;
    private long soLenhSanChiu;
    private long soLenhVanChuyenChiu;
    private long soKhieuNaiBacBo;
}
