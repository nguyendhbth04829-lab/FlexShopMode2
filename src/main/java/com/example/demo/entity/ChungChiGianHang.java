package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chung_chi_gian_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChungChiGianHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_chung_chi")
    private Long maChungChi;

    @Column(name = "ma_gian_hang", nullable = false)
    private Long maGianHang;

    @Column(name = "loai_giay_to", length = 100, nullable = false)
    private String loaiGiayTo; // GIAY_PHEP_KINH_DOANH, MA_SO_THUE, CAN_CUOC_CONG_DAN

    @Column(name = "so_giay_to", length = 100, nullable = false)
    private String soGiayTo;

    @Column(name = "link_anh_giay_to", length = 500, nullable = false)
    private String linkAnhGiayTo;

    @Builder.Default
    @Column(name = "trang_thai_duyet", length = 30)
    private String trangThaiDuyet = "CHO_DUYET"; // CHO_DUYET, DA_DUYET, TU_CHOI

    @Builder.Default
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();
}
