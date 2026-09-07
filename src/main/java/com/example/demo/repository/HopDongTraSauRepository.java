package com.example.demo.repository;

import com.example.demo.entity.HopDongTraSau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - Repository Quản lý Hợp Đồng Trả Sau
 * =====================================================================
 */
@Repository
public interface HopDongTraSauRepository extends JpaRepository<HopDongTraSau, Long> {

    Optional<HopDongTraSau> findByDonHangTong_MaDonHangTong(Long maDonHangTong);

    List<HopDongTraSau> findByTaiKhoanTraSau_MaTkTraSauOrderByNgayTaoDesc(Long maTkTraSau);

    /**
     * Tìm kiếm và phân trang hợp đồng trả sau theo từ khóa (mã hợp đồng, mã đơn hàng, tên khách hàng)
     */
    @Query("SELECT h FROM HopDongTraSau h WHERE " +
           "(:maTkTraSau IS NULL OR h.taiKhoanTraSau.maTkTraSau = :maTkTraSau) " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "     OR LOWER(h.donHangTong.maCodeDonTong) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(h.taiKhoanTraSau.nguoiDung.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(h.taiKhoanTraSau.nguoiDung.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<HopDongTraSau> timKiemNangCao(
            @Param("keyword") String keyword,
            @Param("maTkTraSau") Long maTkTraSau,
            Pageable pageable
    );

    @Query("SELECT COUNT(h) FROM HopDongTraSau h WHERE h.taiKhoanTraSau.maTkTraSau = :maTkTraSau")
    long demTongSoHopDongTheoTaiKhoan(@Param("maTkTraSau") Long maTkTraSau);

    @Query("SELECT COALESCE(SUM(h.tongSoTienVay), 0) FROM HopDongTraSau h WHERE h.taiKhoanTraSau.maTkTraSau = :maTkTraSau")
    BigDecimal tinhTongTienVayTheoTaiKhoan(@Param("maTkTraSau") Long maTkTraSau);
}
