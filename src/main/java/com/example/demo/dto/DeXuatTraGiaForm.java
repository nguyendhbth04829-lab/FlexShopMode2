package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeXuatTraGiaForm {

    @NotNull(message = "Mã cuộc trò chuyện không được để trống")
    private Long maCuocTroChuyen;

    @NotNull(message = "Mã sản phẩm không được để trống")
    private Long maSanPham;

    private Long maBienThe;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng mua tối thiểu là 1")
    @Max(value = 100, message = "Số lượng mua tối đa cho mỗi đề xuất là 100")
    private Integer soLuong = 1;

    @NotNull(message = "Giá đề xuất không được để trống")
    @DecimalMin(value = "1000", message = "Giá đề xuất tối thiểu là 1.000 VNĐ")
    private BigDecimal giaDeXuat;

    @Size(max = 500, message = "Ghi chú gửi Shop tối đa 500 ký tự")
    private String ghiChuKhach;
}
