package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Lịch Sử Giao Dịch Xu (US-55 - Coin Reward System)
 * Ghi vết toàn bộ biến động cộng xu, trừ xu, điểm danh, hoàn xu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_giao_dich_xu")
public class LichSuGiaoDichXu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_giao_dich_xu")
    private Long maGiaoDichXu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "so_xu_thay_doi", nullable = false)
    private Long soXuThayDoi;

    @Column(name = "loai_giao_dich", nullable = false, length = 50)
    private String loaiGiaoDich; // TICH_XU_DON_HANG, TRU_XU_DON_HANG, DIEM_DANH_HANG_NGAY, HOAN_XU_HUY_DON

    @Column(name = "ma_tham_chieu", length = 100)
    private String maThamChieu;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "so_xu_sau_giao_dich")
    private Long soXuSauGiaoDich;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    public boolean isCongXu() {
        return soXuThayDoi != null && soXuThayDoi > 0;
    }

    public String getNhanLoaiGiaoDich() {
        if (loaiGiaoDich == null) return "Giao dịch xu";
        return switch (loaiGiaoDich) {
            case "TICH_XU_DON_HANG" -> "Tích xu đơn hàng";
            case "TRU_XU_DON_HANG" -> "Dùng xu mua hàng";
            case "DIEM_DANH_HANG_NGAY" -> "Điểm danh hàng ngày";
            case "HOAN_XU_HUY_DON" -> "Hoàn xu hủy đơn";
            default -> loaiGiaoDich;
        };
    }
}
