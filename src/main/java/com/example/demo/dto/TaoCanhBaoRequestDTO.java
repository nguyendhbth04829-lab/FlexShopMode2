package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & AN NINH SÀN (DEV 5 - MINH)
 * USER STORY: US-68 - DTO Tạo Mới Cảnh Báo Gian Lận
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaoCanhBaoRequestDTO {

    private String loaiDoiTuong;
    private Long maDoiTuong;
    private Integer diemRuiRo;
    private String lyDoCanhBao;

    public void validate() {
        if (loaiDoiTuong == null || loaiDoiTuong.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại đối tượng nghi vấn không được để trống!");
        }
        if (maDoiTuong == null || maDoiTuong <= 0) {
            throw new IllegalArgumentException("Mã đối tượng không hợp lệ!");
        }
        if (diemRuiRo == null || diemRuiRo < 1 || diemRuiRo > 100) {
            throw new IllegalArgumentException("Điểm rủi ro phải nằm trong thang điểm từ 1 đến 100!");
        }
        if (lyDoCanhBao == null || lyDoCanhBao.trim().isEmpty()) {
            throw new IllegalArgumentException("Lý do cảnh báo gian lận không được để trống!");
        }
    }
}
