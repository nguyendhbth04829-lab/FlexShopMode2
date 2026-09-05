package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể ghi nhận Lịch sử sử dụng mã giảm giá (US-52 - VOUCHER)
 * Ánh xạ bảng lich_su_dung_ma_giam_gia trong CSDL FlexShop_V2_Full.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_dung_ma_giam_gia")
public class LichSuDungMaGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lich_su_dung")
    private Long maLichSuDung;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_voucher", referencedColumnName = "ma_voucher", nullable = false)
    private MaGiamGia voucher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nguoi_dung", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_tong", referencedColumnName = "ma_don_hang_tong", nullable = false)
    private DonHangTong donHangTong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_shop", referencedColumnName = "ma_don_hang_shop")
    private DonHangShop donHangShop;

    @Column(name = "so_tien_da_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienDaGiam;

    @Column(name = "ngay_su_dung")
    private LocalDateTime ngaySuDung = LocalDateTime.now();
}
