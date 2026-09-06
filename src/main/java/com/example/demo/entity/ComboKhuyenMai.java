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
 * Thực thể biểu diễn Chương Trình Combo Khuyến Mãi & Mua Kèm Deal Sốc (US-54 - PROMOTION)
 * Ánh xạ bảng combo_khuyen_mai trong CSDL FlexShop_V2_Full.
 * Tiêu chí cốt lõi: Cấu hình "Mua sản phẩm chính A giảm giá 50% cho phụ kiện B".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "combo_khuyen_mai")
public class ComboKhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_combo")
    private Long maCombo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", nullable = false)
    @ToString.Exclude
    private GianHang gianHang;

    @Column(name = "ten_combo", nullable = false, length = 150)
    private String tenCombo;

    /**
     * Phân loại chương trình:
     * - DEAL_SOC_MUA_KEM: Mua sản phẩm chính A được giảm giá sốc phụ kiện B (Add-on Deals)
     * - COMBO_GIAM_PHAN_TRAM: Mua tối thiểu X sản phẩm giảm Y% (Bundle % Off)
     * - COMBO_GIAM_TIEN: Mua tối thiểu X sản phẩm giảm Y VNĐ (Bundle Cash Off)
     */
    @Column(name = "loai_combo", nullable = false, length = 50)
    private String loaiCombo;

    /**
     * Số lượng sản phẩm tối thiểu để kích hoạt ưu đãi (Deal sốc: 1 món chính; Combo: 2 món trở lên)
     */
    @Column(name = "so_luong_toi_thieu", nullable = false)
    private Integer soLuongToiThieu = 1;

    /**
     * Giá trị giảm (Ví dụ: 50.00 cho 50% hoặc 50000.00 cho 50.000đ)
     */
    @Column(name = "gia_tri_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(name = "dang_hoat_dong")
    private Boolean dangHoatDong = true;

    @OneToMany(mappedBy = "comboKhuyenMai", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SanPhamComboKhuyenMai> danhSachSanPham = new ArrayList<>();

    // =========================================================================
    // HELPER METHODS NGHIỆP VỤ & HIỂN THỊ
    // =========================================================================

    public boolean isDangDienRa() {
        if (Boolean.FALSE.equals(this.dangHoatDong)) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(this.ngayBatDau) && !now.isAfter(this.ngayKetThuc);
    }

    public boolean isSapDienRa() {
        if (Boolean.FALSE.equals(this.dangHoatDong)) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isBefore(this.ngayBatDau);
    }

    public boolean isDaKetThuc() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(this.ngayKetThuc);
    }

    public String getTrangThaiHienThi() {
        if (Boolean.FALSE.equals(this.dangHoatDong)) return "Tạm Dừng";
        if (isDangDienRa()) return "Đang Diễn Ra";
        if (isSapDienRa()) return "Sắp Diễn Ra";
        return "Đã Kết Thúc";
    }

    public String getBadgeClass() {
        if (Boolean.FALSE.equals(this.dangHoatDong)) return "bg-secondary";
        if (isDangDienRa()) return "bg-success animate-pulse";
        if (isSapDienRa()) return "bg-warning text-dark";
        return "bg-dark";
    }

    public String getLoaiComboHienThi() {
        if ("DEAL_SOC_MUA_KEM".equalsIgnoreCase(this.loaiCombo)) {
            return "Mua Kèm Deal Sốc (Add-on Deal)";
        } else if ("COMBO_GIAM_PHAN_TRAM".equalsIgnoreCase(this.loaiCombo)) {
            return "Combo Giảm Theo %";
        } else if ("COMBO_GIAM_TIEN".equalsIgnoreCase(this.loaiCombo)) {
            return "Combo Giảm Tiền Mặt";
        }
        return "Combo Khuyến Mãi";
    }

    public List<SanPhamComboKhuyenMai> getDanhSachSanPhamChinh() {
        if (danhSachSanPham == null) return List.of();
        return danhSachSanPham.stream()
                .filter(sp -> "SAN_PHAM_CHINH".equalsIgnoreCase(sp.getVaiTro()))
                .toList();
    }

    public List<SanPhamComboKhuyenMai> getDanhSachSanPhamMuaKem() {
        if (danhSachSanPham == null) return List.of();
        return danhSachSanPham.stream()
                .filter(sp -> "MUA_KEM_DEAL_SOC".equalsIgnoreCase(sp.getVaiTro()))
                .toList();
    }
}