package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_nguoi_dung")
    private Long maNguoiDung;

    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    @Column(name = "so_dien_thoai", length = 20, unique = true)
    private String soDienThoai;

    @Column(name = "mat_khau_ma_hoa", length = 255, nullable = false)
    private String matKhauMaHoa;

    @Column(name = "ho_va_ten", length = 100, nullable = false)
    private String hoVaTen;

    @Column(name = "anh_dai_dien", length = 500)
    private String anhDaiDien;

    @Builder.Default
    @Column(name = "trang_thai", length = 30)
    private String trangThai = "HOAT_DONG";

    @Column(name = "ly_do_khoa", length = 500)
    private String lyDoKhoa;

    @Builder.Default
    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_xoa")
    private LocalDateTime ngayXoa;

    @Builder.Default
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Builder.Default
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "nguoi_dung_vai_tro",
            joinColumns = @JoinColumn(name = "ma_nguoi_dung"),
            inverseJoinColumns = @JoinColumn(name = "ma_vai_tro")
    )
    private Set<VaiTro> danhSachVaiTro = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (this.ngayTao == null) {
            this.ngayTao = LocalDateTime.now();
        }
        if (this.ngayCapNhat == null) {
            this.ngayCapNhat = LocalDateTime.now();
        }
        if (this.trangThai == null) {
            this.trangThai = "HOAT_DONG";
        }
        if (this.daXoa == null) {
            this.daXoa = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = LocalDateTime.now();
    }
}
