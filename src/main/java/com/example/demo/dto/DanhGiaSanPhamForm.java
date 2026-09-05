package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DanhGiaSanPhamForm {

    @NotNull(message = "Mã chi tiết đơn hàng không được để trống")
    private Long maChiTietDon;

    @NotNull(message = "Vui lòng chọn số sao đánh giá chất lượng sản phẩm")
    @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Số sao đánh giá tối đa là 5 sao")
    private Integer soSao = 5;

    @NotBlank(message = "Nội dung nhận xét đánh giá không được để trống")
    @Size(min = 5, max = 2000, message = "Nội dung nhận xét phải từ 5 đến 2.000 ký tự chi tiết")
    private String noiDung;

    private Boolean anDanh = false;

    private List<MultipartFile> danhSachTepAnh = new ArrayList<>();
}
