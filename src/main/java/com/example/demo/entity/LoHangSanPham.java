package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "lo_hang_san_pham")
public class LoHangSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lo")
    private Long maLo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bien_the", nullable = false)
    private BienTheSanPham bienThe;

    @Column(name = "ma_so_lo", nullable = false, length = 100)
    private String maSoLo;

    @Column(name = "ngay_san_xuat", nullable = false)
    private LocalDate ngaySanXuat;

    @Column(name = "han_su_dung")
    private LocalDate hanSuDung;

    public Long getMaLo() { return maLo; }
    public void setMaLo(Long maLo) { this.maLo = maLo; }

    public BienTheSanPham getBienThe() { return bienThe; }
    public void setBienThe(BienTheSanPham bienThe) { this.bienThe = bienThe; }

    public String getMaSoLo() { return maSoLo; }
    public void setMaSoLo(String maSoLo) { this.maSoLo = maSoLo; }

    public LocalDate getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(LocalDate ngaySanXuat) { this.ngaySanXuat = ngaySanXuat; }

    public LocalDate getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(LocalDate hanSuDung) { this.hanSuDung = hanSuDung; }
}
