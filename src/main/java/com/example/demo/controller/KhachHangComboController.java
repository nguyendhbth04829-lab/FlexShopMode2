package com.example.demo.controller;

import com.example.demo.dto.DealSocMuaKemDTO;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.ComboKhuyenMai;
import com.example.demo.entity.SanPhamComboKhuyenMai;
import com.example.demo.repository.BienTheSanPhamRepository;
import com.example.demo.service.ComboKhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller phục vụ Khách hàng khám phá Combo Khuyến Mãi & Mua Kèm Deal Sốc (US-54 - PROMOTION)
 */
@Controller
public class KhachHangComboController {

    @Autowired
    private ComboKhuyenMaiService comboKhuyenMaiService;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    /**
     * Trang Portal Khám phá Combo & Mua Kèm Deal Sốc
     */
    @GetMapping("/khuyen-mai/combo-deal-soc")
    public String trangKhuyenMaiCombo(
            @RequestParam(value = "maBienTheChinh", required = false) Long maBienTheChinh,
            Model model
    ) {
        List<ComboKhuyenMai> dsComboDangChay = comboKhuyenMaiService.layDanhSachComboDangChay();

        BienTheSanPham sanPhamChinh = null;
        List<DealSocMuaKemDTO> dsDealSocMuaKem = List.of();

        if (maBienTheChinh != null) {
            sanPhamChinh = bienTheSanPhamRepository.findById(maBienTheChinh).orElse(null);
            if (sanPhamChinh != null) {
                dsDealSocMuaKem = comboKhuyenMaiService.layDanhSachDealSocChoKhachHang(maBienTheChinh);
            }
        } else if (!dsComboDangChay.isEmpty()) {
            // Tự động chọn sản phẩm chính đầu tiên từ combo đang chạy làm tiêu điểm nổi bật
            for (ComboKhuyenMai cb : dsComboDangChay) {
                for (SanPhamComboKhuyenMai sp : cb.getDanhSachSanPham()) {
                    if ("SAN_PHAM_CHINH".equalsIgnoreCase(sp.getVaiTro())) {
                        sanPhamChinh = sp.getBienTheSanPham();
                        dsDealSocMuaKem = comboKhuyenMaiService.layDanhSachDealSocChoKhachHang(sanPhamChinh.getMaBienThe());
                        break;
                    }
                }
                if (sanPhamChinh != null) break;
            }
        }

        model.addAttribute("dsComboDangChay", dsComboDangChay);
        model.addAttribute("sanPhamChinh", sanPhamChinh);
        model.addAttribute("dsDealSocMuaKem", dsDealSocMuaKem);

        return "khach-hang/combo-deal-soc";
    }

    /**
     * REST API: Lấy danh sách Deal Sốc Phụ Kiện B mua kèm Sản phẩm chính A
     * Dùng cho gọi AJAX bất đồng bộ trên trang Chi tiết sản phẩm (PDP) hoặc Giỏ hàng
     */
    @GetMapping("/api/deal-soc/{maBienTheChinh}")
    @ResponseBody
    public ResponseEntity<List<DealSocMuaKemDTO>> layDanhSachDealSocAjax(@PathVariable("maBienTheChinh") Long maBienTheChinh) {
        List<DealSocMuaKemDTO> dsDealSoc = comboKhuyenMaiService.layDanhSachDealSocChoKhachHang(maBienTheChinh);
        return ResponseEntity.ok(dsDealSoc);
    }
}
