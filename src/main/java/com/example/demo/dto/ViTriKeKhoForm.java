package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ViTriKeKhoForm {

    private Long maViTri;

    @NotNull(message = "Mã kho không được để trống")
    private Long maKho;

    @NotBlank(message = "Mã khu vực/kệ không được để trống")
    @Size(max = 50, message = "Mã khu vực không vượt quá 50 ký tự")
    private String maKhuVuc;

    public Long getMaViTri() { return maViTri; }
    public void setMaViTri(Long maViTri) { this.maViTri = maViTri; }

    public Long getMaKho() { return maKho; }
    public void setMaKho(Long maKho) { this.maKho = maKho; }

    public String getMaKhuVuc() { return maKhuVuc; }
    public void setMaKhuVuc(String maKhuVuc) { this.maKhuVuc = maKhuVuc; }
}
