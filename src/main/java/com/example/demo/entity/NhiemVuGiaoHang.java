package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nhiem_vu_giao_hang")
public class NhiemVuGiaoHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nhiem_vu")
    private Long maNhiemVu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_don_hang_shop", referencedColumnName = "ma_don_hang_shop", nullable = false)
    private DonHangShop donHangShop;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_tai_xe", referencedColumnName = "ma_tai_xe", nullable = false)
    private TaiXeGiaoHang taiXe;

    @Column(name = "loai_nhiem_vu", length = 30)
    private String loaiNhiemVu = "GIAO_HANG";

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "THANH_CONG";

    @Column(name = "tien_cod_can_thu", precision = 18, scale = 2)
    private BigDecimal tienCodCanThu = BigDecimal.ZERO;

    @Column(name = "da_thu_cod")
    private Boolean daThuCod = false;

    @Column(name = "link_anh_bang_chung_pod", length = 500)
    private String linkAnhBangChungPod;

    @Column(name = "vi_do_giao_hang", precision = 10, scale = 7)
    private BigDecimal viDoGiaoHang;

    @Column(name = "kinh_do_giao_hang", precision = 10, scale = 7)
    private BigDecimal kinhDoGiaoHang;

    @Column(name = "ma_otp_xac_nhan", length = 10)
    private String maOtpXacNhan;

    @Column(name = "ly_do_that_bai", length = 255)
    private String lyDoThatBai;

    @Column(name = "so_lan_giao")
    private Integer soLanGiao = 1;

    @Column(name = "thoi_gian_hen_giao_lai")
    private LocalDateTime thoiGianHenGiaoLai;

    @Column(name = "thoi_gian_lay_hang")
    private LocalDateTime thoiGianLayHang;

    @Column(name = "thoi_gian_giao_thanh_cong")
    private LocalDateTime thoiGianGiaoThanhCong;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    public String getTrangThaiDisplay() {
        if (trangThai == null) return "Chưa cập nhật";
        switch (trangThai) {
            case "THANH_CONG": return "Giao hàng thành công";
            case "DANG_GIAO": return "Đang trên đường giao";
            case "CHO_LAY_HANG": return "Chờ lấy hàng từ Shop";
            case "THAT_BAI": return "Giao thất bại";
            case "CHUYEN_HOAN": return "Chuyển hoàn kho";
            default: return trangThai;
        }
    }
}
