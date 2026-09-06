package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Thực thể biểu diễn Sản phẩm đăng ký Flash Sale trong khung giờ (US-53 - PROMOTION)
 * Ánh xạ bảng san_pham_flash_sale trong CSDL FlexShop_V2_Full.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "san_pham_flash_sale")
public class SanPhamFlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_san_pham_fs")
    private Long maSanPhamFs;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_flash_sale", referencedColumnName = "ma_flash_sale", nullable = false)
    private KhungGioFlashSale khungGioFlashSale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_bien_the", referencedColumnName = "ma_bien_the", nullable = false)
    private BienTheSanPham bienTheSanPham;

    @Column(name = "gia_flash_sale", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaFlashSale;

    @Column(name = "so_luong_gioi_han", nullable = false)
    private Integer soLuongGioiHan;

    @Column(name = "so_luong_da_ban")
    private Integer soLuongDaBan = 0;

    @Column(name = "gioi_han_mua_moi_khach")
    private Integer gioiHanMuaMoiKhach = 1;

    @Version
    @Column(name = "phien_ban_lock")
    private Integer phienBanLock = 0;

    /**
     * Tính tỷ lệ phần trăm giảm giá so với giá niêm yết hiện tại của biến thể
     */
    public int getPhanTramGiamGia() {
        if (bienTheSanPham == null || bienTheSanPham.getGiaBan() == null || bienTheSanPham.getGiaBan().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        BigDecimal giaGoc = bienTheSanPham.getGiaBan();
        if (this.giaFlashSale.compareTo(giaGoc) >= 0) return 0;

        BigDecimal chenhLech = giaGoc.subtract(this.giaFlashSale);
        return chenhLech.multiply(BigDecimal.valueOf(100))
                .divide(giaGoc, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * Tính tỷ lệ % đã bán được trong khung giờ Flash Sale
     */
    public int getPhanTramDaBan() {
        if (this.soLuongGioiHan == null || this.soLuongGioiHan <= 0) return 0;
        int daBan = (this.soLuongDaBan != null) ? this.soLuongDaBan : 0;
        int pct = (int) Math.round(((double) daBan / this.soLuongGioiHan) * 100);
        return Math.min(100, pct);
    }

    /**
     * Kiểm tra xem sản phẩm Flash Sale đã cháy hàng (Sold out) chưa
     */
    public boolean isChayHang() {
        int daBan = (this.soLuongDaBan != null) ? this.soLuongDaBan : 0;
        return this.soLuongGioiHan != null && daBan >= this.soLuongGioiHan;
    }

    /**
     * Số suất Flash Sale còn lại có thể mua
     */
    public int getSoLuongConLai() {
        int daBan = (this.soLuongDaBan != null) ? this.soLuongDaBan : 0;
        int gioiHan = (this.soLuongGioiHan != null) ? this.soLuongGioiHan : 0;
        return Math.max(0, gioiHan - daBan);
    }
}
