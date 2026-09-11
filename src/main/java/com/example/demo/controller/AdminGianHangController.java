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

    @PostMapping("/quet-danh-hieu")
    public String quetDanhHieu(RedirectAttributes redirectAttributes) {
        try {
            List<GianHang> shops = gianHangRepository.findAll();
            List<SanPham> tatCaSanPham = sanPhamRepository.findAll();
            
            int count = 0;

            for (GianHang shop : shops) {
                // Tính tổng đã bán của tất cả sản phẩm thuộc shop
                long tongDaBan = tatCaSanPham.stream()
                        .filter(sp -> sp.getMaGianHang() != null && sp.getMaGianHang().equals(shop.getMaGianHang()))
                        .mapToLong(sp -> sp.getTongDaBan() != null ? sp.getTongDaBan() : 0)
                        .sum();
                
                String hangCu = shop.getHangGianHang();
                String hangMoi = "CHUAN";

                if (tongDaBan >= 50) {
                    hangMoi = "MALL";
                } else if (tongDaBan >= 10) {
                    hangMoi = "YEU_THICH";
                }

                if (!hangMoi.equals(hangCu)) {
                    shop.setHangGianHang(hangMoi);
                    gianHangRepository.save(shop);
                    count++;
                }
            }
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Quét thành công! Cập nhật danh hiệu cho " + count + " gian hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/gian-hang/danh-hieu";
    }
}
