package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThietLapHoSoShopResponse;
import com.example.demo.dto.response.ThongKeThietLapShopResponse;
import com.example.demo.service.NguoiDungService;
import com.example.demo.service.ThietLapHoSoShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ThietLapHoSoShopWebController {

    private final NguoiDungService nguoiDungService;
    private final ThietLapHoSoShopService thietLapHoSoShopService;

    /**
     * Màn hình thiết lập hồ sơ Shop (Banner, Logo, Giờ hoạt động, Địa chỉ lấy hàng) - US-10
     */
    @GetMapping("/seller/thiet-lap-shop")
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public String hienThiThietLapShop(Model model) {
        NguoiDungResponse nguoiDung = nguoiDungService.layNguoiDungHienTai();
        try {
            ThietLapHoSoShopResponse shop = thietLapHoSoShopService.layThietLapShop(nguoiDung.getId());
            ThongKeThietLapShopResponse thongKe = thietLapHoSoShopService.layThongKeThietLap(nguoiDung.getId());
            model.addAttribute("shop", shop);
            model.addAttribute("thongKe", thongKe);
        } catch (Exception e) {
            log.warn("Người dùng ID: {} truy cập thiết lập shop nhưng gặp lỗi: {}", nguoiDung.getId(), e.getMessage());
            return "redirect:/seller/dang-ky";
        }

        model.addAttribute("user", nguoiDung);
        model.addAttribute("pageTitle", "Thiết Lập Hồ Sơ Gian Hàng - FlexShop Seller");
        model.addAttribute("currentRole", "NGUOI_BAN");

        return "dashboard/thiet-lap-shop";
    }
}
