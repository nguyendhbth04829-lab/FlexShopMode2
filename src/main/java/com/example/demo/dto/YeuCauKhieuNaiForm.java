package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private Long maDonHangShop;

    @NotBlank(message = "Vui lòng chọn lý do khiếu nại (Hỏng vỡ, Giao sai, Hàng giả, Thiếu hàng...).")
    private String loaiKhieuNai;

    @NotBlank(message = "Vui lòng chọn giải pháp yêu cầu mong muốn (Trả hàng & Hoàn tiền, Đổi hàng,...).")
    private String giaiPhapYeuCau;

    @NotBlank(message = "Vui lòng chọn mức độ ưu tiên xử lý.")
    private String mucDoUuTien = "TRUNG_BINH";

    @DecimalMin(value = "1000", message = "Số tiền yêu cầu hoàn lại tối thiểu là 1.000 VNĐ.")
    private BigDecimal soTienHoanTra;

    @NotBlank(message = "Vui lòng nhập nội dung mô tả chi tiết tình trạng lỗi hoặc sự cố của sản phẩm.")
    @Size(min = 10, max = 2000, message = "Nội dung mô tả phải có độ dài từ 10 đến 2.000 ký tự để CSKH có đủ căn cứ thụ lý.")
    private String noiDungMoTa;

    private MultipartFile[] filesBangChung;
}
