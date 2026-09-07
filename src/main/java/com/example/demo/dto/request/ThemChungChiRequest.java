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
public class ThemChungChiRequest {

    @NotBlank(message = "Loại giấy tờ/chứng chỉ không được để trống")
    private String loaiGiayTo; // GIAY_PHEP_KINH_DOANH, AN_TOAN_THUC_PHAM, CHUNG_NHAN_CHAT_LUONG, UY_QUYEN_PHAN_PHOI, KHAC

    @NotBlank(message = "Số giấy tờ / Mã số thuế không được để trống")
    @Size(min = 3, max = 100, message = "Số giấy tờ phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_/.]+$", message = "Số giấy tờ chỉ được chứa chữ cái, chữ số và các ký tự: -, _, /, .")
    private String soGiayTo;
}
