package com.example.demo.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO phản hồi thống kê và tổng quan hồ sơ tài khoản (US-04)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeHoSoResponse {

    private NguoiDungResponse thongTinNguoiDung;

    @Builder.Default
    private long tongSoDiaChi = 0;

    @Builder.Default
    private int gioiHanDiaChi = 20;

    private long soNgayThamGia;

    private LocalDateTime ngayTao;

    private LocalDateTime ngayCapNhat;

    @Builder.Default
    private String trangThaiBaoMat = "Bảo mật cao (BCrypt salt 12)";
}
