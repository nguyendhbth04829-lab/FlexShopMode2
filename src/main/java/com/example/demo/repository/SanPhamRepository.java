package com.example.demo.repository;

import com.example.demo.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - Repository Quản lý Sản Phẩm
 * =====================================================================
 */
@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {

    List<SanPham> findByGianHang_MaGianHangAndDaXoaFalse(Long maGianHang);

    @Query("SELECT s FROM SanPham s WHERE s.daXoa = false AND LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SanPham> timKiemSanPhamTuNhien(@Param("keyword") String keyword);
}
