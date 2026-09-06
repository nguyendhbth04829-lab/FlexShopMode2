package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Form nhập số xu muốn sử dụng giảm trừ vào đơn hàng (US-55 - Coin Reward System)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuDungXuForm {

    @NotBlank(message = "Mã đơn hàng không được để trống")
    private String maCodeDonTong;

    @NotNull(message = "Số xu sử dụng không được để trống")
    @Min(value = 100, message = "Số xu sử dụng tối thiểu từ 100 xu trở lên")
    private Long soXuMuonDung;

    private Boolean dungToiDa = false;
}
