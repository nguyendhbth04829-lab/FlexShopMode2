package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vi_tri_ke_kho")
public class ViTriKeKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_vi_tri")
    private Long maViTri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_kho", nullable = false)
    private KhoHang khoHang;

    @Column(name = "ma_khu_vuc", nullable = false, length = 50)
    private String maKhuVuc;

    public Long getMaViTri() { return maViTri; }
    public void setMaViTri(Long maViTri) { this.maViTri = maViTri; }

    public KhoHang getKhoHang() { return khoHang; }
    public void setKhoHang(KhoHang khoHang) { this.khoHang = khoHang; }

    public String getMaKhuVuc() { return maKhuVuc; }
    public void setMaKhuVuc(String maKhuVuc) { this.maKhuVuc = maKhuVuc; }
}
