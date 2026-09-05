package com.example.demo.controller;

import com.example.demo.dto.DatCauHoiForm;
import com.example.demo.dto.ThongKeHoiDapSanPhamDTO;
import com.example.demo.dto.ThongKeTuongTacKhachHangDTO;
import com.example.demo.dto.TraLoiCauHoiForm;
import com.example.demo.entity.*;
import com.example.demo.repository.DanhMucRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.SanPhamRepository;
import com.example.demo.service.TuongTacKhachHangService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý nghiệp vụ Tương tác Khách hàng (US-51 - ENGAGE):
 * - Danh sách Yêu thích (Wishlist)
 * - Theo dõi Gian hàng (Follow Shop)
 * - Hỏi - Đáp Cộng đồng (Q&A) trên trang sản phẩm
 */
@Controller
public class TuongTacKhachHangController {

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    @Autowired
    private TuongTacKhachHangService tuongTacKhachHangService;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    /**
     * Xác định tài khoản khách hàng demo hiện tại
     */
    private NguoiDung layKhachHangHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().contains("khach"))
                        .findFirst()
                        .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElse(null)));
    }

    // =========================================================================
    // 1. WISHLIST - SẢN PHẨM YÊU THÍCH
    // =========================================================================

    /**
     * Trang xem danh sách sản phẩm yêu thích (Wishlist) của khách hàng
     */
    @GetMapping("/khach-hang/yeu-thich")
    public String xemDanhSachWishlist(
            @RequestParam(value = "maDanhMuc", required = false) Long maDanhMuc,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

        Page<SanPhamYeuThich> pageWishlist = tuongTacKhachHangService.layDanhSachWishlist(
                maKhachHang, maDanhMuc, tuKhoa, page, size
        );

        ThongKeTuongTacKhachHangDTO thongKe = tuongTacKhachHangService.layThongKeTuongTacKhachHang(maKhachHang);

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("pageWishlist", pageWishlist);
        model.addAttribute("danhSachYeuThich", pageWishlist.getContent());
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("danhMucList", danhMucRepository.findAll());
        model.addAttribute("maDanhMucHienTai", maDanhMuc);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageWishlist.getTotalPages());
        model.addAttribute("totalElements", pageWishlist.getTotalElements());

        return "khach-hang/san-pham-yeu-thich";
    }

    /**
     * Bật/Tắt yêu thích sản phẩm (Hỗ trợ cả Form Post và AJAX)
     */
    @PostMapping("/khach-hang/yeu-thich/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleYeuThich(
            @RequestParam("maSanPham") Long maSanPham
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            NguoiDung khachHang = layKhachHangHienTai();
            Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

            boolean daYeuThich = tuongTacKhachHangService.toggleYeuThich(maKhachHang, maSanPham);
            long tongLuotThich = tuongTacKhachHangService.demLuotYeuThichSanPham(maSanPham);

            response.put("success", true);
            response.put("daYeuThich", daYeuThich);
            response.put("tongLuotThich", tongLuotThich);
            response.put("message", daYeuThich ? "Đã thêm sản phẩm vào danh sách yêu thích!" : "Đã bỏ sản phẩm khỏi danh sách yêu thích.");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     */
    @PostMapping("/khach-hang/yeu-thich/xoa/{maSanPham}")
    public String xoaKhoiWishlist(
            @PathVariable("maSanPham") Long maSanPham,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NguoiDung khachHang = layKhachHangHienTai();
            Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;
            tuongTacKhachHangService.xoaKhoiYeuThich(maKhachHang, maSanPham);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã xóa sản phẩm khỏi danh sách yêu thích thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }
        return "redirect:/khach-hang/yeu-thich";
    }

    // =========================================================================
    // 2. THEO DÕI GIAN HÀNG (FOLLOW SHOP)
    // =========================================================================

    /**
     * Trang xem danh sách gian hàng đang theo dõi của khách hàng
     */
    @GetMapping("/khach-hang/gian-hang-theo-doi")
    public String xemDanhSachGianHangTheoDoi(
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

        Page<TheoDoiGianHang> pageTheoDoi = tuongTacKhachHangService.layDanhSachGianHangTheoDoi(
                maKhachHang, tuKhoa, page, size
        );

        ThongKeTuongTacKhachHangDTO thongKe = tuongTacKhachHangService.layThongKeTuongTacKhachHang(maKhachHang);

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("pageTheoDoi", pageTheoDoi);
        model.addAttribute("danhSachTheoDoi", pageTheoDoi.getContent());
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageTheoDoi.getTotalPages());
        model.addAttribute("totalElements", pageTheoDoi.getTotalElements());

        return "khach-hang/gian-hang-theo-doi";
    }

    /**
     * Bật/Tắt theo dõi gian hàng (Hỗ trợ cả Form Post và AJAX)
     */
    @PostMapping("/khach-hang/theo-doi/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleTheoDoiGianHang(
            @RequestParam("maGianHang") Long maGianHang
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            NguoiDung khachHang = layKhachHangHienTai();
            Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

            boolean daTheoDoi = tuongTacKhachHangService.toggleTheoDoiGianHang(maKhachHang, maGianHang);
            long tongLuotTheoDoi = tuongTacKhachHangService.demLuotTheoDoiGianHang(maGianHang);

            response.put("success", true);
            response.put("daTheoDoi", daTheoDoi);
            response.put("tongLuotTheoDoi", tongLuotTheoDoi);
            response.put("message", daTheoDoi ? "Đã theo dõi gian hàng thành công!" : "Đã hủy theo dõi gian hàng.");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Hủy theo dõi gian hàng
     */
    @PostMapping("/khach-hang/theo-doi/xoa/{maGianHang}")
    public String huyTheoDoiGianHang(
            @PathVariable("maGianHang") Long maGianHang,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NguoiDung khachHang = layKhachHangHienTai();
            Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;
            tuongTacKhachHangService.huyTheoDoiGianHang(maKhachHang, maGianHang);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã hủy theo dõi gian hàng thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }
        return "redirect:/khach-hang/gian-hang-theo-doi";
    }

    // =========================================================================
    // 3. HỎI - ĐÁP CỘNG ĐỒNG (Q&A) TRÊN TRANG SẢN PHẨM
    // =========================================================================

    /**
     * Trang xem và đặt câu hỏi Hỏi-Đáp (Q&A) về sản phẩm
     */
    @GetMapping("/san-pham/{maSanPham}/hoi-dap")
    public String xemHoiDapSanPham(
            @PathVariable("maSanPham") Long maSanPham,
            @RequestParam(value = "trangThai", required = false, defaultValue = "TAT_CA") String trangThai,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        SanPham sanPham = sanPhamRepository.findById(maSanPham).orElse(null);
        if (sanPham == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Không tìm thấy sản phẩm mã #" + maSanPham);
            return "redirect:/san-pham/1/danh-gia";
        }

        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

        Page<HoiDapSanPham> pageHoiDap = tuongTacKhachHangService.layDanhSachHoiDapChoSanPham(
                maSanPham, trangThai, tuKhoa, page, size
        );

        ThongKeHoiDapSanPhamDTO thongKeHoiDap = tuongTacKhachHangService.layThongKeHoiDap(maSanPham);

        boolean daYeuThich = tuongTacKhachHangService.kiemTraDaYeuThich(maKhachHang, maSanPham);
        boolean daTheoDoiShop = (sanPham.getGianHang() != null)
                && tuongTacKhachHangService.kiemTraDaTheoDoi(maKhachHang, sanPham.getGianHang().getMaGianHang());
        long tongFollowShop = (sanPham.getGianHang() != null)
                ? tuongTacKhachHangService.demLuotTheoDoiGianHang(sanPham.getGianHang().getMaGianHang()) : 0;
        long tongLuotThich = tuongTacKhachHangService.demLuotYeuThichSanPham(maSanPham);

        model.addAttribute("sanPham", sanPham);
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("pageHoiDap", pageHoiDap);
        model.addAttribute("danhSachHoiDap", pageHoiDap.getContent());
        model.addAttribute("thongKeHoiDap", thongKeHoiDap);
        model.addAttribute("daYeuThich", daYeuThich);
        model.addAttribute("daTheoDoiShop", daTheoDoiShop);
        model.addAttribute("tongFollowShop", tongFollowShop);
        model.addAttribute("tongLuotThich", tongLuotThich);
        model.addAttribute("trangThaiHienTai", trangThai);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageHoiDap.getTotalPages());
        model.addAttribute("totalElements", pageHoiDap.getTotalElements());

        if (!model.containsAttribute("datCauHoiForm")) {
            DatCauHoiForm form = new DatCauHoiForm();
            form.setMaSanPham(maSanPham);
            model.addAttribute("datCauHoiForm", form);
        }

        return "san-pham/hoi-dap";
    }

    /**
     * Khách hàng gửi câu hỏi mới
     */
    @PostMapping("/san-pham/{maSanPham}/hoi-dap/dat-cau-hoi")
    public String guiCauHoi(
            @PathVariable("maSanPham") Long maSanPham,
            @Valid @ModelAttribute("datCauHoiForm") DatCauHoiForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("thongBaoLoi", errorMsg);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.datCauHoiForm", bindingResult);
            redirectAttributes.addFlashAttribute("datCauHoiForm", form);
            return "redirect:/san-pham/" + maSanPham + "/hoi-dap";
        }

        try {
            NguoiDung khachHang = layKhachHangHienTai();
            Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

            tuongTacKhachHangService.datCauHoi(maSanPham, maKhachHang, form.getCauHoi());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Gửi câu hỏi cộng đồng thành công! Câu hỏi của bạn sẽ sớm được Shop hoặc cộng đồng giải đáp.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
            redirectAttributes.addFlashAttribute("datCauHoiForm", form);
        }

        return "redirect:/san-pham/" + maSanPham + "/hoi-dap";
    }

    /**
     * Người bán hoặc Người dùng trả lời câu hỏi
     */
    @PostMapping("/san-pham/hoi-dap/tra-loi")
    public String traLoiCauHoi(
            @Valid @ModelAttribute("traLoiCauHoiForm") TraLoiCauHoiForm form,
            BindingResult bindingResult,
            @RequestParam(value = "maSanPham", defaultValue = "1") Long maSanPham,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("thongBaoLoi", errorMsg);
            return "redirect:/san-pham/" + maSanPham + "/hoi-dap";
        }

        try {
            NguoiDung nguoiDung = layKhachHangHienTai();
            Long maNguoiTraLoi = (nguoiDung != null) ? nguoiDung.getMaNguoiDung() : 1L;

            tuongTacKhachHangService.traLoiCauHoi(form.getMaHoiDap(), maNguoiTraLoi, form.getCauTraLoi());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã gửi câu trả lời giải đáp thắc mắc thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/san-pham/" + maSanPham + "/hoi-dap";
    }
}
