package com.example.demo.controller;

import com.example.demo.entity.DonHangShop;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.service.HanhTrinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Tra cuu van don: quet barcode tren phieu A6 (US-31) -> nhap ma -> xem don + timeline.
 * Responsive dung chung PC + mobile: /logistics/tra-cuu
 */
@Controller
@RequestMapping("/logistics/tra-cuu")
public class TraCuuController {

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private HanhTrinhService hanhTrinhService;

    @GetMapping
    public String traCuu(
            Model model,
            @RequestParam(value = "maVanDon", required = false) String maVanDon) {
        if (maVanDon != null && !maVanDon.isBlank()) {
            String ma = maVanDon.trim();
            model.addAttribute("maVanDon", ma);
            donHangShopRepository.findByMaVanDon(ma).ifPresentOrElse(
                    don -> {
                        model.addAttribute("don", don);
                        model.addAttribute("timeline",
                                hanhTrinhService.layTimeline(don.getMaDonHangShop()));
                    },
                    () -> model.addAttribute("khongThay", true));
        }
        return "logistics/tra-cuu";
    }
}
