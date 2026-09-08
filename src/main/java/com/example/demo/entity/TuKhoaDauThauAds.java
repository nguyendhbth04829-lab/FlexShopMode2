package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - Thực thể Từ Khóa Đấu Thầu Ads (tu_khoa_dau_thau_ads)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tu_khoa_dau_thau_ads")
public class TuKhoaDauThauAds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tu_khoa")
    private Long maTuKhoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chien_dich", nullable = false)
    private ChienDichQuangCao chienDichQuangCao;

    @Column(name = "tu_khoa", nullable = false, length = 100)
    private String tuKhoa;

    @Column(name = "gia_thau_moi_click_cpc", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaThauMoiClickCpc = new BigDecimal("1000.00");

    @Column(name = "tong_luot_click")
    private Integer tongLuotClick = 0;

    @Column(name = "dang_kich_hoat")
    private Boolean dangKichHoat = true;

    @Transient
    public BigDecimal getTongChiPhiTuKhoa() {
        if (giaThauMoiClickCpc == null || tongLuotClick == null || tongLuotClick <= 0) {
            return BigDecimal.ZERO;
        }
        return giaThauMoiClickCpc.multiply(new BigDecimal(tongLuotClick));
    }
}
