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
@Table(name = "giao_dich_ky_quy")
public class GiaoDichKyQuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_ky_quy")
    private Long maKyQuy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_shop", nullable = false, unique = true)
    private DonHangShop donHangShop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @Column(name = "tong_tien_don_hang", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTienDonHang;

    @Column(name = "ty_le_phi_san_phan_tram", precision = 5, scale = 2)
    private BigDecimal tyLePhiSanPhanTram = new BigDecimal("3.00"); // 3% mặc định

    @Column(name = "tien_phi_san", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienPhiSan;

    @Column(name = "tien_phi_thanh_toan", precision = 18, scale = 2)
    private BigDecimal tienPhiThanhToan = BigDecimal.ZERO;

    @Column(name = "tien_phi_dich_vu", precision = 18, scale = 2)
    private BigDecimal tienPhiDichVu = BigDecimal.ZERO;

    @Column(name = "tien_thuc_nhan_ve_vi", nullable = false, precision = 18, scale = 2)
    private BigDecimal tienThucNhanVeVi;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "DANG_TAM_GIU"; // DANG_TAM_GIU (Đang giữ tiền 3 ngày), DA_GIAI_NGAN (Đã cộng vào ví), DA_HOAN_TIEN (Khiếu nại hoàn tiền)

    @Column(name = "ngay_du_kien_nha_tien", nullable = false)
    private LocalDateTime ngayDuKienNhaTien; // Thời điểm hết 3 ngày (Shopee Guarantee)

    @Column(name = "ngay_thuc_te_nha_tien")
    private LocalDateTime ngayThucTeNhaTien;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Transient
    public String getTenTrangThaiTiengViet() {
        if (trangThai == null) return "Chưa xác định";
        return switch (trangThai) {
            case "DANG_TAM_GIU" -> "Đang tạm giữ Escrow (3 ngày)";
            case "DA_GIAI_NGAN" -> "Đã giải ngân về ví";
            case "DA_HOAN_TIEN" -> "Đã hoàn tiền cho khách";
            default -> trangThai;
        };
    }

    @Transient
    public String getBadgeClass() {
        if (trangThai == null) return "bg-secondary text-white";
        return switch (trangThai) {
            case "DANG_TAM_GIU" -> "bg-warning text-dark";
            case "DA_GIAI_NGAN" -> "bg-success text-white";
            case "DA_HOAN_TIEN" -> "bg-danger text-white";
            default -> "bg-secondary text-white";
        };
    }

    @Transient
    public boolean isDuDieuKienTuDongGiaiNgan() {
        return "DANG_TAM_GIU".equals(trangThai) &&
               ngayDuKienNhaTien != null &&
               LocalDateTime.now().isAfter(ngayDuKienNhaTien);
    }
}
