package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class KhoHangForm {
    
    private Long maKho;

    @NotBlank(message = "Tên kho không được để trống")
    @Size(max = 150, message = "Tên kho tối đa 150 ký tự")
    private String tenKho;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String diaChi;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 100, message = "Tỉnh/Thành phố tối đa 100 ký tự")
    private String tinhThanh;

    @NotBlank(message = "SĐT liên hệ không được để trống")
    @Size(max = 20, message = "SĐT liên hệ tối đa 20 ký tự")
    private String sdtLienHe;

    private Boolean laKhoChinh = false;
    private Boolean dangHoatDong = true;

    // Getters and setters
    public Long getMaKho() { return maKho; }
    public void setMaKho(Long maKho) { this.maKho = maKho; }

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
}
