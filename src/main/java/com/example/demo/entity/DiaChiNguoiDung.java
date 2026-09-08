package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dia_chi_nguoi_dung")
public class DiaChiNguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_dia_chi")
    private Long maDiaChi;


    private String tenNguoiNhan;

    private String soDienThoai;

    private String tinhThanh;

    private String quanHuyen;

    private String xaPhuong;

    private String diaChiChiTiet;

    @Column(name = "la_mac_dinh")
    private Boolean laMacDinh = false;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    private LocalDateTime ngayTao = LocalDateTime.now();
}
