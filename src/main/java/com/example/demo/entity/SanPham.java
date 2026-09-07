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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_thuong_hieu")
    private ThuongHieu thuongHieu;

    @Column(name = "ten_san_pham", nullable = false, length = 255)
    private String tenSanPham;

    @Column(name = "duong_dan_slug", nullable = false, length = 280, unique = true)
    private String duongDanSlug;

    @Column(name = "mo_ta_ngan", length = 500)
    private String moTaNgan;

    @Column(name = "mo_ta_chi_tiet", columnDefinition = "NVARCHAR(MAX)")
    private String moTaChiTiet;

    @Column(name = "gia_co_ban", nullable = false)
    private BigDecimal giaCoBan;

    @Column(name = "bi_khoa")
    private Boolean biKhoa = false;

    @Column(name = "ly_do_khoa")
    private String lyDoKhoa;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "HOAT_DONG";

    @Column(name = "can_nang_gram", nullable = false)
    private Integer canNangGram = 200;

    @Column(name = "chieu_dai_cm", nullable = false)
    private Integer chieuDaiCm = 10;

    @Column(name = "chieu_rong_cm", nullable = false)
    private Integer chieuRongCm = 10;

    @Column(name = "chieu_cao_cm", nullable = false)
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

    // Getters and Setters omitted for brevity but assumed present
    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }

    public Long getMaGianHang() { return maGianHang; }
    public void setMaGianHang(Long maGianHang) { this.maGianHang = maGianHang; }

    public DanhMuc getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }

    public ThuongHieu getThuongHieu() { return thuongHieu; }
    public void setThuongHieu(ThuongHieu thuongHieu) { this.thuongHieu = thuongHieu; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getDuongDanSlug() { return duongDanSlug; }
    public void setDuongDanSlug(String duongDanSlug) { this.duongDanSlug = duongDanSlug; }

    public String getMoTaNgan() { return moTaNgan; }
    public void setMoTaNgan(String moTaNgan) { this.moTaNgan = moTaNgan; }

    public String getMoTaChiTiet() { return moTaChiTiet; }
    public void setMoTaChiTiet(String moTaChiTiet) { this.moTaChiTiet = moTaChiTiet; }

    public BigDecimal getGiaCoBan() { return giaCoBan; }
    public void setGiaCoBan(BigDecimal giaCoBan) { this.giaCoBan = giaCoBan; }

    public Integer getCanNangGram() { return canNangGram; }
    public void setCanNangGram(Integer canNangGram) { this.canNangGram = canNangGram; }

    public Integer getChieuDaiCm() { return chieuDaiCm; }
    public void setChieuDaiCm(Integer chieuDaiCm) { this.chieuDaiCm = chieuDaiCm; }

    public Integer getChieuRongCm() { return chieuRongCm; }
    public void setChieuRongCm(Integer chieuRongCm) { this.chieuRongCm = chieuRongCm; }

    public Integer getChieuCaoCm() { return chieuCaoCm; }
    public void setChieuCaoCm(Integer chieuCaoCm) { this.chieuCaoCm = chieuCaoCm; }

    public Boolean getDaXoa() { return daXoa; }
    public void setDaXoa(Boolean daXoa) { this.daXoa = daXoa; }
}
