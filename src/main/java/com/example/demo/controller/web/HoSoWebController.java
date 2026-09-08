package com.example.demo.controller.web;

import com.example.demo.dto.response.ThongKeHoSoResponse;
import com.example.demo.service.HoSoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web Controller hiển thị giao diện Hồ sơ cá nhân (US-04)
 * Hỗ trợ tất cả vai trò (All Roles)
 */
@Controller
@RequiredArgsConstructor
public class HoSoWebController {

    private final HoSoService hoSoService;

    /**
     * Trang xem và cập nhật thông tin cá nhân, đổi mật khẩu, upload avatar
     */
    @GetMapping("/ho-so")
    @PreAuthorize("isAuthenticated()")
    public String trangHoSoCaNhan(Model model) {
        ThongKeHoSoResponse thongKe = hoSoService.layThongKeHoSo();

        model.addAttribute("user", thongKe.getThongTinNguoiDung());
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageTitle", "Hồ Sơ Cá Nhân & Bảo Mật - FlexShop");

        return "dashboard/ho-so";
    }
}
