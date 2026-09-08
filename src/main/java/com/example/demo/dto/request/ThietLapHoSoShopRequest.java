package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThietLapHoSoShopRequest {

    @NotBlank(message = "Tên gian hàng không được để trống")
    @Size(min = 3, max = 100, message = "Tên gian hàng phải từ 3 đến 100 ký tự")
    private String tenGianHang;

    @Size(max = 2000, message = "Mô tả gian hàng không được vượt quá 2000 ký tự")
    private String moTa;

    @NotBlank(message = "Số điện thoại kho lấy hàng không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại kho phải là số di động Việt Nam hợp lệ (10 chữ số)")
    private String sdtKho;

    @NotBlank(message = "Địa chỉ kho lấy hàng không được để trống")
    @Size(min = 5, max = 255, message = "Địa chỉ kho phải từ 5 đến 255 ký tự")
    private String diaChiKho;

    @NotBlank(message = "Giờ mở cửa không được để trống")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ mở cửa phải theo định dạng HH:mm (ví dụ: 08:00)")
    private String gioMoCua;

    @NotBlank(message = "Giờ đóng cửa không được để trống")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Giờ đóng cửa phải theo định dạng HH:mm (ví dụ: 22:00)")
    private String gioDongCua;

    @Builder.Default
    private Boolean dangMoCua = true;

    @Size(max = 255, message = "Ghi chú kho không được vượt quá 255 ký tự")
    private String ghiChuKho;

    @Size(max = 100, message = "Tên người liên hệ kho không được vượt quá 100 ký tự")
    private String nguoiLienHeKho;
}
