package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & AN NINH SÀN (DEV 5 - MINH)
 * USER STORY: US-68 - DTO Xử Lý Phán Quyết Cảnh Báo Gian Lận
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XuLyCanhBaoRequestDTO {

    private Long maCanhBao;
    private String hanhDong; // KHOA_DOI_TUONG, BO_QUA_CANH_BAO
    private String ghiChuXuLy;

    public void validate() {
        if (maCanhBao == null || maCanhBao <= 0) {
            throw new IllegalArgumentException("Mã cảnh báo gian lận không hợp lệ!");
        }
        if (hanhDong == null || (!"KHOA_DOI_TUONG".equalsIgnoreCase(hanhDong) && !"BO_QUA_CANH_BAO".equalsIgnoreCase(hanhDong))) {
            throw new IllegalArgumentException("Hành động xử lý phải là 'KHOA_DOI_TUONG' hoặc 'BO_QUA_CANH_BAO'!");
        }
    }
}
