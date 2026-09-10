package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "gia_tri_thuoc_tinh")
public class GiaTriThuocTinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_gia_tri")
    private Long maGiaTri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_thuoc_tinh", nullable = false)
    private ThuocTinhSanPham thuocTinh;

    @Column(name = "gia_tri", nullable = false, length = 100)
    private String giaTri;

    public Long getMaGiaTri() { return maGiaTri; }
    public void setMaGiaTri(Long maGiaTri) { this.maGiaTri = maGiaTri; }

    public ThuocTinhSanPham getThuocTinh() { return thuocTinh; }
    public void setThuocTinh(ThuocTinhSanPham thuocTinh) { this.thuocTinh = thuocTinh; }

    public String getGiaTri() { return giaTri; }
    public void setGiaTri(String giaTri) { this.giaTri = giaTri; }
}
