package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeDashboardCskhDTO {
    private long tongSoTicket;
    private long ticketChuaTiepNhan;
    private long ticketUuTienCao;
    private long khieuNaiVanChuyenPod;
    private long ticketChoShopPhanHoi;
    private long ticketDangXuLy;
}
