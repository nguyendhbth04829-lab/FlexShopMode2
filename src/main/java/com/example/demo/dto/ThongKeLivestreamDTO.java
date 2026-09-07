package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO thống kê hoạt động Livestream của Seller (US-61)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeLivestreamDTO {

    @Builder.Default
    private Long tongSoPhienLive = 0L;

    @Builder.Default
    private Long soPhienDangLive = 0L;

    @Builder.Default
    private Long soPhienSapDienRa = 0L;

    @Builder.Default
    private Long soPhienDaKetThuc = 0L;

    @Builder.Default
    private Long tongLuotXem = 0L;

    @Builder.Default
    private Long tongLuotThich = 0L;

    @Builder.Default
    private Long tongBinhLuan = 0L;

    @Builder.Default
    private Long tongDonHangLive = 0L;

    @Builder.Default
    private BigDecimal tongDoanhThuLive = BigDecimal.ZERO;
}
