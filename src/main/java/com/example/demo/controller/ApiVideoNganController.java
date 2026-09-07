package com.example.demo.controller;

import com.example.demo.dto.BinhLuanVideoDTO;
import com.example.demo.dto.SanPhamGanKemDTO;
import com.example.demo.entity.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.VideoNganService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller phục vụ tương tác trên Shopee Video (US-62)
 * Hỗ trợ các hành động AJAX: Thả tim, Bình luận, Tăng view, Gắn sản phẩm.
 */
@Slf4j
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class ApiVideoNganController {

    private final VideoNganService videoNganService;
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
     * 1. Thả tim (Like) hoặc Bỏ thích (Unlike) video ngắn review
     */
    @PostMapping("/{maVideo}/thich")
    public ResponseEntity<?> thichVideo(@PathVariable("maVideo") Long maVideo) {
        NguoiDung nguoiDung = layNguoiDungHienTai();
        if (nguoiDung == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập để thả tim"));
        }
        try {
            Map<String, Object> ketQua = videoNganService.thichHoacBoThich(maVideo, nguoiDung);
            ketQua.put("success", true);
            return ResponseEntity.ok(ketQua);
        } catch (Exception e) {
            log.error("Lỗi khi thả tim video {}: {}", maVideo, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 2. Gửi bình luận mới vào video
     */
    @PostMapping("/{maVideo}/binh-luan")
    public ResponseEntity<?> guiBinhLuan(
            @PathVariable("maVideo") Long maVideo,
            @RequestBody Map<String, String> payload
    ) {
        NguoiDung nguoiDung = layNguoiDungHienTai();
        if (nguoiDung == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập để bình luận"));
        }

        String noiDung = payload.get("noiDung");
        try {
            BinhLuanVideoDTO blDto = videoNganService.themBinhLuan(maVideo, noiDung, nguoiDung);
            return ResponseEntity.ok(Map.of("success", true, "binhLuan", blDto));
        } catch (Exception e) {
            log.error("Lỗi khi thêm bình luận video {}: {}", maVideo, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 3. Lấy danh sách bình luận của video
     */
    @GetMapping("/{maVideo}/binh-luan")
    public ResponseEntity<?> layDanhSachBinhLuan(@PathVariable("maVideo") Long maVideo) {
        NguoiDung nguoiDung = layNguoiDungHienTai();
        Long maNguoiDung = nguoiDung != null ? nguoiDung.getMaNguoiDung() : null;
        try {
            List<BinhLuanVideoDTO> ds = videoNganService.layDanhSachBinhLuan(maVideo, maNguoiDung);
            return ResponseEntity.ok(Map.of("success", true, "dsBinhLuan", ds));
        } catch (Exception e) {
            log.error("Lỗi khi lấy bình luận video {}: {}", maVideo, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 4. Tăng lượt xem (khi cuộn video tới trong tầm nhìn)
     */
    @PostMapping("/{maVideo}/tang-view")
    public ResponseEntity<?> tangView(@PathVariable("maVideo") Long maVideo) {
        videoNganService.tangLuotXem(maVideo);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * 5. Tăng lượt chia sẻ
     */
    @PostMapping("/{maVideo}/chia-se")
    public ResponseEntity<?> chiaSe(@PathVariable("maVideo") Long maVideo) {
        int luotMoi = videoNganService.tangLuotChiaSe(maVideo);
        return ResponseEntity.ok(Map.of("success", true, "tongLuotChiaSe", luotMoi));
    }

    /**
     * 6. Tìm kiếm sản phẩm nhanh để gắn vào giỏ hàng
     */
    @GetMapping("/tim-kiem-san-pham")
    public ResponseEntity<List<SanPhamGanKemDTO>> timKiemSanPham(
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa
    ) {
        List<SanPhamGanKemDTO> ds = videoNganService.timKiemSanPhamDeGan(tuKhoa);
        return ResponseEntity.ok(ds);
    }
}

