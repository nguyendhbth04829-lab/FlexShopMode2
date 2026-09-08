package com.example.demo.controller.api;

import com.example.demo.dto.request.CapNhatHoSoRequest;
import com.example.demo.dto.request.DoiMatKhauRequest;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.dto.response.ThongKeHoSoResponse;
import com.example.demo.service.HoSoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST API phục vụ chức năng Hồ sơ cá nhân & Đổi mật khẩu (US-04)
 * Áp dụng cho mọi vai trò người dùng (All Roles)
 */
@RestController
@RequestMapping("/api/v1/ho-so")
@RequiredArgsConstructor
public class HoSoApiController {

    private final HoSoService hoSoService;

    /**
     * Lấy thông tin chi tiết hồ sơ người dùng hiện tại
     */
    @GetMapping
    public ResponseEntity<PhanHoiApi<NguoiDungResponse>> layHoSo() {
        NguoiDungResponse phanHoi = hoSoService.layHoSoHienTai();
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thông tin hồ sơ thành công", phanHoi));
    }

    /**
     * Cập nhật thông tin hồ sơ cơ bản (Họ tên, SĐT, Avatar URL)
     */
    @PutMapping
    public ResponseEntity<PhanHoiApi<NguoiDungResponse>> capNhatHoSo(@Valid @RequestBody CapNhatHoSoRequest yeuCau) {
        NguoiDungResponse phanHoi = hoSoService.capNhatHoSo(yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Cập nhật thông tin hồ sơ thành công", phanHoi));
    }

    /**
     * Tải lên ảnh đại diện mới từ máy tính
     */
    @PostMapping("/avatar")
    public ResponseEntity<PhanHoiApi<NguoiDungResponse>> taiLenAvatar(@RequestParam("file") MultipartFile file) {
        NguoiDungResponse phanHoi = hoSoService.taiLenAnhDaiDien(file);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Tải lên ảnh đại diện mới thành công", phanHoi));
    }

    /**
     * Đổi mật khẩu tài khoản người dùng
     * (Validate không được trùng mật khẩu cũ)
     */
    @PutMapping("/doi-mat-khau")
    public ResponseEntity<PhanHoiApi<NguoiDungResponse>> doiMatKhau(@Valid @RequestBody DoiMatKhauRequest yeuCau) {
        NguoiDungResponse phanHoi = hoSoService.doiMatKhau(yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Đổi mật khẩu thành công! Mật khẩu mới đã được cập nhật.", phanHoi));
    }

    /**
     * Thống kê tổng quan hoạt động của tài khoản
     */
    @GetMapping("/thong-ke")
    public ResponseEntity<PhanHoiApi<ThongKeHoSoResponse>> layThongKe() {
        ThongKeHoSoResponse phanHoi = hoSoService.layThongKeHoSo();
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy dữ liệu thống kê hồ sơ thành công", phanHoi));
    }
}
