package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & RÚT TIỀN (DEV 5 - MINH)
 * USER STORY: US-44 - DTO Truyền Dữ Liệu Yêu Cầu Rút Tiền Người Bán
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutTienNguoiBanRequestDTO {

    private Long maGianHang;

    private BigDecimal soTienRut;

    private String tenNganHang;

    private String soTaiKhoan;

    private String tenChuTaiKhoan;

    private String ghiChu;
}
