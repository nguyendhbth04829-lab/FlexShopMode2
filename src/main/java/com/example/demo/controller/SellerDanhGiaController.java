package com.example.demo.controller;

import com.example.demo.dto.PhanHoiDanhGiaForm;
import com.example.demo.dto.ThongKeDanhGiaSellerDTO;
import com.example.demo.entity.DanhGiaSanPham;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.NguoiDung;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.DanhGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller phục vụ người bán (Seller) quản lý và phản hồi đánh giá của khách hàng (US-50 - ENGAGE)
 */
@Controller
@RequestMapping("/seller/danh-gia")
public class SellerDanhGiaController {

    // Tài khoản người bán demo mặc định: Trần Minh Đức (TechZone Flagship Store - ID 2)
    private static final String EMAIL_SELLER_MAC_DINH = "techzone@flexshop.vn";

    @Autowired
    private DanhGiaService danhGiaService;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    /**
     * Xác định tài khoản người bán hiện tại
     */
    private NguoiDung laySellerHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_SELLER_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> gianHangRepository.findByChuSoHuu_MaNguoiDung(u.getMaNguoiDung()).isPresent())
                        .findFirst()
                        .orElse(null));
    }

    /**
     * Xác định gian hàng của người bán hiện tại
     */
    private GianHang layGianHangCuaSeller(Long maSeller) {
        if (maSeller == null) return null;
        return gianHangRepository.findByChuSoHuu_MaNguoiDung(maSeller)
                .orElseGet(() -> gianHangRepository.findAll().stream().findFirst().orElse(null));
    }

    /**
     * 1. Dashboard quản lý và xem danh sách đánh giá của Gian hàng
     */
    @GetMapping({"", "/", "/danh-sach"})
    public String quanLyDanhGia(
            @RequestParam(value = "soSao", required = false) Integer soSao,
            @RequestParam(value = "trangThaiPhanHoi", required = false, defaultValue = "TAT_CA") String trangThaiPhanHoi,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        if (seller == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Không tìm thấy thông tin tài khoản người bán.");
            return "redirect:/san-pham/1/danh-gia";
        }

        GianHang gianHang = layGianHangCuaSeller(seller.getMaNguoiDung());
        if (gianHang == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Tài khoản của bạn chưa liên kết với gian hàng nào.");
            return "redirect:/san-pham/1/danh-gia";
        }

        // Lấy thống kê tỷ lệ phản hồi & số lượng đánh giá của shop
        ThongKeDanhGiaSellerDTO thongKe = danhGiaService.layThongKeDanhGiaChoSeller(gianHang.getMaGianHang());

        // Lấy danh sách đánh giá có lọc đa tiêu chí và phân trang
        Page<DanhGiaSanPham> pageDanhGia = danhGiaService.layDanhSachDanhGiaChoSeller(
                gianHang.getMaGianHang(), soSao, trangThaiPhanHoi, tuKhoa, page, size
        );

        model.addAttribute("seller", seller);
        model.addAttribute("gianHang", gianHang);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageDanhGia", pageDanhGia);
        model.addAttribute("danhSachDanhGia", pageDanhGia.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", pageDanhGia.getTotalPages());
        model.addAttribute("totalElements", pageDanhGia.getTotalElements());

        // Giữ lại các bộ lọc trên giao diện
        model.addAttribute("soSao", soSao);
        model.addAttribute("trangThaiPhanHoi", trangThaiPhanHoi);
        model.addAttribute("tuKhoa", tuKhoa);

        if (!model.containsAttribute("phanHoiForm")) {
            model.addAttribute("phanHoiForm", new PhanHoiDanhGiaForm());
        }

        return "seller/quan-ly-danh-gia";
    }

    /**
     * 2. Seller gửi hoặc cập nhật phản hồi công khai cho đánh giá của khách hàng (POST)
     */
    @PostMapping("/phan-hoi")
    public String guiPhanHoi(
            @Valid @ModelAttribute("phanHoiForm") PhanHoiDanhGiaForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        if (seller == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Phiên làm việc hết hạn. Vui lòng đăng nhập lại.");
            return "redirect:/seller/danh-gia";
        }

        if (bindingResult.hasErrors()) {
            String loiDauTien = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("thongBaoLoi", loiDauTien);
            return "redirect:/seller/danh-gia";
        }

        try {
            danhGiaService.sellerPhanHoiDanhGia(form.getMaDanhGia(), form.getNoiDungPhanHoi(), seller.getMaNguoiDung());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", 
                    "Đã gửi phản hồi thành công! Phản hồi của bạn đã được cập nhật công khai ngay dưới đánh giá ở trang sản phẩm.");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Có lỗi xảy ra trong quá trình phản hồi: " + e.getMessage());
        }

        return "redirect:/seller/danh-gia";
    }
}
