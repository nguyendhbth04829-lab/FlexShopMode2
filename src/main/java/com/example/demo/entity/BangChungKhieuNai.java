package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bang_chung_khieu_nai")
public class BangChungKhieuNai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bang_chung")
    private Long maBangChung;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu", referencedColumnName = "ma_phieu", nullable = false)
    private PhieuKhieuNai phieuKhieuNai;

    @Column(name = "link_tep_tin", nullable = false, length = 500)
    private String linkTepTin;

    @Column(name = "loai_tep_tin", length = 20)
    private String loaiTepTin = "HINH_ANH"; // HINH_ANH hoặc VIDEO

    @Column(name = "vai_tro_tai_len", nullable = false, length = 30)
    private String vaiTroTaiLen = "KHACH_HANG"; // KHACH_HANG, NGUOI_BAN, CSKH

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();
}
