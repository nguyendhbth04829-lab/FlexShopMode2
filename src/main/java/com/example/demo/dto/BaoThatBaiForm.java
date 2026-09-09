package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * US-38: Form bao giao hang that bai + hen giao lai.
 * So lan giao toi da 3 (US-40 xu ly lan 3).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaoThatBaiForm {

    @NotBlank(message = "Vui lòng chọn lý do thất bại.")
    @Pattern(regexp = "^(KHACH_KHONG_NGHE_MAY|SAI_DIA_CHI|HEN_LAI|KHACH_TU_CHOI|KHAC)$",
            message = "Lý do thất bại không hợp lệ.")
    private String lyDoThatBai;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime thoiGianHenGiaoLai;
}
