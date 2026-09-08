package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - Thực thể Tiếp Thị Liên Kết (Affiliate Marketing / KOC)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tiep_thi_lien_ket")
public class TiepThiLienKet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_affiliate")
    private Long maAffiliate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_koc", nullable = false)
    private NguoiDung koc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "ma_link_affiliate", nullable = false, unique = true, length = 100)
    private String maLinkAffiliate;

    @Column(name = "ty_le_hoa_hong_phan_tram", nullable = false, precision = 4, scale = 2)
    private BigDecimal tyLeHoaHongPhanTram;

    @Column(name = "tong_hoa_hong_kiem_duoc", precision = 18, scale = 2)
    private BigDecimal tongHoaHongKiemDuoc = BigDecimal.ZERO;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @OneToMany(mappedBy = "tiepThiLienKet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DonHangTiepThi> danhSachDonHang = new ArrayList<>();
}
