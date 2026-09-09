package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * US-32: Bang gia van chuyen theo tuyen (NOI_THANH | TINH | BAN_SO_DIA).
 * Anh xa bang bang_gia_van_chuyen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bang_gia_van_chuyen")
public class BangGiaVanChuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bang_gia")
    private Long maBangGia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_doi_tac", referencedColumnName = "ma_doi_tac", nullable = false)
    private DoiTacVanChuyen doiTac;

    @Column(name = "tuyen_van_chuyen", nullable = false, length = 50)
    private String tuyenVanChuyen;

    @Column(name = "khoi_luong_chuan_gram", nullable = false)
    private Integer khoiLuongChuanGram;

    @Column(name = "cuoc_phi_chuan", nullable = false, precision = 18, scale = 2)
    private BigDecimal cuocPhiChuan;

    @Column(name = "cuoc_phi_vuot_moi_500g", nullable = false, precision = 18, scale = 2)
    private BigDecimal cuocPhiVuotMoi500g;
}
