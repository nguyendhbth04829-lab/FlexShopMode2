package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hinh_anh_san_pham")
public class HinhAnhSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_anh")
    private Long maAnh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "link_anh", nullable = false, length = 500)
    private String linkAnh;

    @Column(name = "la_anh_chinh")
    private Boolean laAnhChinh = false;

    @Column(name = "thu_tu_hien_thi")
    private Integer thuTuHienThi = 0;

    public Long getMaAnh() { return maAnh; }
    public void setMaAnh(Long maAnh) { this.maAnh = maAnh; }

    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }

    public String getLinkAnh() { return linkAnh; }
    public void setLinkAnh(String linkAnh) { this.linkAnh = linkAnh; }

    public Boolean getLaAnhChinh() { return laAnhChinh; }
    public void setLaAnhChinh(Boolean laAnhChinh) { this.laAnhChinh = laAnhChinh; }

    public Integer getThuTuHienThi() { return thuTuHienThi; }
    public void setThuTuHienThi(Integer thuTuHienThi) { this.thuTuHienThi = thuTuHienThi; }
}
