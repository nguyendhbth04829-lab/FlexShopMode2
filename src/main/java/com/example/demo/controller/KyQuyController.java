package com.example.demo.controller;

import com.example.demo.entity.GianHang;
import com.example.demo.entity.GiaoDichKyQuy;
import com.example.demo.entity.LichSuGiaoDichVi;
import com.example.demo.entity.ViNguoiBan;
import com.example.demo.service.KyQuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & PHÍ SÀN (DEV 5 - MINH)
 * USER STORY: US-42 (Ký Quỹ Escrow - Shopee Guarantee 3 Ngày)
 *             US-43 (Khấu Trừ Phí Sàn 3% & Quản Lý Ví Người Bán)
 * =====================================================================
 * Tên Controller: KyQuyController (Đặt tên tiếng Việt dễ hiểu theo yêu cầu)
 * Chức năng chuyên nghiệp:
 *   1. Bảng điều khiển tài chính đồng bộ trực tiếp từ Database.
 *   2. Phân trang, tìm kiếm từ khóa, lọc theo trạng thái, lọc theo gian hàng, lọc đơn quá hạn.
 *   3. Xem chi tiết bóc tách doanh thu và 3% phí sàn (US-43).
 *   4. Giải ngân thủ công khi khách xác nhận "Đã nhận hàng" kèm validate an toàn.
 *   5. Quét và giải ngân tự động hàng loạt cho các đơn quá hạn 3 ngày bảo vệ.
 *   6. Xem Sổ cái biến động số dư và Quản lý Ví Người Bán (vi_nguoi_ban).
 * =====================================================================
 */
@Controller
@RequestMapping("/ky-quy")
public class KyQuyController {

    @Autowired
    private KyQuyService kyQuyService;

    /**
     * [US-42 & US-43] Màn hình danh sách giao dịch ký quỹ, tìm lọc nâng cao và phân trang
     */
    @GetMapping("/danh-sach")
    public String danhSach(
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "maGianHang", required = false) Long maGianHang,
            @RequestParam(value = "chiLayQuaHan", defaultValue = "false") Boolean chiLayQuaHan,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "5") Integer size
    ) {
        Page<GiaoDichKyQuy> pageKyQuy = kyQuyService.getDanhSachKyQuyPhanTrang(
                keyword, trangThai, maGianHang, chiLayQuaHan, page, size
        );
        Map<String, Object> thongKe = kyQuyService.getThongKeEscrowDongBo();
        List<GianHang> listGianHang = kyQuyService.getDanhSachGianHang();

        model.addAttribute("listKyQuy", pageKyQuy.getContent());
        model.addAttribute("pageKyQuy", pageKyQuy);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", pageKyQuy.getTotalPages());
        model.addAttribute("totalElements", pageKyQuy.getTotalElements());

        // Tham số tìm lọc
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("maGianHang", maGianHang);
        model.addAttribute("chiLayQuaHan", chiLayQuaHan);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("listGianHang", listGianHang);

        return "ky-quy/danh-sach";
    }

    /**
     * [US-42 & US-43] Màn hình xem chi tiết bóc tách phí sàn 3% và tiến trình Shopee Guarantee
     */
    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(Model model, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            GiaoDichKyQuy kyQuy = kyQuyService.getChiTietKyQuy(id);
            model.addAttribute("kyQuy", kyQuy);
            return "ky-quy/chi-tiet";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/ky-quy/danh-sach";
        }
    }

    /**
     * [US-42] Kích hoạt giải ngân thủ công (Khách xác nhận đã nhận hàng / Duyệt sớm)
     */
    @PostMapping("/giai-ngan/{id}")
    public String giaiNgan(
            @PathVariable("id") Long id,
            @RequestParam(value = "lyDo", required = false) String lyDo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            GiaoDichKyQuy gd = kyQuyService.giaiNganKyQuy(id, lyDo);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã giải ngân thành công " +
                    String.format("%,.0f đ", gd.getTienThucNhanVeVi()) +
                    " vào số dư khả dụng của gian hàng [" + gd.getGianHang().getTenGianHang() + "]!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cảnh báo nghiệp vụ: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống khi giải ngân: " + e.getMessage());
        }
        return "redirect:/ky-quy/danh-sach";
    }

    /**
     * [US-42] Quét và tự động giải ngân cho tất cả các giao dịch đã quá hạn 3 ngày bảo vệ
     */
    @PostMapping("/quet-tu-dong")
    public String quetTuDong(RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> ketQua = kyQuyService.quetVaTuDongGiaiNganChiTiet();
            int thanhCong = (int) ketQua.get("thanhCong");
            int thatBai = (int) ketQua.get("thatBai");
            BigDecimal tongTien = (BigDecimal) ketQua.get("tongTienGiaiNgan");

            if (thanhCong > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Quét thành công! Đã tự động giải ngân " + thanhCong + " đơn hàng hết hạn 3 ngày Shopee Guarantee với tổng số tiền " +
                        String.format("%,.0f đ", tongTien) + " vào ví người bán." +
                        (thatBai > 0 ? " (" + thatBai + " đơn phát sinh lỗi)" : ""));
            } else {
                redirectAttributes.addFlashAttribute("infoMessage",
                        "Hệ thống đã quét: Không có đơn hàng nào quá hạn 3 ngày cần giải ngân tự động tại thời điểm này.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi quét tự động: " + e.getMessage());
        }
        return "redirect:/ky-quy/danh-sach";
    }

    /**
     * [US-42 & US-43] Màn hình Quản lý Ví Người Bán (vi_nguoi_ban) và Sổ cái biến động số dư
     */
    @GetMapping("/vi-nguoi-ban/{maGianHang}")
    public String viNguoiBan(Model model, @PathVariable("maGianHang") Long maGianHang, RedirectAttributes redirectAttributes) {
        try {
            ViNguoiBan vi = kyQuyService.getViNguoiBanByGianHangId(maGianHang);
            List<LichSuGiaoDichVi> lichSu = kyQuyService.getLichSuGiaoDichVi(vi.getMaVi());

            model.addAttribute("vi", vi);
            model.addAttribute("gianHang", vi.getGianHang());
            model.addAttribute("lichSu", lichSu);

            return "ky-quy/vi-nguoi-ban";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/ky-quy/danh-sach";
        }
    }
}
