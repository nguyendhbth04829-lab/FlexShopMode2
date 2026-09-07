package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuThietLapShopResponse {

    private Long maLichSu;
    private Long maGianHang;
    private String loaiThayDoi;
    private String tenLoaiThayDoi;
    private String noiDungThayDoi;
    private String nguoiThucHien;
    private LocalDateTime thoiGian;
}
