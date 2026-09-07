package com.example.demo.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoSoGianHangDayDuResponse {

    private Long maGianHang;
    private Long maChuSoHuu;
    private String tenChuSoHuu;
    private String emailChuSoHuu;
    private String sdtChuSoHuu;

    private String tenGianHang;
    private String duongDanSlug;
    private String moTa;
    private String linkLogo;
    private String linkBanner;
    private String diaChiKho;
    private String sdtKho;

    private String trangThai; // CHO_DUYET, HOAT_DONG, TU_CHOI, TAM_KHOA
    private String tenTrangThai;
    private String lyDoTuChoi;
    private String hangGianHang;
    private String tenHangGianHang;

    private Integer diemSaoQuaTa;
    private BigDecimal diemDanhGiaTb;
    private Integer tongDanhGia;
    private Integer tongDonHang;
    private BigDecimal tyLePhanHoiChat;
    private LocalDateTime ngayTao;

    // Thống kê chứng chỉ
    @Builder.Default
    private long tongSoChungChi = 0;
    @Builder.Default
    private long soChungChiDaDuyet = 0;
    @Builder.Default
    private long soChungChiChoDuyet = 0;
    @Builder.Default
    private long soChungChiTuChoi = 0;

    // Danh sách chứng chỉ
    @Builder.Default
    private List<ChungChiGianHangResponse> danhSachChungChi = new ArrayList<>();

    public static String chuyenDoiTenTrangThaiShop(String trangThai) {
        if (trangThai == null) return "Chưa xác định";
        return switch (trangThai.toUpperCase()) {
            case "HOAT_DONG" -> "Đang hoạt động";
            case "CHO_DUYET" -> "Chờ duyệt";
            case "TU_CHOI" -> "Bị từ chối";
            case "TAM_KHOA" -> "Tạm khóa";
            default -> trangThai;
        };
    }

    public static String chuyenDoiTenHangShop(String hang) {
        if (hang == null) return "Chưa phân hạng";
        return switch (hang.toUpperCase()) {
            case "TIEM_NANG" -> "Shop Tiềm Năng";
            case "UY_TIN" -> "Shop Uy Tín";
            case "TOP_SELLER" -> "Top Seller";
            default -> hang;
        };
    }
}
