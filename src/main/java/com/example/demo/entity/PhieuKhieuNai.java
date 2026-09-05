package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "phieu_khieu_nai")
public class PhieuKhieuNai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phieu")
    private Long maPhieu;

    @Column(name = "ma_code_phieu", nullable = false, unique = true, length = 50)
    private String maCodePhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khach_hang", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_shop", referencedColumnName = "ma_don_hang_shop", nullable = false)
    private DonHangShop donHangShop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @Column(name = "loai_khieu_nai", nullable = false, length = 50)
    private String loaiKhieuNai; // HONG_VO, GIAO_SAI, HANG_GIA, THIEU_HANG, KHAC

    @Column(name = "muc_do_uu_tien", length = 20)
    private String mucDoUuTien = "TRUNG_BINH"; // THAP, TRUNG_BINH, CAO, KHAN_CAP

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "MO_MOI"; // MO_MOI, DANG_XU_LY, CHO_SHOP_PHAN_HOI, CHAP_NHAN_HOAN_TIEN, TU_CHOI_KHIEU_NAI, DA_HUY, DONG_PHIEU

    @Column(name = "noi_dung_mo_ta", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDungMoTa;

    @Column(name = "giai_phap_yeu_cau", nullable = false, length = 50)
    private String giaiPhapYeuCau; // HOAN_TIEN_TRA_HANG, HOAN_TIEN_KHONG_TRA, DOI_HANG

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cskh_xu_ly", referencedColumnName = "ma_nguoi_dung")
    private NguoiDung cskhXuLy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_phan_quyet", referencedColumnName = "ma_nguoi_dung")
    private NguoiDung nguoiPhanQuyet;

    @Column(name = "ghi_chu_phan_quyet", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChuPhanQuyet;

    @Column(name = "so_tien_hoan_tra", precision = 18, scale = 2)
    private BigDecimal soTienHoanTra = BigDecimal.ZERO;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @ToString.Exclude
    @OneToMany(mappedBy = "phieuKhieuNai", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BangChungKhieuNai> danhSachBangChung = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "phieuKhieuNai", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GhiChuNoiBoKhieuNai> danhSachGhiChuNoiBo = new ArrayList<>();

    @ToString.Exclude
    @OneToOne(mappedBy = "phieuKhieuNai", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LenhHoanTienBoiThuong lenhHoanTien;

    // Tiện ích hiển thị giao diện (View Helpers)
    public String getMucDoUuTienDisplay() {
        if (mucDoUuTien == null) return "Trung bình";
        switch (mucDoUuTien) {
            case "THAP": return "Thấp";
            case "TRUNG_BINH": return "Trung bình";
            case "CAO": return "Cao";
            case "KHAN_CAP": return "Khẩn cấp";
            default: return mucDoUuTien;
        }
    }

    public String getMucDoUuTienBadgeClass() {
        if (mucDoUuTien == null) return "bg-secondary text-white";
        switch (mucDoUuTien) {
            case "THAP": return "bg-secondary-subtle text-secondary border";
            case "TRUNG_BINH": return "bg-info-subtle text-info-emphasis border border-info-subtle";
            case "CAO": return "bg-warning-subtle text-warning-emphasis border border-warning-subtle";
            case "KHAN_CAP": return "bg-danger text-white";
            default: return "bg-secondary text-white";
        }
    }

    public String getTenLoaiKhieuNaiDisplay() {
        if (loaiKhieuNai == null) return "Không xác định";
        switch (loaiKhieuNai) {
            case "HONG_VO": return "Hàng bị hỏng vỡ / Móp méo";
            case "GIAO_SAI": return "Giao sai mẫu mã / Kích thước";
            case "HANG_GIA": return "Hàng giả / Nhái / Kém chất lượng";
            case "THIEU_HANG": return "Giao thiếu sản phẩm / Phụ kiện";
            case "HET_HAN": return "Sản phẩm hết hạn sử dụng";
            case "KHAC": return "Lý do khác";
            default: return loaiKhieuNai;
        }
    }

    public String getTenGiaiPhapDisplay() {
        if (giaiPhapYeuCau == null) return "Không xác định";
        switch (giaiPhapYeuCau) {
            case "HOAN_TIEN_TRA_HANG": return "Trả hàng & Hoàn tiền";
            case "HOAN_TIEN_KHONG_TRA": return "Hoàn tiền (Không cần trả hàng)";
            case "DOI_HANG": return "Đổi sản phẩm mới";
            default: return giaiPhapYeuCau;
        }
    }

    public String getTrangThaiDisplay() {
        if (trangThai == null) return "Chưa xác định";
        switch (trangThai) {
            case "MO_MOI": return "Mở mới";
            case "DANG_XU_LY": return "CSKH đang xử lý";
            case "CHO_SHOP_PHAN_HOI": return "Chờ Shop phản hồi";
            case "CHAP_NHAN_HOAN_TIEN": return "Đã chấp thuận hoàn tiền";
            case "BOI_THUONG_SHOP": return "Đã bồi thường Shop";
            case "TU_CHOI_KHIEU_NAI": return "Từ chối khiếu nại";
            case "DA_HUY": return "Khách đã hủy";
            case "DONG_PHIEU": return "Đã đóng";
            default: return trangThai;
        }
    }

    public String getBadgeClass() {
        if (trangThai == null) return "bg-secondary";
        switch (trangThai) {
            case "MO_MOI": return "bg-primary";
            case "DANG_XU_LY": return "bg-warning text-dark";
            case "CHO_SHOP_PHAN_HOI": return "bg-info text-dark";
            case "CHAP_NHAN_HOAN_TIEN": return "bg-success";
            case "BOI_THUONG_SHOP": return "bg-purple text-white";
            case "TU_CHOI_KHIEU_NAI": return "bg-danger";
            case "DA_HUY": return "bg-secondary";
            case "DONG_PHIEU": return "bg-dark";
            default: return "bg-secondary";
        }
    }
}
