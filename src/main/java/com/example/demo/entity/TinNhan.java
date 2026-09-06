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
@Table(name = "tin_nhan")
public class TinNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tin_nhan")
    private Long maTinNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cuoc_tro_chuyen", referencedColumnName = "ma_cuoc_tro_chuyen", nullable = false)
    private CuocTroChuyen cuocTroChuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_de_xuat")
    private DeXuatTraGia deXuatTraGia;

    @Column(name = "loai_nguoi_gui", nullable = false, length = 20)
    private String loaiNguoiGui; // 'KHACH_HANG', 'SHOP', 'HE_THONG'

    @Column(name = "ma_nguoi_gui", nullable = false)
    private Long maNguoiGui;

    @Column(name = "loai_tin_nhan", length = 30)
    private String loaiTinNhan = "VAN_BAN"; // 'VAN_BAN', 'THE_SAN_PHAM', 'DE_XUAT_TRA_GIA', 'HE_THONG'

    @Column(name = "noi_dung", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "du_lieu_dinh_kem_json", columnDefinition = "NVARCHAR(MAX)")
    private String duLieuDinhKemJson;

    @Column(name = "da_xem")
    private Boolean daXem = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();
}
