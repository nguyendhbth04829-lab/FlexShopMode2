package com.example.demo.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpResponse {

    private String thongBao;
    private String nguoiNhan;
    private Integer soGiayHieuLuc;
    private Long soLanGuiConLai;
    private String maOtpDemo; // Tiện lợi kiểm thử trong môi trường dev
}
