package com.example.demo.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeGianHangResponse {

    private long tongSo;
    private long choDuyet;
    private long hoatDong;
    private long tuChoi;
    private long tamKhoa;
}
