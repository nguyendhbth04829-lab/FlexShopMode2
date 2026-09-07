package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Form đăng tải Video ngắn review sản phẩm (Shopee Video - US-62)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DangVideoNganForm {

    private Long maVideo; // Dùng khi chỉnh sửa

    @NotBlank(message = "Tiêu đề video không được để trống")
    @Size(min = 10, max = 255, message = "Tiêu đề video phải từ 10 đến 255 ký tự để thu hút người xem")
    private String tieuDe;

    @NotNull(message = "Vui lòng chọn sản phẩm trong cửa hàng để gắn link giỏ hàng")
    private Long maSanPhamGanKem;

    private String linkVideo;

    private MultipartFile tepVideo;

    private String linkAnhBia;

    private MultipartFile tepAnhBia;

    @Size(max = 1000, message = "Mô tả review không được vượt quá 1000 ký tự")
    private String moTa;

    private String hashtag;

    private Integer thoiLuongGiay = 30;
}
