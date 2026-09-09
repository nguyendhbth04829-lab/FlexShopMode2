package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DanhMucForm {

    private Long maDanhMuc;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không vượt quá 100 ký tự")
    private String tenDanhMuc;

    private Long maDanhMucCha;
    
    private String linkIcon;
    
    private Integer thuTuHienThi;
    
    private Boolean dangHoatDong;

    // Getters and Setters
    public Long getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(Long maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    public Long getMaDanhMucCha() { return maDanhMucCha; }
    public void setMaDanhMucCha(Long maDanhMucCha) { this.maDanhMucCha = maDanhMucCha; }

    public String getLinkIcon() { return linkIcon; }
    public void setLinkIcon(String linkIcon) { this.linkIcon = linkIcon; }

    public Integer getThuTuHienThi() { return thuTuHienThi; }
    public void setThuTuHienThi(Integer thuTuHienThi) { this.thuTuHienThi = thuTuHienThi; }

    public Boolean getDangHoatDong() { return dangHoatDong; }
    public void setDangHoatDong(Boolean dangHoatDong) { this.dangHoatDong = dangHoatDong; }
}
