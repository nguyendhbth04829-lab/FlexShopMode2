package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO yêu cầu áp dụng voucher đồng thời 3 tầng (US-52 - VOUCHER):
 * - Tầng 1: Voucher Shop (theo từng gian hàng trong đơn hàng)
 * - Tầng 2: Voucher Sàn FlexShop
 * - Tầng 3: Mã Freeship Sàn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApDungVoucherRequestDTO {

    /**
     * Mã đơn hàng tổng cần áp dụng voucher (nếu đang ở trang đơn hàng)
     */
    private Long maDonHangTong;

    /**
     * Mã code Freeship Sàn (Tầng 3)
     */
    private String maFreeshipSan;

    /**
     * Mã code Voucher Sàn FlexShop (Tầng 2)
     */
    private String maVoucherSan;

    /**
     * Bản đồ mã Voucher Shop (Tầng 1):
     * Key: maGianHang (hoặc maDonHangShop)
     * Value: maCodeVoucher của Shop đó
     */
    private Map<Long, String> maVoucherShopMap = new HashMap<>();
}
