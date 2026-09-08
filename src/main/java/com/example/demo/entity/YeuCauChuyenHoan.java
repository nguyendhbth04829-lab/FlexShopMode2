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
@Table(name = "yeu_cau_chuyen_hoan")
public class YeuCauChuyenHoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_chuyen_hoan")
    private Long maChuyenHoan;

    // US-40: 1 ShopOrder chỉ có tối đa 1 yêu cầu chuyển hoàn (UNIQUE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_hang_shop", referencedColumnName = "ma_don_hang_shop", nullable = false, unique = true)
    private DonHangShop donHangShop;

    @Column(name = "ly_do_chuyen_hoan", nullable = false, length = 255)
    private String lyDoChuyenHoan;

    @Column(name = "ma_van_don_tra_hang", length = 100)
    private String maVanDonTraHang;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "DANG_CHUYEN_HOAN";

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();
}
