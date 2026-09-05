package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YeuCauKhieuNaiForm {

    @NotNull(message = "Vui lòng chọn đơn hàng shop cần khiếu nại / đổi trả.")
    @Min(value = 1, message = "Mã đơn hàng shop không hợp lệ.")
    private Long maDonHangShop;

    @NotBlank(message = "Vui lòng chọn lý do khiếu nại (Hỏng vỡ, Giao sai, Hàng giả, Thiếu hàng...).")
    @Pattern(
        regexp = "^(HONG_VO|GIAO_SAI|HANG_GIA|THIEU_HANG|HET_HAN|KHAC)$",
        message = "Lý do khiếu nại không hợp lệ. Vui lòng chọn trong danh mục được hệ thống hỗ trợ."
    )
    private String loaiKhieuNai;

    @NotBlank(message = "Vui lòng chọn giải pháp yêu cầu mong muốn (Trả hàng & Hoàn tiền, Đổi hàng,...).")
    @Pattern(
        regexp = "^(HOAN_TIEN_TRA_HANG|HOAN_TIEN_KHONG_TRA|DOI_HANG)$",
        message = "Giải pháp yêu cầu không hợp lệ. Vui lòng chọn Trả hàng hoàn tiền, Hoàn tiền không trả hàng hoặc Đổi hàng."
    )
    private String giaiPhapYeuCau;

    @NotBlank(message = "Vui lòng chọn mức độ ưu tiên xử lý.")
    @Pattern(
        regexp = "^(THAP|TRUNG_BINH|CAO|KHAN_CAP)$",
        message = "Mức độ ưu tiên không hợp lệ. Chỉ chấp nhận Thấp, Trung bình, Cao hoặc Khẩn cấp."
    )
    private String mucDoUuTien = "TRUNG_BINH";

    @DecimalMin(value = "1000", message = "Số tiền yêu cầu hoàn lại tối thiểu là 1.000 VNĐ.")
    @Digits(integer = 15, fraction = 2, message = "Số tiền hoàn lại không hợp lệ (tối đa 15 chữ số phần nguyên và 2 chữ số thập phân).")
    private BigDecimal soTienHoanTra;

    @NotBlank(message = "Vui lòng nhập nội dung mô tả chi tiết tình trạng lỗi hoặc sự cố của sản phẩm.")
    @Size(min = 10, max = 2000, message = "Nội dung mô tả phải có độ dài từ 10 đến 2.000 ký tự để CSKH có đủ căn cứ thụ lý.")
    @Pattern(
        regexp = "^(?!\\s*$).+",
        message = "Nội dung mô tả không được chỉ chứa khoảng trắng."
    )
    private String noiDungMoTa;

    private MultipartFile[] filesBangChung;
}
