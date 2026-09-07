package com.example.demo.controller;

import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.VideoNganReview;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VideoNganReviewRepository;
import com.example.demo.service.VideoNganService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller phục vụ người dùng xem & lướt Shopee Video (US-62)
 * Trải nghiệm: Lướt video dạng ngắn (TikTok/Reels), chạm icon giỏ hàng mua ngay, thả tim, bình luận.
 */
@Slf4j
@Controller
@RequestMapping("/khach-hang/video")
@RequiredArgsConstructor
public class VideoNganController {

    private final VideoNganService videoNganService;
    private final VideoNganReviewRepository videoNganReviewRepository;
    private final NguoiDungRepository nguoiDungRepository;

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    private NguoiDung layNguoiDungHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && (u.getEmail().contains("khach") || u.getEmail().contains("nguyen")))
                        .findFirst()
                        .orElse(null));
    }

    /**
     * 1. Màn hình Lướt Shopee Video (Dạng cuộn dọc Reels/TikTok Feed)
     */
    @GetMapping
    public String trangLuotVideo(
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        NguoiDung nguoiDung = layNguoiDungHienTai();
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoNganReview> trangVideo = videoNganService.layDanhSachFeed(tuKhoa, pageable);

        // Lưu thông tin trạng thái thích cho từng video
        Map<Long, Boolean> mapDaThich = new HashMap<>();
        if (nguoiDung != null) {
            for (VideoNganReview v : trangVideo.getContent()) {
                mapDaThich.put(v.getMaVideo(), videoNganService.daThichVideo(v.getMaVideo(), nguoiDung.getMaNguoiDung()));
            }
        }

        model.addAttribute("dsVideo", trangVideo.getContent());
        model.addAttribute("trangVideo", trangVideo);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("nguoiDungHienTai", nguoiDung);
        model.addAttribute("mapDaThich", mapDaThich);

        return "khach-hang/video/luot-video";
    }

    /**
     * 2. Màn hình Chi tiết một Video ngắn review
     */
    @GetMapping("/{maVideo}")
    public String trangChiTietVideo(
            @PathVariable("maVideo") Long maVideo,
            Model model
    ) {
        NguoiDung nguoiDung = layNguoiDungHienTai();
        VideoNganReview video = videoNganService.layVideoChiTiet(maVideo);

        // Tự động tăng lượt xem
        videoNganService.tangLuotXem(maVideo);

        boolean daThich = nguoiDung != null && videoNganService.daThichVideo(maVideo, nguoiDung.getMaNguoiDung());
        List<VideoNganReview> dsVideoLienQuan = videoNganReviewRepository
                .findTop10BySanPhamGanKem_MaSanPhamAndDaXoaFalseAndTrangThaiOrderByTongLuotTimDesc(
                        video.getSanPhamGanKem().getMaSanPham(), "HOAT_DONG"
                );

        model.addAttribute("video", video);
        model.addAttribute("daThich", daThich);
        model.addAttribute("dsVideoLienQuan", dsVideoLienQuan);
        model.addAttribute("nguoiDungHienTai", nguoiDung);

        return "khach-hang/video/chi-tiet-video";
    }
}
