package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Thực thể Đơn hàng của từng Shop (Sub-order)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "don_hang_shop")
public class DonHangShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_don_hang_shop")
    private Long maDonHangShop;

    @Column(name = "ma_code_don_shop", nullable = false, unique = true, length = 60)
    private String maCodeDonShop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_tong", referencedColumnName = "ma_don_hang_tong", nullable = false)
    private DonHangTong donHangTong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @Column(name = "tien_hang_shop", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienHangShop;

    @Column(name = "phi_van_chuyen", nullable = false, precision = 18, scale = 2)
    private BigDecimal phiVanChuyen;

    @Column(name = "tien_thue_vat", precision = 18, scale = 2)
    private BigDecimal tienThueVat = BigDecimal.ZERO;

    @Column(name = "giam_gia_voucher_shop", precision = 18, scale = 2)
    private BigDecimal giamGiaVoucherShop = BigDecimal.ZERO;

    @Column(name = "giam_gia_voucher_san", precision = 18, scale = 2)
    private BigDecimal giamGiaVoucherSan = BigDecimal.ZERO;

    @Column(name = "tong_tien_shop_nhan", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTienShopNhan;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "CHO_XAC_NHAN";

    @Column(name = "ly_do_huy", length = 255)
    private String lyDoHuy;

    @Column(name = "nguoi_huy", length = 50)
    private String nguoiHuy;

    @Column(name = "ma_van_don", length = 100)
    private String maVanDon;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @ToString.Exclude
    @OneToMany(mappedBy = "donHangShop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChiTietDonHang> danhSachChiTiet = new ArrayList<>();
}
