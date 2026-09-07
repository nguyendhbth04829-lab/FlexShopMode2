package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ThuongHieuForm {

    private Long maThuongHieu;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 100, message = "Tên thương hiệu không vượt quá 100 ký tự")
    private String tenThuongHieu;

    private String linkLogo;
    
    // Thêm trường để hứng file upload từ form
    private org.springframework.web.multipart.MultipartFile fileLogo;
    
    private Boolean dangHoatDong = true;

    public Long getMaThuongHieu() { return maThuongHieu; }
    public void setMaThuongHieu(Long maThuongHieu) { this.maThuongHieu = maThuongHieu; }

    public String getTenThuongHieu() { return tenThuongHieu; }
    public void setTenThuongHieu(String tenThuongHieu) { this.tenThuongHieu = tenThuongHieu; }

    public String getLinkLogo() { return linkLogo; }
    public void setLinkLogo(String linkLogo) { this.linkLogo = linkLogo; }

    public org.springframework.web.multipart.MultipartFile getFileLogo() { return fileLogo; }
    public void setFileLogo(org.springframework.web.multipart.MultipartFile fileLogo) { this.fileLogo = fileLogo; }

    public Boolean getDangHoatDong() { return dangHoatDong; }
    public void setDangHoatDong(Boolean dangHoatDong) { this.dangHoatDong = dangHoatDong; }
}
