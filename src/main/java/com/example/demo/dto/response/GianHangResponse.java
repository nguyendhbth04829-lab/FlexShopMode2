package com.example.demo.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GianHangResponse {

    private Long maGianHang;
    private Long maChuSoHuu;
    private String tenChuSoHuu;
    private String emailChuSoHuu;
    private String sdtChuSoHuu;

    private String tenGianHang;
    private String duongDanSlug;
    private String moTa;
    private String linkLogo;
    private String linkBanner;
    private String diaChiKho;
    private String sdtKho;

    private String trangThai; // CHO_DUYET, HOAT_DONG, TU_CHOI, TAM_KHOA
    private String lyDoTuChoi;
    private String hangGianHang;

    // Thông tin giấy chứng nhận / giấy phép kinh doanh đính kèm
    private Long maChungChi;
    private String loaiGiayTo;
    private String soGiayTo;
    private String linkAnhGiayTo;
    private String trangThaiGiayTo;

    private LocalDateTime ngayTao;
}
