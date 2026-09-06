package com.example.demo.controller;

import com.example.demo.dto.DanhMucForm;
import com.example.demo.entity.DanhMuc;
import com.example.demo.service.DanhMucService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/danh-muc")
public class DanhMucController {

    @Autowired
    private DanhMucService danhMucService;

    @GetMapping
    public String danhSach(
            @RequestParam(name = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<DanhMuc> pageDanhMuc = danhMucService.layDanhSach(tuKhoa, page, size);
        model.addAttribute("pageDanhMuc", pageDanhMuc);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("danhSachCha", danhMucService.layTatCaKhongPhanTrang());
        model.addAttribute("formDanhMuc", new DanhMucForm());
        return "danhmuc/danh-sach";
    }

    @PostMapping("/them")
    public String themMoi(
            @Valid @ModelAttribute("formDanhMuc") DanhMucForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
            return "redirect:/admin/danh-muc";
        }
        
        try {
            danhMucService.themMoi(form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Thêm danh mục mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/danh-muc";
    }

    @PostMapping("/sua/{id}")
    public String capNhat(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("formDanhMuc") DanhMucForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu cập nhật không hợp lệ.");
            return "redirect:/admin/danh-muc";
        }

        try {
            danhMucService.capNhat(id, form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Cập nhật danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/danh-muc";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            danhMucService.xoa(id);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/danh-muc";
    }
}
