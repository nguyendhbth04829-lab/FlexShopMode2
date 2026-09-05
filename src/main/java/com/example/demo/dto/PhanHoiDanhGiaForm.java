package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO tiếp nhận dữ liệu Seller phản hồi đánh giá của khách hàng (US-50)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhanHoiDanhGiaForm {

    @NotNull(message = "Mã đánh giá không được để trống.")
    private Long maDanhGia;

    @NotBlank(message = "Nội dung phản hồi không được để trống hoặc chỉ chứa khoảng trắng.")
    @Size(min = 5, max = 1000, message = "Nội dung phản hồi phải từ 5 đến 1.000 ký tự chi tiết và lịch sự.")
    private String noiDungPhanHoi;
}
