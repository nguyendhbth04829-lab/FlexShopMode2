package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO chi tiết kết quả phân bổ tài chính cho từng Shop (US-52 - VOUCHER)
 * Phản ánh chính xác nguồn tiền tài trợ của từng đơn hàng shop trong đơn hàng tổng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KetQuaPhanBoShopDTO {

    private Long maDonHangShop;
    private Long maGianHang;
    private String tenGianHang;

    /**
     * Tiền hàng gốc của shop trước khi giảm giá
     */
    private BigDecimal tienHangGoc = BigDecimal.ZERO;

    /**
     * Phí vận chuyển của shop
     */
    private BigDecimal phiVanChuyen = BigDecimal.ZERO;

    /**
     * Mã voucher shop áp dụng
     */
    private String codeVoucherShop;

    /**
     * Tiền voucher shop giảm (100% GIAN HÀNG chịu)
     */
    private BigDecimal giamGiaVoucherShop = BigDecimal.ZERO;

    /**
     * Tiền voucher Sàn phân bổ cho shop này (100% SÀN FLEXSHOP tài trợ bù)
     */
    private BigDecimal giamGiaVoucherSan = BigDecimal.ZERO;

    /**
     * Tiền hàng còn lại sau khi trừ voucher shop
     */
    private BigDecimal tienHangSauVoucherShop = BigDecimal.ZERO;

    /**
     * Tổng số tiền Shop thực tế nhận được:
     * tongTienShopNhan = tienHangGoc - giamGiaVoucherShop
     * (Lưu ý: Voucher sàn không trừ vào tiền shop nhận vì Sàn sẽ thanh toán bù cho Shop)
     */
    private BigDecimal tongTienShopNhan = BigDecimal.ZERO;

    /**
     * Thông báo trạng thái áp dụng cho Shop
     */
    private String ghiChu;
}
