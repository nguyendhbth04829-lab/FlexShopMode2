package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý ngoại lệ toàn cục chuyên nghiệp cho hệ thống FlexShop
 * Đặc biệt bắt và xử lý êm đẹp các lỗi upload video/ảnh vượt quá dung lượng (US-62)
 */
@Slf4j
@ControllerAdvice
public class QuanLyNgoaiLeController {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object xuLyLoiVuotQuaKichThuocTep(MaxUploadSizeExceededException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        log.warn("Cảnh báo: Người dùng tải lên tệp vượt quá kích thước cho phép tại URL: {}. Chi tiết: {}",
                request.getRequestURI(), ex.getMessage());

        String thongBaoLoi = "Dung lượng tệp tải lên vượt quá giới hạn tối đa (tối đa 200MB). Vui lòng nén video hoặc dán link video trực tiếp.";

        if (laRequestAjax(request)) {
            Map<String, Object> phanHoi = new HashMap<>();
            phanHoi.put("thanhCong", false);
            phanHoi.put("maLoi", "FILE_TOO_LARGE");
            phanHoi.put("thongDiep", thongBaoLoi);
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(phanHoi);
        }

        String uri = request.getRequestURI();
        redirectAttributes.addFlashAttribute("errorMessage", thongBaoLoi);

        if (uri != null && uri.contains("/video")) {
            return "redirect:/khach-hang/video/koc/dang-video";
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/khach-hang/video");
    }

    @ExceptionHandler(MultipartException.class)
    public Object xuLyLoiMultipart(MultipartException ex,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        log.error("Lỗi MultipartException tại URL {}: {}", request.getRequestURI(), ex.getMessage());

        String thongBaoLoi = "Lỗi trong quá trình tải tệp lên máy chủ. Vui lòng kiểm tra lại định dạng tệp hoặc dán link video trực tiếp.";

        if (laRequestAjax(request)) {
            Map<String, Object> phanHoi = new HashMap<>();
            phanHoi.put("thanhCong", false);
            phanHoi.put("thongDiep", thongBaoLoi);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(phanHoi);
        }

        redirectAttributes.addFlashAttribute("errorMessage", thongBaoLoi);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/khach-hang/video/koc/dang-video");
    }

    private boolean laRequestAjax(HttpServletRequest request) {
        String xrw = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xrw) || (accept != null && accept.contains("application/json"));
    }
}
