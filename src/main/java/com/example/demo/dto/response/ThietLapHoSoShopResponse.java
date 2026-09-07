package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThietLapHoSoShopResponse {

    private Long maGianHang;
    private Long maChuSoHuu;
    private String tenGianHang;
    private String duongDanSlug;
    private String moTa;
    private String linkLogo;
    private String linkBanner;
    private String diaChiKho;
    private String sdtKho;
    private String gioMoCua;
    private String gioDongCua;
    private Boolean dangMoCua;
    private String ghiChuKho;
    private String nguoiLienHeKho;
    private String trangThai;
    private String lyDoTuChoi;
    private String hangGianHang;
    private Integer diemSaoQuaTa;
    private BigDecimal diemDanhGiaTb;
    private Integer tongDanhGia;
    private Integer tongDonHang;
    private BigDecimal tyLePhanHoiChat;
    private LocalDateTime ngayTao;

    // Chỉ số hoàn thiện hồ sơ phục vụ US-10 mở rộng
    private Integer phanTramHoanThien;
    private List<String> cacBuocConThieu;
}
