package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Form tạo hoặc chỉnh sửa phiên phát Livestream (US-61)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaoPhongLiveForm {

    private Long maLive;

    @NotBlank(message = "Vui lòng nhập tiêu đề cho buổi Livestream")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String tieuDe;

    private String linkStreamRtmp;

    private String linkAnhBia;

    private String moTa;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Builder.Default
    private LocalDateTime thoiGianBatDau = LocalDateTime.now();

    /**
     * Chế độ phát video:
     * - "WEBCAM": Sử dụng Camera / Webcam thực tế của máy tính/điện thoại
     * - "VIDEO_SAMPLE": Sử dụng luồng video mẫu FlexShop demo
     */
    @Builder.Default
    private String nguonVideo = "WEBCAM";

    // Danh sách các mã sản phẩm chọn đưa vào live
    @Builder.Default
    private List<Long> danhSachMaSanPham = new ArrayList<>();

    // Giá khuyến mãi độc quyền live cho từng sản phẩm (Key: maSanPham, Value: giaLive)
    @Builder.Default
    private Map<Long, BigDecimal> mapGiaDocQuyen = new HashMap<>();

    // Số lượng bán giới hạn trên live cho từng sản phẩm
    @Builder.Default
    private Map<Long, Integer> mapSoLuongGioiHan = new HashMap<>();
}
