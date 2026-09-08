package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chu_so_huu", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung chuSoHuu;


    @Column(name = "ten_gian_hang", nullable = false, unique = true, length = 100)

    private String tenGianHang;

    @Column(name = "duong_dan_slug", nullable = false, unique = true, length = 120)

    private String duongDanSlug;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")

    private String moTa;

    @Column(name = "link_logo", length = 500)
    private String linkLogo;

    @Column(name = "link_banner", length = 500)
    private String linkBanner;

    @Column(name = "dia_chi_kho", nullable = false, length = 255)

    private String diaChiKho;

    @Column(name = "sdt_kho", nullable = false, length = 20)

    private String sdtKho;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "CHO_DUYET";


    @Column(name = "ly_do_tu_choi", length = 255)
    private String lyDoTuChoi;

    @Column(name = "hang_gian_hang", length = 30)
    private String hangGianHang = "CHUAN";


    @Column(name = "diem_sao_qua_ta")
    private Integer diemSaoQuaTa = 0;

    @Column(name = "diem_danh_gia_tb", precision = 3, scale = 2)
    private BigDecimal diemDanhGiaTb = BigDecimal.ZERO;


    @Column(name = "tong_danh_gia")
    private Integer tongDanhGia = 0;

    @Column(name = "tong_don_hang")
    private Integer tongDonHang = 0;

    @Column(name = "ty_le_phan_hoi_chat", precision = 5, scale = 2)
    private BigDecimal tyLePhanHoiChat = new BigDecimal("100.00");


    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_xoa")
    private LocalDateTime ngayXoa;

    @Column(name = "ngay_tao")

    private LocalDateTime ngayTao = LocalDateTime.now();
}
