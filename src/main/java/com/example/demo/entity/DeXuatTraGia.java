package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "de_xuat_tra_gia")
public class DeXuatTraGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_de_xuat")
    private Long maDeXuat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cuoc_tro_chuyen", referencedColumnName = "ma_cuoc_tro_chuyen", nullable = false)
    private CuocTroChuyen cuocTroChuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khach_hang", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", referencedColumnName = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bien_the", referencedColumnName = "ma_bien_the")
    private BienTheSanPham bienThe;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong = 1;

    @Column(name = "gia_goc", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaGoc;

    @Column(name = "gia_de_xuat", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaDeXuat;

    @Column(name = "gia_shop_phan_hoi", precision = 18, scale = 2)
    private BigDecimal giaShopPhanHoi;

    @Column(name = "trang_thai", nullable = false, length = 30)
    private String trangThai = "CHO_DUYET"; // 'CHO_DUYET', 'DONG_Y', 'TU_CHOI', 'PHAN_HOI_LAI', 'HET_HAN'

    @Column(name = "ghi_chu_khach", length = 500)
    private String ghiChuKhach;

    @Column(name = "phan_hoi_shop", length = 500)
    private String phanHoiShop;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "ngay_het_han")
    private LocalDateTime ngayHetHan;
}
