package com.example.demo.dto;

import com.example.demo.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoiChieuBaBenDTO {

    private PhieuKhieuNai phieuKhieuNai;

    private DonHangShop donHangShop;

    private NhiemVuGiaoHang nhiemVuGiaoHang;

    private TaiXeGiaoHang taiXe;

    @Builder.Default
    private List<ChiTietDonHang> danhSachChiTietDonHang = new ArrayList<>();

    @Builder.Default
    private List<LichSuTrangThaiDon> danhSachLichSuTrangThai = new ArrayList<>();

    @Builder.Default
    private List<GhiChuNoiBoKhieuNai> danhSachGhiChuNoiBo = new ArrayList<>();

    @Builder.Default
    private List<BangChungKhieuNai> danhSachBangChungKhachHang = new ArrayList<>();
}
