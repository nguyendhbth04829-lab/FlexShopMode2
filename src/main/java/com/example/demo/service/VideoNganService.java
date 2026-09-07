package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ Shopee Video Review & Gắn link giỏ hàng (US-62)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoNganService {

    private final VideoNganReviewRepository videoNganReviewRepository;
    private final BinhLuanVideoRepository binhLuanVideoRepository;
    private final LuotThichVideoRepository luotThichVideoRepository;
    private final SanPhamRepository sanPhamRepository;
    private final NguoiDungRepository nguoiDungRepository;

    private static final String THU_MUC_UPLOAD_VIDEO = "uploads/video-review/";
    private static final String THU_MUC_UPLOAD_ANH = "uploads/video-review/poster/";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    /**
     * Lấy danh sách video ngắn cho Feed lướt xem (Shopee Video Reels)
     */
    @Transactional(readOnly = true)
    public Page<VideoNganReview> layDanhSachFeed(String tuKhoa, Pageable pageable) {
        return videoNganReviewRepository.timKiemFeed("HOAT_DONG", tuKhoa, pageable);
    }

    /**
     * Lấy chi tiết một video ngắn review
     */
    @Transactional(readOnly = true)
    public VideoNganReview layVideoChiTiet(Long maVideo) {
        return videoNganReviewRepository.findByMaVideoAndDaXoaFalse(maVideo)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Video ngắn với mã: " + maVideo));
    }

    /**
     * Lấy danh sách video của người dùng/KOC đã đăng kèm tìm kiếm
     */
    @Transactional(readOnly = true)
    public Page<VideoNganReview> layVideoCuaToi(Long maNguoiDung, String tuKhoa, Pageable pageable) {
        return videoNganReviewRepository.timKiemCuaToi(maNguoiDung, tuKhoa, pageable);
    }

    /**
     * Đăng tải Video ngắn review mới có gắn link giỏ hàng
     */
    @Transactional
    public VideoNganReview dangVideo(DangVideoNganForm form, NguoiDung nguoiDang) {
        if (nguoiDang == null) {
            throw new IllegalArgumentException("Người dùng chưa đăng nhập hoặc không hợp lệ");
        }

        // Validate sản phẩm gắn kèm
        SanPham sanPham = sanPhamRepository.findById(form.getMaSanPhamGanKem())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm gắn kèm không tồn tại trên hệ thống"));

        if (Boolean.TRUE.equals(sanPham.getDaXoa()) || Boolean.TRUE.equals(sanPham.getBiKhoa())) {
            throw new IllegalArgumentException("Sản phẩm gắn kèm hiện đang tạm ngừng kinh doanh hoặc đã bị khóa");
        }

        // Xử lý tệp video hoặc đường link video
        String duongDanVideo = xuLyLuuTepVideo(form.getTepVideo(), form.getLinkVideo());
        String duongDanPoster = xuLyLuuTepAnhPoster(form.getTepAnhBia(), form.getLinkAnhBia());

        // Nếu người dùng không chỉ định poster mà tệp tải lên là hình ảnh thì dùng luôn hình ảnh đó làm poster
        if (!StringUtils.hasText(form.getLinkAnhBia()) && (form.getTepAnhBia() == null || form.getTepAnhBia().isEmpty())) {
            String lowerVid = duongDanVideo.toLowerCase();
            if (lowerVid.endsWith(".jpg") || lowerVid.endsWith(".jpeg") || lowerVid.endsWith(".png") || lowerVid.endsWith(".webp") || lowerVid.endsWith(".gif")) {
                duongDanPoster = duongDanVideo;
            }
        }

        // Chuẩn hóa hashtag
        String hashtagChuanHoa = chuanHoaHashtag(form.getHashtag());

        VideoNganReview video = new VideoNganReview();
        video.setNguoiDang(nguoiDang);
        video.setSanPhamGanKem(sanPham);
        video.setTieuDe(form.getTieuDe().trim());
        video.setLinkVideo(duongDanVideo);
        video.setLinkAnhBia(duongDanPoster);
        video.setMoTa(form.getMoTa() != null ? form.getMoTa().trim() : null);
        video.setHashtag(hashtagChuanHoa);
        video.setThoiLuongGiay(form.getThoiLuongGiay() != null && form.getThoiLuongGiay() > 0 ? form.getThoiLuongGiay() : 30);
        video.setTongLuotTim(0);
        video.setTongLuotXem(0);
        video.setTongLuotBinhLuan(0);
        video.setTongLuotChiaSe(0);
        video.setTrangThai("HOAT_DONG");
        video.setDaXoa(false);
        video.setNgayDang(LocalDateTime.now());

        VideoNganReview daLuu = videoNganReviewRepository.save(video);
        log.info("Đăng video ngắn review thành công: maVideo={}, tieuDe={}, spGankem={}",
                daLuu.getMaVideo(), daLuu.getTieuDe(), sanPham.getTenSanPham());
        return daLuu;
    }

    /**
     * Chỉnh sửa thông tin Video ngắn review
     */
    @Transactional
    public VideoNganReview capNhatVideo(Long maVideo, DangVideoNganForm form, NguoiDung nguoiDang) {
        VideoNganReview video = layVideoChiTiet(maVideo);

        // Kiểm tra quyền sở hữu
        if (!video.getNguoiDang().getMaNguoiDung().equals(nguoiDang.getMaNguoiDung())) {
            throw new SecurityException("Bạn không có quyền chỉnh sửa video này");
        }

        // Kiểm tra sản phẩm gắn kèm nếu có thay đổi
        if (!video.getSanPhamGanKem().getMaSanPham().equals(form.getMaSanPhamGanKem())) {
            SanPham sanPhamMoi = sanPhamRepository.findById(form.getMaSanPhamGanKem())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm gắn kèm mới không tồn tại"));
            video.setSanPhamGanKem(sanPhamMoi);
        }

        video.setTieuDe(form.getTieuDe().trim());
        video.setMoTa(form.getMoTa() != null ? form.getMoTa().trim() : null);
        video.setHashtag(chuanHoaHashtag(form.getHashtag()));

        // Cập nhật video nếu có tệp/link mới
        if (form.getTepVideo() != null && !form.getTepVideo().isEmpty()) {
            video.setLinkVideo(xuLyLuuTepVideo(form.getTepVideo(), null));
        } else if (StringUtils.hasText(form.getLinkVideo())) {
            video.setLinkVideo(form.getLinkVideo().trim());
        }

        // Cập nhật poster nếu có
        if (form.getTepAnhBia() != null && !form.getTepAnhBia().isEmpty()) {
            video.setLinkAnhBia(xuLyLuuTepAnhPoster(form.getTepAnhBia(), null));
        } else if (StringUtils.hasText(form.getLinkAnhBia())) {
            video.setLinkAnhBia(form.getLinkAnhBia().trim());
        }

        return videoNganReviewRepository.save(video);
    }

    /**
     * Xóa mềm Video ngắn
     */
    @Transactional
    public void xoaVideo(Long maVideo, NguoiDung nguoiDang) {
        VideoNganReview video = layVideoChiTiet(maVideo);
        if (!video.getNguoiDang().getMaNguoiDung().equals(nguoiDang.getMaNguoiDung())) {
            throw new SecurityException("Bạn không có quyền xóa video này");
        }
        video.setDaXoa(true);
        videoNganReviewRepository.save(video);
        log.info("Đã xóa mềm video ngắn review: maVideo={}", maVideo);
    }

    /**
     * Thả tim (Like) hoặc Bỏ thích (Unlike) video ngắn review
     * @return Map chứa {daThich: boolean, tongLuotTim: int}
     */
    @Transactional
    public Map<String, Object> thichHoacBoThich(Long maVideo, NguoiDung nguoiDung) {
        VideoNganReview video = layVideoChiTiet(maVideo);
        Optional<LuotThichVideo> optThich = luotThichVideoRepository
                .findByVideo_MaVideoAndNguoiDung_MaNguoiDung(maVideo, nguoiDung.getMaNguoiDung());

        boolean daThichHienTai;
        if (optThich.isPresent()) {
            // Đã thích -> Bỏ thích
            luotThichVideoRepository.delete(optThich.get());
            int luotTimMoi = Math.max(0, (video.getTongLuotTim() != null ? video.getTongLuotTim() : 1) - 1);
            video.setTongLuotTim(luotTimMoi);
            daThichHienTai = false;
        } else {
            // Chưa thích -> Thả tim mới
            LuotThichVideo thichMoi = new LuotThichVideo();
            thichMoi.setVideo(video);
            thichMoi.setNguoiDung(nguoiDung);
            thichMoi.setThoiGianThich(LocalDateTime.now());
            luotThichVideoRepository.save(thichMoi);

            int luotTimMoi = (video.getTongLuotTim() != null ? video.getTongLuotTim() : 0) + 1;
            video.setTongLuotTim(luotTimMoi);
            daThichHienTai = true;
        }

        videoNganReviewRepository.save(video);
        Map<String, Object> ketQua = new HashMap<>();
        ketQua.put("daThich", daThichHienTai);
        ketQua.put("tongLuotTim", video.getTongLuotTim());
        return ketQua;
    }

    /**
     * Kiểm tra người dùng đã thích video chưa
     */
    @Transactional(readOnly = true)
    public boolean daThichVideo(Long maVideo, Long maNguoiDung) {
        if (maNguoiDung == null) return false;
        return luotThichVideoRepository.existsByVideo_MaVideoAndNguoiDung_MaNguoiDung(maVideo, maNguoiDung);
    }

    /**
     * Thêm bình luận mới vào video ngắn review
     */
    @Transactional
    public BinhLuanVideoDTO themBinhLuan(Long maVideo, String noiDung, NguoiDung nguoiDung) {
        if (!StringUtils.hasText(noiDung)) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống");
        }
        if (noiDung.trim().length() > 1000) {
            throw new IllegalArgumentException("Bình luận không được vượt quá 1000 ký tự");
        }

        VideoNganReview video = layVideoChiTiet(maVideo);

        BinhLuanVideo bl = new BinhLuanVideo();
        bl.setVideo(video);
        bl.setNguoiDung(nguoiDung);
        bl.setHoTenNguoiDung(nguoiDung.getHoVaTen());
        bl.setNoiDung(noiDung.trim());
        bl.setThoiGianGui(LocalDateTime.now());
        bl.setDaXoa(false);

        BinhLuanVideo daLuu = binhLuanVideoRepository.save(bl);

        // Tăng bộ đếm bình luận của video
        int luotBlMoi = (video.getTongLuotBinhLuan() != null ? video.getTongLuotBinhLuan() : 0) + 1;
        video.setTongLuotBinhLuan(luotBlMoi);
        videoNganReviewRepository.save(video);

        return chuyenSangDTO(daLuu, nguoiDung.getMaNguoiDung());
    }

    /**
     * Lấy danh sách bình luận của video
     */
    @Transactional(readOnly = true)
    public List<BinhLuanVideoDTO> layDanhSachBinhLuan(Long maVideo, Long maNguoiDungHienTai) {
        List<BinhLuanVideo> dsBinhLuan = binhLuanVideoRepository.findByVideo_MaVideoAndDaXoaFalseOrderByThoiGianGuiDesc(maVideo);
        return dsBinhLuan.stream()
                .map(bl -> chuyenSangDTO(bl, maNguoiDungHienTai))
                .collect(Collectors.toList());
    }

    /**
     * Tăng số lượt xem khi video được lướt tới
     */
    @Transactional
    public void tangLuotXem(Long maVideo) {
        try {
            videoNganReviewRepository.tangLuotXem(maVideo);
        } catch (Exception e) {
            log.warn("Không thể tăng lượt xem cho maVideo={}: {}", maVideo, e.getMessage());
        }
    }

    /**
     * Tăng số lượt chia sẻ video
     */
    @Transactional
    public int tangLuotChiaSe(Long maVideo) {
        VideoNganReview video = layVideoChiTiet(maVideo);
        int chiaSeMoi = (video.getTongLuotChiaSe() != null ? video.getTongLuotChiaSe() : 0) + 1;
        video.setTongLuotChiaSe(chiaSeMoi);
        videoNganReviewRepository.save(video);
        return chiaSeMoi;
    }

    /**
     * Thống kê toàn diện hiệu quả video của một KOC/người dùng
     */
    @Transactional(readOnly = true)
    public ThongKeVideoDTO layThongKeCuaNguoiDung(Long maNguoiDung) {
        long tongSoVideo = videoNganReviewRepository.countByNguoiDang_MaNguoiDungAndDaXoaFalse(maNguoiDung);
        long tongLuotXem = videoNganReviewRepository.tongLuotXemCuaNguoiDung(maNguoiDung);
        long tongLuotTim = videoNganReviewRepository.tongLuotTimCuaNguoiDung(maNguoiDung);
        long tongLuotBinhLuan = videoNganReviewRepository.tongLuotBinhLuanCuaNguoiDung(maNguoiDung);
        long tongLuotChiaSe = videoNganReviewRepository.tongLuotChiaSeCuaNguoiDung(maNguoiDung);

        double tiLeTuongTac = 0.0;
        if (tongLuotXem > 0) {
            tiLeTuongTac = ((double) (tongLuotTim + tongLuotBinhLuan + tongLuotChiaSe) / tongLuotXem) * 100.0;
            tiLeTuongTac = Math.round(tiLeTuongTac * 100.0) / 100.0;
        }

        return new ThongKeVideoDTO(
                tongSoVideo,
                tongLuotXem,
                tongLuotTim,
                tongLuotBinhLuan,
                tongLuotChiaSe,
                tiLeTuongTac
        );
    }

    /**
     * Tìm kiếm sản phẩm nhanh (Autocomplete) để gắn vào giỏ hàng video
     */
    @Transactional(readOnly = true)
    public List<SanPhamGanKemDTO> timKiemSanPhamDeGan(String tuKhoa) {
        List<SanPham> dsSanPham;
        if (StringUtils.hasText(tuKhoa)) {
            dsSanPham = sanPhamRepository.findAll().stream()
                    .filter(sp -> !Boolean.TRUE.equals(sp.getDaXoa()) && !Boolean.TRUE.equals(sp.getBiKhoa()))
                    .filter(sp -> sp.getTenSanPham() != null && sp.getTenSanPham().toLowerCase().contains(tuKhoa.toLowerCase().trim()))
                    .limit(10)
                    .collect(Collectors.toList());
        } else {
            dsSanPham = sanPhamRepository.findAll().stream()
                    .filter(sp -> !Boolean.TRUE.equals(sp.getDaXoa()) && !Boolean.TRUE.equals(sp.getBiKhoa()))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        return dsSanPham.stream().map(sp -> new SanPhamGanKemDTO(
                sp.getMaSanPham(),
                sp.getTenSanPham(),
                sp.getGiaCoBan(),
                null,
                sp.getDanhGiaTb(),
                sp.getTongDaBan(),
                sp.getGianHang() != null ? sp.getGianHang().getTenGianHang() : "FlexShop",
                sp.getDuongDanSlug()
        )).collect(Collectors.toList());
    }

    // ==========================================
    // HÀM TIỆN ÍCH NỘI BỘ
    // ==========================================

    private String xuLyLuuTepVideo(MultipartFile file, String linkFallback) {
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String extension = fileName != null && fileName.contains(".")
                    ? fileName.substring(fileName.lastIndexOf(".")).toLowerCase()
                    : ".mp4";

            List<String> dinhDangHopLe = Arrays.asList(
                    ".mp4", ".webm", ".mov", ".mkv",
                    ".jpg", ".jpeg", ".png", ".webp", ".gif"
            );

            if (!dinhDangHopLe.contains(extension)) {
                throw new IllegalArgumentException("Chỉ chấp nhận tệp video (MP4, WebM, MOV, MKV) hoặc hình ảnh review (JPG, PNG, WEBP, GIF)");
            }

            try {
                Path uploadPath = Paths.get(THU_MUC_UPLOAD_VIDEO);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String uniqueName = "vid_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;
                Path targetLocation = uploadPath.resolve(uniqueName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                return "/" + THU_MUC_UPLOAD_VIDEO + uniqueName;
            } catch (IOException ex) {
                log.error("Lỗi khi lưu tệp video/ảnh: {}", ex.getMessage());
                throw new RuntimeException("Không thể lưu tệp lên máy chủ: " + ex.getMessage());
            }
        }

        if (StringUtils.hasText(linkFallback)) {
            return linkFallback.trim();
        }

        throw new IllegalArgumentException("Vui lòng tải lên tệp (video/ảnh) hoặc cung cấp đường link hợp lệ");
    }

    private String xuLyLuuTepAnhPoster(MultipartFile file, String linkFallback) {
        if (file != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(THU_MUC_UPLOAD_ANH);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String ext = ".jpg";
                if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
                    ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();
                }
                List<String> dinhDangAnhHopLe = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");
                if (!dinhDangAnhHopLe.contains(ext)) {
                    ext = ".jpg";
                }
                String uniqueName = "thumb_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + ext;
                Path targetLocation = uploadPath.resolve(uniqueName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                return "/" + THU_MUC_UPLOAD_ANH + uniqueName;
            } catch (IOException ex) {
                log.warn("Không thể lưu poster video: {}", ex.getMessage());
            }
        }
        if (StringUtils.hasText(linkFallback)) {
            return linkFallback.trim();
        }
        return "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80";
    }

    private String chuanHoaHashtag(String input) {
        if (!StringUtils.hasText(input)) {
            return "#review #shopeevideo #dealhot";
        }
        String[] parts = input.split("[,\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            if (!p.startsWith("#")) {
                p = "#" + p;
            }
            if (sb.length() > 0) sb.append(" ");
            sb.append(p);
        }
        return sb.toString();
    }

    private BinhLuanVideoDTO chuyenSangDTO(BinhLuanVideo bl, Long maNguoiDungHienTai) {
        boolean laToi = maNguoiDungHienTai != null
                && bl.getNguoiDung() != null
                && maNguoiDungHienTai.equals(bl.getNguoiDung().getMaNguoiDung());

        String thoiGian = bl.getThoiGianGui() != null ? bl.getThoiGianGui().format(DINH_DANG_NGAY) : "";
        String anhDaiDien = bl.getNguoiDung() != null && StringUtils.hasText(bl.getNguoiDung().getAnhDaiDien())
                ? bl.getNguoiDung().getAnhDaiDien()
                : "https://ui-avatars.com/api/?name=" + (bl.getHoTenNguoiDung() != null ? bl.getHoTenNguoiDung() : "U") + "&background=random";

        return new BinhLuanVideoDTO(
                bl.getMaBinhLuan(),
                bl.getVideo() != null ? bl.getVideo().getMaVideo() : null,
                bl.getNguoiDung() != null ? bl.getNguoiDung().getMaNguoiDung() : null,
                bl.getHoTenNguoiDung(),
                anhDaiDien,
                bl.getNoiDung(),
                thoiGian,
                laToi
        );
    }
}
