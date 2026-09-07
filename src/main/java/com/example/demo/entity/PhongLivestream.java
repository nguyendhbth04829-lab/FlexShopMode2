package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Phòng Livestream bán hàng (FlexShop Live US-61)
 * Ánh xạ bảng phong_livestream trong CSDL FlexShop_V2_Full.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "phong_livestream")
public class PhongLivestream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_live")
    private Long maLive;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @Column(name = "tieu_de", nullable = false, length = 510)
    private String tieuDe;

    @Column(name = "link_stream_rtmp", nullable = false, length = 1000)
    private String linkStreamRtmp;

    @Column(name = "link_anh_bia", length = 1000)
    private String linkAnhBia;

    @Column(name = "tong_luot_xem")
    @Builder.Default
    private Integer tongLuotXem = 0;

    @Column(name = "so_nguoi_xem_hien_tai")
    @Builder.Default
    private Integer soNguoiXemHienTai = 0;

    @Column(name = "so_luot_thich")
    @Builder.Default
    private Integer soLuotThich = 0;

    /**
     * Trạng thái phòng live:
     * - DANG_LIVE: Đang phát trực tiếp
     * - SAP_DIEN_RA: Lên lịch sắp phát sóng
     * - DA_KET_THUC: Đã kết thúc
     */
    @Column(name = "trang_thai", length = 60)
    @Builder.Default
    private String trangThai = "DANG_LIVE";

    @Column(name = "thoi_gian_bat_dau")
    private LocalDateTime thoiGianBatDau;

    @Column(name = "thoi_gian_ket_thuc")
    private LocalDateTime thoiGianKetThuc;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "ngay_tao")
    @Builder.Default
    private LocalDateTime ngayTao = LocalDateTime.now();

    public boolean isDangLive() {
        return "DANG_LIVE".equalsIgnoreCase(this.trangThai);
    }

    public boolean isSapDienRa() {
        return "SAP_DIEN_RA".equalsIgnoreCase(this.trangThai);
    }

    public boolean isDaKetThuc() {
        return "DA_KET_THUC".equalsIgnoreCase(this.trangThai);
    }

    public String getTrangThaiHienThi() {
        if (this.trangThai == null) return "Chưa xác định";
        switch (this.trangThai) {
            case "DANG_LIVE":
                return "ĐANG LIVE";
            case "SAP_DIEN_RA":
                return "Sắp diễn ra";
            case "DA_KET_THUC":
                return "Đã kết thúc";
            default:
                return this.trangThai;
        }
    }

    public String getBadgeClass() {
        if (this.trangThai == null) return "bg-secondary";
        switch (this.trangThai) {
            case "DANG_LIVE":
                return "bg-danger text-white pulse-live";
            case "SAP_DIEN_RA":
                return "bg-warning text-dark";
            case "DA_KET_THUC":
                return "bg-secondary text-white";
            default:
                return "bg-secondary";
        }
    }
}
