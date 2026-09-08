package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapNhatThongTinShopRequest {

    @NotBlank(message = "Tên gian hàng không được để trống")
    @Size(min = 3, max = 100, message = "Tên gian hàng phải từ 3 đến 100 ký tự")
    private String tenGianHang;

    @Size(max = 2000, message = "Mô tả gian hàng không được vượt quá 2000 ký tự")
    private String moTa;

    @NotBlank(message = "Địa chỉ kho hàng không được để trống")
    @Size(min = 5, max = 255, message = "Địa chỉ kho hàng phải từ 5 đến 255 ký tự")
    private String diaChiKho;

    @NotBlank(message = "Số điện thoại kho hàng không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại kho không đúng định dạng di động Việt Nam (10 số, bắt đầu bằng 03, 05, 07, 08, 09)")
    private String sdtKho;
}
