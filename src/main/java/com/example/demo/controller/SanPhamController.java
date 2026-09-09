package com.example.demo.controller;

import com.example.demo.dto.BienTheForm;
import com.example.demo.dto.SanPhamForm;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.SanPham;
import com.example.demo.service.DanhMucService;
import com.example.demo.service.SanPhamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/seller/san-pham")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private DanhMucService danhMucService;

    @GetMapping
    public String danhSach(
            @RequestParam(name = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<SanPham> pageSanPham = sanPhamService.layDanhSach(tuKhoa, page, size);
        model.addAttribute("pageSanPham", pageSanPham);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("danhSachDanhMuc", danhMucService.layTatCaKhongPhanTrang());
        model.addAttribute("formSanPham", new SanPhamForm());
        return "sanpham/danh-sach";
    }

    @PostMapping("/them")
    public String themSanPham(
            @Valid @ModelAttribute("formSanPham") SanPhamForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu sản phẩm không hợp lệ.");
            return "redirect:/seller/san-pham";
        }
        try {
            // Giả lập Lấy mã gian hàng từ Session/JWT
            form.setMaGianHang(1L); 
            sanPhamService.themSanPham(form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/seller/san-pham";
    }

    @PostMapping("/bien-the/them")
    public String themBienThe(
            @Valid @ModelAttribute("formBienThe") BienTheForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu biến thể không hợp lệ.");
            return "redirect:/seller/san-pham"; // Quay lại trang danh sách hoặc chi tiết
        }
        try {
            sanPhamService.themBienThe(form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Thêm biến thể SKU thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi thêm biến thể: " + e.getMessage());
        }
        return "redirect:/seller/san-pham";
    }

    @PostMapping("/bien-the/{id}/cap-nhat-gia")
    public String capNhatGiaBienThe(
            @PathVariable("id") Long idBienThe,
            @RequestParam("giaMoi") BigDecimal giaMoi,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Giả lập user ID thao tác = 1
            sanPhamService.capNhatGiaBienThe(idBienThe, giaMoi, 1L);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Cập nhật giá và lưu lịch sử thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi cập nhật giá: " + e.getMessage());
        }
        return "redirect:/seller/san-pham";
    }
}
