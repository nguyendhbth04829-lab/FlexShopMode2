package com.example.demo.controller;

import com.example.demo.dto.PhieuNhapXuatKhoForm;
import com.example.demo.service.KhoHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller/kho-hang")
public class KhoHangController {

    @Autowired
    private KhoHangService khoHangService;

    @GetMapping
    public String hienThiKhoHang(org.springframework.ui.Model model) {
        // Chỉ là giao diện demo để test
        model.addAttribute("formPhieuKho", new PhieuNhapXuatKhoForm());
        return "khohang/danh-sach";
    }

    // Các method xem danh sách kho, view giao diện... (bỏ qua để tập trung nghiệp vụ chính)

    @PostMapping("/phieu/tao-moi")
    public String taoPhieuNhapXuatKho(
            @Valid @ModelAttribute("formPhieuKho") PhieuNhapXuatKhoForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu phiếu kho không hợp lệ.");
            return "redirect:/seller/kho-hang";
        }

        try {
            // Giả lập user ID người lập phiếu = 1
            khoHangService.taoPhieuNhapXuatKho(form, 1L);
            String action = form.getLoaiPhieu().equals("NHAP_KHO") ? "Nhập kho" : "Xuất kho";
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Tạo phiếu " + action + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi tạo phiếu: " + e.getMessage());
        }
        return "redirect:/seller/kho-hang";
    }
}
