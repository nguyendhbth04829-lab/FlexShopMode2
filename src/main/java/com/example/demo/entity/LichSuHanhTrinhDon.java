package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_hanh_trinh_don")
public class LichSuHanhTrinhDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_hanh_trinh")
    private Long maHanhTrinh;

    // US-33: mỗi checkpoint hành trình thuộc 1 ShopOrder
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_shop", referencedColumnName = "ma_don_hang_shop", nullable = false)
    private DonHangShop donHangShop;

    // Nullable: mốc như CHO_LAY_HANG / DANG_GIAO có thể chưa qua Hub
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hub", referencedColumnName = "ma_hub")
    private TramTrungChuyenHub hub;

    @Column(name = "tieu_de_moc", nullable = false, length = 150)
    private String tieuDeMoc;

    @Column(name = "vi_tri_hien_tai", length = 255)
    private String viTriHienTai;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian = LocalDateTime.now();
}
