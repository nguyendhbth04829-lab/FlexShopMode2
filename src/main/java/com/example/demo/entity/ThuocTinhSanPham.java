package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "thuoc_tinh_san_pham")
public class ThuocTinhSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_thuoc_tinh")
    private Long maThuocTinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "ten_thuoc_tinh", nullable = false, length = 100)
    private String tenThuocTinh;

    @OneToMany(mappedBy = "thuocTinh", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GiaTriThuocTinh> danhSachGiaTri = new ArrayList<>();

    public Long getMaThuocTinh() { return maThuocTinh; }
    public void setMaThuocTinh(Long maThuocTinh) { this.maThuocTinh = maThuocTinh; }

    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }

    public String getTenThuocTinh() { return tenThuocTinh; }
    public void setTenThuocTinh(String tenThuocTinh) { this.tenThuocTinh = tenThuocTinh; }

    public List<GiaTriThuocTinh> getDanhSachGiaTri() { return danhSachGiaTri; }
    public void setDanhSachGiaTri(List<GiaTriThuocTinh> danhSachGiaTri) { this.danhSachGiaTri = danhSachGiaTri; }
}
