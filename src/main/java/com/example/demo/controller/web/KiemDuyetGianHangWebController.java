package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.service.KiemDuyetGianHangService;
import com.example.demo.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller phục vụ giao diện Web Kiểm duyệt mở Shop của Seller dành cho Admin (US-09)
 */
@Controller
@RequestMapping("/admin/kiem-duyet-shop")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class KiemDuyetGianHangWebController {

    private final NguoiDungService nguoiDungService;
    private final KiemDuyetGianHangService kiemDuyetGianHangService;

    @GetMapping
    public String hienThiTrangKiemDuyet(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        ThongKeGianHangResponse thongKe = kiemDuyetGianHangService.layThongKeKiemDuyet();

        model.addAttribute("user", nguoiDung);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageTitle", "Kiểm Duyệt Yêu Cầu Mở Shop - Quản Trị FlexShop");
        model.addAttribute("currentRole", "ADMIN");

        return "dashboard/kiem-duyet-shop";
    }
}
