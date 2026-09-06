package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "gian_hang")
public class GianHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_gian_hang")
    private Long maGianHang;

    @Column(name = "ma_chu_so_huu", nullable = false)
    private Long maChuSoHuu;

    @Column(name = "ten_gian_hang", nullable = false, length = 100, unique = true)
    private String tenGianHang;

    @Column(name = "diem_sao_qua_ta")
    private Integer diemSaoQuaTa = 0;

    @Column(name = "hang_gian_hang", length = 30)
    private String hangGianHang = "CHUAN";

    // ... other fields omitted for brevity

    public Long getMaGianHang() { return maGianHang; }
    public void setMaGianHang(Long maGianHang) { this.maGianHang = maGianHang; }

    public Long getMaChuSoHuu() { return maChuSoHuu; }
    public void setMaChuSoHuu(Long maChuSoHuu) { this.maChuSoHuu = maChuSoHuu; }

    public String getTenGianHang() { return tenGianHang; }
    public void setTenGianHang(String tenGianHang) { this.tenGianHang = tenGianHang; }

    public Integer getDiemSaoQuaTa() { return diemSaoQuaTa; }
    public void setDiemSaoQuaTa(Integer diemSaoQuaTa) { this.diemSaoQuaTa = diemSaoQuaTa; }

    public String getHangGianHang() { return hangGianHang; }
    public void setHangGianHang(String hangGianHang) { this.hangGianHang = hangGianHang; }
}
