package com.example.demo.repository;

import com.example.demo.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - Repository Quản lý Sản Phẩm
 * =====================================================================
 */
@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {

    boolean existsByDanhMuc_MaDanhMucAndDaXoaFalse(Long maDanhMuc);

    Page<SanPham> findByDaXoaFalse(Pageable pageable);
    List<SanPham> findByGianHang_MaGianHangAndDaXoaFalse(Long maGianHang);

    @Query("SELECT s FROM SanPham s WHERE s.daXoa = false AND (s.tenSanPham LIKE %:tuKhoa% OR s.duongDanSlug LIKE %:tuKhoa%)")
    Page<SanPham> timKiemSanPham(String tuKhoa, Pageable pageable);
    @Query("SELECT s FROM SanPham s WHERE s.daXoa = false AND LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SanPham> timKiemSanPhamTuNhien(@Param("keyword") String keyword);
    Optional<SanPham> findByDuongDanSlug(String duongDanSlug);
    java.util.List<SanPham> findByGianHang_MaGianHang(Long maGianHang);
    java.util.List<SanPham> findByGianHang_MaGianHangAndTrangThai(Long maGianHang, String trangThai);
}
