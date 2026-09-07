package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeDiaChiResponse;
import com.example.demo.service.DiaChiService;
import com.example.demo.service.NguoiDungService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DiaChiWebController {

    private final NguoiDungService nguoiDungService;
    private final DiaChiService diaChiService;

    /**
     * Trang quản lý Sổ Địa Chỉ Giao Hàng (US-05)
     * Chỉ dành cho Khách Hàng (KHACH_HANG) và Người Bán (NGUOI_BAN)
     */
    @GetMapping({"/customer/dia-chi", "/dia-chi"})
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'NGUOI_BAN')")
    public String trangSoDiaChi(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        ThongKeDiaChiResponse thongKe = diaChiService.thongKeDiaChi(nguoiDung.getId());

        String role = (nguoiDung.getRoles() != null && nguoiDung.getRoles().contains("NGUOI_BAN")) ? "NGUOI_BAN" : "KHACH_HANG";

        model.addAttribute("user", nguoiDung);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageTitle", "Sổ Địa Chỉ Giao Hàng - FlexShop");
        model.addAttribute("currentRole", role);

        return "dashboard/so-dia-chi";
    }
}
