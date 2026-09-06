package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiTinNhanForm {

    @NotNull(message = "Mã cuộc trò chuyện không được để trống")
    private Long maCuocTroChuyen;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 2000, message = "Nội dung tin nhắn tối đa 2000 ký tự")
    private String noiDung;

    private String loaiNguoiGui; // 'KHACH_HANG' hoặc 'SHOP'
}
