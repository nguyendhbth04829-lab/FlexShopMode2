package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_trang_thai_don")
public class LichSuTrangThaiDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lich_su")
    private Long maLichSu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_shop", referencedColumnName = "ma_don_hang_shop", nullable = false)
    private DonHangShop donHangShop;

    @Column(name = "trang_thai_cu", length = 50)
    private String trangThaiCu;

    @Column(name = "trang_thai_moi", nullable = false, length = 50)
    private String trangThaiMoi;

    @Column(name = "nguoi_thuc_hien", nullable = false, length = 100)
    private String nguoiThucHien;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian = LocalDateTime.now();
}
