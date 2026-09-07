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

    @NotNull(message = "Vui long nhap can nang thuc te (gram).")
    @Min(value = 1, message = "Can nang phai > 0 gram.")
    @Max(value = 500000, message = "Can nang toi da 500kg.")
    private Integer canNangGram;

    @NotNull(message = "Vui long nhap chieu dai (cm).")
    @Min(value = 1, message = "Chieu dai phai > 0 cm.")
    @Max(value = 300, message = "Chieu dai toi da 300cm.")
    private Integer chieuDaiCm;

    @NotNull(message = "Vui long nhap chieu rong (cm).")
    @Min(value = 1, message = "Chieu rong phai > 0 cm.")
    @Max(value = 300, message = "Chieu rong toi da 300cm.")
    private Integer chieuRongCm;

    @NotNull(message = "Vui long nhap chieu cao (cm).")
    @Min(value = 1, message = "Chieu cao phai > 0 cm.")
    @Max(value = 300, message = "Chieu cao toi da 300cm.")
    private Integer chieuCaoCm;

    @NotBlank(message = "Vui long chon tuyen van chuyen.")
    @Pattern(regexp = "^(NOI_THANH|TINH|BAN_SO_DIA)$",
            message = "Tuyen van chuyen chi chap nhan NOI_THANH, TINH hoac BAN_SO_DIA.")
    private String tuyenVanChuyen = "NOI_THANH";

    private Integer maDoiTac;
}
