package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Form DTO nhận dữ liệu và validate thiết lập tin nhắn tự động (US-60)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CauHinhTinNhanTuDongForm {

    private Long maCauHinh;

    @NotBlank(message = "Tiêu đề kịch bản không được để trống!")
    @Size(min = 3, max = 150, message = "Tiêu đề phải từ 3 đến 150 ký tự!")
    private String tieuDe;

    @NotBlank(message = "Vui lòng chọn loại tin nhắn tự động!")
    private String loaiTinNhanTuDong;

    @NotBlank(message = "Nội dung tin nhắn tự động không được để trống!")
    @Size(max = 2000, message = "Nội dung phản hồi không được vượt quá 2000 ký tự!")
    private String noiDungTinNhan;

    private Long maVoucher;

    private String gioBatDau; // Format: "18:00"

    private String gioKetThuc; // Format: "08:00"

    @Size(max = 255, message = "Từ khóa kích hoạt tối đa 255 ký tự!")
    private String tuKhoaKichHoat;

    @Builder.Default
    private Boolean kichHoat = true;

    @NotNull(message = "Vui lòng thiết lập độ trễ phản hồi!")
    @Min(value = 0, message = "Độ trễ không được âm!")
    @Max(value = 60, message = "Độ trễ tối đa 60 giây!")
    @Builder.Default
    private Integer doTreGiay = 1;

    @NotNull(message = "Vui lòng chọn giới hạn gửi cho mỗi khách hàng!")
    @Min(value = 1, message = "Giới hạn tối thiểu 1 lần/khách/ngày!")
    @Max(value = 20, message = "Giới hạn tối đa 20 lần/khách/ngày!")
    @Builder.Default
    private Integer gioiHanGuiMoiKhachNgay = 1;
}
