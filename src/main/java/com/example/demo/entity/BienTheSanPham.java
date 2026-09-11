package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bien_the_san_pham")
public class BienTheSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_bien_the")
    private Long maBienThe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_san_pham", referencedColumnName = "ma_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "ma_sku", nullable = false, length = 100, unique = true)
    private String maSku;

    @Column(name = "ten_bien_the", nullable = false, length = 200)
    private String tenBienThe;

    @Column(name = "gia_ban", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "gia_goc", precision = 18, scale = 2)
    private BigDecimal giaGoc;

    @Column(name = "link_anh", length = 500)
    private String linkAnh;

    @Column(name = "can_nang_gram")
    private Integer canNangGram;

    @Column(name = "da_xoa")
    private Boolean daXoa = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();
}
