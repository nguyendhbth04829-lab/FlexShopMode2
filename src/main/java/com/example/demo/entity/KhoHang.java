package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kho_hang")
public class KhoHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_kho")
    private Long maKho;

    @Column(name = "ma_gian_hang", nullable = false)
    private Long maGianHang;

    @Column(name = "ten_kho", nullable = false, length = 150)
    private String tenKho;

    @Column(name = "dia_chi", nullable = false, length = 255)
    private String diaChi;

    @Column(name = "tinh_thanh", nullable = false, length = 100)
    private String tinhThanh;

    @Column(name = "sdt_lien_he", nullable = false, length = 20)
    private String sdtLienHe;

    @Column(name = "la_kho_chinh")
    private Boolean laKhoChinh = false;

    @Column(name = "dang_hoat_dong")
    private Boolean dangHoatDong = true;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    // Getters and Setters
    public Long getMaKho() { return maKho; }
    public void setMaKho(Long maKho) { this.maKho = maKho; }

    public Long getMaGianHang() { return maGianHang; }
    public void setMaGianHang(Long maGianHang) { this.maGianHang = maGianHang; }

    public String getTenKho() { return tenKho; }
    public void setTenKho(String tenKho) { this.tenKho = tenKho; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getTinhThanh() { return tinhThanh; }
    public void setTinhThanh(String tinhThanh) { this.tinhThanh = tinhThanh; }

    public String getSdtLienHe() { return sdtLienHe; }
    public void setSdtLienHe(String sdtLienHe) { this.sdtLienHe = sdtLienHe; }

    public Boolean getLaKhoChinh() { return laKhoChinh; }
    public void setLaKhoChinh(Boolean laKhoChinh) { this.laKhoChinh = laKhoChinh; }

    public Boolean getDangHoatDong() { return dangHoatDong; }
    public void setDangHoatDong(Boolean dangHoatDong) { this.dangHoatDong = dangHoatDong; }

    public Boolean getDaXoa() { return daXoa; }
    public void setDaXoa(Boolean daXoa) { this.daXoa = daXoa; }
}
