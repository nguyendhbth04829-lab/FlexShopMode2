package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Form khách hàng đặt câu hỏi Hỏi-Đáp (Q&A) về sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatCauHoiForm {

    @NotNull(message = "Mã sản phẩm không được để trống")
    private Long maSanPham;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(min = 10, max = 1000, message = "Câu hỏi phải từ 10 đến 1.000 ký tự chi tiết và lịch sự")
    private String cauHoi;
}
