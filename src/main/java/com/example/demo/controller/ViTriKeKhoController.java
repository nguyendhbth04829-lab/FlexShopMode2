package com.example.demo.controller;

import com.example.demo.dto.ViTriKeKhoForm;
import com.example.demo.service.ViTriKeKhoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller/vi-tri-kho")
public class ViTriKeKhoController {

    @Autowired
    private ViTriKeKhoService viTriKeKhoService;

    @PostMapping("/them")
    public String themMoi(
            @Valid @ModelAttribute("formViTri") ViTriKeKhoForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu vị trí kệ kho không hợp lệ.");
            return "redirect:/seller/kho-hang";
        }
        try {
            viTriKeKhoService.themMoi(form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Thêm vị trí kệ kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/kho-hang";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            viTriKeKhoService.xoa(id);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Xóa vị trí kệ kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/kho-hang";
    }
}
