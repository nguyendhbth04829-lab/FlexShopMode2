package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "xac_thuc_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XacThucOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_otp")
    private Long maOtp;

    @Column(name = "nguoi_nhan", length = 150, nullable = false)
    private String nguoiNhan;

    @Column(name = "ma_xac_thuc", length = 10, nullable = false)
    private String maXacThuc;

    @Column(name = "loai_otp", length = 30, nullable = false)
    private String loaiOtp;

    @Column(name = "thoi_gian_het_han", nullable = false)
    private LocalDateTime thoiGianHetHan;

    @Builder.Default
    @Column(name = "da_su_dung")
    private Boolean daSuDung = false;

    @Builder.Default
    @Column(name = "so_lan_nhap_sai")
    private Integer soLanNhapSai = 0;

    @Column(name = "khoa_den_thoi_gian")
    private LocalDateTime khoaDenThoiGian;

    @Builder.Default
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    public boolean isHetHan() {
        return LocalDateTime.now().isAfter(this.thoiGianHetHan);
    }

    public boolean isDangBiKhoa() {
        return this.khoaDenThoiGian != null && LocalDateTime.now().isBefore(this.khoaDenThoiGian);
    }
}
