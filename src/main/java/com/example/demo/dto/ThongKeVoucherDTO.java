package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO Thống kê kho voucher và lịch sử tiết kiệm của khách hàng (US-52 - VOUCHER)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeVoucherDTO {

    /**
     * Tổng số voucher còn hiệu lực và còn lượt dùng trong hệ thống
     */
    private long tongVoucherKhaDung;

    /**
     * Số voucher Sàn FlexShop
     */
    private long tongVoucherSan;

    /**
     * Số mã Freeship Sàn
     */
    private long tongVoucherFreeship;

    /**
     * Số voucher các Gian hàng
     */
    private long tongVoucherShop;

    /**
     * Tổng số tiền khách hàng đã tiết kiệm từ trước tới nay thông qua voucher
     */
    private BigDecimal tongTienTietKiemDuoc;

    /**
     * Tổng số lần khách hàng đã sử dụng voucher thành công
     */
    private long tongSoLanSuDung;
}
