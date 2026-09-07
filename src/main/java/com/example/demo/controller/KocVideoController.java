package com.example.demo.controller;

import com.example.demo.dto.DangVideoNganForm;
import com.example.demo.dto.ThongKeVideoDTO;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.SanPham;
import com.example.demo.entity.VideoNganReview;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.SanPhamRepository;
import com.example.demo.service.VideoNganService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller dành cho KOC / Người dùng đăng video và quản lý Studio Video (US-62)
 */
@Slf4j
@Controller
@RequestMapping("/khach-hang/video/koc")
@RequiredArgsConstructor
public class KocVideoController {

    private final VideoNganService videoNganService;
    private final SanPhamRepository sanPhamRepository;
    private final NguoiDungRepository nguoiDungRepository;

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    private NguoiDung layNguoiDungHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && (u.getEmail().contains("khach") || u.getEmail().contains("nguyen")))
                        .findFirst()
                        .orElse(null));
    }

    private List<SanPham> layDanhSachSanPhamHopLe() {
        return sanPhamRepository.findAll().stream()
                .filter(sp -> !Boolean.TRUE.equals(sp.getDaXoa()) && !Boolean.TRUE.equals(sp.getBiKhoa()))
                .limit(20)
                .toList();
    }

    /**
     * Giao diện form đăng Video ngắn review mới
     */
    @GetMapping("/dang-video")
    public String formDangVideo(Model model) {
        if (!model.containsAttribute("form")) {
            DangVideoNganForm form = new DangVideoNganForm();
            form.setHashtag("#review #dealhot #flexshop");
            form.setThoiLuongGiay(30);
            model.addAttribute("form", form);
        }
        model.addAttribute("dsSanPham", layDanhSachSanPhamHopLe());
        model.addAttribute("nguoiDungHienTai", layNguoiDungHienTai());
        return "khach-hang/video/dang-video";
    }

    /**
     * Tiếp nhận submit Đăng Video ngắn review
     */
    @PostMapping("/dang-video")
    public String xuLyDangVideo(
            @Valid @ModelAttribute("form") DangVideoNganForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        NguoiDung nguoiDung = layNguoiDungHienTai();
        if (nguoiDung == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để đăng video");
            return "redirect:/khach-hang/video";
        }

        // Validate tùy chỉnh: bắt buộc phải có file video hoặc link video
        boolean coFileVideo = form.getTepVideo() != null && !form.getTepVideo().isEmpty();
        boolean coLinkVideo = form.getLinkVideo() != null && !form.getLinkVideo().trim().isEmpty();
        if (!coFileVideo && !coLinkVideo) {
            bindingResult.rejectValue("linkVideo", "error.linkVideo", "Vui lòng tải lên tệp video hoặc dán đường link video hợp lệ");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("dsSanPham", layDanhSachSanPhamHopLe());
            model.addAttribute("nguoiDungHienTai", nguoiDung);
            return "khach-hang/video/dang-video";
        }

        try {
            VideoNganReview videoMoi = videoNganService.dangVideo(form, nguoiDung);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng video ngắn review thành công: \"" + videoMoi.getTieuDe() + "\"!");
            return "redirect:/khach-hang/video/" + videoMoi.getMaVideo();
        } catch (Exception e) {
            log.error("Lỗi khi đăng video ngắn: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("dsSanPham", layDanhSachSanPhamHopLe());
            model.addAttribute("nguoiDungHienTai", nguoiDung);
            return "khach-hang/video/dang-video";
        }
    }

    /**
     * Trang Quản lý danh sách video của tôi & Thống kê tương tác
     */
    @GetMapping("/video-cua-toi")
    public String trangVideoCuaToi(
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        NguoiDung nguoiDung = layNguoiDungHienTai();
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoNganReview> trangVideo = videoNganService.layVideoCuaToi(nguoiDung.getMaNguoiDung(), tuKhoa, pageable);
        ThongKeVideoDTO thongKe = videoNganService.layThongKeCuaNguoiDung(nguoiDung.getMaNguoiDung());

        model.addAttribute("dsVideo", trangVideo.getContent());
        model.addAttribute("trangVideo", trangVideo);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("nguoiDungHienTai", nguoiDung);

        return "khach-hang/video/danh-sach-video-cua-toi";
    }

    /**
     * Xóa video của tôi
     */
    @PostMapping("/xoa/{maVideo}")
    public String xoaVideo(
            @PathVariable("maVideo") Long maVideo,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung nguoiDung = layNguoiDungHienTai();
        try {
            videoNganService.xoaVideo(maVideo, nguoiDung);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa video thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/khach-hang/video/koc/video-cua-toi";
    }
}

