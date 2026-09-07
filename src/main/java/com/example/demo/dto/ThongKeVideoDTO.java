package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa số liệu thống kê hiệu quả video review (US-62)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeVideoDTO {

    private long tongSoVideo;
    private long tongLuotXem;
    private long tongLuotTim;
    private long tongLuotBinhLuan;
    private long tongLuotChiaSe;
    private double tiLeTuongTac;
}
