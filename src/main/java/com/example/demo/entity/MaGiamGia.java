package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể biểu diễn Mã giảm giá / Voucher (US-52 - VOUCHER)
 * Ánh xạ bảng ma_giam_gia trong CSDL FlexShop_V2_Full.
 * - ma_gian_hang IS NULL: Voucher toàn sàn FlexShop hoặc Freeship Sàn.
 * - ma_gian_hang IS NOT NULL: Voucher của riêng gian hàng đó.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ma_giam_gia")
public class MaGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_voucher")
    private Long maVoucher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang")
    private GianHang gianHang;

    @Column(name = "ma_code_voucher", nullable = false, length = 50)
    private String maCodeVoucher;

    @Column(name = "ten_voucher", nullable = false, length = 150)
    private String tenVoucher;

    /**
     * Phân loại voucher:
     * - FREESHIP: Giảm phí vận chuyển (Sàn tài trợ)
     * - GIAM_GIA: Giảm tiền mặt trực tiếp (VND)
     * - PHAN_TRAM: Giảm theo % giá trị đơn hàng
     */
    @Column(name = "loai_voucher", nullable = false, length = 30)
    private String loaiVoucher;

    @Column(name = "gia_tri_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "gia_tri_don_toi_thieu", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTriDonToiThieu;

    @Column(name = "tong_so_luong_phat_hanh", nullable = false)
    private Integer tongSoLuongPhatHanh;

    @Column(name = "gioi_han_moi_nguoi", nullable = false)
    private Integer gioiHanMoiNguoi = 1;

    @Column(name = "so_luong_da_dung")
    private Integer soLuongDaDung = 0;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(name = "dang_hoat_dong")
    private Boolean dangHoatDong = true;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Version
    @Column(name = "phien_ban_lock")
    private Integer phienBanLock = 0;

    /**
     * Kiểm tra xem voucher này có phải voucher Sàn (FlexShop) hay không
     */
    public boolean laVoucherSan() {
        return this.gianHang == null;
    }

    /**
     * Kiểm tra xem voucher này có phải là Freeship hay không
     */
    public boolean laVoucherFreeship() {
        return "FREESHIP".equalsIgnoreCase(this.loaiVoucher);
    }

    /**
     * Kiểm tra xem voucher này có phải voucher của Shop cụ thể không
     */
    public boolean laVoucherShop() {
        return this.gianHang != null;
    }
}
