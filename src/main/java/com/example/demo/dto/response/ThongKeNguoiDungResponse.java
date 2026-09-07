package com.example.demo.dto.response;

import lombok.*;

/**
 * DTO số liệu thống kê người dùng phục vụ Admin KPI - US-06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeNguoiDungResponse {

    @Builder.Default
    private long tongSoNguoiDung = 0;

    @Builder.Default
    private long soKhachHang = 0;

    @Builder.Default
    private long soNguoiBan = 0;

    @Builder.Default
    private long soTaiXe = 0;

    @Builder.Default
    private long soCskh = 0;

    @Builder.Default
    private long soAdmin = 0;

    @Builder.Default
    private long soBiKhoa = 0;

    @Builder.Default
    private long soHoatDong = 0;

    @Builder.Default
    private long soNguoiDungMoiHomNay = 0;
}