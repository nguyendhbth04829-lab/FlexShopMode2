package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "danh_gia_san_pham")
public class DanhGiaSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_danh_gia")
    private Long maDanhGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_chi_tiet_don", referencedColumnName = "ma_chi_tiet_don", nullable = false)
    private ChiTietDonHang chiTietDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", referencedColumnName = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", referencedColumnName = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_dung", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "so_sao", nullable = false)
    private Integer soSao;

    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "an_danh")
    private Boolean anDanh = false;

    @Column(name = "bi_an")
    private Boolean biAn = false;

    @Column(name = "phan_hoi_cua_shop", columnDefinition = "NVARCHAR(MAX)")
    private String phanHoiCuaShop;

    @Column(name = "ngay_shop_phan_hoi")
    private LocalDateTime ngayShopPhanHoi;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @ToString.Exclude
    @OneToMany(mappedBy = "danhGiaSanPham", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HinhAnhDanhGia> danhSachHinhAnh = new ArrayList<>();

    // Helper: Tên hiển thị người đánh giá (Hỗ trợ ẩn danh)
    public String getTenNguoiDanhGiaDisplay() {
        if (Boolean.TRUE.equals(anDanh)) {
            if (nguoiDung != null && nguoiDung.getHoVaTen() != null && !nguoiDung.getHoVaTen().isBlank()) {
                String ten = nguoiDung.getHoVaTen().trim();
                if (ten.length() <= 2) {
                    return ten.charAt(0) + "*** (Ẩn danh)";
                }
                return ten.charAt(0) + "***" + ten.charAt(ten.length() - 1) + " (Ẩn danh)";
            }
            return "Khách hàng ẩn danh";
        }
        return nguoiDung != null ? nguoiDung.getHoVaTen() : "Khách hàng FlexShop";
    }

    // Helper: Mô tả mức độ hài lòng
    public String getMucDoHaiLongDisplay() {
        if (soSao == null) return "Chưa đánh giá";
        return switch (soSao) {
            case 5 -> "Cực kỳ hài lòng";
            case 4 -> "Hài lòng";
            case 3 -> "Bình thường";
            case 2 -> "Không hài lòng";
            case 1 -> "Rất tệ";
            default -> soSao + " Sao";
        };
    }
}
