package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & AN NINH SÀN (DEV 5 - MINH)
 * USER STORY: US-68 - Thực thể Cảnh Báo Gian Lận (Anti-Fraud & Risk Engine)
 * =====================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "canh_bao_gian_lan")
public class CanhBaoGianLan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_canh_bao")
    private Long maCanhBao;

    @Column(name = "loai_doi_tuong", nullable = false, length = 50)
    private String loaiDoiTuong; // RUT_TIEN, AFFILIATE, ADS, TRA_SAU, DON_HANG, NGUOI_DUNG

    @Column(name = "ma_doi_tuong", nullable = false)
    private Long maDoiTuong;

    @Column(name = "diem_rui_ro", nullable = false)
    private Integer diemRuiRo; // Thang điểm 1 - 100

    @Column(name = "ly_do_canh_bao", nullable = false, length = 255)
    private String lyDoCanhBao;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "CHO_DIEU_TRA"; // CHO_DIEU_TRA, DA_XU_LY, BO_QUA_CANH_BAO

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    /**
     * Mức độ rủi ro dựa trên điểm số
     */
    public String getMucDoRuiRo() {
        if (this.diemRuiRo == null) return "KHONG_XAC_DINH";
        if (this.diemRuiRo >= 75) return "RẤT CAO (MỨC ĐỎ)";
        if (this.diemRuiRo >= 40) return "TRUNG BÌNH (MỨC VÀNG)";
        return "THẤP (MỨC XANH)";
    }

    /**
     * CSS class cho Badge Điểm rủi ro
     */
    public String getBadgeClassRuiRo() {
        if (this.diemRuiRo == null) return "bg-secondary";
        if (this.diemRuiRo >= 75) return "bg-danger text-white";
        if (this.diemRuiRo >= 40) return "bg-warning text-dark";
        return "bg-info text-dark";
    }

    /**
     * Tên trạng thái tiếng Việt
     */
    public String getTenTrangThaiTiengViet() {
        if ("CHO_DIEU_TRA".equalsIgnoreCase(this.trangThai)) return "Chờ điều tra";
        if ("DA_XU_LY".equalsIgnoreCase(this.trangThai)) return "Đã xử lý chế tài";
        if ("BO_QUA_CANH_BAO".equalsIgnoreCase(this.trangThai)) return "Đã bỏ qua / An toàn";
        return this.trangThai;
    }

    /**
     * CSS class cho Badge Trạng thái
     */
    public String getBadgeClassTrangThai() {
        if ("CHO_DIEU_TRA".equalsIgnoreCase(this.trangThai)) return "bg-warning text-dark";
        if ("DA_XU_LY".equalsIgnoreCase(this.trangThai)) return "bg-danger text-white";
        if ("BO_QUA_CANH_BAO".equalsIgnoreCase(this.trangThai)) return "bg-success text-white";
        return "bg-secondary text-white";
    }

    /**
     * Tên phân hệ đối tượng tiếng Việt
     */
    public String getTenLoaiDoiTuongTiengViet() {
        if ("RUT_TIEN".equalsIgnoreCase(this.loaiDoiTuong)) return "Rút Tiền Ví Shop (US-44)";
        if ("AFFILIATE".equalsIgnoreCase(this.loaiDoiTuong)) return "Tiếp Thị Liên Kết (US-65)";
        if ("ADS".equalsIgnoreCase(this.loaiDoiTuong)) return "Quảng Cáo CPC Ads (US-64)";
        if ("TRA_SAU".equalsIgnoreCase(this.loaiDoiTuong)) return "Mua Trước Trả Sau (US-63)";
        if ("DON_HANG".equalsIgnoreCase(this.loaiDoiTuong)) return "Đơn Hàng Sàn (US-26)";
        if ("NGUOI_DUNG".equalsIgnoreCase(this.loaiDoiTuong)) return "Tài Khoản Người Dùng";
        return this.loaiDoiTuong;
    }
}
