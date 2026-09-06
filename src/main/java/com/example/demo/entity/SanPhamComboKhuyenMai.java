package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Thực thể gán sản phẩm vào Combo / Mua Kèm Deal Sốc (US-54 - PROMOTION)
 * Ánh xạ bảng san_pham_combo_khuyen_mai trong CSDL FlexShop_V2_Full.
 * Phân tách rõ ràng:
 * - vai_tro = 'SAN_PHAM_CHINH': Sản phẩm điều kiện kích hoạt (Sản phẩm A)
 * - vai_tro = 'MUA_KEM_DEAL_SOC': Phụ kiện B được mua kèm với giá giảm sốc 50%
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "san_pham_combo_khuyen_mai")
public class SanPhamComboKhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_san_pham_combo")
    private Long maSanPhamCombo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_combo", nullable = false)
    @ToString.Exclude
    private ComboKhuyenMai comboKhuyenMai;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_bien_the", nullable = false)
    private BienTheSanPham bienTheSanPham;

    /**
     * Vai trò của sản phẩm trong combo:
     * - SAN_PHAM_CHINH: Sản phẩm chính A
     * - MUA_KEM_DEAL_SOC: Phụ kiện B mua kèm giảm giá 50%
     */
    @Column(name = "vai_tro", nullable = false, length = 30)
    private String vaiTro;

    /**
     * Phần trăm giảm giá ưu đãi khi mua kèm (Ví dụ: 50.00 cho 50%)
     */
    @Column(name = "phan_tram_giam", precision = 5, scale = 2)
    private BigDecimal phanTramGiam = BigDecimal.ZERO;

    /**
     * Giá bán ưu đãi sau khi giảm sốc (Ví dụ: Giá gốc 400k -> Giá ưu đãi 200k)
     */
    @Column(name = "gia_uu_dai", precision = 18, scale = 2)
    private BigDecimal giaUuDai;

    /**
     * Giới hạn số lượng mua kèm phụ kiện này cho mỗi 1 sản phẩm chính trong đơn hàng
     */
    @Column(name = "gioi_han_mua_kem_moi_don")
    private Integer gioiHanMuaKemMoiDon = 1;

    /**
     * Tổng số suất khuyến mãi mua kèm mở bán
     */
    @Column(name = "so_luong_toi_da")
    private Integer soLuongToiDa = 100;

    /**
     * Số lượng suất đã bán được
     */
    @Column(name = "so_luong_da_ban")
    private Integer soLuongDaBan = 0;

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    public boolean isSanPhamChinh() {
        return "SAN_PHAM_CHINH".equalsIgnoreCase(this.vaiTro);
    }

    public boolean isSanPhamMuaKem() {
        return "MUA_KEM_DEAL_SOC".equalsIgnoreCase(this.vaiTro);
    }

    public BigDecimal getGiaGoc() {
        if (this.bienTheSanPham != null && this.bienTheSanPham.getGiaBan() != null) {
            return this.bienTheSanPham.getGiaBan();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTietKiem() {
        if (this.giaUuDai == null) return BigDecimal.ZERO;
        BigDecimal giaGoc = getGiaGoc();
        if (giaGoc.compareTo(this.giaUuDai) > 0) {
            return giaGoc.subtract(this.giaUuDai);
        }
        return BigDecimal.ZERO;
    }

    public int getPhanTramDaBan() {
        if (this.soLuongToiDa == null || this.soLuongToiDa <= 0) return 0;
        int daBan = (this.soLuongDaBan != null) ? this.soLuongDaBan : 0;
        return Math.min(100, (int) Math.round(((double) daBan / this.soLuongToiDa) * 100));
    }

    public boolean isChayHang() {
        int daBan = (this.soLuongDaBan != null) ? this.soLuongDaBan : 0;
        int toiDa = (this.soLuongToiDa != null) ? this.soLuongToiDa : 0;
        return daBan >= toiDa;
    }
}