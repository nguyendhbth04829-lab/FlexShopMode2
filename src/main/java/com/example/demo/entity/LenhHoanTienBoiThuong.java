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
@Table(name = "lenh_hoan_tien_boi_thuong")
public class LenhHoanTienBoiThuong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lenh")
    private Long maLenh;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu", referencedColumnName = "ma_phieu", nullable = false, unique = true)
    private PhieuKhieuNai phieuKhieuNai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_nhan_tien", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiNhanTien;

    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "ben_chiu_phi", nullable = false, length = 50)
    private String benChiuPhi; // NGUOI_BAN, SAN_FLEXSHOP, DON_VI_VAN_CHUYEN

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "DA_CHUYEN_TIEN";

    @Column(name = "ngay_thuc_hien")
    private LocalDateTime ngayThucHien = LocalDateTime.now();

    public String getBenChiuPhiDisplay() {
        if (benChiuPhi == null) return "Chưa xác định";
        switch (benChiuPhi) {
            case "NGUOI_BAN": return "Người bán (Shop chịu phí)";
            case "SAN_FLEXSHOP": return "Sàn FlexShop (Trợ cấp/Bảo hiểm)";
            case "DON_VI_VAN_CHUYEN": return "Đơn vị vận chuyển (Làm hỏng/mất)";
            default: return benChiuPhi;
        }
    }

    public String getBenChiuPhiBadgeClass() {
        if (benChiuPhi == null) return "bg-secondary";
        switch (benChiuPhi) {
            case "NGUOI_BAN": return "bg-danger text-white";
            case "SAN_FLEXSHOP": return "bg-primary text-white";
            case "DON_VI_VAN_CHUYEN": return "bg-warning text-dark";
            default: return "bg-secondary text-white";
        }
    }

    public String getTrangThaiDisplay() {
        if (trangThai == null) return "Chưa xử lý";
        switch (trangThai) {
            case "DA_CHUYEN_TIEN":
            case "HOAN_TAT": return "Đã giải ngân thành công";
            case "DANG_XU_LY": return "Đang giải ngân";
            case "THAT_BAI": return "Giải ngân thất bại";
            default: return trangThai;
        }
    }

    public String getTrangThaiBadgeClass() {
        if (trangThai == null) return "bg-secondary";
        switch (trangThai) {
            case "DA_CHUYEN_TIEN":
            case "HOAN_TAT": return "bg-success text-white";
            case "DANG_XU_LY": return "bg-warning text-dark";
            case "THAT_BAI": return "bg-danger text-white";
            default: return "bg-secondary text-white";
        }
    }
}
