package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO chứa kết quả tính toán thuật toán Voucher lồng nhau (Stackable Vouchers)
 * Phân bổ chính xác nguồn tiền tài trợ giữa Sàn và Gian hàng (US-52 - VOUCHER).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KetQuaApDungVoucherDTO {

    /**
     * Trạng thái hợp lệ của toàn bộ tổ hợp voucher đã áp dụng
     */
    @Builder.Default
    private boolean hopLe = true;

    /**
     * Danh sách lỗi chi tiết nếu có mã không hợp lệ
     */
    @Builder.Default
    private List<String> danhSachLoi = new ArrayList<>();

    /**
     * Danh sách thông báo thành công / ghi chú phân bổ
     */
    @Builder.Default
    private List<String> danhSachThongBao = new ArrayList<>();

    /**
     * Tổng tiền hàng gốc của toàn bộ đơn hàng
     */
    @Builder.Default
    private BigDecimal tongTienHangGoc = BigDecimal.ZERO;

    /**
     * Tổng phí vận chuyển gốc của toàn bộ đơn hàng
     */
    @Builder.Default
    private BigDecimal tongPhiVanChuyenGoc = BigDecimal.ZERO;

    // --- TẦNG 3: FREESHIP SÀN ---
    private String codeFreeshipSan;
    private String tenFreeshipSan;
    @Builder.Default
    private BigDecimal giamGiaFreeshipSan = BigDecimal.ZERO;

    // --- TẦNG 2: VOUCHER SÀN FLEXSHOP ---
    private String codeVoucherSan;
    private String tenVoucherSan;
    @Builder.Default
    private BigDecimal giamGiaVoucherSan = BigDecimal.ZERO;

    // --- TẦNG 1: VOUCHER SHOP ---
    @Builder.Default
    private BigDecimal tongGiamGiaShop = BigDecimal.ZERO;

    // --- TỔNG KẾT NGUỒN TÀI TRỢ & TIẾT KIỆM ---
    /**
     * Tổng số tiền Sàn FlexShop tài trợ = giamGiaVoucherSan + giamGiaFreeshipSan
     */
    @Builder.Default
    private BigDecimal tongTaiTroSan = BigDecimal.ZERO;

    /**
     * Tổng số tiền các Gian hàng tự tài trợ = tongGiamGiaShop
     */
    @Builder.Default
    private BigDecimal tongTaiTroShop = BigDecimal.ZERO;

    /**
     * Tổng số tiền khách hàng được giảm / tiết kiệm = tongTaiTroSan + tongTaiTroShop
     */
    @Builder.Default
    private BigDecimal tongTietKiem = BigDecimal.ZERO;

    /**
     * Phí vận chuyển sau khi trừ mã Freeship sàn
     */
    @Builder.Default
    private BigDecimal phiVanChuyenKhachTra = BigDecimal.ZERO;

    /**
     * Tổng số tiền khách hàng thực tế phải thanh toán cuối cùng
     */
    @Builder.Default
    private BigDecimal tongThanhToanCuoi = BigDecimal.ZERO;

    /**
     * Cờ kiểm định bảo toàn dòng tiền tài chính:
     * tongThanhToanCuoi + tongTaiTroSan + tongTaiTroShop == tongTienHangGoc + tongPhiVanChuyenGoc
     */
    @Builder.Default
    private boolean baoToanTaiChinh = true;

    /**
     * Danh sách phân bổ chi tiết cho từng shop trong đơn
     */
    @Builder.Default
    private List<KetQuaPhanBoShopDTO> danhSachPhanBoShop = new ArrayList<>();
}
