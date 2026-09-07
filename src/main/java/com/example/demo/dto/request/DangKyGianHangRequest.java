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
public class DangKyGianHangRequest {

    @NotBlank(message = "Tên gian hàng không được để trống")
    @Size(min = 3, max = 100, message = "Tên gian hàng phải từ 3 đến 100 ký tự")
    @Pattern(
            regexp = "^[a-zA-Z0-9\\p{L}\\s-_.]+$",
            message = "Tên gian hàng chỉ được chứa chữ cái tiếng Việt, số, khoảng trắng và dấu - _ ."
    )
    private String tenGianHang;

    @Size(max = 120, message = "Đường dẫn định danh (Slug) không được vượt quá 120 ký tự")
    @Pattern(
            regexp = "^[a-z0-9-]*$",
            message = "Đường dẫn slug chỉ được chứa chữ cái thường không dấu (a-z), chữ số (0-9) và dấu gạch ngang (-)"
    )
    private String duongDanSlug;

    @NotBlank(message = "Số điện thoại kho hàng không được để trống")
    @Pattern(
            regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
            message = "Số điện thoại không đúng định dạng di động Việt Nam (gồm 10 chữ số, bắt đầu bằng 03, 05, 07, 08, 09 hoặc +84)"
    )
    private String sdtKho;

    @NotBlank(message = "Địa chỉ kho hàng không được để trống")
    @Size(min = 5, max = 255, message = "Địa chỉ kho hàng phải có độ dài từ 5 đến 255 ký tự")
    private String diaChiKho;

    @Size(max = 2000, message = "Mô tả gian hàng không được vượt quá 2000 ký tự")
    private String moTa;

    @NotBlank(message = "Mã số thuế / Số giấy phép kinh doanh không được để trống")
    @Size(min = 5, max = 50, message = "Số giấy phép kinh doanh phải từ 5 đến 50 ký tự")
    @Pattern(
            regexp = "^[A-Za-z0-9-]+$",
            message = "Số giấy phép kinh doanh chỉ chứa chữ cái, chữ số và dấu gạch ngang"
    )
    private String soGiayTo;

    @Builder.Default
    private String loaiGiayTo = "GIAY_PHEP_KINH_DOANH";
}
