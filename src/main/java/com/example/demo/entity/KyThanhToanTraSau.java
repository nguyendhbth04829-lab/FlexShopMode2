package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - Thực thể Kỳ Thanh Toán Trả Sau (ky_thanh_toan_tra_sau)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ky_thanh_toan_tra_sau")
public class KyThanhToanTraSau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ky")
    private Long maKy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hop_dong", nullable = false)
    private HopDongTraSau hopDongTraSau;

    @Column(name = "ky_so", nullable = false)
    private Integer kySo;

    @Column(name = "so_tien_can_tra", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienCanTra;

    @Column(name = "so_tien_da_tra", precision = 18, scale = 2)
    private BigDecimal soTienDaTra = BigDecimal.ZERO;

    @Column(name = "han_chot_thanh_toan", nullable = false)
    private LocalDate hanChotThanhToan;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "CHUA_TRA"; // CHUA_TRA, DA_TRA, QUA_HAN

    @Transient
    public boolean isQuaHan() {
        if ("DA_TRA".equalsIgnoreCase(trangThai)) return false;
        return hanChotThanhToan != null && hanChotThanhToan.isBefore(LocalDate.now());
    }

    @Transient
    public BigDecimal getSoTienConLai() {
        BigDecimal canTra = soTienCanTra != null ? soTienCanTra : BigDecimal.ZERO;
        BigDecimal daTra = soTienDaTra != null ? soTienDaTra : BigDecimal.ZERO;
        return canTra.subtract(daTra).max(BigDecimal.ZERO);
    }

    @Transient
    public String getTenTrangThaiTiengViet() {
        if ("DA_TRA".equalsIgnoreCase(trangThai)) return "Đã thanh toán";
        if (isQuaHan() || "QUA_HAN".equalsIgnoreCase(trangThai)) return "Quá hạn thanh toán";
        return "Chưa thanh toán";
    }

    @Transient
    public String getBadgeClass() {
        if ("DA_TRA".equalsIgnoreCase(trangThai)) return "bg-success text-white";
        if (isQuaHan() || "QUA_HAN".equalsIgnoreCase(trangThai)) return "bg-danger text-white";
        return "bg-warning text-dark";
    }
}
