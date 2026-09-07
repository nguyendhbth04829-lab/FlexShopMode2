package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gian_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GianHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_gian_hang")
    private Long maGianHang;

    @Column(name = "ma_chu_so_huu", nullable = false)
    private Long maChuSoHuu;

    @Column(name = "ten_gian_hang", length = 100, nullable = false, unique = true)
    private String tenGianHang;

    @Column(name = "duong_dan_slug", length = 120, nullable = false, unique = true)
    private String duongDanSlug;

    @Column(name = "mo_ta", columnDefinition = "nvarchar(max)")
    private String moTa;

    @Column(name = "link_logo", length = 500)
    private String linkLogo;

    @Column(name = "link_banner", length = 500)
    private String linkBanner;

    @Column(name = "dia_chi_kho", length = 255, nullable = false)
    private String diaChiKho;

    @Column(name = "sdt_kho", length = 20, nullable = false)
    private String sdtKho;

    @Builder.Default
    @Column(name = "gio_mo_cua", length = 10)
    private String gioMoCua = "08:00";

    @Builder.Default
    @Column(name = "gio_dong_cua", length = 10)
    private String gioDongCua = "22:00";

    @Builder.Default
    @Column(name = "dang_mo_cua")
    private Boolean dangMoCua = true;

    @Column(name = "ghi_chu_kho", length = 255)
    private String ghiChuKho;

    @Column(name = "nguoi_lien_he_kho", length = 100)
    private String nguoiLienHeKho;

    @Builder.Default
    @Column(name = "trang_thai", length = 30)
    private String trangThai = "CHO_DUYET"; // CHO_DUYET, HOAT_DONG, TU_CHOI, TAM_KHOA

    @Column(name = "ly_do_tu_choi", length = 255)
    private String lyDoTuChoi;

    @Builder.Default
    @Column(name = "hang_gian_hang", length = 30)
    private String hangGianHang = "TIEM_NANG";

    @Builder.Default
    @Column(name = "diem_sao_qua_ta")
    private Integer diemSaoQuaTa = 0;

    @Builder.Default
    @Column(name = "diem_danh_gia_tb", precision = 3, scale = 1)
    private BigDecimal diemDanhGiaTb = BigDecimal.valueOf(0.0);

    @Builder.Default
    @Column(name = "tong_danh_gia")
    private Integer tongDanhGia = 0;

    @Builder.Default
    @Column(name = "tong_don_hang")
    private Integer tongDonHang = 0;

    @Builder.Default
    @Column(name = "ty_le_phan_hoi_chat", precision = 5, scale = 2)
    private BigDecimal tyLePhanHoiChat = BigDecimal.valueOf(100.00);

    @Builder.Default
    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_xoa")
    private LocalDateTime ngayXoa;

    @Builder.Default
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();
}
