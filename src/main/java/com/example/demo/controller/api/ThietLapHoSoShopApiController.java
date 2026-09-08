package com.example.demo.controller.api;

import com.example.demo.dto.request.ThietLapHoSoShopRequest;
import com.example.demo.dto.response.LichSuThietLapShopResponse;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.dto.response.ThietLapHoSoShopResponse;
import com.example.demo.dto.response.ThongKeThietLapShopResponse;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.ThietLapHoSoShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/thiet-lap-shop")
@RequiredArgsConstructor
public class ThietLapHoSoShopApiController {

    private final ThietLapHoSoShopService thietLapHoSoShopService;

    /**
     * Lấy toàn bộ thông tin thiết lập hồ sơ shop, tiến độ hoàn thiện và checklist
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<ThietLapHoSoShopResponse>> layThietLapShop(
            @AuthenticationPrincipal NguoiDungPrincipal principal
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        ThietLapHoSoShopResponse phanHoi = thietLapHoSoShopService.layThietLapShop(maNguoiDung);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thông tin thiết lập shop thành công!", phanHoi));
    }

    /**
     * Lưu thông tin thiết lập hồ sơ shop (Banner, Logo, Giờ hoạt động, Địa chỉ lấy hàng)
     */
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.POST}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<ThietLapHoSoShopResponse>> capNhatThietLapShop(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @ModelAttribute @Valid ThietLapHoSoShopRequest yeuCau,
            @RequestParam(value = "fileLogo", required = false) MultipartFile fileLogo,
            @RequestParam(value = "fileBanner", required = false) MultipartFile fileBanner
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        ThietLapHoSoShopResponse phanHoi = thietLapHoSoShopService.capNhatThietLapShop(maNguoiDung, yeuCau, fileLogo, fileBanner);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Thiết lập hồ sơ Shop thành công!", phanHoi));
    }

    /**
     * Bật / Tắt nhanh trạng thái nhận đơn (Đang mở cửa / Tạm nghỉ)
     */
    @PatchMapping("/trang-thai-mo-cua")
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<ThietLapHoSoShopResponse>> chuyenTrangThaiNhanDon(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @RequestParam("dangMoCua") Boolean dangMoCua
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        ThietLapHoSoShopResponse phanHoi = thietLapHoSoShopService.chuyenTrangThaiNhanDon(maNguoiDung, dangMoCua);
        String thongBao = Boolean.TRUE.equals(dangMoCua) ? "Đã mở cửa nhận đơn hàng!" : "Đã tạm dừng nhận đơn hàng!";
        return ResponseEntity.ok(PhanHoiApi.thanhCong(thongBao, phanHoi));
    }

    /**
     * Lấy danh sách lịch sử thay đổi thiết lập hồ sơ shop (có tìm kiếm, lọc và phân trang)
     */
    @GetMapping("/lich-su")
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<Page<LichSuThietLapShopResponse>>> layLichSuThayDoi(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @RequestParam(value = "loaiThayDoi", required = false) String loaiThayDoi,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        Pageable pageable = PageRequest.of(page, size);
        Page<LichSuThietLapShopResponse> phanTrang = thietLapHoSoShopService.layLichSuThayDoi(maNguoiDung, loaiThayDoi, tuKhoa, pageable);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy lịch sử thay đổi thiết lập thành công!", phanTrang));
    }

    /**
     * Lấy thông tin thống kê thiết lập hồ sơ shop
     */
    @GetMapping("/thong-ke")
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<ThongKeThietLapShopResponse>> layThongKe(
            @AuthenticationPrincipal NguoiDungPrincipal principal
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        ThongKeThietLapShopResponse thongKe = thietLapHoSoShopService.layThongKeThietLap(maNguoiDung);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thống kê thiết lập thành công!", thongKe));
    }
}
