package com.example.demo.repository;

import com.example.demo.entity.SanPhamFlashSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository thao tác bảng san_pham_flash_sale (US-53 - PROMOTION)
 */
@Repository
public interface SanPhamFlashSaleRepository extends JpaRepository<SanPhamFlashSale, Long> {

    /**
     * Lấy danh sách sản phẩm theo khung giờ
     */
    List<SanPhamFlashSale> findByKhungGioFlashSale_MaFlashSale(Long maFlashSale);

    /**
     * Lấy danh sách sản phẩm theo khung giờ có phân trang
     */
    Page<SanPhamFlashSale> findByKhungGioFlashSale_MaFlashSale(Long maFlashSale, Pageable pageable);

    /**
     * Kiểm tra biến thể sản phẩm đã đăng ký vào khung giờ này chưa (chống trùng)
     */
    boolean existsByKhungGioFlashSale_MaFlashSaleAndBienTheSanPham_MaBienThe(Long maFlashSale, Long maBienThe);

    /**
     * Đếm tổng số sản phẩm tham gia trong 1 khung giờ
     */
    long countByKhungGioFlashSale_MaFlashSale(Long maFlashSale);

    /**
     * Tính tổng số suất đã bán trong khung giờ
     */
    @Query("SELECT COALESCE(SUM(s.soLuongDaBan), 0) FROM SanPhamFlashSale s WHERE s.khungGioFlashSale.maFlashSale = :maFlashSale")
    Integer tongSoLuongDaBanTrongKhungGio(@Param("maFlashSale") Long maFlashSale);

    /**
     * Tính tổng doanh thu từ tất cả sản phẩm Flash Sale đã bán ra
     */
    @Query("SELECT COALESCE(SUM(s.soLuongDaBan * s.giaFlashSale), 0) FROM SanPhamFlashSale s")
    BigDecimal tongDoanhThuFlashSaleToanHeThong();

    /**
     * Tính tổng số suất Flash Sale đã bán trên toàn hệ thống
     */
    @Query("SELECT COALESCE(SUM(s.soLuongDaBan), 0) FROM SanPhamFlashSale s")
    Long tongSuatDaBanToanHeThong();
}
