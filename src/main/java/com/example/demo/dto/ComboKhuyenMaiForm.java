package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Form tạo hoặc chỉnh sửa chương trình Combo & Mua Kèm Deal Sốc (US-54 - PROMOTION)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboKhuyenMaiForm {

    private Long maCombo;

    @NotBlank(message = "Tên chương trình khuyến mãi không được để trống")
    @Size(min = 3, max = 150, message = "Tên chương trình phải từ 3 đến 150 ký tự")
    private String tenCombo;

    @NotBlank(message = "Vui lòng chọn loại chương trình khuyến mãi")
    private String loaiCombo = "DEAL_SOC_MUA_KEM";

    @NotNull(message = "Số lượng sản phẩm tối thiểu không được để trống")
    @Min(value = 1, message = "Số lượng tối thiểu phải từ 1 trở lên")
    private Integer soLuongToiThieu = 1;

    @NotNull(message = "Mức giảm giá ưu đãi không được để trống")
    @DecimalMin(value = "0.01", message = "Mức giảm giá phải lớn hơn 0")
    private BigDecimal giaTriGiam = new BigDecimal("50.00");

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm"})
    private LocalDateTime thoiGianBatDau;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = {"yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm"})
    private LocalDateTime thoiGianKetThuc;

    private Boolean dangHoatDong = true;
}