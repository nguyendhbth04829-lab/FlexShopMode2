package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_giao_dich_vi")
public class LichSuGiaoDichVi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_giao_dich")
    private Long maGiaoDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_vi", referencedColumnName = "ma_vi", nullable = false)
    @JoinColumn(name = "ma_vi", nullable = false)
    private ViNguoiBan viNguoiBan;

    @Column(name = "loai_giao_dich", nullable = false, length = 50)
    private String loaiGiaoDich; // CONG_TIEN_BOI_THUONG, TRU_TIEN_HOAN_TRA, GIAI_PHONG_ESCROW, RUT_TIEN
    private String loaiGiaoDich; // GIAI_NGAN_DON_HANG, RUT_TIEN, HOAN_TIEN_KHI_KHOA

    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "so_du_sau_giao_dich", nullable = false, precision = 18, scale = 2)
    private BigDecimal soDuSauGiaoDich;

    @Column(name = "ma_tham_chieu", nullable = false, length = 100)
    private String maThamChieu;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();
}
