package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Thực thể Cấu hình Tin nhắn tự động trả lời (Auto-responder US-60)
 * Cho phép Shop thiết lập tin nhắn chào mừng, ngoài giờ làm việc, vắng mặt hoặc theo từ khóa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cau_hinh_tin_nhan_tu_dong")
public class CauHinhTinNhanTuDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_cau_hinh")
    private Long maCauHinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @Column(name = "tieu_de", nullable = false, length = 150)
    private String tieuDe;

    /**
     * Phân loại:
     * - CHAO_MUNG: Khách hàng mới nhắn tin lần đầu
     * - NGOAI_GIO_LAM_VIEC: Ngoài khung giờ trực chat
     * - VANG_MAT_TAM_THOI: Shop tạm vắng mặt / bận
     * - TU_KHOA: Phản hồi tự động theo từ khóa khớp
     */
    @Column(name = "loai_tin_nhan_tu_dong", nullable = false, length = 50)
    private String loaiTinNhanTuDong;

    @Column(name = "noi_dung_tin_nhan", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDungTinNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_voucher")
    private MaGiamGia maGiamGia;

    @Column(name = "gio_bat_dau")
    private LocalTime gioBatDau;

    @Column(name = "gio_ket_thuc")
    private LocalTime gioKetThuc;

    @Column(name = "tu_khoa_kich_hoat", length = 255)
    private String tuKhoaKichHoat;

    @Column(name = "kich_hoat", nullable = false)
    @Builder.Default
    private Boolean kichHoat = true;

    @Column(name = "do_tre_giay", nullable = false)
    @Builder.Default
    private Integer doTreGiay = 1;

    @Column(name = "gioi_han_gui_moi_khach_ngay", nullable = false)
    @Builder.Default
    private Integer gioiHanGuiMoiKhachNgay = 1;

    @Column(name = "so_lan_da_gui", nullable = false)
    @Builder.Default
    private Integer soLanDaGui = 0;

    @Column(name = "ngay_tao")
    @Builder.Default
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    /**
     * Tên hiển thị loại tin nhắn thân thiện tiếng Việt
     */
    public String getTenLoaiHienThi() {
        if (loaiTinNhanTuDong == null) return "Chưa xác định";
        switch (loaiTinNhanTuDong) {
            case "CHAO_MUNG":
                return "Chào mừng khách mới";
            case "NGOAI_GIO_LAM_VIEC":
                return "Ngoài giờ làm việc";
            case "VANG_MAT_TAM_THOI":
                return "Shop tạm vắng mặt";
            case "TU_KHOA":
                return "Hỏi đáp theo từ khóa";
            default:
                return loaiTinNhanTuDong;
        }
    }

    /**
     * Badge màu sắc Bootstrap đại diện cho loại kịch bản
     */
    public String getBadgeClass() {
        if (loaiTinNhanTuDong == null) return "bg-secondary";
        switch (loaiTinNhanTuDong) {
            case "CHAO_MUNG":
                return "bg-success";
            case "NGOAI_GIO_LAM_VIEC":
                return "bg-warning text-dark";
            case "VANG_MAT_TAM_THOI":
                return "bg-info text-dark";
            case "TU_KHOA":
                return "bg-primary";
            default:
                return "bg-secondary";
        }
    }
}
