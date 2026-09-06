package com.example.demo.controller;

import com.example.demo.dto.ComboKhuyenMaiForm;
import com.example.demo.dto.ThemSanPhamComboForm;
import com.example.demo.dto.ThongKeComboDTO;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.ComboKhuyenMai;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.SanPhamComboKhuyenMai;
import com.example.demo.repository.BienTheSanPhamRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.ComboKhuyenMaiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller phục vụ người bán (Seller) Quản lý Combo Khuyến Mãi & Mua Kèm Deal Sốc (US-54 - PROMOTION)
 */
@Controller
@RequestMapping("/seller/combo-khuyen-mai")
public class SellerComboController {

    private static final String EMAIL_SELLER_MAC_DINH = "techzone@flexshop.vn";

    @Autowired
    private ComboKhuyenMaiService comboKhuyenMaiService;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    private NguoiDung laySellerHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_SELLER_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> gianHangRepository.findByChuSoHuu_MaNguoiDung(u.getMaNguoiDung()).isPresent())
                        .findFirst()
                        .orElse(null));
    }

    private GianHang layGianHangCuaSeller(NguoiDung seller) {
        if (seller == null) {
            return gianHangRepository.findAll().stream().findFirst().orElse(null);
        }
        return gianHangRepository.findByChuSoHuu_MaNguoiDung(seller.getMaNguoiDung())
                .orElseGet(() -> gianHangRepository.findAll().stream().findFirst().orElse(null));
    }

    /**
     * 1. Dashboard & Danh sách Combo Khuyến Mãi của Seller
     */
    @GetMapping
    public String danhSachCombo(
            @RequestParam(value = "loaiCombo", defaultValue = "TAT_CA") String loaiCombo,
            @RequestParam(value = "trangThai", defaultValue = "TAT_CA") String trangThai,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        if (gianHang == null) {
            model.addAttribute("thongBaoLoi", "Không tìm thấy thông tin gian hàng của bạn.");
            return "seller/combo/danh-sach-combo";
        }

        Page<ComboKhuyenMai> pageCombo = comboKhuyenMaiService.layDanhSachCombo(
                gianHang.getMaGianHang(), loaiCombo, trangThai, tuKhoa, page, size
        );
        ThongKeComboDTO thongKe = comboKhuyenMaiService.layThongKeCombo(gianHang.getMaGianHang());

        model.addAttribute("seller", seller);
        model.addAttribute("gianHang", gianHang);
        model.addAttribute("pageCombo", pageCombo);
        model.addAttribute("danhSachCombo", pageCombo.getContent());
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("loaiComboHienTai", loaiCombo);
        model.addAttribute("trangThaiHienTai", trangThai);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageCombo.getTotalPages());
        model.addAttribute("totalElements", pageCombo.getTotalElements());

        if (!model.containsAttribute("comboForm")) {
            ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
            form.setThoiGianBatDau(LocalDateTime.now());
            form.setThoiGianKetThuc(LocalDateTime.now().plusDays(7));
            model.addAttribute("comboForm", form);
        }

        return "seller/combo/danh-sach-combo";
    }

    /**
     * 2. Tạo mới chương trình Combo & Deal sốc
     */
    @PostMapping("/tao-moi")
    public String taoCombo(
            @Valid @ModelAttribute("comboForm") ComboKhuyenMaiForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        if (gianHang == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Không tìm thấy gian hàng của bạn!");
            return "redirect:/seller/combo-khuyen-mai";
        }

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("thongBaoLoi", errorMsg);
            redirectAttributes.addFlashAttribute("comboForm", form);
            return "redirect:/seller/combo-khuyen-mai";
        }

        try {
            ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, gianHang.getMaGianHang());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Khởi tạo chương trình '" + combo.getTenCombo() + "' thành công! Hãy thêm Sản phẩm chính và Phụ kiện mua kèm giảm sốc 50%.");
            return "redirect:/seller/combo-khuyen-mai/" + combo.getMaCombo();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
            redirectAttributes.addFlashAttribute("comboForm", form);
            return "redirect:/seller/combo-khuyen-mai";
        }
    }

    /**
     * 3. Chi tiết chương trình Combo: Xem và cấu hình Sản phẩm chính A & Phụ kiện B
     */
    @GetMapping("/{maCombo}")
    public String chiTietCombo(
            @PathVariable("maCombo") Long maCombo,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        try {
            ComboKhuyenMai combo = comboKhuyenMaiService.layChiTietCombo(maCombo, gianHang != null ? gianHang.getMaGianHang() : null);

            List<SanPhamComboKhuyenMai> dsSanPhamChinh = combo.getDanhSachSanPham().stream()
                    .filter(sp -> "SAN_PHAM_CHINH".equalsIgnoreCase(sp.getVaiTro()))
                    .toList();

            List<SanPhamComboKhuyenMai> dsPhuKienMuaKem = combo.getDanhSachSanPham().stream()
                    .filter(sp -> !"SAN_PHAM_CHINH".equalsIgnoreCase(sp.getVaiTro()))
                    .toList();

            // Lấy danh sách sản phẩm/biến thể thuộc gian hàng để chọn thêm
            List<BienTheSanPham> dsBienTheShop = bienTheSanPhamRepository.findBySanPham_GianHang_MaGianHangAndDaXoaFalse(gianHang.getMaGianHang());

            model.addAttribute("seller", seller);
            model.addAttribute("gianHang", gianHang);
            model.addAttribute("combo", combo);
            model.addAttribute("dsSanPhamChinh", dsSanPhamChinh);
            model.addAttribute("dsPhuKienMuaKem", dsPhuKienMuaKem);
            model.addAttribute("dsBienTheShop", dsBienTheShop);

            if (!model.containsAttribute("themSanPhamForm")) {
                ThemSanPhamComboForm form = new ThemSanPhamComboForm();
                form.setMaCombo(combo.getMaCombo());
                form.setPhanTramGiam(combo.getGiaTriGiam());
                model.addAttribute("themSanPhamForm", form);
            }

            return "seller/combo/chi-tiet-combo";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
            return "redirect:/seller/combo-khuyen-mai";
        }
    }

    /**
     * 4. Thêm Sản phẩm chính A hoặc Phụ kiện B vào Combo
     */
    @PostMapping("/{maCombo}/them-san-pham")
    public String themSanPhamVaoCombo(
            @PathVariable("maCombo") Long maCombo,
            @Valid @ModelAttribute("themSanPhamForm") ThemSanPhamComboForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("thongBaoLoi", errorMsg);
            return "redirect:/seller/combo-khuyen-mai/" + maCombo;
        }

        try {
            form.setMaCombo(maCombo);
            comboKhuyenMaiService.themSanPhamVaoCombo(form, gianHang.getMaGianHang());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Thêm sản phẩm vào chương trình thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/seller/combo-khuyen-mai/" + maCombo;
    }

    /**
     * 5. Gỡ bỏ sản phẩm khỏi combo
     */
    @PostMapping("/xoa-san-pham/{maSanPhamCombo}")
    public String xoaSanPhamKhoiCombo(
            @PathVariable("maSanPhamCombo") Long maSanPhamCombo,
            @RequestParam("maCombo") Long maCombo,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        try {
            comboKhuyenMaiService.xoaSanPhamKhoiCombo(maSanPhamCombo, gianHang.getMaGianHang());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã gỡ bỏ sản phẩm khỏi chương trình combo.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/seller/combo-khuyen-mai/" + maCombo;
    }

    /**
     * 6. Bật / Tắt hoạt động của chương trình Combo
     */
    @PostMapping("/doi-trang-thai/{maCombo}")
    public String doiTrangThaiCombo(
            @PathVariable("maCombo") Long maCombo,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        try {
            comboKhuyenMaiService.doiTrangThaiCombo(maCombo, gianHang.getMaGianHang());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Cập nhật trạng thái chương trình thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/seller/combo-khuyen-mai";
    }

    /**
     * 7. Xóa chương trình Combo
     */
    @PostMapping("/xoa/{maCombo}")
    public String xoaCombo(
            @PathVariable("maCombo") Long maCombo,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        try {
            comboKhuyenMaiService.xoaCombo(maCombo, gianHang.getMaGianHang());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã xóa chương trình combo thành công.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/seller/combo-khuyen-mai";
    }
}
