package com.example.demo.controller;

import com.example.demo.dto.ThuongHieuForm;
import com.example.demo.entity.ThuongHieu;
import com.example.demo.service.ThuongHieuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/thuong-hieu")
public class ThuongHieuController {

    @Autowired
    private ThuongHieuService thuongHieuService;

    @GetMapping
    public String danhSach(
            @RequestParam(name = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<ThuongHieu> pageData = thuongHieuService.layDanhSach(tuKhoa, page, size);
        model.addAttribute("pageData", pageData);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("formThuongHieu", new ThuongHieuForm());
        return "thuonghieu/danh-sach";
    }

    @PostMapping("/them")
    public String themMoi(
            @Valid @ModelAttribute("formThuongHieu") ThuongHieuForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu không hợp lệ.");
            return "redirect:/admin/thuong-hieu";
        }
        try {
            thuongHieuService.themMoi(form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Thêm thương hiệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/thuong-hieu";
    }

    @PostMapping("/sua/{id}")
    public String capNhat(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("formThuongHieu") ThuongHieuForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu cập nhật không hợp lệ.");
            return "redirect:/admin/thuong-hieu";
        }
        try {
            thuongHieuService.capNhat(id, form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Cập nhật thương hiệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/thuong-hieu";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            thuongHieuService.xoa(id);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Xóa thương hiệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/thuong-hieu";
    }
}
