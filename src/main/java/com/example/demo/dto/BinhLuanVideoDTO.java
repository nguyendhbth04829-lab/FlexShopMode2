package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO hiển thị và truyền nhận dữ liệu bình luận video qua AJAX (US-62)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BinhLuanVideoDTO {

    private Long maBinhLuan;
    private Long maVideo;
    private Long maNguoiDung;
    private String hoTenNguoiDung;
    private String anhDaiDien;
    private String noiDung;
    private String thoiGianGui;
    private boolean laToi;
}
