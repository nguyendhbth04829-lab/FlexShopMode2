package com.example.demo.controller;

import com.example.demo.entity.DonHangShop;
import com.example.demo.service.KhachHangDonHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * US-41: Khach hang xem don + bam "Da nhan duoc hang".
 * Giao dien responsive dung chung PC + mobile: /khach-hang/don-hang
 */
@Controller
@RequestMapping("/khach-hang/don-hang")
public class KhachHangDonHangController {

    @Autowired
    private KhachHangDonHangService khachHangDonHangService;

    @GetMapping({"", "/"})
    public String danhSach(Model model) {
        Long maKhachHang = khachHangDonHangService.layMaKhachHangHienTai();
        model.addAttribute("listDon", khachHangDonHangService.layDonCuaKhachHang(maKhachHang));
        model.addAttribute("donChoXacNhan",
                khachHangDonHangService.layDonChoXacNhan(maKhachHang).size());
        return "khach-hang/don-hang";
    }

    @PostMapping("/da-nhan/{maDon}")
    public String daNhan(@PathVariable("maDon") Long maDon, Model model) {
        Long maKhachHang = khachHangDonHangService.layMaKhachHangHienTai();
        try {
            khachHangDonHangService.xacNhanDaNhan(maKhachHang, maDon);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("listDon", khachHangDonHangService.layDonCuaKhachHang(maKhachHang));
            model.addAttribute("donChoXacNhan",
                    khachHangDonHangService.layDonChoXacNhan(maKhachHang).size());
            model.addAttribute("loiNghiepVu", ex.getMessage());
            return "khach-hang/don-hang";
        }
        return "redirect:/khach-hang/don-hang?nhan=ok";
    }
}
