package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Ví Xu Người Dùng (US-55 - Coin Reward System)
 * Quản lý số dư xu hiện tại, tổng xu tích lũy và khóa lạc quan chống Race Condition
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vi_xu_nguoi_dung")
public class ViXuNguoiDung {

    @Id
    @Column(name = "ma_nguoi_dung")
    private Long maNguoiDung;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ma_nguoi_dung")
    private NguoiDung nguoiDung;

    @Column(name = "so_xu_hien_tai")
    private Long soXuHienTai = 0L;

    @Column(name = "tong_xu_da_tich_luy")
    private Long tongXuDaTichLuy = 0L;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    @Version
    @Column(name = "phien_ban_lock")
    private Integer phienBanLock = 1;

    /**
     * Quy đổi 1 Xu = 1 VNĐ
     */
    public Long getGiaTriQuyDoiVnd() {
        return soXuHienTai != null ? soXuHienTai : 0L;
    }
}
