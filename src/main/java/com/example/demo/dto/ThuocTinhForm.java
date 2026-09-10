package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ThuocTinhForm {

    private Long maThuocTinh;

    @NotNull(message = "Mã sản phẩm không được để trống")
    private Long maSanPham;

    @NotBlank(message = "Tên thuộc tính không được để trống (vd: Màu sắc, Kích cỡ)")
    @Size(max = 100, message = "Tên thuộc tính không quá 100 ký tự")
    private String tenThuocTinh;

    @NotEmpty(message = "Cần ít nhất một giá trị cho thuộc tính")
    private List<String> danhSachGiaTri;

    public Long getMaThuocTinh() { return maThuocTinh; }
    public void setMaThuocTinh(Long maThuocTinh) { this.maThuocTinh = maThuocTinh; }

    public Long getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Long maSanPham) { this.maSanPham = maSanPham; }

    public String getTenThuocTinh() { return tenThuocTinh; }
    public void setTenThuocTinh(String tenThuocTinh) { this.tenThuocTinh = tenThuocTinh; }

    public List<String> getDanhSachGiaTri() { return danhSachGiaTri; }
    public void setDanhSachGiaTri(List<String> danhSachGiaTri) { this.danhSachGiaTri = danhSachGiaTri; }
}
