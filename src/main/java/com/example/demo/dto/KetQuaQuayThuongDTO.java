package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Trả về kết quả sau khi khách hàng thực hiện quay thưởng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KetQuaQuayThuongDTO {

    private boolean thanhCong;
    private String thongBao;
    private Long maLuotQuay;
    private Long maPhanThuong;
    private String tenPhanThuong;
    private String loaiPhanThuong; // XU, VOUCHER, MAY_MAN_LAN_SAU
    private Long giaTriXu;
    private String maCodeVoucher;
    private String tenVoucher;
    private Integer thuTuO; // Vị trí ô từ 1 đến 8
    private Integer gocDung; // Góc dừng chính xác để bánh xe Canvas quay tới
    private Long soXuHienTai; // Số dư ví xu sau khi cộng thưởng
    private Integer soLuotQuayConLai; // Số lượt quay còn lại hôm nay
    private String moTa;
}
