package com.example.demo.controller.api;

import com.example.demo.dto.request.CapNhatDiaChiRequest;
import com.example.demo.dto.request.TaoDiaChiRequest;
import com.example.demo.dto.response.DiaChiResponse;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.dto.response.ThongKeDiaChiResponse;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.DiaChiService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/dia-chi")
@PreAuthorize("hasAnyRole('KHACH_HANG', 'NGUOI_BAN', 'ADMIN')")
@RequiredArgsConstructor
public class DiaChiApiController {

    private final DiaChiService diaChiService;

    /**
     * US-05: Lấy danh sách địa chỉ nhận hàng có phân trang, tìm kiếm và lọc
     */
    @GetMapping
    public ResponseEntity<PhanHoiApi<com.example.demo.dto.response.PhanTrangResponse<DiaChiResponse>>> layDanhSach(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) Boolean laMacDinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "laMacDinh,desc;ngayTao,desc") String sort) {

        Long userId = layMaNguoiDung(principal);
        Sort sortObj = Sort.by(Sort.Order.desc("laMacDinh"), Sort.Order.desc("ngayTao"));
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<DiaChiResponse> danhSach = diaChiService.layDanhSachPhanTrang(userId, keyword, tinhThanh, laMacDinh, pageable);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy danh sách địa chỉ thành công", com.example.demo.dto.response.PhanTrangResponse.tuPage(danhSach)));
    }

    /**
     * Lấy toàn bộ danh sách địa chỉ (dùng cho dropdown chọn địa chỉ khi mua hàng)
     */
    @GetMapping("/toan-bo")
    public ResponseEntity<PhanHoiApi<List<DiaChiResponse>>> layToanBo(
            @AuthenticationPrincipal NguoiDungPrincipal principal) {
        Long userId = layMaNguoiDung(principal);
        List<DiaChiResponse> danhSach = diaChiService.layToanBoDiaChi(userId);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy toàn bộ địa chỉ thành công", danhSach));
    }

    /**
     * Thống kê sổ địa chỉ (tổng số X/20, còn lại, địa chỉ mặc định)
     */
    @GetMapping("/thong-ke")
    public ResponseEntity<PhanHoiApi<ThongKeDiaChiResponse>> thongKe(
            @AuthenticationPrincipal NguoiDungPrincipal principal) {
        Long userId = layMaNguoiDung(principal);
        ThongKeDiaChiResponse thongKe = diaChiService.thongKeDiaChi(userId);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thống kê sổ địa chỉ thành công", thongKe));
    }

    /**
     * Lấy địa chỉ mặc định
     */
    @GetMapping("/mac-dinh")
    public ResponseEntity<PhanHoiApi<DiaChiResponse>> layMacDinh(
            @AuthenticationPrincipal NguoiDungPrincipal principal) {
        Long userId = layMaNguoiDung(principal);
        DiaChiResponse macDinh = diaChiService.layDiaChiMacDinh(userId)
                .orElseThrow(() -> new NgoaiLeUngDung("Bạn chưa thiết lập địa chỉ mặc định nào", HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy địa chỉ mặc định thành công", macDinh));
    }

    /**
     * Chi tiết 1 địa chỉ
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhanHoiApi<DiaChiResponse>> layChiTiet(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal NguoiDungPrincipal principal) {
        Long userId = layMaNguoiDung(principal);
        DiaChiResponse chiTiet = diaChiService.layChiTietDiaChi(id, userId);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy chi tiết địa chỉ thành công", chiTiet));
    }

    /**
     * US-05: Thêm mới địa chỉ nhận hàng
     * - Tối đa 20 địa chỉ
     * - Nếu set la_mac_dinh = true: Tự động chuyển các địa chỉ khác về false trong 1 Transaction
     */
    @PostMapping
    public ResponseEntity<PhanHoiApi<DiaChiResponse>> taoDiaChi(
            @Valid @RequestBody TaoDiaChiRequest yeuCau,
            @AuthenticationPrincipal NguoiDungPrincipal principal) {
        Long userId = layMaNguoiDung(principal);
        DiaChiResponse diaChiMoi = diaChiService.taoDiaChi(yeuCau, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PhanHoiApi.thanhCong("Thêm địa chỉ giao hàng mới thành công!", diaChiMoi));
    }

    /**
     * US-05: Cập nhật địa chỉ nhận hàng
     */
    @PutMapping("/{id}")
    public ResponseEntity<PhanHoiApi<DiaChiResponse>> capNhatDiaChi(
            @PathVariable("id") Long id,
            @Valid @RequestBody CapNhatDiaChiRequest yeuCau,
            @AuthenticationPrincipal NguoiDungPrincipal principal) {
        Long userId = layMaNguoiDung(principal);
        DiaChiResponse capNhat = diaChiService.capNhatDiaChi(id, yeuCau, userId);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Cập nhật địa chỉ thành công!", capNhat));
    }

    /**
     * US-05 Nghiệp vụ cốt lõi: Thiết lập địa chỉ mặc định
     * Tự động chuyển tất cả địa chỉ khác về la_mac_dinh = 0 trong cùng 1 Transaction
     */
    @PatchMapping("/{id}/dat-mac-dinh")
    public ResponseEntity<PhanHoiApi<DiaChiResponse>> datLamMacDinh(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal NguoiDungPrincipal principal) {
        Long userId = layMaNguoiDung(principal);
        DiaChiResponse macDinh = diaChiService.datLamMacDinh(id, userId);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Đã thiết lập địa chỉ này làm mặc định!", macDinh));
    }

    /**
     * US-05: Xóa địa chỉ (Xóa mềm)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<PhanHoiApi<Void>> xoaDiaChi(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal NguoiDungPrincipal principal) {
        Long userId = layMaNguoiDung(principal);
        diaChiService.xoaDiaChi(id, userId);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Đã xóa địa chỉ giao hàng thành công!", null));
    }

    private Long layMaNguoiDung(NguoiDungPrincipal principal) {
        if (principal == null) {
            throw new NgoaiLeUngDung("Vui lòng đăng nhập để thực hiện chức năng này!", HttpStatus.UNAUTHORIZED);
        }
        return principal.getMaNguoiDung();
    }
}
