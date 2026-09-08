package com.example.demo.controller.api;

import com.example.demo.dto.request.CapNhatTrangThaiNguoiDungRequest;
import com.example.demo.dto.request.PhanQuyenNguoiDungRequest;
import com.example.demo.dto.request.TaoNhanVienRequest;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.dto.response.QuanLyNguoiDungResponse;
import com.example.demo.dto.response.ThongKeNguoiDungResponse;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.QuanLyNguoiDungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Quản lý người dùng & Phân quyền nội bộ Admin (US-06)
 * Quyền hạn: CHỈ DÀNH RIÊNG CHO ADMIN
 */
@RestController
@RequestMapping("/api/v1/admin/nguoi-dung")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class QuanLyNguoiDungApiController {

    private final QuanLyNguoiDungService quanLyNguoiDungService;

    /**
     * 1. Lấy danh sách người dùng phân trang, tìm kiếm đa tiêu chí, lọc vai trò & trạng thái
     */
    @GetMapping
    public ResponseEntity<PhanHoiApi<Page<QuanLyNguoiDungResponse>>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vaiTro,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ngayTao") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);
        Page<QuanLyNguoiDungResponse> ketQua = quanLyNguoiDungService.layDanhSachNguoiDung(keyword, vaiTro, trangThai, pageable);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy danh sách người dùng thành công", ketQua));
    }

    /**
     * 2. Lấy số liệu thống kê người dùng KPI
     */
    @GetMapping("/thong-ke")
    public ResponseEntity<PhanHoiApi<ThongKeNguoiDungResponse>> layThongKe() {
        ThongKeNguoiDungResponse thongKe = quanLyNguoiDungService.layThongKeNguoiDung();
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thống kê người dùng thành công", thongKe));
    }

    /**
     * 3. Lấy chi tiết thông tin một người dùng
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhanHoiApi<QuanLyNguoiDungResponse>> layChiTiet(@PathVariable Long id) {
        QuanLyNguoiDungResponse chiTiet = quanLyNguoiDungService.layChiTietNguoiDung(id);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thông tin người dùng thành công", chiTiet));
    }

    /**
     * 4. Cấp tài khoản nhân viên nội bộ (Shipper hoặc CSKH)
     */
    @PostMapping("/tao-nhan-vien")
    public ResponseEntity<PhanHoiApi<QuanLyNguoiDungResponse>> taoNhanVien(
            @Valid @RequestBody TaoNhanVienRequest request,
            @AuthenticationPrincipal NguoiDungPrincipal principal
    ) {
        Long adminId = principal != null ? principal.getMaNguoiDung() : null;
        QuanLyNguoiDungResponse nhanVien = quanLyNguoiDungService.taoTaiKhoanNhanVien(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PhanHoiApi.thanhCong("Cấp tài khoản nhân viên thành công!", nhanVien));
    }

    /**
     * 5. Khóa hoặc Mở khóa tài khoản người dùng
     */
    @PatchMapping("/{id}/trang-thai")
    public ResponseEntity<PhanHoiApi<QuanLyNguoiDungResponse>> doiTrangThai(
            @PathVariable Long id,
            @Valid @RequestBody CapNhatTrangThaiNguoiDungRequest request,
            @AuthenticationPrincipal NguoiDungPrincipal principal
    ) {
        Long adminId = principal != null ? principal.getMaNguoiDung() : null;
        QuanLyNguoiDungResponse capNhat = quanLyNguoiDungService.doiTrangThaiKhoa(id, request, adminId);
        String thongBao = "HOAT_DONG".equalsIgnoreCase(capNhat.getTrangThai())
                ? "Mở khóa tài khoản thành công!"
                : "Khóa tài khoản thành công!";
        return ResponseEntity.ok(PhanHoiApi.thanhCong(thongBao, capNhat));
    }

    /**
     * 6. Phân quyền vai trò người dùng
     */
    @PutMapping("/{id}/phan-quyen")
    public ResponseEntity<PhanHoiApi<QuanLyNguoiDungResponse>> phanQuyen(
            @PathVariable Long id,
            @Valid @RequestBody PhanQuyenNguoiDungRequest request,
            @AuthenticationPrincipal NguoiDungPrincipal principal
    ) {
        Long adminId = principal != null ? principal.getMaNguoiDung() : null;
        QuanLyNguoiDungResponse capNhat = quanLyNguoiDungService.phanQuyenNguoiDung(id, request, adminId);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Cập nhật phân quyền người dùng thành công!", capNhat));
    }
}
