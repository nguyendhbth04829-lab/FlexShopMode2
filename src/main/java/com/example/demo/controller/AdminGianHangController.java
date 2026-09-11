package com.example.demo.controller;

import com.example.demo.entity.GianHang;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/gian-hang")
public class AdminGianHangController {

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @GetMapping("/danh-hieu")
    public String danhHieuShop(Model model) {
        List<GianHang> danhSach = gianHangRepository.findAll();
        model.addAttribute("danhSach", danhSach);
        return "admin/gianhang/danh-hieu";
    }

    @Autowired
    private com.example.demo.service.DanhHieuScheduler danhHieuScheduler;

    @PostMapping("/quet-danh-hieu")
    public String quetDanhHieu(RedirectAttributes redirectAttributes) {
        try {
            danhHieuScheduler.quetVaCapDanhHieuTuDong();
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Quét thành công! Đã chạy kịch bản đánh giá toàn bộ gian hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/gian-hang/danh-hieu";
    }
}
