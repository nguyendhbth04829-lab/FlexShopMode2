package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Bình luận / Tin nhắn tương tác trong phòng Livestream (US-61)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "binh_luan_livestream")
public class BinhLuanLivestream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_binh_luan")
    private Long maBinhLuan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_live", nullable = false)
    private PhongLivestream phongLivestream;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "ho_ten_nguoi_dung", nullable = false, length = 100)
    private String hoTenNguoiDung;

    @Column(name = "noi_dung", nullable = false, length = 500)
    private String noiDung;

    @Column(name = "thoi_gian_gui")
    @Builder.Default
    private LocalDateTime thoiGianGui = LocalDateTime.now();

    @Column(name = "la_tin_he_thong")
    @Builder.Default
    private Boolean laTinHeThong = false;

    @Column(name = "la_nguoi_ban")
    @Builder.Default
    private Boolean laNguoiBan = false;
}
