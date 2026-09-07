package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * US-32: Doi tac van chuyen 3PL (GHN, GHTK...).
 * Anh xa bang doi_tac_van_chuyen.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doi_tac_van_chuyen")
public class DoiTacVanChuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_doi_tac")
    private Integer maDoiTac;

    @Column(name = "ten_doi_tac", nullable = false, length = 100)
    private String tenDoiTac;

    @Column(name = "ma_ket_noi_api", length = 100)
    private String maKetNoiApi;

    @Column(name = "dang_hoat_dong")
    private Boolean dangHoatDong = true;
}
