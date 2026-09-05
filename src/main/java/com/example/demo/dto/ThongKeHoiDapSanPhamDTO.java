package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thống kê các chỉ số Q&A của Sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeHoiDapSanPhamDTO {

    private Long tongSoCauHoi;
    private Long soCauHoiDaTraLoi;
    private Long soCauHoiChoTraLoi;
    private Integer tyLeDaTraLoi; // Phần trăm (%)
}
