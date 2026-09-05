package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhanLoaiTicketForm {

    @NotBlank(message = "Vui lòng chọn nhóm loại khiếu nại (Hỏng vỡ, Giao sai, Hàng giả, Thiếu hàng...).")
    @Pattern(
        regexp = "^(HONG_VO|GIAO_SAI|HANG_GIA|THIEU_HANG|HET_HAN|KHAC)$",
        message = "Nhóm phân loại sự cố không hợp lệ. Vui lòng chọn trong danh mục được hệ thống quy định."
    )
    private String loaiKhieuNai;

    @NotBlank(message = "Vui lòng chọn mức độ ưu tiên xử lý (Thấp, Trung bình, Cao, Khẩn cấp).")
    @Pattern(
        regexp = "^(THAP|TRUNG_BINH|CAO|KHAN_CAP)$",
        message = "Mức độ ưu tiên xử lý không hợp lệ. Chỉ chấp nhận THAP, TRUNG_BINH, CAO hoặc KHAN_CAP."
    )
    private String mucDoUuTien;

    @NotBlank(message = "Vui lòng chọn trạng thái xử lý tiếp theo.")
    @Pattern(
        regexp = "^(MO_MOI|DANG_XU_LY|CHO_SHOP_PHAN_HOI|CHAP_NHAN_HOAN_TIEN|TU_CHOI_KHIEU_NAI|DONG_PHIEU)$",
        message = "Trạng thái xử lý không hợp lệ. Vui lòng chọn trạng thái theo đúng quy trình phân xử sàn."
    )
    private String trangThai;

    @Min(value = 1, message = "Mã nhân viên CSKH phụ trách không hợp lệ.")
    private Long maCskhPhuTrach;

    @Size(max = 1000, message = "Ghi chú điều phối xử lý tối đa 1.000 ký tự.")
    private String ghiChuXuLy;
}
