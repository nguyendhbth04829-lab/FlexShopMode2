package com.example.demo.service;

import com.example.demo.dto.ThongKeBaoCaoDTO;
import com.example.demo.entity.GianHang;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, BÁO CÁO & PHÂN TÍCH HIỆU SUẤT (DEV 5 - MINH)
 * USER STORY: US-58 - Service Tính Toán Thống Kê & Báo Cáo Hiệu Suất (Analytics)
 * =====================================================================
 * Nghiệp vụ chi tiết:
 *   1. Tính toán GMV, Doanh thu phí sàn 3%, Tiền thực nhận người bán.
 *   2. Thống kê số lượng đơn hàng, tỷ lệ chuyển đổi, tỷ lệ hủy đơn an toàn.
 *   3. Lọc theo khoảng thời gian linh hoạt (Hôm nay, 7 ngày, 30 ngày, Tháng này, Tùy chọn).
 *   4. Lọc theo phạm vi Toàn sàn (Admin) hoặc Từng Gian Hàng (Seller).
 *   5. Chuẩn bị dữ liệu mảng phục vụ vẽ biểu đồ Chart.js trực quan.
 *   6. Truy vấn Top sản phẩm bán chạy nhất.
 * =====================================================================
 */
@Service
public class ThongKeBaoCaoService {

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private GiaoDichKyQuyRepository giaoDichKyQuyRepository;

