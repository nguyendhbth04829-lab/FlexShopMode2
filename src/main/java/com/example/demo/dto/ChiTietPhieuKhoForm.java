package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ChiTietPhieuKhoForm {
    
    @NotNull(message = "Mã biến thể không được để trống")
    private Long maBienThe;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng nhập/xuất phải lớn hơn 0")
    private Integer soLuong;
    
    private BigDecimal donGiaVon;

    public Long getMaBienThe() { return maBienThe; }
    public void setMaBienThe(Long maBienThe) { this.maBienThe = maBienThe; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGiaVon() { return donGiaVon; }
    public void setDonGiaVon(BigDecimal donGiaVon) { this.donGiaVon = donGiaVon; }
}
