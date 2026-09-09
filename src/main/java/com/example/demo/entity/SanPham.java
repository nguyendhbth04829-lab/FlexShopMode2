package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢN LÝ SẢN PHẨM & MARKETING NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-64 - Thực thể Sản Phẩm (san_pham)
 * =====================================================================
 */
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

    @Column(name = "ma_gian_hang", nullable = false)
    private Long maGianHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_muc", nullable = false)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang", nullable = false)
    @JoinColumn(name = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_muc", referencedColumnName = "ma_danh_muc", nullable = false)
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_thuong_hieu")
    private ThuongHieu thuongHieu;

    @Column(name = "ten_san_pham", nullable = false, length = 255)
    private String tenSanPham;

    @Column(name = "duong_dan_slug", nullable = false, unique = true, length = 280)
    @Column(name = "duong_dan_slug", nullable = false, length = 280)
    @Column(name = "duong_dan_slug", nullable = false, length = 280, unique = true)
    private String duongDanSlug;

    @Column(name = "mo_ta_ngan", length = 500)
    private String moTaNgan;

    @Column(name = "mo_ta_chi_tiet", columnDefinition = "NVARCHAR(MAX)")
    private String moTaChiTiet;

    @Column(name = "gia_co_ban", nullable = false, precision = 18, scale = 2)
    @Column(name = "gia_co_ban", nullable = false)
    private BigDecimal giaCoBan;

    @Column(name = "bi_khoa")
    private Boolean biKhoa = false;

    @Column(name = "ly_do_khoa")
    private String lyDoKhoa;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "HOAT_DONG";

    @Column(name = "can_nang_gram")
    @Column(name = "can_nang_gram", nullable = false)
    private Integer canNangGram = 200;

    @Column(name = "chieu_dai_cm")
    @Column(name = "chieu_dai_cm", nullable = false)
    private Integer chieuDaiCm = 10;

    @Column(name = "chieu_rong_cm")
    @Column(name = "chieu_rong_cm", nullable = false)
    private Integer chieuRongCm = 10;

    @Column(name = "chieu_cao_cm")
    @Column(name = "chieu_cao_cm", nullable = false)
    private Integer chieuCaoCm = 10;

    @Column(name = "danh_gia_tb", precision = 3, scale = 2)
    private BigDecimal danhGiaTb = new BigDecimal("4.8");
    private BigDecimal danhGiaTb = BigDecimal.ZERO;

    @Column(name = "tong_da_ban")
    private Integer tongDaBan = 0;

    @Column(name = "tong_luot_xem")
    private Integer tongLuotXem = 0;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    // Getters and Setters omitted for brevity but assumed present
    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }

    public Long getMaGianHang() { return maGianHang; }
    public void setMaGianHang(Long maGianHang) { this.maGianHang = maGianHang; }
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    public DanhMuc getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }
    @Transient
    public String getHienThiGia() {
        if (giaCoBan == null) return "0 đ";
        return String.format("%,.0f đ", giaCoBan);
    private String linkAnh;

    public ThuongHieu getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(ThuongHieu thuongHieu) { this.thuongHieu = thuongHieu; }
    public String getLinkAnh() {
        if (linkAnh != null && !linkAnh.isBlank()) {
            return linkAnh;
        }
        return "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=200";
    }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public void setLinkAnh(String linkAnh) {
        this.linkAnh = linkAnh;
    }

    @Transient
    private BigDecimal giaNiemYet;
    public String getDuongDanSlug() { return duongDanSlug; }
    public void setDuongDanSlug(String duongDanSlug) { this.duongDanSlug = duongDanSlug; }

    public BigDecimal getGiaNiemYet() {
        return giaNiemYet != null ? giaNiemYet : (giaCoBan != null ? giaCoBan : BigDecimal.ZERO);
    }
    public String getMoTaNgan() { return moTaNgan; }
    public void setMoTaNgan(String moTaNgan) { this.moTaNgan = moTaNgan; }

    public void setGiaNiemYet(BigDecimal giaNiemYet) {
        this.giaNiemYet = giaNiemYet;
    }
    public String getMoTaChiTiet() { return moTaChiTiet; }
    public void setMoTaChiTiet(String moTaChiTiet) { this.moTaChiTiet = moTaChiTiet; }

    public BigDecimal getGiaCoBan() { return giaCoBan; }
    public void setGiaCoBan(BigDecimal giaCoBan) { this.giaCoBan = giaCoBan; }

    public Boolean getDaXoa() { return daXoa; }
    public void setDaXoa(Boolean daXoa) { this.daXoa = daXoa; }
}
