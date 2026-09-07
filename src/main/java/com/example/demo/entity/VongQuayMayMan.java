package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Lịch Sử Lượt Quay Vòng Quay May Mắn (Shopee Lucky Wheel - US-66)
 * Quản lý lượt quay số, trả thưởng tức thì vào ví Xu hoặc Kho Voucher cá nhân
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vong_quay_may_man")
public class VongQuayMayMan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_luot_quay")
    private Long maLuotQuay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "phan_thuong", nullable = false, length = 150)
    private String phanThuong;

    /**
     * Phân loại phần thưởng:
     * - XU: Cộng thưởng tức thì vào ví Xu
     * - VOUCHER: Lưu mã giảm giá vào Kho Voucher
     * - MAY_MAN_LAN_SAU: Hộp quà khích lệ / Chúc may mắn lần sau
     */
    @Column(name = "loai_phan_thuong", nullable = false, length = 30)
    private String loaiPhanThuong = "XU";

    @Column(name = "so_xu_nhan", nullable = false)
    private Long soXuNhan = 0L;

    @Column(name = "ma_voucher")
    private Long maVoucher;

    @Column(name = "ma_code_voucher", length = 50)
    private String maCodeVoucher;

    @Column(name = "trang_thai_tra_thuong", nullable = false, length = 50)
    private String trangThaiTraThuong = "Đã cộng thưởng";

    @Column(name = "goc_quay_do")
    private Integer gocQuayDo = 0;

    @Column(name = "mo_ta_ket_qua", length = 255)
    private String moTaKetQua;

    @Column(name = "ngay_quay")
    private LocalDateTime ngayQuay = LocalDateTime.now();

    public boolean isNhanXu() {
        return "XU".equalsIgnoreCase(this.loaiPhanThuong) && this.soXuNhan != null && this.soXuNhan > 0;
    }

    public boolean isNhanVoucher() {
        return "VOUCHER".equalsIgnoreCase(this.loaiPhanThuong) && this.maCodeVoucher != null && !this.maCodeVoucher.trim().isEmpty();
    }
}
