package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Form tạo hoặc chỉnh sửa khung giờ Flash Sale (US-53 - PROMOTION)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhungGioFlashSaleForm {

    private Long maFlashSale;

    @NotBlank(message = "Tiêu đề khung giờ Flash Sale không được để trống")
    @Size(min = 2, max = 150, message = "Tiêu đề phải từ 2 đến 150 ký tự")
    private String tieuDe;

    private String linkBanner;

    /**
     * Tệp ảnh banner được chọn từ máy tính của Seller/Admin
     */
    private MultipartFile tepAnhBanner;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm"})
    private LocalDateTime thoiGianBatDau;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm"})
    private LocalDateTime thoiGianKetThuc;

    /**
     * Mốc giờ tạo nhanh: 0H, 12H, 21H hoặc CUSTOM
     */
    private String mocGioNhanh;
}
