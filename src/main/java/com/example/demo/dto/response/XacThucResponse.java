package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XacThucResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String loaiToken = "Bearer";
    @Builder.Default
    private Long thoiGianHetHan = 900L; // 15 phút (900 giây)
    private NguoiDungResponse thongTinNguoiDung;

    // Getter tương thích cho JSON & Frontend JavaScript
    public String getTokenType() {
        return loaiToken;
    }

    public Long getExpiresIn() {
        return thoiGianHetHan;
    }

    public NguoiDungResponse getUser() {
        return thongTinNguoiDung;
    }
}
