package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_san_pham")
    private Long maSanPham;

    @Column(name = "ma_gian_hang", nullable = false)
    private Long maGianHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_muc", nullable = false)
    private DanhMuc danhMuc;

    @Column(name = "ten_san_pham", nullable = false, length = 255)
    private String tenSanPham;

    @Column(name = "duong_dan_slug", nullable = false, length = 280, unique = true)
    private String duongDanSlug;

    @Column(name = "gia_co_ban", nullable = false)
    private BigDecimal giaCoBan;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    // Getters and setters
    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }

    public Long getMaGianHang() { return maGianHang; }
    public void setMaGianHang(Long maGianHang) { this.maGianHang = maGianHang; }

    public DanhMuc getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public Boolean getDaXoa() { return daXoa; }
    public void setDaXoa(Boolean daXoa) { this.daXoa = daXoa; }
}
