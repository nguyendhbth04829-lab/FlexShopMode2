package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Cấu Hình 8 Ô Phần Thưởng Vòng Quay May Mắn (Shopee Lucky Wheel - US-66)
 * Quản lý tỷ lệ xác suất trúng thưởng (%), số lượng giới hạn, màu sắc ô và icon hiển thị
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cau_hinh_phan_thuong_vong_quay")
public class PhanThuongVongQuay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phan_thuong")
    private Long maPhanThuong;

    @Column(name = "ten_phan_thuong", nullable = false, length = 150)
    private String tenPhanThuong;

    @Column(name = "loai_phan_thuong", nullable = false, length = 30)
    private String loaiPhanThuong; // XU, VOUCHER, MAY_MAN_LAN_SAU

    @Column(name = "gia_tri_xu", nullable = false)
    private Long giaTriXu = 0L;

    @Column(name = "ma_voucher")
    private Long maVoucher;

    @Column(name = "ty_le_trung", nullable = false)
    private Double tyLeTrung = 12.50; // Phần trăm xác suất trúng (Tổng = 100%)

    @Column(name = "so_luong_gioi_han", nullable = false)
    private Integer soLuongGioiHan = 9999;

    @Column(name = "so_luong_da_trung", nullable = false)
    private Integer soLuongDaTrung = 0;

    @Column(name = "mau_sac_o", nullable = false, length = 20)
    private String mauSacO = "#FF5722";

    @Column(name = "mau_chu", nullable = false, length = 20)
    private String mauChu = "#FFFFFF";

    @Column(name = "icon", nullable = false, length = 50)
    private String icon = "bi-coin";

    @Column(name = "thu_tu_o", nullable = false)
    private Integer thuTuO; // Vị trí từ 1 đến 8 trên vòng quay

    @Column(name = "dang_hoat_dong", nullable = false)
    private Boolean dangHoatDong = true;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    public boolean conPhanThuong() {
        return Boolean.TRUE.equals(dangHoatDong) && (soLuongGioiHan == null || soLuongDaTrung < soLuongGioiHan);
    }
}
