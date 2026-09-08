package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - DTO truyền dữ liệu yêu cầu thanh toán
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThanhToanRequestDTO {

    private Long maDonHangTong;

    private String phuongThuc; // COD hoặc MOCK_ONLINE

    private String maNganHangMock; // VCB, MB, TECHCOMBANK, FLEXPAY_WALLET

    private String soTheMock;

    private String tenChuTheMock;

    private String hanDungMock;
}
