package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.LivestreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller phục vụ Seller quản trị và phát FlexShop Live (US-61)
 */
@Slf4j
@Controller
@RequestMapping("/seller/livestream")
@RequiredArgsConstructor
public class SellerLivestreamController {

    private static final String EMAIL_SELLER_MAC_DINH = "techzone@flexshop.vn";

    private final LivestreamService livestreamService;
    private final PhongLivestreamRepository phongLivestreamRepository;
    private final SanPhamRepository sanPhamRepository;
    private final GianHangRepository gianHangRepository;
    private final NguoiDungRepository nguoiDungRepository;

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
     * 1. Danh sách phiên Livestream của Shop kèm thống kê và phân trang
     */
    @GetMapping
    public String danhSachLivestream(
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size,
            Model model
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        if (gianHang == null) {
            model.addAttribute("loi", "Không tìm thấy gian hàng tương ứng");
            return "seller/livestream/danh-sach";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PhongLivestream> trangPhongLive = livestreamService.timKiemSeller(
                gianHang.getMaGianHang(), trangThai, tuKhoa, pageable);

        ThongKeLivestreamDTO thongKe = livestreamService.layThongKeSeller(gianHang.getMaGianHang());

        model.addAttribute("gianHang", gianHang);
        model.addAttribute("seller", seller);
        model.addAttribute("trangPhongLive", trangPhongLive);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("trangThaiChon", trangThai);
        model.addAttribute("tuKhoa", tuKhoa);

        return "seller/livestream/danh-sach";
    }

    /**
     * 2. Trang thiết lập phòng Live mới
     */
    @GetMapping("/tao-moi")
    public String trangTaoMoi(Model model) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        TaoPhongLiveForm form = new TaoPhongLiveForm();
        form.setTieuDe("🔥 MEGA LIVE SIÊU SALE CÔNG NGHỆ - GIẢM ĐẾN 50%");
        form.setMoTa("Phiên live độc quyền tri ân khách hàng cùng hàng ngàn voucher giảm giá sốc!");

        List<SanPham> danhSachSanPhamShop = sanPhamRepository.findByGianHang_MaGianHang(gianHang.getMaGianHang());

        model.addAttribute("gianHang", gianHang);
        model.addAttribute("form", form);
        model.addAttribute("danhSachSanPhamShop", danhSachSanPhamShop);

        return "seller/livestream/tao-moi";
    }

    /**
     * 3. Xử lý tạo phòng Live và chuyển hướng ngay tới Livestream Studio
     */
    @PostMapping("/tao-moi")
    public String xuLyTaoMoi(
            @Valid @ModelAttribute("form") TaoPhongLiveForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        if (bindingResult.hasErrors()) {
            List<SanPham> danhSachSanPhamShop = sanPhamRepository.findByGianHang_MaGianHang(gianHang.getMaGianHang());
            model.addAttribute("gianHang", gianHang);
            model.addAttribute("danhSachSanPhamShop", danhSachSanPhamShop);
            return "seller/livestream/tao-moi";
        }

        try {
            PhongLivestream phongLive = livestreamService.taoPhongLive(form, seller);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Tạo phòng Livestream thành công! Bắt đầu phát trực tiếp ngay.");
            return "redirect:/seller/livestream/studio/" + phongLive.getMaLive();
        } catch (Exception e) {
            log.error("Lỗi khi tạo phòng livestream: ", e);
            model.addAttribute("loiHeThong", e.getMessage());
            List<SanPham> danhSachSanPhamShop = sanPhamRepository.findByGianHang_MaGianHang(gianHang.getMaGianHang());
            model.addAttribute("gianHang", gianHang);
            model.addAttribute("danhSachSanPhamShop", danhSachSanPhamShop);
            return "seller/livestream/tao-moi";
        }
    }

