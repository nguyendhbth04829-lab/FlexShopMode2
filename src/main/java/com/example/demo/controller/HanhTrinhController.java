package com.example.demo.controller;

import com.example.demo.dto.GhiHanhTrinhForm;
import com.example.demo.entity.DonHangShop;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.service.HanhTrinhService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * US-33: Xem + ghi hanh trinh kien hang qua Hub.
 * Giao dien test tong xanh duong: /logistics/hanh-trinh
 */
@Controller
@RequestMapping("/logistics/hanh-trinh")
public class HanhTrinhController {

    @Autowired
    private HanhTrinhService hanhTrinhService;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @GetMapping
    public String hanhTrinh(
            Model model,
            @RequestParam(value = "maDon", required = false) Long maDon) {
        model.addAttribute("listDon", donHangShopRepository.findAll());
        model.addAttribute("listHub", hanhTrinhService.layTatCaHub());
        if (!model.containsAttribute("form")) {
            GhiHanhTrinhForm form = new GhiHanhTrinhForm();
            form.setMaDonHangShop(maDon);
            model.addAttribute("form", form);
        }
        if (maDon != null) {
            model.addAttribute("maDon", maDon);
            model.addAttribute("timeline", hanhTrinhService.layTimeline(maDon));
            DonHangShop don = donHangShopRepository.findById(maDon).orElse(null);
            model.addAttribute("donChon", don);
        }
        return "logistics/hanh-trinh";
    }

    @PostMapping("/ghi-moc")
    public String ghiMoc(
            @Valid @ModelAttribute("form") GhiHanhTrinhForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("listDon", donHangShopRepository.findAll());
            model.addAttribute("listHub", hanhTrinhService.layTatCaHub());
            if (form.getMaDonHangShop() != null) {
                model.addAttribute("maDon", form.getMaDonHangShop());
                model.addAttribute("timeline", hanhTrinhService.layTimeline(form.getMaDonHangShop()));
            }
            return "logistics/hanh-trinh";
        }
        try {
            hanhTrinhService.ghiMoc(form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("listDon", donHangShopRepository.findAll());
            model.addAttribute("listHub", hanhTrinhService.layTatCaHub());
            model.addAttribute("loiNghiepVu", ex.getMessage());
            return "logistics/hanh-trinh";
        }
        return "redirect:/logistics/hanh-trinh?maDon=" + form.getMaDonHangShop();
    }
}
