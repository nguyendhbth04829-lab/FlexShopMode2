package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_thiet_lap_shop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuThietLapShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lich_su")
    private Long maLichSu;

    @Column(name = "ma_gian_hang", nullable = false)
    private Long maGianHang;

    @Column(name = "loai_thay_doi", length = 50, nullable = false)
    private String loaiThayDoi; // THIET_LAP_TONG_THE, DOI_LOGO, DOI_BANNER, BAT_TAT_NHAN_DON, DOI_GIO_HOAT_DONG, DOI_DIA_CHI_KHO

    @Column(name = "noi_dung_thay_doi", length = 500, nullable = false)
    private String noiDungThayDoi;

    @Column(name = "nguoi_thuc_hien", length = 100, nullable = false)
    private String nguoiThucHien;

    @Builder.Default
    @Column(name = "thoi_gian", updatable = false)
    private LocalDateTime thoiGian = LocalDateTime.now();
}
