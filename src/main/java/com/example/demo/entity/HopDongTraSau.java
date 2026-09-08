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
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - Thực thể Hợp Đồng Trả Sau (hop_dong_tra_sau)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hop_dong_tra_sau")
public class HopDongTraSau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hop_dong")
    private Long maHopDong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tk_tra_sau", nullable = false)
    private TaiKhoanTraSau taiKhoanTraSau;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_tong", nullable = false, unique = true)
    private DonHangTong donHangTong;

    @Column(name = "tong_so_tien_vay", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongSoTienVay;

    @Column(name = "so_ky_tra_gop", nullable = false)
    private Integer soKyTraGop = 3;

    @Column(name = "lai_suat_thang_phan_tram", precision = 4, scale = 2)
    private BigDecimal laiSuatThangPhanTram = BigDecimal.ZERO;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @OneToMany(mappedBy = "hopDongTraSau", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @OrderBy("kySo ASC")
    private List<KyThanhToanTraSau> danhSachKy = new ArrayList<>();

    @Transient
    public BigDecimal getTongTienCanThanhToan() {
        if (danhSachKy == null || danhSachKy.isEmpty()) {
            return tongSoTienVay != null ? tongSoTienVay : BigDecimal.ZERO;
        }
        return danhSachKy.stream()
                .map(k -> k.getSoTienCanTra() != null ? k.getSoTienCanTra() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getTongTienDaThanhToan() {
        if (danhSachKy == null || danhSachKy.isEmpty()) return BigDecimal.ZERO;
        return danhSachKy.stream()
                .map(k -> k.getSoTienDaTra() != null ? k.getSoTienDaTra() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getTongTienConNo() {
        return getTongTienCanThanhToan().subtract(getTongTienDaThanhToan()).max(BigDecimal.ZERO);
    }

    @Transient
    public long getSoKyDaTra() {
        if (danhSachKy == null) return 0;
        return danhSachKy.stream()
                .filter(k -> "DA_TRA".equalsIgnoreCase(k.getTrangThai()))
                .count();
    }

    @Transient
    public boolean isDaTatToan() {
        if (danhSachKy == null || danhSachKy.isEmpty()) return false;
        return danhSachKy.stream()
                .allMatch(k -> "DA_TRA".equalsIgnoreCase(k.getTrangThai()));
    }

    @Transient
    public boolean isCoNoQuaHan() {
        if (danhSachKy == null || danhSachKy.isEmpty()) return false;
        return danhSachKy.stream()
                .anyMatch(KyThanhToanTraSau::isQuaHan);
    }

    @Transient
    public String getTrangThaiHopDongTiengViet() {
        if (isDaTatToan()) return "Đã tất toán";
        if (isCoNoQuaHan()) return "Có kỳ quá hạn";
        return "Đang trả góp";
    }

    @Transient
    public String getBadgeClass() {
        if (isDaTatToan()) return "bg-success text-white";
        if (isCoNoQuaHan()) return "bg-danger text-white";
        return "bg-primary text-white";
    }
}
