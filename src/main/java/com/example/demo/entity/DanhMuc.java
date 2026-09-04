package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "danh_muc")
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_danh_muc")
    private Long maDanhMuc;

    @Column(name = "ten_danh_muc", nullable = false, length = 100)
    private String tenDanhMuc;

    @Column(name = "duong_dan_slug", nullable = false, unique = true, length = 120)
    private String duongDanSlug;

    @Column(name = "link_icon", length = 500)
    private String linkIcon;

    @Column(name = "cap_do")
    private Integer capDo = 1;

    @Column(name = "thu_tu_hien_thi")
    private Integer thuTuHienThi = 0;

    @Column(name = "dang_hoat_dong")
    private Boolean dangHoatDong = true;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;
}
