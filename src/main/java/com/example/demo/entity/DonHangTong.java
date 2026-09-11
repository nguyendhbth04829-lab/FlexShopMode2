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
 * USER STORY: US-26 - Entity Đơn hàng tổng (Master Order) quản lý thanh toán
 * =====================================================================
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
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
    private String phuongThucThanhToan = "CHUA_CHON";

    @Column(name = "trang_thai_thanh_toan", length = 30)
    private String trangThaiThanhToan = "CHUA_THANH_TOAN";

    @Column(name = "trang_thai_don_hang", length = 30)
    private String trangThaiDonHang = "CHO_XU_LY";

    @Column(name = "so_xu_da_dung")
    private Long soXuDaDung = 0L;

    @Column(name = "so_tien_giam_tu_xu", precision = 18, scale = 2)
    private BigDecimal soTienGiamTuXu = BigDecimal.ZERO;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @OneToMany(mappedBy = "donHangTong", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<DonHangShop> danhSachShopOrder = new ArrayList<>();

    @Transient
    public String getTenPhuongThucTiengViet() {
        if (phuongThucThanhToan == null) return "Chưa xác định";
        return switch (phuongThucThanhToan) {
            case "COD" -> "Thanh toán khi nhận hàng (COD)";
            case "MOCK_ONLINE" -> "Cổng thanh toán Trực tuyến (Mock Online Payment)";
            case "SPAYLATER" -> "Mua trước trả sau (SPayLater)";
            default -> "Chưa chọn phương thức";
        };
    }

    @Transient
    public String getTenTrangThaiThanhToanTiengViet() {
        if (trangThaiThanhToan == null) return "Chưa thanh toán";
        return switch (trangThaiThanhToan) {
            case "DA_THANH_TOAN" -> "Đã thanh toán";
            case "CHUA_THANH_TOAN" -> "Chưa thanh toán (Chờ COD / Chờ Online)";
            case "THANH_TOAN_THAT_BAI" -> "Thanh toán thất bại";
            default -> trangThaiThanhToan;
        };
    }

    @Transient
    public String getBadgeTrangThaiThanhToan() {
        if (trangThaiThanhToan == null) return "bg-secondary text-white";
        return switch (trangThaiThanhToan) {
            case "DA_THANH_TOAN" -> "bg-success text-white";
            case "CHUA_THANH_TOAN" -> "bg-warning text-dark";
            case "THANH_TOAN_THAT_BAI" -> "bg-danger text-white";
            default -> "bg-secondary text-white";
        };
    }
}