    @Autowired
    private YeuCauRutTienRepository yeuCauRutTienRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    /**
     * [US-58] Tổng hợp báo cáo thống kê phân tích hiệu suất theo bộ lọc
     */
    public ThongKeBaoCaoDTO layBaoCaoThongKe(
            String khoangThoiGian,
            LocalDate tuNgayCustom,
            LocalDate denNgayCustom,
            Long maGianHang
    ) {
        // 1. Xác định mốc thời gian bắt đầu và kết thúc
        LocalDate now = LocalDate.now();
        LocalDate tuNgay;
        LocalDate denNgay;

        if (khoangThoiGian == null || khoangThoiGian.trim().isEmpty()) {
            khoangThoiGian = "7_NGAY_QUA"; // Mặc định 7 ngày gần nhất
        }

        switch (khoangThoiGian) {
            case "HOM_NAY":
                tuNgay = now;
                denNgay = now;
                break;
            case "30_NGAY_QUA":
                tuNgay = now.minusDays(29);
                denNgay = now;
                break;
            case "THANG_NAY":
                tuNgay = now.withDayOfMonth(1);
                denNgay = now;
                break;
            case "TAT_CA":
                tuNgay = null;
                denNgay = null;
                break;
            case "TUY_CHON":
                tuNgay = (tuNgayCustom != null) ? tuNgayCustom : now.minusDays(7);
                denNgay = (denNgayCustom != null) ? denNgayCustom : now;
                // Validate ngày bắt đầu không được lớn hơn ngày kết thúc
                if (tuNgay.isAfter(denNgay)) {
                    LocalDate temp = tuNgay;
                    tuNgay = denNgay;
                    denNgay = temp;
                }
                break;
            case "7_NGAY_QUA":
            default:
                khoangThoiGian = "7_NGAY_QUA";
                tuNgay = now.minusDays(6);
                denNgay = now;
                break;
        }

        LocalDateTime startDateTime = (tuNgay != null) ? tuNgay.atStartOfDay() : null;
        LocalDateTime endDateTime = (denNgay != null) ? denNgay.atTime(LocalTime.MAX) : null;

        // 2. Tính toán các chỉ số tài chính cốt lõi trực tiếp từ CSDL
        BigDecimal tongGmv = donHangShopRepository.tinhTongGmv(maGianHang, startDateTime, endDateTime);
        BigDecimal doanhThuSan = giaoDichKyQuyRepository.tinhTongPhiSanTheoKhoang(maGianHang, startDateTime, endDateTime);
        BigDecimal doanhThuShopThucNhan = donHangShopRepository.tinhDoanhThuShopThucNhan(maGianHang, startDateTime, endDateTime);
        BigDecimal tongTienDangGiuEscrow = giaoDichKyQuyRepository.tinhTongTienDangGiuEscrowTheoKhoang(maGianHang, startDateTime, endDateTime);
        BigDecimal tongTienDaRut = yeuCauRutTienRepository.tinhTongTienDaRutTheoKhoang(maGianHang, startDateTime, endDateTime);

        // 3. Tính toán các chỉ số đơn hàng & tỷ lệ chuyển đổi
        long tongSoDon = donHangShopRepository.demTongSoDon(maGianHang, startDateTime, endDateTime);
        long soDonThanhCong = donHangShopRepository.demSoDonTheoTrangThai(maGianHang, startDateTime, endDateTime, "DA_GIAO");
        long soDonDangGiao = donHangShopRepository.demSoDonTheoTrangThai(maGianHang, startDateTime, endDateTime, "DANG_GIAO");
        long soDonChoXacNhan = donHangShopRepository.demSoDonTheoTrangThai(maGianHang, startDateTime, endDateTime, "CHO_XAC_NHAN");
        long soDonDaHuy = donHangShopRepository.demSoDonTheoTrangThai(maGianHang, startDateTime, endDateTime, "DA_HUY");

        // Tỷ lệ hoàn tất & hủy đơn (chống chia cho 0)
        double tyLeThanhCong = (tongSoDon > 0)
                ? BigDecimal.valueOf((double) soDonThanhCong * 100 / tongSoDon).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        double tyLeHuy = (tongSoDon > 0)
                ? BigDecimal.valueOf((double) soDonDaHuy * 100 / tongSoDon).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // 4. Lấy danh sách Top 5 sản phẩm bán chạy nhất
        List<Object[]> rawTopSanPham = chiTietDonHangRepository.timTopSanPhamBanChay(
                maGianHang, startDateTime, endDateTime, PageRequest.of(0, 5)
        );
        List<ThongKeBaoCaoDTO.TopSanPhamDTO> listTopSanPham = new ArrayList<>();
        if (rawTopSanPham != null) {
            for (Object[] row : rawTopSanPham) {
                String tenSanPham = (row[0] != null) ? row[0].toString() : "Sản phẩm";
                String tenBienThe = (row[1] != null) ? row[1].toString() : "";
                String maSku = (row[2] != null) ? row[2].toString() : "-";
                Long soLuong = (row[3] instanceof Number) ? ((Number) row[3]).longValue() : 0L;
                BigDecimal doanhThu = (row[4] instanceof BigDecimal) ? (BigDecimal) row[4] : BigDecimal.ZERO;

                listTopSanPham.add(ThongKeBaoCaoDTO.TopSanPhamDTO.builder()
                        .tenSanPham(tenSanPham)
                        .tenBienThe(tenBienThe)
                        .maSku(maSku)
                        .soLuongDaBan(soLuong)
                        .tongDoanhThu(doanhThu)
                        .build());
            }
        }

        // 5. Chuẩn bị dữ liệu phục vụ vẽ biểu đồ Chart.js theo ngày
        List<Object[]> rawDoanhThuNgay = donHangShopRepository.thongKeDoanhThuTungNgay(maGianHang, startDateTime, endDateTime);
        List<String> danhSachNgay = new ArrayList<>();
        List<BigDecimal> danhSachGmv = new ArrayList<>();
        List<BigDecimal> danhSachDoanhThuSan = new ArrayList<>();
        List<Long> danhSachSoDon = new ArrayList<>();

        if (rawDoanhThuNgay != null && !rawDoanhThuNgay.isEmpty()) {
            for (Object[] row : rawDoanhThuNgay) {
                Date sqlDate = (row[0] instanceof Date) ? (Date) row[0] : null;
                String labelNgay = (sqlDate != null) ? sqlDate.toLocalDate().format(DATE_FORMATTER) : "N/A";
                BigDecimal gmvNgay = (row[1] instanceof BigDecimal) ? (BigDecimal) row[1] : BigDecimal.ZERO;
                Long soDonNgay = (row[2] instanceof Number) ? ((Number) row[2]).longValue() : 0L;

                danhSachNgay.add(labelNgay);
                danhSachGmv.add(gmvNgay);
                danhSachDoanhThuSan.add(gmvNgay.multiply(new BigDecimal("0.03")).setScale(0, RoundingMode.HALF_UP));
                danhSachSoDon.add(soDonNgay);
            }
        } else {
            // Nếu chưa có dữ liệu, hiển thị ngày hôm nay với số 0
            danhSachNgay.add(now.format(DATE_FORMATTER));
            danhSachGmv.add(BigDecimal.ZERO);
            danhSachDoanhThuSan.add(BigDecimal.ZERO);
            danhSachSoDon.add(0L);
        }

        // 6. Lấy tên gian hàng nếu lọc theo shop
        String tenGianHang = "Toàn Hệ Thống Sàn";
        if (maGianHang != null) {
            Optional<GianHang> ghOpt = gianHangRepository.findById(maGianHang);
            if (ghOpt.isPresent()) {
                tenGianHang = ghOpt.get().getTenGianHang();
            }
        }

        // 7. Đóng gói DTO kết quả
        return ThongKeBaoCaoDTO.builder()
                .tongGmv(tongGmv != null ? tongGmv : BigDecimal.ZERO)
                .doanhThuSan(doanhThuSan != null ? doanhThuSan : BigDecimal.ZERO)
                .doanhThuShopThucNhan(doanhThuShopThucNhan != null ? doanhThuShopThucNhan : BigDecimal.ZERO)
                .tongTienDangGiuEscrow(tongTienDangGiuEscrow != null ? tongTienDangGiuEscrow : BigDecimal.ZERO)
                .tongTienDaRut(tongTienDaRut != null ? tongTienDaRut : BigDecimal.ZERO)
                .tongSoDonHang(tongSoDon)
                .soDonThanhCong(soDonThanhCong)
                .soDonDangGiao(soDonDangGiao)
                .soDonChoXacNhan(soDonChoXacNhan)
                .soDonDaHuy(soDonDaHuy)
                .tyLeThanhCong(tyLeThanhCong)
                .tyLeHuy(tyLeHuy)
                .topSanPhamBanChay(listTopSanPham)
                .danhSachNgay(danhSachNgay)
                .danhSachGmvTheoNgay(danhSachGmv)
                .danhSachDoanhThuSanTheoNgay(danhSachDoanhThuSan)
                .danhSachSoDonTheoNgay(danhSachSoDon)
                .khoangThoiGian(khoangThoiGian)
                .tuNgay(tuNgay)
                .denNgay(denNgay)
                .maGianHang(maGianHang)
                .tenGianHang(tenGianHang)
                .build();
    }
}
