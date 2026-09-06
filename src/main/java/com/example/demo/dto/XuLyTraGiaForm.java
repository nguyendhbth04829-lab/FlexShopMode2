package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XuLyTraGiaForm {

    @NotNull(message = "Mã đề xuất trả giá không được để trống")
    private Long maDeXuat;

    @NotBlank(message = "Hành động xử lý không được để trống")
    private String hanhDong; // 'DONG_Y', 'TU_CHOI', 'PHAN_HOI_LAI'

    private BigDecimal giaShopPhanHoi;

    @Size(max = 500, message = "Phản hồi của Shop tối đa 500 ký tự")
    private String phanHoiShop;
}
