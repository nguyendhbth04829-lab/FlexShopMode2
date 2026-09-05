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
@Table(name = "vi_nguoi_ban")
public class ViNguoiBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_vi")
    private Long maVi;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang", nullable = false, unique = true)
    private GianHang gianHang;

    @Column(name = "so_du_kha_dung", precision = 18, scale = 2)
    private BigDecimal soDuKhaDung = BigDecimal.ZERO;

    @Column(name = "so_du_tam_giu_escrow", precision = 18, scale = 2)
    private BigDecimal soDuTamGiuEscrow = BigDecimal.ZERO;

    @Column(name = "tong_tien_da_rut", precision = 18, scale = 2)
    private BigDecimal tongTienDaRut = BigDecimal.ZERO;

    @Version
    @Column(name = "phien_ban_lock")
    private Integer phienBanLock = 0;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();
}
