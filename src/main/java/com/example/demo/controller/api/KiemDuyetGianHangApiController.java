package com.example.demo.controller.api;

import com.example.demo.dto.request.TuChoiGianHangRequest;
import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.dto.response.PhanTrangResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.service.KiemDuyetGianHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/kiem-duyet-shop")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class KiemDuyetGianHangApiController {

    private final KiemDuyetGianHangService kiemDuyetGianHangService;

    /**
     * US-09: Lấy danh sách hồ sơ mở Shop có phân trang, tìm kiếm và lọc trạng thái
     */
    @GetMapping
    public ResponseEntity<PhanHoiApi<PhanTrangResponse<GianHangResponse>>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PhanTrangResponse<GianHangResponse> danhSach = kiemDuyetGianHangService.danhSachGianHangPhanTrang(keyword, trangThai, page, size);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy danh sách hồ sơ gian hàng thành công", danhSach));
    }

    /**
     * Thống kê KPI hồ sơ kiểm duyệt gian hàng
     */
    @GetMapping("/thong-ke")
    public ResponseEntity<PhanHoiApi<ThongKeGianHangResponse>> layThongKe() {
        ThongKeGianHangResponse thongKe = kiemDuyetGianHangService.layThongKeKiemDuyet();
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thống kê kiểm duyệt gian hàng thành công", thongKe));
    }

    /**
     * Xem chi tiết 1 hồ sơ gian hàng
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhanHoiApi<GianHangResponse>> layChiTiet(@PathVariable Long id) {
        GianHangResponse gianHang = kiemDuyetGianHangService.layChiTietGianHang(id);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy chi tiết hồ sơ gian hàng thành công", gianHang));
    }

    /**
     * US-09: Phê duyệt (Approve) yêu cầu mở gian hàng của Seller
     * - Cập nhật gian hàng thành HOAT_DONG
     * - Cập nhật giấy phép thành DA_DUYET
     * - Gán vai trò NGUOI_BAN vào nguoi_dung_vai_tro
     */
    @PutMapping("/{id}/phe-duyet")
    public ResponseEntity<PhanHoiApi<GianHangResponse>> pheDuyet(@PathVariable Long id) {
        GianHangResponse phanHoi = kiemDuyetGianHangService.pheDuyetGianHang(id);
        return ResponseEntity.ok(PhanHoiApi.thanhCong(
                "Phê duyệt gian hàng '" + phanHoi.getTenGianHang() + "' thành công! Gian hàng đã chuyển sang trạng thái HOẠT ĐỘNG và chủ sở hữu đã được cấp vai trò NGƯỜI BÁN.",
                phanHoi
        ));
    }

    /**
     * US-09: Từ chối (Reject) yêu cầu mở gian hàng của Seller
     * - Cập nhật gian hàng thành TU_CHOI
     * - Bắt buộc ghi nhận lý do từ chối
     * - Cập nhật giấy phép thành TU_CHOI
     */
    @PutMapping("/{id}/tu-choi")
    public ResponseEntity<PhanHoiApi<GianHangResponse>> tuChoi(
            @PathVariable Long id,
            @Valid @RequestBody TuChoiGianHangRequest yeuCau
    ) {
        GianHangResponse phanHoi = kiemDuyetGianHangService.tuChoiGianHang(id, yeuCau);
        return ResponseEntity.ok(PhanHoiApi.thanhCong(
                "Đã từ chối yêu cầu mở gian hàng '" + phanHoi.getTenGianHang() + "'. Lý do đã được ghi nhận vào hồ sơ.",
                phanHoi
        ));
    }
}
