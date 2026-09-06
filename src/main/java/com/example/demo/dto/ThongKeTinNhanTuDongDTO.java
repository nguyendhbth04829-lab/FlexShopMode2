package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Thống kê số liệu hệ thống Tin nhắn tự động trả lời (US-60)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeTinNhanTuDongDTO {

    private long tongSoCauHinh;
    private long soCauHinhDangBat;
    private long tongSoTinTuDongDaGui;
    private long tongSoVoucherDaTang;
}
