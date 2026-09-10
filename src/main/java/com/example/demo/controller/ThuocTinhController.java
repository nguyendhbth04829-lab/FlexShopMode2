package com.example.demo.controller;

import com.example.demo.dto.ThuocTinhForm;
import com.example.demo.service.ThuocTinhService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller/thuoc-tinh")
public class ThuocTinhController {

    @Autowired
    private ThuocTinhService thuocTinhService;

    @PostMapping("/them")
    public String themMoi(
            @Valid @ModelAttribute("formThuocTinh") ThuocTinhForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu thuộc tính không hợp lệ.");
            return "redirect:/seller/san-pham";
        }
        try {
            thuocTinhService.themThuocTinh(form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Thêm thuộc tính sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/san-pham";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            thuocTinhService.xoaThuocTinh(id);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Xóa thuộc tính thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/san-pham";
    }
}
