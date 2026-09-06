package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & RÚT TIỀN (DEV 5 - MINH)
 * USER STORY: US-44 - Thực thể Yêu Cầu Rút Tiền Người Bán (yeu_cau_rut_tien)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "yeu_cau_rut_tien")
public class YeuCauRutTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_yeu_cau")
    private Long maYeuCau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @Column(name = "so_tien_rut", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienRut;

    @Column(name = "ten_ngan_hang", nullable = false, length = 100)
    private String tenNganHang;

    @Column(name = "so_tai_khoan", nullable = false, length = 50)
    private String soTaiKhoan;

    @Column(name = "ten_chu_tai_khoan", nullable = false, length = 100)
    private String tenChuTaiKhoan;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "CHO_DUYET"; // CHO_DUYET, DA_DUYET, TU_CHOI

    @Column(name = "ly_do_tu_choi", length = 255)
    private String lyDoTuChoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_admin_duyet")
    private NguoiDung adminDuyet;

    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Transient
    public String getTenTrangThaiTiengViet() {
        if (trangThai == null) return "Chưa xác định";
        return switch (trangThai) {
            case "CHO_DUYET" -> "Chờ Admin Duyệt";
            case "DA_DUYET" -> "Đã Duyệt & Chuyển Tiền";
            case "TU_CHOI" -> "Bị Từ Chối (Đã Hoàn Tiền)";
            default -> trangThai;
        };
    }

    @Transient
    public String getBadgeClass() {
        if (trangThai == null) return "bg-secondary text-white";
        return switch (trangThai) {
            case "CHO_DUYET" -> "bg-warning text-dark";
            case "DA_DUYET" -> "bg-success text-white";
            case "TU_CHOI" -> "bg-danger text-white";
            default -> "bg-secondary text-white";
        };
    }
}
