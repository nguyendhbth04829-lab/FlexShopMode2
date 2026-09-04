package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lenh_hoan_tien_boi_thuong")
public class LenhHoanTienBoiThuong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lenh")
    private Long maLenh;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu", referencedColumnName = "ma_phieu", nullable = false, unique = true)
    private PhieuKhieuNai phieuKhieuNai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_nhan_tien", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiNhanTien;

    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "ben_chiu_phi", nullable = false, length = 50)
    private String benChiuPhi; // NGUOI_BAN, SAN_FLEXSHOP, DON_VI_VAN_CHUYEN

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "DA_CHUYEN_TIEN";

    @Column(name = "ngay_thuc_hien")
    private LocalDateTime ngayThucHien = LocalDateTime.now();
}
