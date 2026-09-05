package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể ánh xạ bảng theo_doi_gian_hang (Khách hàng Follow Shop)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "theo_doi_gian_hang")
@IdClass(TheoDoiGianHangId.class)
public class TheoDoiGianHang {

    @Id
    @Column(name = "ma_nguoi_dung")
    private Long maNguoiDung;

    @Id
    @Column(name = "ma_gian_hang")
    private Long maGianHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", insertable = false, updatable = false)
    private NguoiDung nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", insertable = false, updatable = false)
    private GianHang gianHang;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    public TheoDoiGianHang(Long maNguoiDung, Long maGianHang) {
        this.maNguoiDung = maNguoiDung;
        this.maGianHang = maGianHang;
        this.ngayTao = LocalDateTime.now();
    }
}
