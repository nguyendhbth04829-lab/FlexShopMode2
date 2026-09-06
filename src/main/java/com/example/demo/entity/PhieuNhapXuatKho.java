package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_nhap_xuat_kho")
public class PhieuNhapXuatKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phieu_kho")
    private Long maPhieuKho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho", nullable = false)
    private KhoHang khoHang;

    @Column(name = "loai_phieu", nullable = false, length = 30)
    private String loaiPhieu; // NHAP_KHO, XUAT_KHO

    @Column(name = "ma_chung_tu_lien_quan", length = 100)
    private String maChungTuLienQuan;

    @Column(name = "nguoi_lap_phieu", nullable = false)
    private Long nguoiLapPhieu;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "ngay_lap")
    private LocalDateTime ngayLap = LocalDateTime.now();

    public Long getMaPhieuKho() { return maPhieuKho; }
    public void setMaPhieuKho(Long maPhieuKho) { this.maPhieuKho = maPhieuKho; }

    public KhoHang getKhoHang() { return khoHang; }
    public void setKhoHang(KhoHang khoHang) { this.khoHang = khoHang; }

    public String getLoaiPhieu() { return loaiPhieu; }
    public void setLoaiPhieu(String loaiPhieu) { this.loaiPhieu = loaiPhieu; }

    public String getMaChungTuLienQuan() { return maChungTuLienQuan; }
    public void setMaChungTuLienQuan(String maChungTuLienQuan) { this.maChungTuLienQuan = maChungTuLienQuan; }

    public Long getNguoiLapPhieu() { return nguoiLapPhieu; }
    public void setNguoiLapPhieu(Long nguoiLapPhieu) { this.nguoiLapPhieu = nguoiLapPhieu; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
}
