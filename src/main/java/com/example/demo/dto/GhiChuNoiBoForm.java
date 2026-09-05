package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GhiChuNoiBoForm {

    @NotNull(message = "Mã nhân viên CSKH không được để trống.")
    @Min(value = 1, message = "Mã định danh nhân viên CSKH không hợp lệ.")
    private Long maNhanVien;

    @NotBlank(message = "Nội dung ghi chú điều tra nội bộ không được để trống.")
    @Size(min = 5, max = 2000, message = "Nội dung ghi chú phải từ 5 đến 2.000 ký tự để bảo đảm đủ căn cứ đối chiếu và xác minh.")
    @Pattern(
        regexp = "^(?!\\s*$).+",
        message = "Nội dung ghi chú không được chỉ chứa ký tự khoảng trắng hoặc xuống dòng."
    )
    private String noiDung;
}
