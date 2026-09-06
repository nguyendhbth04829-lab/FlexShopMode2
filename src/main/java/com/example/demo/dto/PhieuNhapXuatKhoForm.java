package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PhieuNhapXuatKhoForm {
    
    @NotNull(message = "Mã kho không được để trống")
    private Long maKho;
    
    @NotBlank(message = "Loại phiếu không được để trống (NHAP_KHO, XUAT_KHO)")
    private String loaiPhieu;
    
    private String maChungTuLienQuan;
    private String ghiChu;
    
    @NotEmpty(message = "Danh sách sản phẩm không được rỗng")
    @Valid
    private List<ChiTietPhieuKhoForm> chiTietList;

    public Long getMaKho() { return maKho; }
    public void setMaKho(Long maKho) { this.maKho = maKho; }

    public String getLoaiPhieu() { return loaiPhieu; }
    public void setLoaiPhieu(String loaiPhieu) { this.loaiPhieu = loaiPhieu; }

    public String getMaChungTuLienQuan() { return maChungTuLienQuan; }
    public void setMaChungTuLienQuan(String maChungTuLienQuan) { this.maChungTuLienQuan = maChungTuLienQuan; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public List<ChiTietPhieuKhoForm> getChiTietList() { return chiTietList; }
    public void setChiTietList(List<ChiTietPhieuKhoForm> chiTietList) { this.chiTietList = chiTietList; }
}
