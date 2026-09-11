package com.example.demo.controller;

import com.example.demo.entity.GianHang;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @GetMapping("/{maGianHang}")
    public String xemHoSoShop(@PathVariable("maGianHang") Long maGianHang, Model model) {
        GianHang shop = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Shop"));

        // Lấy danh sách sản phẩm của shop (không bị xóa, không bị khóa, đang hoạt động)
        // Note: For demo, we might just filter in memory or fetch all and filter to avoid complex queries if not already defined
        java.util.List<SanPham> sanPhams = sanPhamRepository.findByDaXoaFalse(org.springframework.data.domain.Pageable.unpaged()).getContent()
                .stream()
                .filter(sp -> sp.getMaGianHang().equals(maGianHang))
                .filter(sp -> sp.getBiKhoa() == null || !sp.getBiKhoa())
                .filter(sp -> "HOAT_DONG".equals(sp.getTrangThai()))
                .toList();

        model.addAttribute("shop", shop);
        model.addAttribute("sanPhams", sanPhams);
        return "shop/profile";
    }
}
