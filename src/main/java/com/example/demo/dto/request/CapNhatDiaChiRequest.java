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
public class CapNhatDiaChiRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(min = 2, max = 100, message = "Tên người nhận phải từ 2 đến 100 ký tự")
    private String tenNguoiNhan;

    @NotBlank(message = "Số điện thoại nhận hàng không được để trống")
    @Pattern(
            regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
            message = "Số điện thoại không đúng định dạng di động Việt Nam (gồm 10 số, bắt đầu bằng 03, 05, 07, 08, 09 hoặc +84)"
    )
    private String soDienThoai;

    @NotBlank(message = "Tỉnh / Thành phố không được để trống")
    @Size(max = 100, message = "Tỉnh / Thành phố tối đa 100 ký tự")
    private String tinhThanh;

    @NotBlank(message = "Quận / Huyện không được để trống")
    @Size(max = 100, message = "Quận / Huyện tối đa 100 ký tự")
    private String quanHuyen;

    @NotBlank(message = "Xã / Phường không được để trống")
    @Size(max = 100, message = "Xã / Phường tối đa 100 ký tự")
    private String xaPhuong;

    @NotBlank(message = "Địa chỉ chi tiết (số nhà, tên đường, ngõ ngách...) không được để trống")
    @Size(min = 5, max = 255, message = "Địa chỉ chi tiết phải có độ dài từ 5 đến 255 ký tự")
    private String diaChiChiTiet;

    private Boolean laMacDinh;
}