    /**
     * 4. Chuyển hướng nhanh tới Studio phòng Live đang phát sóng
     */
    @GetMapping("/studio")
    public String studioMacDinh(RedirectAttributes redirectAttributes) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);
        if (gianHang != null) {
            List<PhongLivestream> dangLive = phongLivestreamRepository.findByGianHang_MaGianHangAndTrangThaiOrderByThoiGianBatDauDesc(
                    gianHang.getMaGianHang(), "DANG_LIVE", PageRequest.of(0, 1)).getContent();
            if (!dangLive.isEmpty()) {
                return "redirect:/seller/livestream/studio/" + dangLive.get(0).getMaLive();
            }
            List<PhongLivestream> allRooms = phongLivestreamRepository.findByGianHang_MaGianHangOrderByThoiGianBatDauDesc(
                    gianHang.getMaGianHang(), PageRequest.of(0, 1)).getContent();
            if (!allRooms.isEmpty()) {
                return "redirect:/seller/livestream/studio/" + allRooms.get(0).getMaLive();
            }
        }
        redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Vui lòng tạo hoặc chọn một phiên Live để mở Studio.");
        return "redirect:/seller/livestream";
    }

    /**
     * 5. Giao diện FLEXSHOP LIVE STUDIO dành cho Seller
     * - Phát trực tiếp qua Webcam máy tính / Video demo
     * - Ghim sản phẩm giảm giá sốc
     * - Quản lý tương tác chat & thả tim real-time
     */
    @GetMapping("/studio/{maLive}")
    public String studioLivestream(@PathVariable("maLive") Long maLive, Model model, RedirectAttributes redirectAttributes) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        // Kiểm tra an toàn: nếu không tìm thấy phòng live với mã yêu cầu
        Optional<PhongLivestream> optPhong = phongLivestreamRepository.findById(maLive);
        if (optPhong.isEmpty()) {
            if (gianHang != null) {
                List<PhongLivestream> activeRooms = phongLivestreamRepository.findByGianHang_MaGianHangOrderByThoiGianBatDauDesc(
                        gianHang.getMaGianHang(), PageRequest.of(0, 1)).getContent();
                if (!activeRooms.isEmpty()) {
                    redirectAttributes.addFlashAttribute("thongBaoThanhCong", 
                            "Không tìm thấy phiên Live #" + maLive + ". Hệ thống đã tự động chuyển bạn đến phiên Live khả dụng #" + activeRooms.get(0).getMaLive());
                    return "redirect:/seller/livestream/studio/" + activeRooms.get(0).getMaLive();
                }
            }
            redirectAttributes.addFlashAttribute("loi", "Không tìm thấy phòng Livestream với mã: " + maLive);
            return "redirect:/seller/livestream";
        }

        PhongLivestream phongLive = optPhong.get();
        List<SanPhamGhimDTO> danhSachSpLive = livestreamService.layDanhSachSanPhamLiveDTO(maLive);
        SanPhamGhimDTO spDangGhim = livestreamService.laySanPhamDangGhimDTO(maLive).orElse(null);
        List<BinhLuanLiveDTO> danhSachBinhLuan = livestreamService.layDanhSachBinhLuan(maLive);

        // Danh sách sản phẩm của shop chưa có trong live để có thể thêm nhanh
        List<SanPham> tatCaSanPhamShop = (gianHang != null) 
                ? sanPhamRepository.findByGianHang_MaGianHang(gianHang.getMaGianHang()) 
                : List.of();

        model.addAttribute("seller", seller);
        model.addAttribute("gianHang", gianHang);
        model.addAttribute("phongLive", phongLive);
        model.addAttribute("danhSachSpLive", danhSachSpLive);
        model.addAttribute("spDangGhim", spDangGhim);
        model.addAttribute("danhSachBinhLuan", danhSachBinhLuan);
        model.addAttribute("tatCaSanPhamShop", tatCaSanPhamShop);

        return "seller/livestream/studio";
    }

    // ==========================================
    // CÁC REST API PHỤC VỤ THAO TÁC STUDIO REAL-TIME
    // ==========================================

    /**
     * API Ghim sản phẩm lên màn hình live
     */
    @PostMapping("/api/{maLive}/ghim")
    @ResponseBody
    public ResponseEntity<?> ghimSanPham(@PathVariable("maLive") Long maLive, @RequestParam("maSanPham") Long maSanPham) {
        try {
            SanPhamGhimDTO dto = livestreamService.ghimSanPham(maLive, maSanPham);
            return ResponseEntity.ok(Map.of("thanhCong", true, "duLieu", dto, "thongBao", "Đã ghim sản phẩm thành công!"));
        } catch (Exception e) {
            log.error("Lỗi khi ghim sản phẩm: ", e);
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Gỡ ghim sản phẩm
     */
    @PostMapping("/api/{maLive}/bo-ghim")
    @ResponseBody
    public ResponseEntity<?> boGhimSanPham(@PathVariable("maLive") Long maLive, @RequestParam("maSanPham") Long maSanPham) {
        try {
            livestreamService.boGhimSanPham(maLive, maSanPham);
            return ResponseEntity.ok(Map.of("thanhCong", true, "thongBao", "Đã gỡ ghim sản phẩm!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Thêm nhanh sản phẩm vào phòng live
     */
    @PostMapping("/api/{maLive}/them-san-pham")
    @ResponseBody
    public ResponseEntity<?> themSanPhamVaoLive(@PathVariable("maLive") Long maLive, @RequestBody ThemSanPhamLiveForm form) {
        try {
            form.setMaLive(maLive);
            SanPhamLivestream sp = livestreamService.themSanPhamVaoLive(form);
            return ResponseEntity.ok(Map.of("thanhCong", true, "thongBao", "Đã thêm sản phẩm vào phiên live!", "maSpLive", sp.getMaSpLive()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Xóa sản phẩm khỏi phòng live
     */
    @DeleteMapping("/api/{maLive}/san-pham/{maSanPham}")
    @ResponseBody
    public ResponseEntity<?> xoaSanPhamKhoiLive(@PathVariable("maLive") Long maLive, @PathVariable("maSanPham") Long maSanPham) {
        try {
            livestreamService.xoaSanPhamKhoiLive(maLive, maSanPham);
            return ResponseEntity.ok(Map.of("thanhCong", true, "thongBao", "Đã xóa sản phẩm khỏi live!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Chuyển trạng thái phòng live (Bắt đầu / Kết thúc)
     */
    @PostMapping("/api/{maLive}/chuyen-trang-thai")
    @ResponseBody
    public ResponseEntity<?> chuyenTrangThai(@PathVariable("maLive") Long maLive, @RequestParam("trangThai") String trangThai) {
        try {
            PhongLivestream phong = livestreamService.chuyenTrangThai(maLive, trangThai);
            return ResponseEntity.ok(Map.of("thanhCong", true, "trangThai", phong.getTrangThai(), "thongBao", "Cập nhật trạng thái thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Seller gửi tin nhắn vào phòng live
     */
    @PostMapping("/api/{maLive}/binh-luan")
    @ResponseBody
    public ResponseEntity<?> guiBinhLuanSeller(@PathVariable("maLive") Long maLive, @RequestBody Map<String, String> body) {
        try {
            String noiDung = body.get("noiDung");
            NguoiDung seller = laySellerHienTai();
            BinhLuanLiveDTO dto = livestreamService.guiBinhLuan(maLive, seller, noiDung, true);
            return ResponseEntity.ok(Map.of("thanhCong", true, "duLieu", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }
}
