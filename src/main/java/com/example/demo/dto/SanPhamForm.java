package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class SanPhamForm {

    private Long maSanPham;

    @NotNull(message = "Gian hàng không được để trống")
    private Long maGianHang;

    @NotNull(message = "Danh mục không được để trống")
    private Long maDanhMuc;

    private Long maThuongHieu;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 10, max = 255, message = "Tên sản phẩm phải từ 10 đến 255 ký tự")
    private String tenSanPham;

    @Size(max = 500, message = "Mô tả ngắn không được vượt quá 500 ký tự")
    private String moTaNgan;

    private String moTaChiTiet;

    @NotNull(message = "Giá cơ bản không được để trống")
    @Min(value = 0, message = "Giá cơ bản phải lớn hơn hoặc bằng 0")
    private BigDecimal giaCoBan;

    @NotNull(message = "Cân nặng không được để trống")
    @Min(value = 1, message = "Cân nặng phải lớn hơn 0")
    private Integer canNangGram;

    @NotNull(message = "Chiều dài không được để trống")
    @Min(value = 1, message = "Chiều dài phải lớn hơn 0")
    private Integer chieuDaiCm;

    @NotNull(message = "Chiều rộng không được để trống")
    @Min(value = 1, message = "Chiều rộng phải lớn hơn 0")
    private Integer chieuRongCm;

    @NotNull(message = "Chiều cao không được để trống")
    @Min(value = 1, message = "Chiều cao phải lớn hơn 0")
    private Integer chieuCaoCm;

    // Getters and Setters (omitted standard implementation)
    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }

    public Long getMaGianHang() { return maGianHang; }
    public void setMaGianHang(Long maGianHang) { this.maGianHang = maGianHang; }

    public Long getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(Long maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public Long getMaThuongHieu() { return maThuongHieu; }
    public void setMaThuongHieu(Long maThuongHieu) { this.maThuongHieu = maThuongHieu; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getMoTaNgan() { return moTaNgan; }
    public void setMoTaNgan(String moTaNgan) { this.moTaNgan = moTaNgan; }

    public String getMoTaChiTiet() { return moTaChiTiet; }
    public void setMoTaChiTiet(String moTaChiTiet) { this.moTaChiTiet = moTaChiTiet; }

    public BigDecimal getGiaCoBan() { return giaCoBan; }
    public void setGiaCoBan(BigDecimal giaCoBan) { this.giaCoBan = giaCoBan; }

    public Integer getCanNangGram() { return canNangGram; }
    public void setCanNangGram(Integer canNangGram) { this.canNangGram = canNangGram; }

    public Integer getChieuDaiCm() { return chieuDaiCm; }
    public void setChieuDaiCm(Integer chieuDaiCm) { this.chieuDaiCm = chieuDaiCm; }

    public Integer getChieuRongCm() { return chieuRongCm; }
    public void setChieuRongCm(Integer chieuRongCm) { this.chieuRongCm = chieuRongCm; }

    public Integer getChieuCaoCm() { return chieuCaoCm; }
    public void setChieuCaoCm(Integer chieuCaoCm) { this.chieuCaoCm = chieuCaoCm; }
}
