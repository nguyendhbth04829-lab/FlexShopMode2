package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhanQuyetTranhChapForm {

    @NotNull(message = "Mã phiếu khiếu nại không được để trống")
    private Long maPhieu;

    @NotBlank(message = "Vui lòng chọn quyết định xử lý tranh chấp")
    @Pattern(
            regexp = "^(DUYET_HOAN_TIEN_KHACH|BOI_THUONG_SHOP|BAC_BO_KHIEU_NAI)$",
            message = "Quyết định xử lý tranh chấp phải là DUYET_HOAN_TIEN_KHACH, BOI_THUONG_SHOP hoặc BAC_BO_KHIEU_NAI"
    )
    private String quyetDinh;

    @NotNull(message = "Số tiền phê duyệt không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền không được là số âm")
    @Digits(integer = 15, fraction = 2, message = "Định dạng số tiền không hợp lệ")
    private BigDecimal soTien;

    @Pattern(
            regexp = "^(NGUOI_BAN|SAN_FLEXSHOP|DON_VI_VAN_CHUYEN)?$",
            message = "Bên chịu phí chỉ có thể là NGUOI_BAN, SAN_FLEXSHOP hoặc DON_VI_VAN_CHUYEN"
    )
    private String benChiuPhi;

    @NotBlank(message = "Căn cứ và lý do phán quyết không được để trống")
    @Size(min = 10, max = 2000, message = "Căn cứ và lý do phán quyết phải từ 10 đến 2.000 ký tự giải trình chi tiết")
    @Pattern(regexp = "^(?!\\s*$).+", message = "Lý do phán quyết không được chỉ chứa ký tự khoảng trắng")
    private String ghiChuPhanQuyet;
}
