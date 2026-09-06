package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ton_kho_chi_tiet")
public class TonKhoChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ton_kho")
    private Long maTonKho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho", nullable = false)
    private KhoHang khoHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_vi_tri")
    private ViTriKeKho viTriKeKho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bien_the", nullable = false)
    private BienTheSanPham bienThe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_lo")
    private LoHangSanPham loHang;

    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon = 0;

    @Column(name = "so_luong_tam_giu", nullable = false)
    private Integer soLuongTamGiu = 0;

    @Version
    @Column(name = "phien_ban_lock")
    private Integer phienBanLock = 1;

    public Long getMaTonKho() { return maTonKho; }
    public void setMaTonKho(Long maTonKho) { this.maTonKho = maTonKho; }

    public KhoHang getKhoHang() { return khoHang; }
    public void setKhoHang(KhoHang khoHang) { this.khoHang = khoHang; }

    public ViTriKeKho getViTriKeKho() { return viTriKeKho; }
    public void setViTriKeKho(ViTriKeKho viTriKeKho) { this.viTriKeKho = viTriKeKho; }

    public BienTheSanPham getBienThe() { return bienThe; }
    public void setBienThe(BienTheSanPham bienThe) { this.bienThe = bienThe; }

    public LoHangSanPham getLoHang() { return loHang; }
    public void setLoHang(LoHangSanPham loHang) { this.loHang = loHang; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public Integer getSoLuongTamGiu() { return soLuongTamGiu; }
    public void setSoLuongTamGiu(Integer soLuongTamGiu) { this.soLuongTamGiu = soLuongTamGiu; }

    public Integer getPhienBanLock() { return phienBanLock; }
    public void setPhienBanLock(Integer phienBanLock) { this.phienBanLock = phienBanLock; }
    
    // Utility method: Tính số lượng thực tế có thể bán (Khả dụng)
    public Integer getSoLuongKhaDung() {
        return this.soLuongTon - this.soLuongTamGiu;
    }
}
