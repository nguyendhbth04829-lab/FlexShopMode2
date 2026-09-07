package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Video ngắn review sản phẩm (Shopee Video / TikTok Reels - US-62)
 * Ánh xạ bảng video_ngan_review trong CSDL FlexShop_V2_Full
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "video_ngan_review")
public class VideoNganReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_video")
    private Long maVideo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nguoi_dang", nullable = false)
    private NguoiDung nguoiDang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_san_pham_gan_kem", nullable = false)
    private SanPham sanPhamGanKem;

    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;

    @Column(name = "link_video", nullable = false, length = 500)
    private String linkVideo;

    @Column(name = "link_anh_bia", length = 500)
    private String linkAnhBia;

    @Column(name = "mo_ta", length = 1000)
    private String moTa;

    @Column(name = "hashtag", length = 255)
    private String hashtag;

    @Column(name = "thoi_luong_giay")
    private Integer thoiLuongGiay = 30;

    @Column(name = "tong_luot_tim")
    private Integer tongLuotTim = 0;

    @Column(name = "tong_luot_xem")
    private Integer tongLuotXem = 0;

    @Column(name = "tong_luot_binh_luan")
    private Integer tongLuotBinhLuan = 0;

    @Column(name = "tong_luot_chia_se")
    private Integer tongLuotChiaSe = 0;

    @Column(name = "trang_thai", length = 50)
    private String trangThai = "HOAT_DONG";

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_dang")
    private LocalDateTime ngayDang = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.ngayDang == null) {
            this.ngayDang = LocalDateTime.now();
        }
        if (this.tongLuotTim == null) this.tongLuotTim = 0;
        if (this.tongLuotXem == null) this.tongLuotXem = 0;
        if (this.tongLuotBinhLuan == null) this.tongLuotBinhLuan = 0;
        if (this.tongLuotChiaSe == null) this.tongLuotChiaSe = 0;
        if (this.thoiLuongGiay == null) this.thoiLuongGiay = 30;
        if (this.trangThai == null) this.trangThai = "HOAT_DONG";
        if (this.daXoa == null) this.daXoa = false;
    }
}
