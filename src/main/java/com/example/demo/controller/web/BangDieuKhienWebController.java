package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class BangDieuKhienWebController {

    private final NguoiDungService nguoiDungService;

    /**
     * 1. Dashboard Quản trị viên hệ thống (Role: ADMIN)
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String bangDieuKhienAdmin(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        model.addAttribute("user", nguoiDung);
        model.addAttribute("pageTitle", "Quản trị Sàn FlexShop");
        model.addAttribute("currentRole", "ADMIN");
        return "dashboard/admin";
    }

    /**
     * 2. Dashboard Kênh người bán / Shop (Role: NGUOI_BAN hoặc ADMIN)
     */
    @GetMapping("/seller/dashboard")
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public String bangDieuKhienNguoiBan(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        model.addAttribute("user", nguoiDung);
        model.addAttribute("pageTitle", "Kênh Người Bán - Sàn FlexShop");
        model.addAttribute("currentRole", "NGUOI_BAN");
        return "dashboard/seller";
    }

    /**
     * 3. Dashboard Khách hàng mua sắm (Role: KHACH_HANG hoặc ADMIN)
     */
    @GetMapping("/customer/dashboard")
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'ADMIN')")
    public String bangDieuKhienKhachHang(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        model.addAttribute("user", nguoiDung);
        model.addAttribute("pageTitle", "Trung Tâm Khách Hàng - Sàn FlexShop");
        model.addAttribute("currentRole", "KHACH_HANG");
        return "dashboard/customer";
    }

    /**
     * 4. Dashboard Tài xế giao hàng POD (Role: TAI_XE, SHIPPER hoặc ADMIN)
     */
    @GetMapping("/shipper/dashboard")
    @PreAuthorize("hasAnyRole('TAI_XE', 'SHIPPER', 'ADMIN')")
    public String bangDieuKhienTaiXe(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        model.addAttribute("user", nguoiDung);
        model.addAttribute("pageTitle", "Cổng Vận Chuyển Tài Xế Giao Hàng");
        model.addAttribute("currentRole", "TAI_XE");
        return "dashboard/shipper";
    }

    /**
     * 5. Dashboard Nhân viên Chăm sóc khách hàng (Role: CSKH hoặc ADMIN)
     */
    @GetMapping("/cskh/dashboard")
    @PreAuthorize("hasAnyRole('CSKH', 'ADMIN')")
    public String bangDieuKhienCskh(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        model.addAttribute("user", nguoiDung);
        model.addAttribute("pageTitle", "Hệ Thống Chăm Sóc & Khiếu Nại CSKH");
        model.addAttribute("currentRole", "CSKH");
        return "dashboard/cskh";
    }
}
