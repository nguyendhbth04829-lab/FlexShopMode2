package com.example.demo.controller;

import com.example.demo.service.SaoQuaTaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sao-qua-ta")
public class SaoQuaTaController {

    @Autowired
    private SaoQuaTaService saoQuaTaService;

    @PostMapping("/phat")
    public String phatSaoQuaTa(@RequestParam Long maGianHang, 
                               @RequestParam int soDiemPhat, 
                               @RequestParam String lyDo, 
                               @RequestParam String loaiViPham,
                               RedirectAttributes redirectAttributes) {
        try {
            saoQuaTaService.ghiNhanPhatSaoQuaTa(maGianHang, soDiemPhat, lyDo, loaiViPham);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã ghi nhận phạt Sao Quả Tạ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/sao-qua-ta/danh-sach";
    }
}
