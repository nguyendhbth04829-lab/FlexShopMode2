package com.example.demo.dto;

import com.example.demo.entity.ChiTietDonHang;
import com.example.demo.entity.DanhGiaSanPham;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangDanhGiaItemDTO {

    private ChiTietDonHang chiTietDonHang;

    private Long maSanPham;

    private String linkAnhSanPham;

    private boolean daDanhGia;

    private DanhGiaSanPham danhGia;
}
