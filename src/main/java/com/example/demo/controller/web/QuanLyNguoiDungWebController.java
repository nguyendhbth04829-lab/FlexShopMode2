package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeNguoiDungResponse;
import com.example.demo.service.NguoiDungService;
import com.example.demo.service.QuanLyNguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller phục vụ giao diện Web Quản lý người dùng & Phân quyền Admin (US-06)
 */
@Controller
@RequestMapping("/admin/nguoi-dung")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class QuanLyNguoiDungWebController {

    private final NguoiDungService nguoiDungService;
    private final QuanLyNguoiDungService quanLyNguoiDungService;

    @GetMapping
    public String hienThiTrangQuanLy(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        ThongKeNguoiDungResponse thongKe = quanLyNguoiDungService.layThongKeNguoiDung();

        model.addAttribute("user", nguoiDung);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageTitle", "Quản Lý Người Dùng & Phân Quyền Hệ Thống - FlexShop");
        model.addAttribute("currentRole", "ADMIN");

        return "dashboard/quan-ly-nguoi-dung";
    }
}
