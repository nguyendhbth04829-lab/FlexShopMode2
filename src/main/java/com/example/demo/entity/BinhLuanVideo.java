package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Bình luận trên Video ngắn review (US-62)
 * Ánh xạ bảng binh_luan_video trong CSDL FlexShop_V2_Full
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "binh_luan_video")
public class BinhLuanVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_binh_luan")
    private Long maBinhLuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_video", nullable = false)
    private VideoNganReview video;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "ho_ten_nguoi_dung", length = 100)
    private String hoTenNguoiDung;

    @Column(name = "noi_dung", nullable = false, length = 1000)
    private String noiDung;

    @Column(name = "thoi_gian_gui")
    private LocalDateTime thoiGianGui = LocalDateTime.now();

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @PrePersist
    public void prePersist() {
        if (this.thoiGianGui == null) {
            this.thoiGianGui = LocalDateTime.now();
        }
        if (this.daXoa == null) {
            this.daXoa = false;
        }
    }
}
