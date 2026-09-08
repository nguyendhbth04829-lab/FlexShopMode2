package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - Thực thể Chiến Dịch Quảng Cáo (chien_dich_quang_cao)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chien_dich_quang_cao")
public class ChienDichQuangCao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_chien_dich")
    private Long maChienDich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "ten_chien_dich", nullable = false, length = 150)
    private String tenChienDich;

    @Column(name = "ngan_sach_ngay", nullable = false, precision = 18, scale = 2)
    private BigDecimal nganSachNgay = new BigDecimal("100000.00");

    @Column(name = "tong_chi_phi_da_dung", precision = 18, scale = 2)
    private BigDecimal tongChiPhiDaDung = BigDecimal.ZERO;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "DANG_CHAY"; // DANG_CHAY, TAM_DUNG, HET_NGAN_SACH, KET_THUC

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau = LocalDateTime.now();

    @OneToMany(mappedBy = "chienDichQuangCao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TuKhoaDauThauAds> danhSachTuKhoa = new ArrayList<>();

    @Transient
    public int getTongLuotClick() {
        if (danhSachTuKhoa == null || danhSachTuKhoa.isEmpty()) return 0;
        return danhSachTuKhoa.stream()
                .mapToInt(k -> k.getTongLuotClick() != null ? k.getTongLuotClick() : 0)
                .sum();
    }

    @Transient
    public BigDecimal getNganSachConLaiTrongNgay() {
        BigDecimal nganSach = nganSachNgay != null ? nganSachNgay : BigDecimal.ZERO;
        BigDecimal daDung = tongChiPhiDaDung != null ? tongChiPhiDaDung : BigDecimal.ZERO;
        return nganSach.subtract(daDung).max(BigDecimal.ZERO);
    }

    @Transient
    public double getTiLeTieuNganSachPhanTram() {
        if (nganSachNgay == null || nganSachNgay.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        BigDecimal daDung = tongChiPhiDaDung != null ? tongChiPhiDaDung : BigDecimal.ZERO;
        return daDung.multiply(new BigDecimal("100"))
                .divide(nganSachNgay, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Transient
    public BigDecimal getCpcTrungBinh() {
        int clicks = getTongLuotClick();
        if (clicks <= 0 || tongChiPhiDaDung == null || tongChiPhiDaDung.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return tongChiPhiDaDung.divide(new BigDecimal(clicks), 0, RoundingMode.HALF_UP);
    }

    @Transient
    public String getTenTrangThaiTiengViet() {
        if (trangThai == null) return "Chưa xác định";
        return switch (trangThai) {
            case "DANG_CHAY" -> "Đang hoạt động";
            case "TAM_DUNG" -> "Tạm dừng";
            case "HET_NGAN_SACH" -> "Hết ngân sách ngày";
            case "KET_THUC" -> "Đã kết thúc";
            default -> trangThai;
        };
    }

    @Transient
    public String getBadgeClass() {
        if (trangThai == null) return "bg-secondary text-white";
        return switch (trangThai) {
            case "DANG_CHAY" -> "bg-success text-white";
            case "TAM_DUNG" -> "bg-warning text-dark";
            case "HET_NGAN_SACH" -> "bg-danger text-white";
            case "KET_THUC" -> "bg-dark text-white";
            default -> "bg-secondary text-white";
        };
    }
}
