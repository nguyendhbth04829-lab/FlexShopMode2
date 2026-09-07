package com.example.demo.controller;

import com.example.demo.entity.ChiTietDonHang;
import com.example.demo.entity.DonHangShop;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.service.VanDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * US-31 (làm lại): Seller chọn nhiều đơn -> in hàng loạt phiếu A6.
 * Giao diện test tông xanh dương: /seller/van-don
 */
@Controller
@RequestMapping("/seller/van-don")
public class SellerVanDonController {

    @Autowired
    private VanDonService vanDonService;

    @Autowired
    private GianHangRepository gianHangRepository;

    // Tạm: chưa có login nên lấy gian hàng đầu tiên để test US-31
    private Long layMaGianHangHienTai() {
        return gianHangRepository.findAll().stream()
                .findFirst()
                .map(g -> g.getMaGianHang())
                .orElse(1L);
    }

    @GetMapping({"", "/"})
    public String danhSach(
            Model model,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa) {
        Long maGianHang = layMaGianHangHienTai();
        List<DonHangShop> list = vanDonService.layDanhSachDonChoIn(maGianHang, tuKhoa);
        model.addAttribute("danhSachDon", list);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("soLuong", list.size());
        model.addAttribute("maGianHang", maGianHang);
        return "seller/van-don/danh-sach";
    }

    @PostMapping("/in")
    public String chonDonDeIn(@RequestParam(value = "ids", required = false) List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "redirect:/seller/van-don?loi=chua-chon-don";
        }
        String param = ids.stream().map(String::valueOf)
                .reduce((a, b) -> a + "," + b).orElse("");
        return "redirect:/seller/van-don/in?ids=" + param;
    }

    @GetMapping("/in")
    public String inHangLoat(
            Model model,
            @RequestParam(value = "ids", required = false) List<Long> ids) {
        Long maGianHang = layMaGianHangHienTai();
        List<DonHangShop> list = vanDonService.layDonDeIn(ids, maGianHang);
        Map<Long, List<ChiTietDonHang>> mapChiTiet = new HashMap<>();
        for (DonHangShop d : list) {
            mapChiTiet.put(d.getMaDonHangShop(), vanDonService.layChiTiet(d.getMaDonHangShop()));
        }
        model.addAttribute("danhSachIn", list);
        model.addAttribute("mapChiTiet", mapChiTiet);
        return "seller/van-don/in-hang-loat";
    }
}
