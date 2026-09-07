package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Lưu lượt thích (Thả tim) Video ngắn (US-62)
 * Ánh xạ bảng luot_thich_video trong CSDL FlexShop_V2_Full
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "luot_thich_video", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ma_video", "ma_nguoi_dung"})
})
public class LuotThichVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_thich")
    private Long maThich;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_video", nullable = false)
    private VideoNganReview video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "thoi_gian_thich")
    private LocalDateTime thoiGianThich = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.thoiGianThich == null) {
            this.thoiGianThich = LocalDateTime.now();
        }
    }
}
