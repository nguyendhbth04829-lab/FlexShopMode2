package com.example.demo.controller.web;

import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.service.DangKyGianHangService;
import com.example.demo.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DangKyGianHangWebController {

    private final NguoiDungService nguoiDungService;
    private final DangKyGianHangService dangKyGianHangService;

    /**
     * Màn hình đăng ký mở gian hàng (US-08)
     * - Dành cho Khách hàng / Người dùng muốn mở Shop
     * - Nếu đã có Shop hoạt động -> Chuyển hướng sang Kênh người bán (/seller/dashboard)
     * - Nếu đã gửi hồ sơ -> Hiển thị trạng thái CHO_DUYET hoặc lý do TU_CHOI
     */
    @GetMapping({"/seller/dang-ky", "/dang-ky-shop"})
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'NGUOI_BAN', 'ADMIN')")
    public String hienThiTrangDangKy(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        GianHangResponse gianHang = dangKyGianHangService.layThongTinGianHangCuaToi(nguoiDung.getId());

        // Nếu gian hàng đã được duyệt và đang hoạt động -> chuyển sang Seller Dashboard
        if (gianHang != null && "HOAT_DONG".equalsIgnoreCase(gianHang.getTrangThai())) {
            log.info("Người dùng ID: {} đã có gian hàng hoạt động, chuyển hướng sang Seller Dashboard", nguoiDung.getId());
            return "redirect:/seller/dashboard";
        }

        model.addAttribute("user", nguoiDung);
        model.addAttribute("gianHang", gianHang);
        model.addAttribute("pageTitle", "Đăng Ký Mở Gian Hàng - FlexShop");

        return "dashboard/dang-ky-shop";
    }
}
