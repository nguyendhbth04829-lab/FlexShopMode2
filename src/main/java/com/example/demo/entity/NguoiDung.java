package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Thực thể Người dùng (Khách hàng / Chủ gian hàng)
 * =====================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nguoi_dung")
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nguoi_dung")
    private Long maNguoiDung;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "so_dien_thoai", unique = true, length = 20)
    private String soDienThoai;

    @Column(name = "mat_khau_ma_hoa", nullable = false, length = 255)
    private String matKhauMaHoa;

    @Column(name = "ho_va_ten", nullable = false, length = 100)
    private String hoVaTen;

    public String getHoTen() {
        return hoVaTen;
    }

    public void setHoTen(String hoTen) {
        this.hoVaTen = hoTen;
    }

    @Column(name = "anh_dai_dien", length = 500)
    private String anhDaiDien;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "HOAT_DONG";

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_xoa")
    private LocalDateTime ngayXoa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ly_do_khoa", length = 255)
    private String lyDoKhoa;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "nguoi_dung_vai_tro",
        joinColumns = @JoinColumn(name = "ma_nguoi_dung"),
        inverseJoinColumns = @JoinColumn(name = "ma_vai_tro")
    )
    private Set<VaiTro> danhSachVaiTro = new HashSet<>();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();
}
