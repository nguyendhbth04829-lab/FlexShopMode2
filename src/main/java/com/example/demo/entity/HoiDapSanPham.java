package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Thực thể ánh xạ bảng hoi_dap_san_pham (Q&A cộng đồng trên trang sản phẩm)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hoi_dap_san_pham")
public class HoiDapSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hoi_dap")
    private Long maHoiDap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", referencedColumnName = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_hoi", referencedColumnName = "ma_nguoi_dung", nullable = false)
    private NguoiDung nguoiHoi;

    @Column(name = "cau_hoi", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String cauHoi;

    @Column(name = "cau_tra_loi", columnDefinition = "NVARCHAR(MAX)")
    private String cauTraLoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nguoi_tra_loi", referencedColumnName = "ma_nguoi_dung")
    private NguoiDung nguoiTraLoi;

    @Column(name = "ngay_hoi")
    private LocalDateTime ngayHoi = LocalDateTime.now();

    @Column(name = "ngay_tra_loi")
    private LocalDateTime ngayTraLoi;

    /**
     * Tên người hỏi hiển thị theo chuẩn ẩn danh lịch sự (vd: "Nguyễn V. ***")
     */
    public String getTenNguoiHoiDisplay() {
        if (nguoiHoi == null || nguoiHoi.getHoVaTen() == null || nguoiHoi.getHoVaTen().isBlank()) {
            return "Khách hàng";
        }
        String ten = nguoiHoi.getHoVaTen().trim();
        if (ten.length() <= 3) {
            return ten.charAt(0) + "***";
        }
        return ten.charAt(0) + "***" + ten.charAt(ten.length() - 1);
    }

    /**
     * Kiểm tra xem câu hỏi đã được giải đáp chưa
     */
    public boolean isDaTraLoi() {
        return cauTraLoi != null && !cauTraLoi.trim().isBlank();
    }

    /**
     * Kiểm tra xem người trả lời có phải là Chủ sở hữu của gian hàng hay không
     */
    public boolean isNguoiBanTraLoi() {
        if (!isDaTraLoi() || nguoiTraLoi == null || sanPham == null || sanPham.getGianHang() == null) {
            return false;
        }
        GianHang gh = sanPham.getGianHang();
        if (gh.getChuSoHuu() == null) return false;
        return gh.getChuSoHuu().getMaNguoiDung().equals(nguoiTraLoi.getMaNguoiDung());
    }
}
