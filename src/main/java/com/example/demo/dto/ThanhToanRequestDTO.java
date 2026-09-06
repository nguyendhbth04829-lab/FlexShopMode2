package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
