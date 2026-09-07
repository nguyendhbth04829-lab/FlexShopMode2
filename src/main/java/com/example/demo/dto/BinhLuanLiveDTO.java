package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO dữ liệu bình luận thời gian thực trong phòng Live (US-61)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinhLuanLiveDTO {

    private Long maBinhLuan;
    private Long maLive;
    private Long maNguoiDung;
    private String hoTenNguoiDung;
    private String noiDung;
    private LocalDateTime thoiGianGui;
    private String thoiGianDinhDang;
    private Boolean laTinHeThong;
    private Boolean laNguoiBan;

    public String getThoiGianDinhDang() {
        if (thoiGianGui != null) {
            return thoiGianGui.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        return "";
    }
}
