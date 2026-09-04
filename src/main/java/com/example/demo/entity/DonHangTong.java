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
@Table(name = "don_hang_tong")
public class DonHangTong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_don_hang_tong")
    private Long maDonHangTong;

    @Column(name = "ma_code_don_tong", nullable = false, unique = true, length = 50)
    private String maCodeDonTong;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_khach_hang", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_dia_chi_giao", referencedColumnName = "ma_dia_chi", nullable = false)
    private DiaChiNguoiDung diaChiGiao;

    @Column(name = "tong_tien_hang", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTienHang;

    @Column(name = "tong_phi_van_chuyen", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongPhiVanChuyen;

    @Column(name = "tong_tien_thue_vat", precision = 18, scale = 2)
    private BigDecimal tongTienThueVat = BigDecimal.ZERO;

    @Column(name = "tong_giam_gia_san", precision = 18, scale = 2)
    private BigDecimal tongGiamGiaSan = BigDecimal.ZERO;

    @Column(name = "tong_giam_gia_shop", precision = 18, scale = 2)
    private BigDecimal tongGiamGiaShop = BigDecimal.ZERO;

    @Column(name = "tong_thanh_toan_cuoi", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongThanhToanCuoi;

    @Column(name = "phuong_thuc_thanh_toan", nullable = false, length = 50)
    private String phuongThucThanhToan;

    @Column(name = "trang_thai_thanh_toan", length = 30)
    private String trangThaiThanhToan = "CHUA_THANH_TOAN";

    @Column(name = "trang_thai_don_hang", length = 30)
    private String trangThaiDonHang = "CHO_XU_LY";

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();
}
