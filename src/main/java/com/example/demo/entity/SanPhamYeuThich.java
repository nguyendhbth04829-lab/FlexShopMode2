package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể ánh xạ bảng san_pham_yeu_thich (Wishlist của khách hàng)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "san_pham_yeu_thich")
@IdClass(SanPhamYeuThichId.class)
public class SanPhamYeuThich {

    @Id
    @Column(name = "ma_nguoi_dung")
    private Long maNguoiDung;

    @Id
    @Column(name = "ma_san_pham")
    private Long maSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", insertable = false, updatable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", insertable = false, updatable = false)
    private SanPham sanPham;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    public SanPhamYeuThich(Long maNguoiDung, Long maSanPham) {
        this.maNguoiDung = maNguoiDung;
        this.maSanPham = maSanPham;
        this.ngayTao = LocalDateTime.now();
    }
}
