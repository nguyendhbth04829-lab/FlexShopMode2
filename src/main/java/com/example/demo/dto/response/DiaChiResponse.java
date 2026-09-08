package com.example.demo.dto.response;

import com.example.demo.entity.DiaChiNguoiDung;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChiResponse {

    private Long maDiaChi;
    private Long maNguoiDung;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String tinhThanh;
    private String quanHuyen;
    private String xaPhuong;
    private String diaChiChiTiet;
    private String diaChiDayDu;
    private Boolean laMacDinh;
    private LocalDateTime ngayTao;

    public static DiaChiResponse fromEntity(DiaChiNguoiDung entity) {
        if (entity == null) return null;
        return DiaChiResponse.builder()
                .maDiaChi(entity.getMaDiaChi())
                .maNguoiDung(entity.getMaNguoiDung())
                .tenNguoiNhan(entity.getTenNguoiNhan())
                .soDienThoai(entity.getSoDienThoai())
                .tinhThanh(entity.getTinhThanh())
                .quanHuyen(entity.getQuanHuyen())
                .xaPhuong(entity.getXaPhuong())
                .diaChiChiTiet(entity.getDiaChiChiTiet())
                .diaChiDayDu(entity.getDiaChiDayDu())
                .laMacDinh(Boolean.TRUE.equals(entity.getLaMacDinh()))
                .ngayTao(entity.getNgayTao())
                .build();
    }
}
