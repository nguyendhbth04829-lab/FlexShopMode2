package com.example.demo.controller;

import com.example.demo.dto.LoHangForm;
import com.example.demo.service.LoHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller/lo-hang")
public class LoHangController {

    @Autowired
    private LoHangService loHangService;

    @PostMapping("/them")
    public String themMoi(
            @Valid @ModelAttribute("formLoHang") LoHangForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu lô hàng không hợp lệ.");
            return "redirect:/seller/kho-hang";
        }
        try {
            loHangService.themMoi(form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Thêm lô hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/kho-hang";
    }
}
