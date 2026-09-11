package com.example.demo.controller;

import com.example.demo.entity.SanPham;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/san-pham")
public class AdminSanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @GetMapping("/kiem-duyet")
    public String danhSachKiemDuyet(Model model) {
        // Lấy tất cả sản phẩm chưa xóa
        java.util.List<SanPham> danhSach = sanPhamRepository.findByDaXoaFalse(org.springframework.data.domain.Pageable.unpaged()).getContent();
        model.addAttribute("danhSach", danhSach);
        return "admin/sanpham/kiem-duyet";
    }

    @PostMapping("/{id}/khoa")
    public String khoaSanPham(
            @PathVariable("id") Long id,
            @RequestParam("lyDoKhoa") String lyDoKhoa,
            RedirectAttributes redirectAttributes
    ) {
        try {
            SanPham sp = sanPhamRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            sp.setBiKhoa(true);
            sp.setLyDoKhoa(lyDoKhoa);
            sp.setTrangThai("BI_KHOA"); // Cập nhật trạng thái
            sanPhamRepository.save(sp);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã khóa sản phẩm: " + sp.getTenSanPham());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/san-pham/kiem-duyet";
    }

    @PostMapping("/{id}/mo-khoa")
    public String moKhoaSanPham(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            SanPham sp = sanPhamRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            sp.setBiKhoa(false);
            sp.setLyDoKhoa(null);
            sp.setTrangThai("HOAT_DONG");
            sanPhamRepository.save(sp);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã mở khóa sản phẩm: " + sp.getTenSanPham());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/san-pham/kiem-duyet";
    }
}
