package com.example.demo.controller;

import com.example.demo.dto.ThongKeBaoCaoDTO;
import com.example.demo.entity.GianHang;
import com.example.demo.service.KyQuyService;
import com.example.demo.service.ThongKeBaoCaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, BÁO CÁO & PHÂN TÍCH HIỆU SUẤT (DEV 5 - MINH)
 * USER STORY: US-58 - Controller Dashboard Thống Kê Doanh Thu, GMV & Báo Cáo Hiệu Suất
 * =====================================================================
 * Tên Controller: ThongKeBaoCaoController (Đặt tên tiếng Việt dễ hiểu theo yêu cầu)
 * Chức năng:
 *   1. Màn hình Dashboard phân tích Doanh thu, GMV, Phí sàn 3%, Tiền đã rút.
 *   2. Thống kê tỷ lệ chuyển đổi, tỷ lệ hủy đơn hàng.
 *   3. Bộ lọc thời gian (Hôm nay, 7 ngày, 30 ngày, Tháng này, Tùy chọn).
 *   4. Lọc theo từng Gian hàng cụ thể hoặc toàn bộ sàn.
 *   5. Cung cấp dữ liệu JSON trực tiếp cho thư viện vẽ biểu đồ Chart.js.
 * =====================================================================
 */
@Controller
@RequestMapping("/thong-ke-bao-cao")
public class ThongKeBaoCaoController {

    @Autowired
    private ThongKeBaoCaoService thongKeBaoCaoService;

    @Autowired
    private KyQuyService kyQuyService;

    /**
     * [US-58] Màn hình Dashboard Thống Kê Doanh Thu & Hiệu Suất Bán Hàng
     */
    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            @RequestParam(value = "khoangThoiGian", defaultValue = "7_NGAY_QUA") String khoangThoiGian,
            @RequestParam(value = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(value = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(value = "maGianHang", required = false) Long maGianHang
    ) {
        ThongKeBaoCaoDTO dto = thongKeBaoCaoService.layBaoCaoThongKe(khoangThoiGian, tuNgay, denNgay, maGianHang);
        List<GianHang> listGianHang = kyQuyService.getDanhSachGianHang();

        model.addAttribute("dto", dto);
        model.addAttribute("listGianHang", listGianHang);
        model.addAttribute("khoangThoiGian", khoangThoiGian);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("maGianHang", maGianHang);

        return "thong-ke-bao-cao/dashboard";
    }
}
