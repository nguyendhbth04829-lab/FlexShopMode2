package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * US-32 (lam lai): Form tinh phi van chuyen.
 * Quy tac: KhoiLuongQuyDoi(g) = D*R*C/5, TinhCuoc = MAX(thuc, quyDoi).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TinhPhiVanChuyenForm {

    @NotNull(message = "Vui lòng nhập cân nặng thực tế (gram).")
    @Min(value = 1, message = "Cân nặng phải > 0 gram.")
    @Max(value = 500000, message = "Cân nặng tối đa 500kg.")
    private Integer canNangGram;

    @NotNull(message = "Vui lòng nhập chiều dài (cm).")
    @Min(value = 1, message = "Chiều dài phải > 0 cm.")
    @Max(value = 300, message = "Chiều dài tối đa 300cm.")
    private Integer chieuDaiCm;

    @NotNull(message = "Vui lòng nhập chiều rộng (cm).")
    @Min(value = 1, message = "Chiều rộng phải > 0 cm.")
    @Max(value = 300, message = "Chiều rộng tối đa 300cm.")
    private Integer chieuRongCm;

    @NotNull(message = "Vui lòng nhập chiều cao (cm).")
    @Min(value = 1, message = "Chiều cao phải > 0 cm.")
    @Max(value = 300, message = "Chiều cao tối đa 300cm.")
    private Integer chieuCaoCm;

    @NotBlank(message = "Vui lòng chọn tuyến vận chuyển.")
    @Pattern(regexp = "^(NOI_THANH|TINH|BAN_SO_DIA)$",
            message = "Tuyến vận chuyển chỉ chấp nhận NOI_THANH, TINH hoặc BAN_SO_DIA.")
    private String tuyenVanChuyen = "NOI_THANH";

    private Integer maDoiTac;
}
