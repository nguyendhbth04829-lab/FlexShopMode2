package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "danh_muc")
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_danh_muc")
    private Long maDanhMuc;

    @Column(name = "ten_danh_muc", nullable = false, length = 100)
    private String tenDanhMuc;

    @Column(name = "duong_dan_slug", nullable = false, length = 120, unique = true)
    private String duongDanSlug;

    @Column(name = "link_icon", length = 500)
    private String linkIcon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_muc_cha")
    private DanhMuc danhMucCha;

    @OneToMany(mappedBy = "danhMucCha", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DanhMuc> danhMucCon = new ArrayList<>();

    @Column(name = "cap_do")
    private Integer capDo = 1;

    @Column(name = "thu_tu_hien_thi")
    private Integer thuTuHienThi = 0;

    @Column(name = "dang_hoat_dong")
    private Boolean dangHoatDong = true;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    // Getters and Setters
    public Long getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(Long maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    public String getDuongDanSlug() { return duongDanSlug; }
    public void setDuongDanSlug(String duongDanSlug) { this.duongDanSlug = duongDanSlug; }

    public String getLinkIcon() { return linkIcon; }
    public void setLinkIcon(String linkIcon) { this.linkIcon = linkIcon; }

    public DanhMuc getDanhMucCha() { return danhMucCha; }
    public void setDanhMucCha(DanhMuc danhMucCha) { this.danhMucCha = danhMucCha; }

    public List<DanhMuc> getDanhMucCon() { return danhMucCon; }
    public void setDanhMucCon(List<DanhMuc> danhMucCon) { this.danhMucCon = danhMucCon; }

    public Integer getCapDo() { return capDo; }
    public void setCapDo(Integer capDo) { this.capDo = capDo; }

    public Integer getThuTuHienThi() { return thuTuHienThi; }
    public void setThuTuHienThi(Integer thuTuHienThi) { this.thuTuHienThi = thuTuHienThi; }

    public Boolean getDangHoatDong() { return dangHoatDong; }
    public void setDangHoatDong(Boolean dangHoatDong) { this.dangHoatDong = dangHoatDong; }

    public Boolean getDaXoa() { return daXoa; }
    public void setDaXoa(Boolean daXoa) { this.daXoa = daXoa; }
}
