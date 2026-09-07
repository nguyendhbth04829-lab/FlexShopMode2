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
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - Thực thể Tài Khoản Trả Sau / SPayLater (tai_khoan_tra_sau)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tai_khoan_tra_sau")
public class TaiKhoanTraSau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tk_tra_sau")
    private Long maTkTraSau;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false, unique = true)
    private NguoiDung nguoiDung;

    @Column(name = "han_muc_duoc_cap", nullable = false, precision = 18, scale = 2)
    private BigDecimal hanMucDuocCap = new BigDecimal("5000000.00");

    @Column(name = "han_muc_con_lai", nullable = false, precision = 18, scale = 2)
    private BigDecimal hanMucConLai = new BigDecimal("5000000.00");

    @Column(name = "diem_tin_dung")
    private Integer diemTinDung = 650;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "HOAT_DONG"; // HOAT_DONG, TAM_KHOA, BI_KHOA

    @Column(name = "ngay_cap")
    private LocalDateTime ngayCap = LocalDateTime.now();

    @Transient
    public BigDecimal getHanMucDaSuDung() {
        if (hanMucDuocCap == null) return BigDecimal.ZERO;
        if (hanMucConLai == null) return hanMucDuocCap;
        return hanMucDuocCap.subtract(hanMucConLai).max(BigDecimal.ZERO);
    }

    @Transient
    public double getTiLeSuDungPhanTram() {
        if (hanMucDuocCap == null || hanMucDuocCap.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        BigDecimal daDung = getHanMucDaSuDung();
        return daDung.multiply(new BigDecimal("100"))
                .divide(hanMucDuocCap, 1, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Transient
    public String getTenTrangThaiTiengViet() {
        if (trangThai == null) return "Chưa xác định";
        return switch (trangThai) {
            case "HOAT_DONG" -> "Đang hoạt động";
            case "TAM_KHOA" -> "Tạm khóa (Có nợ quá hạn)";
            case "BI_KHOA" -> "Bị khóa vĩnh viễn";
            default -> trangThai;
        };
    }

    @Transient
    public String getBadgeClass() {
        if (trangThai == null) return "bg-secondary text-white";
        return switch (trangThai) {
            case "HOAT_DONG" -> "bg-success text-white";
            case "TAM_KHOA" -> "bg-warning text-dark";
            case "BI_KHOA" -> "bg-danger text-white";
            default -> "bg-secondary text-white";
        };
    }

    @Transient
    public boolean isDuDieuKienVay() {
        return "HOAT_DONG".equalsIgnoreCase(trangThai)
                && (diemTinDung != null && diemTinDung >= 500)
                && (hanMucConLai != null && hanMucConLai.compareTo(BigDecimal.ZERO) > 0);
    }
}
