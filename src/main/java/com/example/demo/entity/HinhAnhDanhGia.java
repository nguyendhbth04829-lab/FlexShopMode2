package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hinh_anh_danh_gia")
public class HinhAnhDanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_anh_danh_gia")
    private Long maAnhDanhGia;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_danh_gia", referencedColumnName = "ma_danh_gia", nullable = false)
    private DanhGiaSanPham danhGiaSanPham;

    @Column(name = "link_anh", nullable = false, length = 500)
    private String linkAnh;
}
