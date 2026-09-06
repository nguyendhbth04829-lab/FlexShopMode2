package com.example.demo.controller;

import com.example.demo.dto.DeXuatTraGiaForm;
import com.example.demo.dto.GuiTinNhanForm;
import com.example.demo.dto.XuLyTraGiaForm;
import com.example.demo.entity.DeXuatTraGia;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.TinNhan;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.TinNhanRepository;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller phục vụ gửi/nhận tin nhắn, gửi thẻ sản phẩm, tạo và xét duyệt trả giá (US-59)
 */
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";
    private static final String EMAIL_SELLER_MAC_DINH = "techzone@flexshop.vn";

    @Autowired
    private ChatService chatService;

    @Autowired
    private TinNhanRepository tinNhanRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    private Long layMaNguoiDungHienTai(String loai) {
        String email = "SHOP".equalsIgnoreCase(loai) ? EMAIL_SELLER_MAC_DINH : EMAIL_KHACH_HANG_MAC_DINH;
        return nguoiDungRepository.findByEmail(email)
                .map(NguoiDung::getMaNguoiDung)
                .orElse("SHOP".equalsIgnoreCase(loai) ? 2L : 4L);
    }

    /**
     * API 1: Gửi tin nhắn văn bản thông thường
     */
    @PostMapping("/gui-tin-nhan")
    public ResponseEntity<?> guiTinNhan(@Valid @RequestBody GuiTinNhanForm form) {
        try {
            String loai = (form.getLoaiNguoiGui() != null && !form.getLoaiNguoiGui().isEmpty())
                    ? form.getLoaiNguoiGui() : "KHACH_HANG";
            Long maNguoiGui = layMaNguoiDungHienTai(loai);

            TinNhan tinNhan = chatService.guiTinNhan(
                    form.getMaCuocTroChuyen(),
                    maNguoiGui,
                    loai,
                    form.getNoiDung()
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("thanhCong", true);
            resp.put("tinNhan", tinNhan);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBaoLoi", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * API 2: Gửi thẻ sản phẩm vào khung chat
     */
    @PostMapping("/gui-the-san-pham")
    public ResponseEntity<?> guiTheSanPham(
            @RequestParam("maCuocTroChuyen") Long maCuocTroChuyen,
            @RequestParam("maSanPham") Long maSanPham,
            @RequestParam(value = "maBienThe", required = false) Long maBienThe,
            @RequestParam(value = "loaiNguoiGui", defaultValue = "KHACH_HANG") String loaiNguoiGui
    ) {
        try {
            Long maNguoiGui = layMaNguoiDungHienTai(loaiNguoiGui);
            TinNhan tinNhan = chatService.guiTheSanPham(maCuocTroChuyen, maNguoiGui, loaiNguoiGui, maSanPham, maBienThe);

            Map<String, Object> resp = new HashMap<>();
            resp.put("thanhCong", true);
            resp.put("tinNhan", tinNhan);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBaoLoi", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * API 3: Khách hàng tạo Đề xuất Trả Giá Sản Phẩm (Make an Offer)
     */
    @PostMapping("/de-xuat-tra-gia")
    public ResponseEntity<?> taoDeXuatTraGia(@Valid @RequestBody DeXuatTraGiaForm form) {
        try {
            Long maKhachHang = layMaNguoiDungHienTai("KHACH_HANG");
            DeXuatTraGia deXuat = chatService.taoDeXuatTraGia(form, maKhachHang);

            Map<String, Object> resp = new HashMap<>();
            resp.put("thanhCong", true);
            resp.put("thongBao", "Đã gửi đề xuất trả giá thành công tới Shop!");
            resp.put("deXuat", deXuat);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBaoLoi", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * API 4: Shop duyệt Đề xuất Trả Giá (Đồng ý / Từ chối / Phản hồi giá đối ứng)
     */
    @PostMapping("/xu-ly-tra-gia")
    public ResponseEntity<?> xuLyTraGia(@Valid @RequestBody XuLyTraGiaForm form) {
        try {
            Long maSeller = layMaNguoiDungHienTai("SHOP");
            DeXuatTraGia deXuat = chatService.xuLyDuyetTraGia(form, maSeller);

            Map<String, Object> resp = new HashMap<>();
            resp.put("thanhCong", true);
            resp.put("thongBao", "Đã xử lý đề xuất trả giá thành công!");
            resp.put("deXuat", deXuat);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBaoLoi", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * API 5: Lấy danh sách tin nhắn mới (Polling fallback khi WebSocket gián đoạn)
     */
    @GetMapping("/tin-nhan-moi")
    public ResponseEntity<?> layTinNhanMoi(
            @RequestParam("maCuocTroChuyen") Long maCuocTroChuyen,
            @RequestParam(value = "maTinNhanCuoi", defaultValue = "0") Long maTinNhanCuoi
    ) {
        List<TinNhan> tinNhans = tinNhanRepository.findByCuocTroChuyen_MaCuocTroChuyenAndMaTinNhanGreaterThanOrderByNgayTaoAsc(
                maCuocTroChuyen, maTinNhanCuoi
        );
        return ResponseEntity.ok(tinNhans);
    }

    /**
     * API 6: Đánh dấu đã đọc tin nhắn
     */
    @PostMapping("/danh-dau-da-xem")
    public ResponseEntity<?> danhDauDaXem(
            @RequestParam("maCuocTroChuyen") Long maCuocTroChuyen,
            @RequestParam(value = "loaiNguoiXem", defaultValue = "KHACH_HANG") String loaiNguoiXem
    ) {
        chatService.danhDauDaXem(maCuocTroChuyen, loaiNguoiXem);
        return ResponseEntity.ok(Map.of("thanhCong", true));
    }
}
