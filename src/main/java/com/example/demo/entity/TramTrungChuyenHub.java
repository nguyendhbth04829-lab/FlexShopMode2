package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tram_trung_chuyen_hub")
public class TramTrungChuyenHub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hub")
    private Long maHub;

    @Column(name = "ten_hub", nullable = false, length = 150)
    private String tenHub;

    @Column(name = "dia_chi", nullable = false, length = 255)
    private String diaChi;

    @Column(name = "tinh_thanh", nullable = false, length = 100)
    private String tinhThanh;

    @Column(name = "suc_chua_kien_hang")
    private Integer sucChuaKienHang = 50000;
}
