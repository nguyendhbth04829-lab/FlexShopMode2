package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tai_xe_giao_hang")
public class TaiXeGiaoHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_tai_xe")
    private Long maTaiXe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nguoi_dung", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "loai_phuong_tien", length = 50)
    private String loaiPhuongTien;

    @Column(name = "bien_so_xe", nullable = false, length = 30)
    private String bienSoXe;

    @Column(name = "dang_truc_tuyen")
    private Boolean dangTrucTuyen = true;

    @Column(name = "vi_do_hien_tai", precision = 10, scale = 7)
    private BigDecimal viDoHienTai;

    @Column(name = "kinh_do_hien_tai", precision = 10, scale = 7)
    private BigDecimal kinhDoHienTai;

    @Column(name = "so_du_cod_dang_giu", precision = 18, scale = 2)
    private BigDecimal soDuCodDangGiu = BigDecimal.ZERO;

    @Column(name = "diem_danh_gia_tb", precision = 3, scale = 2)
    private BigDecimal diemDanhGiaTb = new BigDecimal("5.0");

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "DANG_HOAT_DONG";
}
