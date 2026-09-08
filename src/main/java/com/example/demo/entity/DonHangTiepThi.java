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
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - Thực thể Đơn Hàng Tiếp Thị (Affiliate Sub-order Attribution)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "don_hang_tiep_thi")
public class DonHangTiepThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_don_affiliate")
    private Long maDonAffiliate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_affiliate", nullable = false)
    private TiepThiLienKet tiepThiLienKet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_shop", nullable = false)
    private DonHangShop donHangShop;

    @Column(name = "hoa_hong_duoc_nhan", nullable = false, precision = 18, scale = 2)
    private BigDecimal hoaHongDuocNhan;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "CHO_DOI_SOAT"; // CHO_DOI_SOAT, DA_DUYET, DA_HUY

    @Column(name = "ngay_ghi_nhan")
    private LocalDateTime ngayGhiNhan = LocalDateTime.now();

    /**
     * Tên hiển thị trạng thái tiếng Việt
     */
    public String getTenTrangThaiTiengViet() {
        if ("CHO_DOI_SOAT".equalsIgnoreCase(this.trangThai)) {
            return "Chờ đối soát";
        } else if ("DA_DUYET".equalsIgnoreCase(this.trangThai)) {
            return "Đã duyệt";
        } else if ("DA_HUY".equalsIgnoreCase(this.trangThai)) {
            return "Đã hủy";
        }
        return this.trangThai;
    }

    /**
     * Badge CSS class cho Thymeleaf UI
     */
    public String getBadgeClass() {
        if ("CHO_DOI_SOAT".equalsIgnoreCase(this.trangThai)) {
            return "bg-warning text-dark";
        } else if ("DA_DUYET".equalsIgnoreCase(this.trangThai)) {
            return "bg-success text-white";
        } else if ("DA_HUY".equalsIgnoreCase(this.trangThai)) {
            return "bg-danger text-white";
        }
        return "bg-secondary text-white";
    }
}
