package com.example.demo.controller;

import com.example.demo.dto.KetQuaQuayThuongDTO;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.PhanThuongVongQuay;
import com.example.demo.entity.VongQuayMayMan;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.VongQuayMayManService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API phục vụ Vòng Quay May Mắn (Shopee Lucky Wheel - US-66)
 * Cung cấp API AJAX cho Animation Canvas HTML5 quay số mượt mà, trả thưởng tức thì.
 */
@RestController
@RequestMapping("/api/vong-quay")
public class ApiVongQuayMayManController {

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    @Autowired
    private VongQuayMayManService vongQuayMayManService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    private NguoiDung layKhachHangHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().contains("khach"))
                        .findFirst()
                        .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElse(null)));
    }

    /**
     * API Thực hiện quay thưởng
     */
    @PostMapping("/quay")
    public ResponseEntity<?> quayThuong(HttpServletRequest request) {
        NguoiDung khachHang = layKhachHangHienTai();
        if (khachHang == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBao", "Không tìm thấy thông tin tài khoản!");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            String ip = request.getRemoteAddr();
            KetQuaQuayThuongDTO ketQua = vongQuayMayManService.thucHienQuayThuong(khachHang.getMaNguoiDung(), ip);
            return ResponseEntity.ok(ketQua);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBao", ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBao", "Lỗi trong quá trình quay thưởng: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    /**
     * API Đổi xu lấy thêm lượt quay
     */
    @PostMapping("/doi-luot")
    public ResponseEntity<?> doiLuotQuay() {
        NguoiDung khachHang = layKhachHangHienTai();
        if (khachHang == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBao", "Không tìm thấy thông tin tài khoản!");
            return ResponseEntity.badRequest().body(err);
        }

        try {
            Long soXuMoi = vongQuayMayManService.doiXuLayLuotQuay(khachHang.getMaNguoiDung());
            int soLuotMoi = vongQuayMayManService.tinhSoLuotQuayConLai(khachHang.getMaNguoiDung());

            Map<String, Object> res = new HashMap<>();
            res.put("thanhCong", true);
            res.put("thongBao", "Đổi 1.000 Xu thành công! Bạn có thêm 1 lượt quay mới.");
            res.put("soXuHienTai", soXuMoi);
            res.put("soLuotQuayConLai", soLuotMoi);
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("thanhCong", false);
            err.put("thongBao", ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * API Lấy danh sách 8 ô quà
     */
    @GetMapping("/danh-sach-qua")
    public ResponseEntity<List<PhanThuongVongQuay>> layDanhSachQua() {
        return ResponseEntity.ok(vongQuayMayManService.layDanhSachPhanThuong());
    }

    /**
     * API Lấy danh sách người trúng thưởng gần nhất (Ticker)
     */
    @GetMapping("/vinh-danh")
    public ResponseEntity<List<VongQuayMayMan>> layDanhSachVinhDanh() {
        return ResponseEntity.ok(vongQuayMayManService.layDanhSachVinhDanhMoiNhat());
    }
}
