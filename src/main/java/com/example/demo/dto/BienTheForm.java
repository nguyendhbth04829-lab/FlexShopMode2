package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BienTheForm {
    
    private Long maBienThe;
    
    @NotNull(message = "Mã sản phẩm không được để trống")
    private Long maSanPham;

    @NotBlank(message = "Mã SKU không được để trống")
    private String maSku;

    @NotBlank(message = "Tên biến thể không được để trống")
    private String tenBienThe;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "1.0", message = "Giá bán phải lớn hơn 0")
    private BigDecimal giaBan;

    private BigDecimal giaGoc;

    private String linkAnh;

    public Long getMaBienThe() { return maBienThe; }
    public void setMaBienThe(Long maBienThe) { this.maBienThe = maBienThe; }

    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }

    public String getMaSku() { return maSku; }
    public void setMaSku(String maSku) { this.maSku = maSku; }

    public String getTenBienThe() { return tenBienThe; }
    public void setTenBienThe(String tenBienThe) { this.tenBienThe = tenBienThe; }

    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }

    public BigDecimal getGiaGoc() { return giaGoc; }
    public void setGiaGoc(BigDecimal giaGoc) { this.giaGoc = giaGoc; }

    public String getLinkAnh() { return linkAnh; }
    public void setLinkAnh(String linkAnh) { this.linkAnh = linkAnh; }
}
