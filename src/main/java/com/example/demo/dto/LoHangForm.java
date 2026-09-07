package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class LoHangForm {

    private Long maLo;

    @NotNull(message = "Mã biến thể không được để trống")
    private Long maBienThe;

    @NotBlank(message = "Mã số lô không được để trống")
    private String maSoLo;

    @NotNull(message = "Ngày sản xuất không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySanXuat;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hanSuDung;

    public Long getMaLo() { return maLo; }
    public void setMaLo(Long maLo) { this.maLo = maLo; }

    public Long getMaBienThe() { return maBienThe; }
    public void setMaBienThe(Long maBienThe) { this.maBienThe = maBienThe; }

    public String getMaSoLo() { return maSoLo; }
    public void setMaSoLo(String maSoLo) { this.maSoLo = maSoLo; }

    public LocalDate getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(LocalDate ngaySanXuat) { this.ngaySanXuat = ngaySanXuat; }

    public LocalDate getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(LocalDate hanSuDung) { this.hanSuDung = hanSuDung; }
}
