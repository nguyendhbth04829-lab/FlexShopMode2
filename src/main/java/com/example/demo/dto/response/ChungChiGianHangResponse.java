package com.example.demo.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChungChiGianHangResponse {

    private Long maChungChi;
    private Long maGianHang;
    private String loaiGiayTo;
    private String tenLoaiGiayTo;
    private String soGiayTo;
    private String linkAnhGiayTo;
    private String trangThaiDuyet;
    private String tenTrangThaiDuyet;
    private LocalDateTime ngayTao;
    private Boolean laTepPdf;

    public static String chuyenDoiTenLoaiGiayTo(String loaiGiayTo) {
        if (loaiGiayTo == null) return "Chưa xác định";
        return switch (loaiGiayTo.toUpperCase()) {
            case "GIAY_PHEP_KINH_DOANH" -> "Giấy phép kinh doanh";
            case "AN_TOAN_THUC_PHAM" -> "Chứng nhận ATTP";
            case "CHUNG_NHAN_CHAT_LUONG" -> "Chứng nhận chất lượng / ISO";
            case "UY_QUYEN_PHAN_PHOI" -> "Ủy quyền phân phối chính hãng";
            case "CAN_CUOC_CONG_DAN" -> "Căn cước công dân / Định danh";
            case "MA_SO_THUE" -> "Mã số thuế doanh nghiệp";
            case "KHAC" -> "Giấy tờ pháp lý khác";
            default -> loaiGiayTo;
        };
    }

    public static String chuyenDoiTenTrangThai(String trangThai) {
        if (trangThai == null) return "Chưa xác định";
        return switch (trangThai.toUpperCase()) {
            case "DA_DUYET" -> "Đã duyệt";
            case "CHO_DUYET" -> "Chờ duyệt";
            case "TU_CHOI" -> "Bị từ chối";
            default -> trangThai;
        };
    }
}
