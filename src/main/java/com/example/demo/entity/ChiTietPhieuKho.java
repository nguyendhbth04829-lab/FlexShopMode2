package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_phieu_kho")
public class ChiTietPhieuKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_chi_tiet_phieu")
    private Long maChiTietPhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu_kho", nullable = false)
    private PhieuNhapXuatKho phieuKho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bien_the", nullable = false)
    private BienTheSanPham bienThe;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia_von")
    private BigDecimal donGiaVon = BigDecimal.ZERO;

    public Long getMaChiTietPhieu() { return maChiTietPhieu; }
    public void setMaChiTietPhieu(Long maChiTietPhieu) { this.maChiTietPhieu = maChiTietPhieu; }

    public PhieuNhapXuatKho getPhieuKho() { return phieuKho; }
    public void setPhieuKho(PhieuNhapXuatKho phieuKho) { this.phieuKho = phieuKho; }

    public BienTheSanPham getBienThe() { return bienThe; }
    public void setBienThe(BienTheSanPham bienThe) { this.bienThe = bienThe; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGiaVon() { return donGiaVon; }
    public void setDonGiaVon(BigDecimal donGiaVon) { this.donGiaVon = donGiaVon; }
}
