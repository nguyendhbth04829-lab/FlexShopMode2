package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cuoc_tro_chuyen")
public class CuocTroChuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_cuoc_tro_chuyen")
    private Long maCuocTroChuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khach_hang", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @Column(name = "tin_nhan_cuoi_cung", columnDefinition = "NVARCHAR(MAX)")
    private String tinNhanCuoiCung;

    @Column(name = "thoi_gian_tin_cuoi")
    private LocalDateTime thoiGianTinCuoi;

    @Column(name = "so_tin_chua_doc_khach")
    private Integer soTinChuaDocKhach = 0;

    @Column(name = "so_tin_chua_doc_shop")
    private Integer soTinChuaDocShop = 0;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();
}
