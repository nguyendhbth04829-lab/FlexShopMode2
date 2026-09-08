package com.example.demo.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeDiaChiResponse {

    private Long tongSoDiaChi;
    private Integer gioiHanToiDa;
    private Long soLuongConLai;
    private Boolean coDiaChiMacDinh;
    private DiaChiResponse diaChiMacDinh;
    private List<String> danhSachTinhThanh;
}
