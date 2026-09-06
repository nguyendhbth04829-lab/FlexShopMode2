package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "thuong_hieu")
public class ThuongHieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_thuong_hieu")
    private Long maThuongHieu;

    @Column(name = "ten_thuong_hieu", nullable = false, length = 100, unique = true)
    private String tenThuongHieu;

    @Column(name = "link_logo", length = 500)
    private String linkLogo;

    @Column(name = "dang_hoat_dong")
    private Boolean dangHoatDong = true;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    // Getters and Setters
    public Long getMaThuongHieu() { return maThuongHieu; }
    public void setMaThuongHieu(Long maThuongHieu) { this.maThuongHieu = maThuongHieu; }

    public String getTenThuongHieu() { return tenThuongHieu; }
    public void setTenThuongHieu(String tenThuongHieu) { this.tenThuongHieu = tenThuongHieu; }

    public String getLinkLogo() { return linkLogo; }
    public void setLinkLogo(String linkLogo) { this.linkLogo = linkLogo; }

    public Boolean getDangHoatDong() { return dangHoatDong; }
    public void setDangHoatDong(Boolean dangHoatDong) { this.dangHoatDong = dangHoatDong; }

    public Boolean getDaXoa() { return daXoa; }
    public void setDaXoa(Boolean daXoa) { this.daXoa = daXoa; }
}
