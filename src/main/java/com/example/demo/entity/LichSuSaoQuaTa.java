package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_sao_qua_ta")
public class LichSuSaoQuaTa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phat")
    private Long maPhat;

    @Column(name = "ma_gian_hang", nullable = false)
    private Long maGianHang;

    @Column(name = "so_diem_phat", nullable = false)
    private Integer soDiemPhat;

    @Column(name = "ly_do", nullable = false, length = 255)
    private String lyDo;

    @Column(name = "loai_vi_pham", nullable = false, length = 50)
    private String loaiViPham;

    @Column(name = "nguoi_phat_admin")
    private Long nguoiPhatAdmin;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    public Long getMaPhat() { return maPhat; }
    public void setMaPhat(Long maPhat) { this.maPhat = maPhat; }

    public Long getMaGianHang() { return maGianHang; }
    public void setMaGianHang(Long maGianHang) { this.maGianHang = maGianHang; }

    public Integer getSoDiemPhat() { return soDiemPhat; }
    public void setSoDiemPhat(Integer soDiemPhat) { this.soDiemPhat = soDiemPhat; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public String getLoaiViPham() { return loaiViPham; }
    public void setLoaiViPham(String loaiViPham) { this.loaiViPham = loaiViPham; }

    public Long getNguoiPhatAdmin() { return nguoiPhatAdmin; }
    public void setNguoiPhatAdmin(Long nguoiPhatAdmin) { this.nguoiPhatAdmin = nguoiPhatAdmin; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
