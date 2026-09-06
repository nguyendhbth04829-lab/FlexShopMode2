package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể Lịch sử gửi Tin nhắn tự động trả lời (US-60)
 * Lưu vết mỗi lần hệ thống kích hoạt tự động gửi tin nhắn cho khách hàng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_tin_nhan_tu_dong")
public class LichSuTinNhanTuDong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lich_su")
    private Long maLichSu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cau_hinh", nullable = false)
    private CauHinhTinNhanTuDong cauHinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_cuoc_tro_chuyen", nullable = false)
    private CuocTroChuyen cuocTroChuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khach_hang", nullable = false)
    private NguoiDung khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_gian_hang", nullable = false)
    private GianHang gianHang;

    @Column(name = "noi_dung_da_gui", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String noiDungDaGui;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_voucher_da_tang")
    private MaGiamGia voucherDaTang;

    @Column(name = "thoi_gian_gui")
    @Builder.Default
    private LocalDateTime thoiGianGui = LocalDateTime.now();
}
