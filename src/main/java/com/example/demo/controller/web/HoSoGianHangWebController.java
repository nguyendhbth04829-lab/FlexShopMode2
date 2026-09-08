package com.example.demo.controller.web;

import com.example.demo.dto.response.HoSoGianHangDayDuResponse;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.service.HoSoGianHangService;
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
public class HoSoGianHangWebController {

    private final NguoiDungService nguoiDungService;
    private final HoSoGianHangService hoSoGianHangService;

    /**
     * Màn hình quản lý Hồ sơ gian hàng & Chứng chỉ kinh doanh (Seller)
     */
    @GetMapping({"/seller/ho-so-shop", "/ho-so-shop"})
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public String hienThiHoSoShop(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        try {
            HoSoGianHangDayDuResponse hoSo = hoSoGianHangService.layHoSoGianHangCuaToi(nguoiDung.getId());
            model.addAttribute("hoSo", hoSo);
        } catch (Exception e) {
            log.warn("Người dùng ID: {} chưa có gian hàng: {}", nguoiDung.getId(), e.getMessage());
            return "redirect:/seller/dang-ky";
        }

        model.addAttribute("user", nguoiDung);
        model.addAttribute("pageTitle", "Hồ Sơ Gian Hàng & Chứng Chỉ Pháp Lý - FlexShop");
        model.addAttribute("currentRole", "NGUOI_BAN");

        return "dashboard/ho-so-shop";
    }
}
