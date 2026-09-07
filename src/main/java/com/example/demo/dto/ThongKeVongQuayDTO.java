package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Thống kê số liệu hoạt động Vòng Quay May Mắn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeVongQuayDTO {

    private Long tongLuotQuayToanSan;
    private Long tongXuDaPhat;
    private Long tongVoucherDaTrao;
    private Long tongLuotQuayHomNay;

    // Số liệu riêng của khách hàng hiện tại
    private Long soLuotQuayCuaToi;
    private Long soXuHienTai;
    private Integer soLuotQuayConLaiHomNay;
    private boolean daDungLuotMienPhiHomNay;
}
