package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dia_chi_nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChiNguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_dia_chi")
    private Long maDiaChi;

    @Column(name = "ma_nguoi_dung", nullable = false)
    private Long maNguoiDung;

    @Column(name = "ten_nguoi_nhan", length = 100, nullable = false)
    private String tenNguoiNhan;

    @Column(name = "so_dien_thoai", length = 20, nullable = false)
    private String soDienThoai;

    @Column(name = "tinh_thanh", length = 100, nullable = false)
    private String tinhThanh;

    @Column(name = "quan_huyen", length = 100, nullable = false)
    private String quanHuyen;

    @Column(name = "xa_phuong", length = 100, nullable = false)
    private String xaPhuong;

    @Column(name = "dia_chi_chi_tiet", length = 255, nullable = false)
    private String diaChiChiTiet;

    @Builder.Default
    @Column(name = "la_mac_dinh")
    private Boolean laMacDinh = false;

    @Builder.Default
    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Builder.Default
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();

    /**
     * Tiện ích: Ghép nối chuỗi địa chỉ đầy đủ theo chuẩn địa chính Việt Nam
     */
    public String getDiaChiDayDu() {
        return String.format("%s, %s, %s, %s", diaChiChiTiet, xaPhuong, quanHuyen, tinhThanh);
    }
}
