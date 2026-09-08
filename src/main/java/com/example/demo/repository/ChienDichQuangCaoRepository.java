package com.example.demo.repository;

import com.example.demo.entity.ChienDichQuangCao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - Repository Quản Lý Chiến Dịch Quảng Cáo Shopee Ads
 * =====================================================================
 */
@Repository
public interface ChienDichQuangCaoRepository extends JpaRepository<ChienDichQuangCao, Long> {

    List<ChienDichQuangCao> findByGianHang_MaGianHangOrderByNgayBatDauDesc(Long maGianHang);

    @Query("SELECT c FROM ChienDichQuangCao c WHERE " +
           "(:maGianHang IS NULL OR c.gianHang.maGianHang = :maGianHang) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR c.trangThai = :trangThai) " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "     OR LOWER(c.tenChienDich) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(c.sanPham.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ChienDichQuangCao> timKiemNangCao(
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            @Param("maGianHang") Long maGianHang,
            Pageable pageable
    );

    long countByGianHang_MaGianHang(Long maGianHang);

    long countByGianHang_MaGianHangAndTrangThai(Long maGianHang, String trangThai);

    @Query("SELECT COALESCE(SUM(c.nganSachNgay), 0) FROM ChienDichQuangCao c " +
           "WHERE c.gianHang.maGianHang = :maGianHang AND c.trangThai = 'DANG_CHAY'")
    BigDecimal tinhTongNganSachNgayCuaShop(@Param("maGianHang") Long maGianHang);

    @Query("SELECT COALESCE(SUM(c.tongChiPhiDaDung), 0) FROM ChienDichQuangCao c " +
           "WHERE c.gianHang.maGianHang = :maGianHang")
    BigDecimal tinhTongChiPhiDaDungCuaShop(@Param("maGianHang") Long maGianHang);
}
