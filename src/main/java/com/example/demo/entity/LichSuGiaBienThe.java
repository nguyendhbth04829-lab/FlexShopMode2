package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_gia_bien_the")
public class LichSuGiaBienThe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lich_su")
    private Long maLichSu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_bien_the", nullable = false)
    private BienTheSanPham bienThe;

    @Column(name = "gia_cu", nullable = false)
    private BigDecimal giaCu;

    @Column(name = "gia_moi", nullable = false)
    private BigDecimal giaMoi;

    @Column(name = "ngay_thay_doi")
    private LocalDateTime ngayThayDoi = LocalDateTime.now();

    @Column(name = "nguoi_thay_doi")
    private Long nguoiThayDoi;

    public Long getMaLichSu() { return maLichSu; }
    public void setMaLichSu(Long maLichSu) { this.maLichSu = maLichSu; }

    public BienTheSanPham getBienThe() { return bienThe; }
    public void setBienThe(BienTheSanPham bienThe) { this.bienThe = bienThe; }

    public BigDecimal getGiaCu() { return giaCu; }
    public void setGiaCu(BigDecimal giaCu) { this.giaCu = giaCu; }

    public BigDecimal getGiaMoi() { return giaMoi; }
    public void setGiaMoi(BigDecimal giaMoi) { this.giaMoi = giaMoi; }

    public LocalDateTime getNgayThayDoi() { return ngayThayDoi; }
    public void setNgayThayDoi(LocalDateTime ngayThayDoi) { this.ngayThayDoi = ngayThayDoi; }

    public Long getNguoiThayDoi() { return nguoiThayDoi; }
    public void setNguoiThayDoi(Long nguoiThayDoi) { this.nguoiThayDoi = nguoiThayDoi; }
}
