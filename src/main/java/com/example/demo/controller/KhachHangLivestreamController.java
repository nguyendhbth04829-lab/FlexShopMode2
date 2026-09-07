package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.PhongLivestreamRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller phục vụ Khách hàng khám phá và xem FlexShop Live (US-61)
 * Trải nghiệm: xem livestream, nhận deal sốc đang ghim, chat, thả tim và mua ngay trên live.
 */
@Slf4j
@Controller
@RequestMapping("/khach-hang/livestream")
@RequiredArgsConstructor
public class KhachHangLivestreamController {

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    private final LivestreamService livestreamService;
    private final PhongLivestreamRepository phongLivestreamRepository;
    private final NguoiDungRepository nguoiDungRepository;

    private NguoiDung layKhachHangHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().contains("khach"))
                        .findFirst()
                        .orElse(null));
    }

    /**
     * 1. Khám phá Livestream FlexShop Live (Kênh mua sắm trực tiếp)
     */
    @GetMapping
    public String trangKhamPha(
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Pageable pageable = PageRequest.of(page, size);
        Page<PhongLivestream> trangLive = livestreamService.timKiemKhamPha(tuKhoa, pageable);

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("trangLive", trangLive);
        model.addAttribute("tuKhoa", tuKhoa);

        return "khach-hang/livestream/kham-pha";
    }

    /**
     * Mở nhanh phòng Live đang phát sóng mới nhất
     */
    @GetMapping("/xem")
    public String xemLiveMacDinh() {
        List<PhongLivestream> dangLive = phongLivestreamRepository.findByTrangThai("DANG_LIVE");
        if (!dangLive.isEmpty()) {
            return "redirect:/khach-hang/livestream/xem/" + dangLive.get(0).getMaLive();
        }
        List<PhongLivestream> allRooms = phongLivestreamRepository.findAll();
        if (!allRooms.isEmpty()) {
            return "redirect:/khach-hang/livestream/xem/" + allRooms.get(0).getMaLive();
        }
        return "redirect:/khach-hang/livestream";
    }

    /**
     * 2. Vào xem trực tiếp một phòng Livestream
     * - Hiển thị luồng video (Webcam hoặc Video loop)
     * - Thẻ sản phẩm đang được ghim (PINNED FLASH SALE) nổi bật với nút MUA NGAY
     * - Khung bình luận trực tiếp & thả tim bay
     */
    @GetMapping("/xem/{maLive}")
    public String xemLivestream(@PathVariable("maLive") Long maLive, Model model) {
        NguoiDung khachHang = layKhachHangHienTai();
        Optional<PhongLivestream> optPhong = phongLivestreamRepository.findById(maLive);
        if (optPhong.isEmpty()) {
            List<PhongLivestream> dangLive = phongLivestreamRepository.findByTrangThai("DANG_LIVE");
            if (!dangLive.isEmpty()) {
                return "redirect:/khach-hang/livestream/xem/" + dangLive.get(0).getMaLive();
            }
            return "redirect:/khach-hang/livestream";
        }

        PhongLivestream phongLive = optPhong.get();
        List<SanPhamGhimDTO> danhSachSpLive = livestreamService.layDanhSachSanPhamLiveDTO(maLive);
        SanPhamGhimDTO spDangGhim = livestreamService.laySanPhamDangGhimDTO(maLive).orElse(null);
        List<BinhLuanLiveDTO> danhSachBinhLuan = livestreamService.layDanhSachBinhLuan(maLive);

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("phongLive", phongLive);
        model.addAttribute("danhSachSpLive", danhSachSpLive);
        model.addAttribute("spDangGhim", spDangGhim);
        model.addAttribute("danhSachBinhLuan", danhSachBinhLuan);

        return "khach-hang/livestream/phong-live";
    }

    // ==========================================
    // CÁC REST API TƯƠNG TÁC LIVE CHO KHÁCH HÀNG
    // ==========================================

    /**
     * API Khách gửi bình luận tương tác
     */
    @PostMapping("/api/{maLive}/binh-luan")
    @ResponseBody
    public ResponseEntity<?> guiBinhLuanKhachHang(
            @PathVariable("maLive") Long maLive,
            @RequestBody Map<String, String> body
    ) {
        try {
            String noiDung = body.get("noiDung");
            NguoiDung khachHang = layKhachHangHienTai();
            BinhLuanLiveDTO dto = livestreamService.guiBinhLuan(maLive, khachHang, noiDung, false);
            return ResponseEntity.ok(Map.of("thanhCong", true, "duLieu", dto));
        } catch (Exception e) {
            log.error("Lỗi khi gửi bình luận: ", e);
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Lấy danh sách bình luận mới nhất (backup polling)
     */
    @GetMapping("/api/{maLive}/binh-luan")
    @ResponseBody
    public ResponseEntity<?> layDanhSachBinhLuan(@PathVariable("maLive") Long maLive) {
        try {
            List<BinhLuanLiveDTO> list = livestreamService.layDanhSachBinhLuan(maLive);
            return ResponseEntity.ok(Map.of("thanhCong", true, "duLieu", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Khách bấm thả tim
     */
    @PostMapping("/api/{maLive}/tha-tim")
    @ResponseBody
    public ResponseEntity<?> thaTim(@PathVariable("maLive") Long maLive) {
        try {
            int tongSoTim = livestreamService.thaTim(maLive);
            return ResponseEntity.ok(Map.of("thanhCong", true, "tongSoTim", tongSoTim));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Lấy sản phẩm đang ghim hiện tại (fallback polling nếu WebSocket gián đoạn)
     */
    @GetMapping("/api/{maLive}/san-pham-ghim")
    @ResponseBody
    public ResponseEntity<?> laySanPhamGhim(@PathVariable("maLive") Long maLive) {
        try {
            SanPhamGhimDTO dto = livestreamService.laySanPhamDangGhimDTO(maLive).orElse(null);
            return ResponseEntity.ok(Map.of("thanhCong", true, "duLieu", dto != null ? dto : Collections.emptyMap()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }

    /**
     * API Đặt hàng nhanh trên Livestream (Chốt Deal Giảm Giá Sốc)
     */
    @PostMapping("/api/{maLive}/dat-hang-nhanh")
    @ResponseBody
    public ResponseEntity<?> datHangNhanh(
            @PathVariable("maLive") Long maLive,
            @Valid @RequestBody DatHangLiveForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            String loi = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", loi));
        }

        try {
            form.setMaLive(maLive);
            NguoiDung khachHang = layKhachHangHienTai();
            String ketQua = livestreamService.datHangNhanhLive(form, khachHang);
            return ResponseEntity.ok(Map.of("thanhCong", true, "thongBao", ketQua));
        } catch (Exception e) {
            log.error("Lỗi khi đặt hàng trên live: ", e);
            return ResponseEntity.badRequest().body(Map.of("thanhCong", false, "thongBao", e.getMessage()));
        }
    }
}
