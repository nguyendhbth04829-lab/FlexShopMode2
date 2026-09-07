package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * US-33: Form ghi checkpoint hanh trinh kien hang qua Hub.
 * maHub nullable: moc nhu CHO_LAY_HANG / DANG_GIAO co the chua qua Hub.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GhiHanhTrinhForm {

    @NotNull(message = "Vui long chon don hang shop.")
    @Min(value = 1, message = "Ma don hang shop khong hop le.")
    private Long maDonHangShop;

    private Long maHub;

    @NotBlank(message = "Vui long nhap tieu de moc (vd Da roi Hub, Dang nhap Hub).")
    @Size(max = 150, message = "Tieu de moc toi da 150 ky tu.")
    private String tieuDeMoc;

    @Size(max = 255, message = "Vi tri hien tai toi da 255 ky tu.")
    private String viTriHienTai;
}
