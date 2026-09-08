package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, BÁO CÁO & PHÂN TÍCH HIỆU SUẤT (DEV 5 - MINH)
 * USER STORY: US-58 - DTO Đóng Gói Dữ Liệu Dashboard Thống Kê & Báo Cáo
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeBaoCaoDTO {

    // 1. CÁC CHỈ SỐ TÀI CHÍNH CỐT LÕI (CORE FINANCIAL KPIS)
    @Builder.Default
    private BigDecimal tongGmv = BigDecimal.ZERO; // Tổng giá trị giao dịch gộp (Gross Merchandise Value)

    @Builder.Default
    private BigDecimal doanhThuSan = BigDecimal.ZERO; // Doanh thu phí sàn 3% thu được từ Ký quỹ (US-43)

    @Builder.Default
    private BigDecimal doanhThuShopThucNhan = BigDecimal.ZERO; // Tiền thực nhận của người bán (97%)

    @Builder.Default
    private BigDecimal tongTienDangGiuEscrow = BigDecimal.ZERO; // Tiền đang tạm giữ Escrow 3 ngày bảo lưu

    @Builder.Default
    private BigDecimal tongTienDaRut = BigDecimal.ZERO; // Tiền người bán đã rút về ngân hàng (US-44)

    // 2. CHỈ SỐ VẬN HÀNH & ĐƠN HÀNG (ORDER PERFORMANCE)
    @Builder.Default
    private Long tongSoDonHang = 0L;

    @Builder.Default
    private Long soDonThanhCong = 0L; // DA_GIAO / DA_THANH_TOAN

    @Builder.Default
    private Long soDonDangGiao = 0L; // DANG_GIAO / DANG_VAN_CHUYEN

    @Builder.Default
    private Long soDonChoXacNhan = 0L; // CHO_XAC_NHAN / PENDING

    @Builder.Default
    private Long soDonDaHuy = 0L; // DA_HUY / CANCELLED

    @Builder.Default
    private Double tyLeThanhCong = 0.0; // Tỷ lệ giao thành công (%)

    @Builder.Default
    private Double tyLeHuy = 0.0; // Tỷ lệ hủy đơn (%)

    // 3. DỮ LIỆU PHỤC VỤ BIỂU ĐỒ CHART.JS
    @Builder.Default
    private List<String> danhSachNgay = new ArrayList<>(); // Nhãn trục hoành: ["01/09", "02/09", ...]

    @Builder.Default
    private List<BigDecimal> danhSachGmvTheoNgay = new ArrayList<>(); // Dữ liệu GMV theo ngày

    @Builder.Default
    private List<BigDecimal> danhSachDoanhThuSanTheoNgay = new ArrayList<>(); // Dữ liệu phí sàn 3% theo ngày

    @Builder.Default
    private List<Long> danhSachSoDonTheoNgay = new ArrayList<>(); // Dữ liệu số đơn theo ngày

    // 4. TOP SẢN PHẨM BÁN CHẠY NHẤT
    @Builder.Default
    private List<TopSanPhamDTO> topSanPhamBanChay = new ArrayList<>();

    // 5. THÔNG TIN BỘ LỌC ĐANG ÁP DỤNG
    private String khoangThoiGian; // HOM_NAY, 7_NGAY_QUA, 30_NGAY_QUA, THANG_NAY, TUY_CHON
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private Long maGianHang;
    private String tenGianHang;

    /**
     * DTO con biểu diễn sản phẩm bán chạy
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopSanPhamDTO {
        private String tenSanPham;
        private String tenBienThe;
        private String maSku;
        private Long soLuongDaBan;
        private BigDecimal tongDoanhThu;
    }

    /**
     * DTO con biểu diễn doanh số gộp theo ngày
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoanhThuNgayRecord {
        private java.sql.Date ngay;
        private BigDecimal tongTien;
        private Long soLuongDon;
    }
}
