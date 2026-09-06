package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể biểu diễn Khung Giờ Flash Sale (US-53 - PROMOTION)
 * Ánh xạ bảng khung_gio_flash_sale trong CSDL FlexShop_V2_Full.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "khung_gio_flash_sale")
public class KhungGioFlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_flash_sale")
    private Long maFlashSale;

    @Column(name = "tieu_de", nullable = false, length = 150)
    private String tieuDe;

    @Column(name = "link_banner", length = 500)
    private String linkBanner;

    @Column(name = "thoi_gian_bat_dau", nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "thoi_gian_ket_thuc", nullable = false)
    private LocalDateTime thoiGianKetThuc;

    /**
     * Trạng thái khung giờ:
     * - SAP_DIEN_RA: Chưa đến giờ
     * - DANG_DIEN_RA: Đang diễn ra
     * - DA_KET_THUC: Đã qua giờ
     * - TAM_KHOA: Bị tạm dừng bởi Admin/Hệ thống
     */
    @Column(name = "trang_thai", length = 30)
    private String trangThai = "SAP_DIEN_RA";

    @ToString.Exclude
    @OneToMany(mappedBy = "khungGioFlashSale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SanPhamFlashSale> danhSachSanPham = new ArrayList<>();

    /**
     * Kiểm tra trạng thái động theo thời gian thực
     */
    public boolean isDangDienRa() {
        LocalDateTime now = LocalDateTime.now();
        return !"TAM_KHOA".equalsIgnoreCase(this.trangThai)
                && !now.isBefore(this.thoiGianBatDau)
                && !now.isAfter(this.thoiGianKetThuc);
    }

    public boolean isSapDienRa() {
        LocalDateTime now = LocalDateTime.now();
        return !"TAM_KHOA".equalsIgnoreCase(this.trangThai)
                && now.isBefore(this.thoiGianBatDau);
    }

    public boolean isDaKetThuc() {
        LocalDateTime now = LocalDateTime.now();
        return "DA_KET_THUC".equalsIgnoreCase(this.trangThai)
                || now.isAfter(this.thoiGianKetThuc);
    }

    /**
     * Lấy nhãn trạng thái hiển thị tiếng Việt
     */
    public String getTrangThaiHienThi() {
        if ("TAM_KHOA".equalsIgnoreCase(this.trangThai)) return "Tạm Khóa";
        if (isDangDienRa()) return "Đang Diễn Ra";
        if (isSapDienRa()) return "Sắp Diễn Ra";
        return "Đã Kết Thúc";
    }

    public String getBadgeClass() {
        if ("TAM_KHOA".equalsIgnoreCase(this.trangThai)) return "bg-secondary";
        if (isDangDienRa()) return "bg-danger animate-pulse";
        if (isSapDienRa()) return "bg-warning text-dark";
        return "bg-dark";
    }

    /**
     * Nhãn hiển thị mốc giờ trên thanh tab Portal người dùng (Chuẩn 24h & chỉ rõ ngày)
     */
    public String getNhanThoiGianTab() {
        if (this.thoiGianBatDau == null) return "00:00";
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate slotDate = this.thoiGianBatDau.toLocalDate();
        String timeStr = this.thoiGianBatDau.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        if (slotDate.equals(today)) {
            return timeStr;
        } else if (slotDate.equals(today.plusDays(1))) {
            return "Mai " + timeStr;
        } else {
            return slotDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + " " + timeStr;
        }
    }
}
