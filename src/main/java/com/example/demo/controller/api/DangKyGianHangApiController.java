package com.example.demo.controller.api;

import com.example.demo.dto.request.DangKyGianHangRequest;
import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.dto.response.PhanTrangResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.DangKyGianHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/dang-ky-shop")
@RequiredArgsConstructor
public class DangKyGianHangApiController {

    private final DangKyGianHangService dangKyGianHangService;

    /**
     * US-08: Gửi hồ sơ đăng ký mở gian hàng kèm tệp Giấy phép kinh doanh
     * - Trạng thái tự động chuyển thành CHO_DUYET
     * - Yêu cầu tệp PDF/JPEG/PNG dung lượng < 5MB
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<GianHangResponse>> dangKyShop(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @ModelAttribute @Valid DangKyGianHangRequest yeuCau,
            @RequestParam(value = "fileGiayPhep", required = false) MultipartFile fileGiayPhep,
            @RequestParam(value = "fileLogo", required = false) MultipartFile fileLogo
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        GianHangResponse phanHoi = dangKyGianHangService.dangKyGianHang(maNguoiDung, yeuCau, fileGiayPhep, fileLogo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PhanHoiApi.thanhCong("Gửi yêu cầu đăng ký mở gian hàng thành công! Đơn của bạn đã được chuyển về trạng thái CHỜ DUYỆT.", phanHoi));
    }

    /**
     * Cập nhật / Nộp lại hồ sơ khi bị từ chối hoặc chỉnh sửa khi đang chờ duyệt
     */
    @PutMapping(value = "/cap-nhat", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<GianHangResponse>> capNhatHoSo(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @ModelAttribute @Valid DangKyGianHangRequest yeuCau,
            @RequestParam(value = "fileGiayPhep", required = false) MultipartFile fileGiayPhep,
            @RequestParam(value = "fileLogo", required = false) MultipartFile fileLogo
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        GianHangResponse phanHoi = dangKyGianHangService.capNhatHoSoDangKy(maNguoiDung, yeuCau, fileGiayPhep, fileLogo);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Cập nhật hồ sơ đăng ký gian hàng thành công! Đơn đã được chuyển về trạng thái CHỜ DUYỆT.", phanHoi));
    }

    /**
     * Tra cứu trạng thái hồ sơ đăng ký của người dùng hiện tại
     */
    @GetMapping("/trang-thai")
    @PreAuthorize("hasAnyRole('KHACH_HANG', 'NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<GianHangResponse>> layTrangThai(
            @AuthenticationPrincipal NguoiDungPrincipal principal
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        GianHangResponse gianHang = dangKyGianHangService.layThongTinGianHangCuaToi(maNguoiDung);
        if (gianHang == null) {
            return ResponseEntity.ok(PhanHoiApi.thanhCong("Tài khoản chưa gửi hồ sơ đăng ký gian hàng nào", null));
        }
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy trạng thái hồ sơ đăng ký thành công", gianHang));
    }

    /**
     * Kiểm tra tính khả dụng của Tên gian hàng & Tự động sinh Slug hợp lệ thời gian thực
     */
    @GetMapping("/kiem-tra-slug")
    public ResponseEntity<PhanHoiApi<Map<String, Object>>> kiemTraVaGoiYSlug(
            @RequestParam(required = false) String tenGianHang,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) Long maGianHang
    ) {
        String slugGoiY = null;
        boolean tenHopLe = true;
        boolean slugHopLe = true;

        if (StringUtils.hasText(tenGianHang)) {
            tenHopLe = dangKyGianHangService.kiemTraTenGianHangKhaDung(tenGianHang, maGianHang);
            slugGoiY = dangKyGianHangService.taoDuongDanSlugTuTenShop(tenGianHang, maGianHang);
        }

        if (StringUtils.hasText(slug)) {
            slugHopLe = dangKyGianHangService.kiemTraSlugKhaDung(slug, maGianHang);
        }

        Map<String, Object> ketQua = Map.of(
                "tenKhaDung", tenHopLe,
                "slugKhaDung", slugHopLe,
                "slugGoiY", slugGoiY != null ? slugGoiY : ""
        );

        return ResponseEntity.ok(PhanHoiApi.thanhCong("Kiểm tra slug thành công", ketQua));
    }

    /**
     * Danh sách phân trang, tìm kiếm và lọc hồ sơ đăng ký mở gian hàng (Mở rộng cho Quản trị & Theo dõi)
     */
    @GetMapping("/danh-sach")
    @PreAuthorize("hasAnyRole('ADMIN', 'CSKH')")
    public ResponseEntity<PhanHoiApi<PhanTrangResponse<GianHangResponse>>> layDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PhanTrangResponse<GianHangResponse> danhSach = dangKyGianHangService.danhSachGianHangPhanTrang(keyword, trangThai, page, size);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy danh sách hồ sơ gian hàng thành công", danhSach));
    }

    /**
     * Thống kê số lượng hồ sơ theo các trạng thái (CHO_DUYET, HOAT_DONG, TU_CHOI, TAM_KHOA)
     */
    @GetMapping("/thong-ke")
    @PreAuthorize("hasAnyRole('ADMIN', 'CSKH')")
    public ResponseEntity<PhanHoiApi<ThongKeGianHangResponse>> layThongKe() {
        ThongKeGianHangResponse thongKe = dangKyGianHangService.layThongKeGianHang();
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy dữ liệu thống kê gian hàng thành công", thongKe));
    }
}
