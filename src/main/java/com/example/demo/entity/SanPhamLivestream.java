package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Thực thể Sản phẩm trong Livestream (US-61)
 * Ánh xạ bảng san_pham_livestream trong CSDL FlexShop_V2_Full.
 * Quản lý trạng thái Ghim lên màn hình (laSanPhamDangGhim) và Giá độc quyền live.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "san_pham_livestream")
public class SanPhamLivestream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_sp_live")
    private Long maSpLive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_live", nullable = false)
    private PhongLivestream phongLivestream;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "la_san_pham_dang_ghim")
    @Builder.Default
    private Boolean laSanPhamDangGhim = false;

    @Column(name = "gia_doc_quyen_live", precision = 18, scale = 2)
    private BigDecimal giaDocQuyenLive;

    @Column(name = "thu_tu_hien_thi")
    @Builder.Default
    private Integer thuTuHienThi = 1;

    @Column(name = "so_luong_gioi_han")
    @Builder.Default
    private Integer soLuongGioiHan = 50;

    @Column(name = "so_luong_da_ban")
    @Builder.Default
    private Integer soLuongDaBan = 0;

    /**
     * Tính % giảm giá độc quyền trên live so với giá gốc sản phẩm
     */
    public int getPhanTramGiamGia() {
        if (sanPham == null || sanPham.getGiaCoBan() == null || giaDocQuyenLive == null) {
            return 0;
        }
        BigDecimal giaGoc = sanPham.getGiaCoBan();
        if (giaGoc.compareTo(BigDecimal.ZERO) <= 0 || giaDocQuyenLive.compareTo(giaGoc) >= 0) {
            return 0;
        }
        BigDecimal chenhLech = giaGoc.subtract(giaDocQuyenLive);
        return chenhLech.multiply(BigDecimal.valueOf(100))
                .divide(giaGoc, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * Số suất mua còn lại trong live
     */
    public int getSoLuongConLai() {
        int gioiHan = soLuongGioiHan != null ? soLuongGioiHan : 0;
        int daBan = soLuongDaBan != null ? soLuongDaBan : 0;
        return Math.max(0, gioiHan - daBan);
    }
}
