package com.example.demo.controller;

import com.example.demo.dto.DangKyFlashSaleForm;
import com.example.demo.dto.KhungGioFlashSaleForm;
import com.example.demo.dto.ThongKeFlashSaleDTO;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.KhungGioFlashSale;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.SanPhamFlashSale;
import com.example.demo.repository.BienTheSanPhamRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.FlashSaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller xử lý Quản lý Khung giờ Flash Sale (0h, 12h, 21h) cho Admin/Seller
 * và Trang Portal Flash Sale đếm ngược thời gian thực cho Khách hàng (US-53 - PROMOTION).
 */
@Controller
public class FlashSaleController {

    private static final String EMAIL_ADMIN_MAC_DINH = "admin@flexshop.vn";
    private static final String EMAIL_SELLER_MAC_DINH = "techzone@flexshop.vn";

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    private NguoiDung layNguoiDungHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_ADMIN_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElse(null));
    }

    // =========================================================================
    // 1. DÀNH CHO ADMIN / SELLER: QUẢN LÝ KHUNG GIỜ FLASH SALE
    // =========================================================================

    /**
     * Màn hình Quản lý Khung Giờ Flash Sale (Admin / Seller)
     */
    @GetMapping("/quan-ly/flash-sale")
    public String quanLyKhungGio(
            @RequestParam(value = "trangThai", defaultValue = "TAT_CA") String trangThai,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Model model
    ) {
        Page<KhungGioFlashSale> pageKhungGio = flashSaleService.layDanhSachKhungGio(trangThai, tuKhoa, page, size);
        ThongKeFlashSaleDTO thongKe = flashSaleService.layThongKeFlashSale();

        model.addAttribute("pageKhungGio", pageKhungGio);
        model.addAttribute("danhSachKhungGio", pageKhungGio.getContent());
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("trangThaiHienTai", trangThai);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageKhungGio.getTotalPages());
        model.addAttribute("totalElements", pageKhungGio.getTotalElements());

        if (!model.containsAttribute("khungGioForm")) {
            model.addAttribute("khungGioForm", new KhungGioFlashSaleForm());
        }

        return "flash-sale/quan-ly-flash-sale";
    }

    /**
     * Tạo nhanh khung giờ chuẩn: 0h, 12h, 21h
     */
    @PostMapping("/quan-ly/flash-sale/tao-nhanh")
    public String taoKhungGioNhanh(
            @RequestParam("mocGio") String mocGio,
            @RequestParam(value = "ngay", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate ngay,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (ngay == null) ngay = LocalDate.now();
            KhungGioFlashSale slot = flashSaleService.taoKhungGioChuan(mocGio, ngay);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Thiết lập thành công khung giờ " + mocGio + " (" + slot.getTieuDe() + ")!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }
        return "redirect:/quan-ly/flash-sale";
    }

    /**
     * Tạo khung giờ tùy chỉnh ngày và giờ
     */
    @PostMapping("/quan-ly/flash-sale/tao-tuy-chinh")
    public String taoKhungGioTuyChinh(
            @Valid @ModelAttribute("khungGioForm") KhungGioFlashSaleForm form,
            BindingResult bindingResult,
            @RequestParam(value = "tepAnhBanner", required = false) org.springframework.web.multipart.MultipartFile tepAnhBanner,
            RedirectAttributes redirectAttributes
    ) {
        if (tepAnhBanner != null && !tepAnhBanner.isEmpty()) {
            form.setTepAnhBanner(tepAnhBanner);
        }
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Dữ liệu nhập không hợp lệ: " + errorMsg);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.khungGioForm", bindingResult);
            redirectAttributes.addFlashAttribute("khungGioForm", form);
            return "redirect:/quan-ly/flash-sale";
        }

        try {
            KhungGioFlashSale slot = flashSaleService.taoKhungGio(form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Tạo thành công khung giờ Flash Sale: " + slot.getTieuDe() + "!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
            redirectAttributes.addFlashAttribute("khungGioForm", form);
        }

        return "redirect:/quan-ly/flash-sale";
    }

    /**
     * Xóa khung giờ Flash Sale
     */
    @PostMapping("/quan-ly/flash-sale/xoa/{maFlashSale}")
    public String xoaKhungGio(
            @PathVariable("maFlashSale") Long maFlashSale,
            RedirectAttributes redirectAttributes
    ) {
        try {
            flashSaleService.xoaKhungGio(maFlashSale);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã xóa khung giờ Flash Sale #" + maFlashSale + " thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }
        return "redirect:/quan-ly/flash-sale";
    }

    /**
     * Chi tiết khung giờ & Quản lý danh sách sản phẩm giảm sốc
     */
    @GetMapping("/quan-ly/flash-sale/{maFlashSale}")
    public String chiTietKhungGio(
            @PathVariable("maFlashSale") Long maFlashSale,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            KhungGioFlashSale khungGio = flashSaleService.layChiTietKhungGio(maFlashSale);
            Page<SanPhamFlashSale> pageSanPham = flashSaleService.layDanhSachSanPhamTheoKhungGio(maFlashSale, page, size);
            List<BienTheSanPham> danhSachBienThe = bienTheSanPhamRepository.findAll();

            model.addAttribute("khungGio", khungGio);
            model.addAttribute("pageSanPham", pageSanPham);
            model.addAttribute("danhSachSanPham", pageSanPham.getContent());
            model.addAttribute("danhSachBienThe", danhSachBienThe);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pageSanPham.getTotalPages());
            model.addAttribute("totalElements", pageSanPham.getTotalElements());

            if (!model.containsAttribute("dangKyForm")) {
                DangKyFlashSaleForm form = new DangKyFlashSaleForm();
                form.setMaFlashSale(maFlashSale);
                model.addAttribute("dangKyForm", form);
            }

            return "flash-sale/chi-tiet-khung-gio";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
            return "redirect:/quan-ly/flash-sale";
        }
    }

    /**
     * Đăng ký sản phẩm giảm sốc vào khung giờ
     */
    @PostMapping("/quan-ly/flash-sale/{maFlashSale}/them-san-pham")
    public String themSanPhamFlashSale(
            @PathVariable("maFlashSale") Long maFlashSale,
            @Valid @ModelAttribute("dangKyForm") DangKyFlashSaleForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        form.setMaFlashSale(maFlashSale);

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("thongBaoLoi", errorMsg);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.dangKyForm", bindingResult);
            redirectAttributes.addFlashAttribute("dangKyForm", form);
            return "redirect:/quan-ly/flash-sale/" + maFlashSale;
        }

        try {
            // Đăng ký với quyền Admin (cho phép đăng ký mọi shop)
            SanPhamFlashSale sp = flashSaleService.dangKySanPhamFlashSale(form, null, true);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Đăng ký thành công sản phẩm '" + sp.getBienTheSanPham().getTenBienThe() +
                            "' vào Flash Sale với giá giảm sốc " + flashSaleService.dinhDangTien(sp.getGiaFlashSale()) +
                            " (-" + sp.getPhanTramGiamGia() + "%)!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
            redirectAttributes.addFlashAttribute("dangKyForm", form);
        }

        return "redirect:/quan-ly/flash-sale/" + maFlashSale;
    }

    /**
     * Gỡ sản phẩm khỏi Flash Sale
     */
    @PostMapping("/quan-ly/flash-sale/xoa-san-pham/{maSanPhamFs}")
    public String xoaSanPhamFlashSale(
            @PathVariable("maSanPhamFs") Long maSanPhamFs,
            @RequestParam("maFlashSale") Long maFlashSale,
            RedirectAttributes redirectAttributes
    ) {
        try {
            flashSaleService.xoaSanPhamKhoiFlashSale(maSanPhamFs, null, true);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã gỡ sản phẩm khỏi chương trình Flash Sale thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }
        return "redirect:/quan-ly/flash-sale/" + maFlashSale;
    }

    /**
     * Đổi trạng thái khung giờ (Khóa / Kích hoạt lại)
     */
    @PostMapping("/quan-ly/flash-sale/doi-trang-thai/{maFlashSale}")
    public String doiTrangThaiKhungGio(
            @PathVariable("maFlashSale") Long maFlashSale,
            @RequestParam("trangThaiMoi") String trangThaiMoi,
            RedirectAttributes redirectAttributes
    ) {
        try {
            flashSaleService.capNhatTrangThaiKhungGio(maFlashSale, trangThaiMoi);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã cập nhật trạng thái khung giờ thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }
        return "redirect:/quan-ly/flash-sale";
    }

    // =========================================================================
    // 2. DÀNH CHO KHÁCH HÀNG: PORTAL FLASH SALE ĐẾM NGƯỢC THỜI GIAN THỰC
    // =========================================================================

    /**
     * Trang chủ Flash Sale dành cho khách hàng (Shopee / Lazada Style)
     * Đếm ngược thời gian thực (Countdown), mốc giờ 0h, 12h, 21h, tiến độ Đã Bán.
     */
    @GetMapping("/flash-sale")
    public String trangChuFlashSale(
            @RequestParam(value = "maFlashSale", required = false) Long maFlashSale,
            Model model
    ) {
        // Lấy danh sách các khung giờ hiển thị cho khách hàng (Đang diễn ra, Sắp diễn ra, Hôm nay, Ngày mai...)
        List<KhungGioFlashSale> dsKhungGioTrongNgay = flashSaleService.layDanhSachKhungGioChoKhachHang();

        KhungGioFlashSale khungGioHienTai = null;
        if (maFlashSale != null) {
            try {
                khungGioHienTai = flashSaleService.layChiTietKhungGio(maFlashSale);
            } catch (Exception ignored) {}
        }

        if (khungGioHienTai == null) {
            khungGioHienTai = flashSaleService.layKhungGioHienTaiHoacGanNhat();
        }

        if (khungGioHienTai == null && !dsKhungGioTrongNgay.isEmpty()) {
            khungGioHienTai = dsKhungGioTrongNgay.get(0);
        }

        List<SanPhamFlashSale> dsSanPham = List.of();
        if (khungGioHienTai != null) {
            dsSanPham = flashSaleService.layDanhSachSanPhamTheoKhungGio(khungGioHienTai.getMaFlashSale(), 0, 50).getContent();
        }

        model.addAttribute("khungGioHienTai", khungGioHienTai);
        model.addAttribute("dsKhungGioTrongNgay", dsKhungGioTrongNgay);
        model.addAttribute("dsSanPham", dsSanPham);

        return "flash-sale/trang-chu-flash-sale";
    }

    /**
     * Thử nghiệm Đặt mua nhanh sản phẩm Flash Sale
     */
    @PostMapping("/flash-sale/mua-ngay")
    public String datMuaFlashSale(
            @RequestParam("maSanPhamFs") Long maSanPhamFs,
            @RequestParam(value = "soLuong", defaultValue = "1") int soLuong,
            @RequestParam(value = "maFlashSale", required = false) Long maFlashSale,
            RedirectAttributes redirectAttributes
    ) {
        try {
            flashSaleService.ghiNhanBanHangFlashSale(maSanPhamFs, soLuong, 4L);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "⚡ Đặt mua thành công " + soLuong + " sản phẩm Flash Sale giá sốc!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/flash-sale" + (maFlashSale != null ? "?maFlashSale=" + maFlashSale : "");
    }
}
