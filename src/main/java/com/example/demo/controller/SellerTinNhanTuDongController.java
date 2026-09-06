package com.example.demo.controller;

import com.example.demo.dto.CauHinhTinNhanTuDongForm;
import com.example.demo.dto.ThongKeTinNhanTuDongDTO;
import com.example.demo.entity.CauHinhTinNhanTuDong;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.MaGiamGia;
import com.example.demo.entity.NguoiDung;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.MaGiamGiaRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.TinNhanTuDongService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller phục vụ Kênh Người Bán (Seller) Quản lý Tin nhắn tự động trả lời (US-60)
 */
@Slf4j
@Controller
@RequestMapping("/seller/tin-nhan-tu-dong")
public class SellerTinNhanTuDongController {

    private static final String EMAIL_SELLER_MAC_DINH = "techzone@flexshop.vn";

    @Autowired
    private TinNhanTuDongService tinNhanTuDongService;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    private GianHang layGianHangCuaSeller() {
        NguoiDung seller = nguoiDungRepository.findByEmail(EMAIL_SELLER_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> gianHangRepository.findByChuSoHuu_MaNguoiDung(u.getMaNguoiDung()).isPresent())
                        .findFirst()
                        .orElse(null));

        if (seller == null) {
            return gianHangRepository.findAll().stream().findFirst().orElse(null);
        }
        return gianHangRepository.findByChuSoHuu_MaNguoiDung(seller.getMaNguoiDung())
                .orElseGet(() -> gianHangRepository.findAll().stream().findFirst().orElse(null));
    }

    /**
     * 1. Màn hình danh sách cấu hình tin nhắn tự động (Dashboard + Phân trang + Bộ lọc)
     */
    @GetMapping
    public String danhSachCauHinh(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false, defaultValue = "TAT_CA") String loai,
            @RequestParam(required = false, defaultValue = "TAT_CA") String trangThai,
            @RequestParam(required = false) String tuKhoa,
            Model model
    ) {
        GianHang gianHang = layGianHangCuaSeller();
        if (gianHang == null) {
            model.addAttribute("error", "Không tìm thấy thông tin gian hàng của bạn!");
            return "seller/tin-nhan-tu-dong";
        }

        Long maGianHang = gianHang.getMaGianHang();

        // Thống kê dashboard
        ThongKeTinNhanTuDongDTO thongKe = tinNhanTuDongService.layThongKe(maGianHang);

        // Phân trang danh sách cấu hình
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by("ngayTao").descending());
        Page<CauHinhTinNhanTuDong> pageCauHinh = tinNhanTuDongService.timKiemVaLoc(maGianHang, loai, trangThai, tuKhoa, pageable);

        // Lấy danh sách Voucher của shop để người bán lựa chọn đính kèm
        List<MaGiamGia> dsVoucher = maGiamGiaRepository.findByGianHangMaGianHangAndDangHoatDongTrueAndDaXoaFalse(maGianHang);

        model.addAttribute("gianHang", gianHang);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageCauHinh", pageCauHinh);
        model.addAttribute("dsVoucher", dsVoucher);
        model.addAttribute("loaiHienTai", loai);
        model.addAttribute("trangThaiHienTai", trangThai);
        model.addAttribute("tuKhoaHienTai", tuKhoa != null ? tuKhoa : "");
        model.addAttribute("form", new CauHinhTinNhanTuDongForm());

        return "seller/tin-nhan-tu-dong";
    }

    /**
     * 2. API lấy chi tiết 1 cấu hình để điền vào Modal Chỉnh Sửa
     */
    @GetMapping("/chi-tiet/{id}")
    @ResponseBody
    public ResponseEntity<?> layChiTiet(@PathVariable("id") Long id) {
        try {
            GianHang gianHang = layGianHangCuaSeller();
            CauHinhTinNhanTuDong entity = tinNhanTuDongService.layTheoId(id, gianHang.getMaGianHang());

            Map<String, Object> res = new HashMap<>();
            res.put("maCauHinh", entity.getMaCauHinh());
            res.put("tieuDe", entity.getTieuDe());
            res.put("loaiTinNhanTuDong", entity.getLoaiTinNhanTuDong());
            res.put("noiDungTinNhan", entity.getNoiDungTinNhan());
            res.put("maVoucher", entity.getMaGiamGia() != null ? entity.getMaGiamGia().getMaVoucher() : null);
            res.put("gioBatDau", entity.getGioBatDau() != null ? entity.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
            res.put("gioKetThuc", entity.getGioKetThuc() != null ? entity.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
            res.put("tuKhoaKichHoat", entity.getTuKhoaKichHoat() != null ? entity.getTuKhoaKichHoat() : "");
            res.put("kichHoat", entity.getKichHoat());
            res.put("doTreGiay", entity.getDoTreGiay());
            res.put("gioiHanGuiMoiKhachNgay", entity.getGioiHanGuiMoiKhachNgay());

            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 3. Xử lý Lưu cấu hình mới hoặc Cập nhật cấu hình
     */
    @PostMapping("/luu")
    public String luuCauHinh(
            @Valid @ModelAttribute("form") CauHinhTinNhanTuDongForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        GianHang gianHang = layGianHangCuaSeller();
        if (gianHang == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy gian hàng!");
            return "redirect:/seller/tin-nhan-tu-dong";
        }

        if (result.hasErrors()) {
            String errorMsg = result.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/seller/tin-nhan-tu-dong";
        }

        try {
            CauHinhTinNhanTuDong saved = tinNhanTuDongService.luuCauHinh(gianHang.getMaGianHang(), form);
            redirectAttributes.addFlashAttribute("successMessage",
                    (form.getMaCauHinh() == null ? "Tạo mới" : "Cập nhật") + " kịch bản '" + saved.getTieuDe() + "' thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi lưu cấu hình auto-responder: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/seller/tin-nhan-tu-dong";
    }

    /**
     * 4. Bật / Tắt nhanh trạng thái Kích hoạt (AJAX API)
     */
    @PostMapping("/doi-trang-thai/{id}")
    @ResponseBody
    public ResponseEntity<?> doiTrangThai(@PathVariable("id") Long id) {
        try {
            GianHang gianHang = layGianHangCuaSeller();
            boolean trangThaiMoi = tinNhanTuDongService.doiTrangThai(id, gianHang.getMaGianHang());
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("kichHoat", trangThaiMoi);
            res.put("message", trangThaiMoi ? "Đã BẬT kích hoạt kịch bản!" : "Đã TẮT kịch bản thành công!");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * 5. Xóa cấu hình tin nhắn tự động
     */
    @PostMapping("/xoa/{id}")
    public String xoaCauHinh(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            GianHang gianHang = layGianHangCuaSeller();
            tinNhanTuDongService.xoaCauHinh(id, gianHang.getMaGianHang());
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa kịch bản tin nhắn tự động thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/seller/tin-nhan-tu-dong";
    }

    /**
     * 6. Xem Nhật ký / Lịch sử các tin nhắn đã tự động gửi cho khách
     */
    @GetMapping("/lich-su")
    public String xemLichSu(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model
    ) {
        GianHang gianHang = layGianHangCuaSeller();
        Pageable pageable = PageRequest.of(Math.max(0, page), size);
        model.addAttribute("gianHang", gianHang);
        model.addAttribute("pageLichSu", tinNhanTuDongService.layLichSu(gianHang.getMaGianHang(), pageable));
        return "seller/lich-su-tin-nhan-tu-dong";
    }
}
