package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeDanhGiaDTO {

    @Builder.Default
    private Double diemTrungBinh = 5.0;

    @Builder.Default
    private Long tongSoDanhGia = 0L;

    @Builder.Default
    private Long soLuong5Sao = 0L;

    @Builder.Default
    private Long soLuong4Sao = 0L;

    @Builder.Default
    private Long soLuong3Sao = 0L;

    @Builder.Default
    private Long soLuong2Sao = 0L;

    @Builder.Default
    private Long soLuong1Sao = 0L;

    @Builder.Default
    private Integer tyLe5Sao = 0;

    @Builder.Default
    private Integer tyLe4Sao = 0;

    @Builder.Default
    private Integer tyLe3Sao = 0;

    @Builder.Default
    private Integer tyLe2Sao = 0;

    @Builder.Default
    private Integer tyLe1Sao = 0;

    @Builder.Default
    private Long soLuongCoHinhAnh = 0L;

    public String getDiemTrungBinhDisplay() {
        if (diemTrungBinh == null || tongSoDanhGia == 0) return "5.0";
        DecimalFormat df = new DecimalFormat("#,##0.0");
        return df.format(diemTrungBinh);
    }

    public int getSoSaoNguyen() {
        if (diemTrungBinh == null || tongSoDanhGia == 0) return 5;
        return (int) Math.round(diemTrungBinh);
    }
}
