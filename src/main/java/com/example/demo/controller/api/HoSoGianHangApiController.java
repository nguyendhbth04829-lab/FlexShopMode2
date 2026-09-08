package com.example.demo.controller.api;

import com.example.demo.dto.request.CapNhatThongTinShopRequest;
import com.example.demo.dto.request.ThemChungChiRequest;
import com.example.demo.dto.response.ChungChiGianHangResponse;
import com.example.demo.dto.response.HoSoGianHangDayDuResponse;
import com.example.demo.dto.response.PhanHoiApi;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.HoSoGianHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/ho-so-shop")
@RequiredArgsConstructor
public class HoSoGianHangApiController {

    private final HoSoGianHangService hoSoGianHangService;

    /**
     * Lấy toàn bộ thông tin hồ sơ gian hàng, thống kê và danh sách chứng chỉ
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<HoSoGianHangDayDuResponse>> layHoSoShop(
            @AuthenticationPrincipal NguoiDungPrincipal principal
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        HoSoGianHangDayDuResponse hoSo = hoSoGianHangService.layHoSoGianHangCuaToi(maNguoiDung);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Lấy thông tin hồ sơ gian hàng thành công!", hoSo));
    }

    /**
     * Cập nhật thông tin gian hàng (Tên shop, mô tả, địa chỉ kho, sđt kho, logo)
     */
    @RequestMapping(value = "/thong-tin", method = {RequestMethod.PUT, RequestMethod.POST}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<HoSoGianHangDayDuResponse>> capNhatThongTin(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @ModelAttribute @Valid CapNhatThongTinShopRequest yeuCau,
            @RequestParam(value = "fileLogo", required = false) MultipartFile fileLogo
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        HoSoGianHangDayDuResponse capNhat = hoSoGianHangService.capNhatThongTinGianHang(maNguoiDung, yeuCau, fileLogo);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Cập nhật thông tin gian hàng thành công!", capNhat));
    }

    /**
     * Nộp thêm chứng chỉ, giấy phép kinh doanh mới cho shop (trạng thái CHO_DUYET)
     */
    @PostMapping(value = "/chung-chi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<ChungChiGianHangResponse>> themChungChi(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @ModelAttribute @Valid ThemChungChiRequest yeuCau,
            @RequestParam("fileGiayTo") MultipartFile fileGiayTo
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        ChungChiGianHangResponse phanHoi = hoSoGianHangService.themChungChi(maNguoiDung, yeuCau, fileGiayTo);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PhanHoiApi.thanhCong("Nộp chứng chỉ thành công! Tài liệu của bạn đã được chuyển vào hàng đợi CHỜ DUYỆT.", phanHoi));
    }

    /**
     * Xóa chứng chỉ pháp lý (chỉ cho phép xóa khi ở trạng thái CHO_DUYET hoặc TU_CHOI)
     */
    @DeleteMapping("/chung-chi/{maChungChi}")
    @PreAuthorize("hasAnyRole('NGUOI_BAN', 'ADMIN')")
    public ResponseEntity<PhanHoiApi<Void>> xoaChungChi(
            @AuthenticationPrincipal NguoiDungPrincipal principal,
            @PathVariable("maChungChi") Long maChungChi
    ) {
        Long maNguoiDung = principal.getMaNguoiDung();
        hoSoGianHangService.xoaChungChi(maNguoiDung, maChungChi);
        return ResponseEntity.ok(PhanHoiApi.thanhCong("Đã xóa chứng chỉ thành công!", null));
    }
}
