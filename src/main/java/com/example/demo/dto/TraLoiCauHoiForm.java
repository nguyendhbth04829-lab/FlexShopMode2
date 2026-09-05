package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Form trả lời câu hỏi Hỏi-Đáp (Q&A) về sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraLoiCauHoiForm {

    @NotNull(message = "Mã câu hỏi không được để trống")
    private Long maHoiDap;

    @NotBlank(message = "Nội dung câu trả lời không được để trống")
    @Size(min = 5, max = 1000, message = "Câu trả lời phải từ 5 đến 1.000 ký tự chi tiết và lịch sự")
    private String cauTraLoi;
}
