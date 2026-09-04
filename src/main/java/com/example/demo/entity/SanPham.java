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
@Table(name = "san_pham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_san_pham")
    private Long maSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_muc", referencedColumnName = "ma_danh_muc", nullable = false)
    private DanhMuc danhMuc;

    @Column(name = "ten_san_pham", nullable = false, length = 255)
    private String tenSanPham;

    @Column(name = "duong_dan_slug", nullable = false, unique = true, length = 280)
    private String duongDanSlug;

    @Column(name = "mo_ta_ngan", length = 500)
    private String moTaNgan;

    @Column(name = "mo_ta_chi_tiet", columnDefinition = "NVARCHAR(MAX)")
    private String moTaChiTiet;

    @Column(name = "gia_co_ban", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaCoBan;

    @Column(name = "bi_khoa")
    private Boolean biKhoa = false;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "HOAT_DONG";

    @Column(name = "can_nang_gram")
    private Integer canNangGram = 200;

    @Column(name = "chieu_dai_cm")
    private Integer chieuDaiCm = 10;

    @Column(name = "chieu_rong_cm")
    private Integer chieuRongCm = 10;

    @Column(name = "chieu_cao_cm")
    private Integer chieuCaoCm = 10;

    @Column(name = "danh_gia_tb", precision = 3, scale = 2)
    private BigDecimal danhGiaTb = BigDecimal.ZERO;

    @Column(name = "tong_da_ban")
    private Integer tongDaBan = 0;

    @Column(name = "tong_luot_xem")
    private Integer tongLuotXem = 0;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();
}
