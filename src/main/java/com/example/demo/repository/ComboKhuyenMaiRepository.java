package com.example.demo.repository;

import com.example.demo.entity.ComboKhuyenMai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác bảng combo_khuyen_mai (US-54 - PROMOTION)
 */
@Repository
public interface ComboKhuyenMaiRepository extends JpaRepository<ComboKhuyenMai, Long> {

    Optional<ComboKhuyenMai> findByMaComboAndGianHangMaGianHang(Long maCombo, Long maGianHang);

    /**
     * Tìm kiếm và phân trang chương trình Combo cho Seller
     */
    @Query("SELECT c FROM ComboKhuyenMai c WHERE c.gianHang.maGianHang = :maGianHang " +
            "AND (:loaiCombo = 'TAT_CA' OR c.loaiCombo = :loaiCombo) " +
            "AND (" +
            "     :trangThai = 'TAT_CA' " +
            "  OR (:trangThai = 'DANG_DIEN_RA' AND c.dangHoatDong = true AND :now >= c.ngayBatDau AND :now <= c.ngayKetThuc) " +
            "  OR (:trangThai = 'SAP_DIEN_RA' AND c.dangHoatDong = true AND :now < c.ngayBatDau) " +
            "  OR (:trangThai = 'DA_KET_THUC' AND :now > c.ngayKetThuc) " +
            "  OR (:trangThai = 'TAM_DUNG' AND c.dangHoatDong = false)" +
            ") " +
            "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(c.tenCombo) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
            "ORDER BY c.maCombo DESC")
    Page<ComboKhuyenMai> timKiemCombo(
            @Param("maGianHang") Long maGianHang,
            @Param("loaiCombo") String loaiCombo,
            @Param("trangThai") String trangThai,
            @Param("tuKhoa") String tuKhoa,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * Lấy các chương trình đang hoạt động (dành cho người mua / trang tổng hợp)
     */
    @Query("SELECT c FROM ComboKhuyenMai c WHERE c.dangHoatDong = true " +
            "AND :now >= c.ngayBatDau AND :now <= c.ngayKetThuc " +
            "ORDER BY c.ngayKetThuc ASC")
    List<ComboKhuyenMai> timComboDangChay(@Param("now") LocalDateTime now);

    /**
     * Đếm tổng số combo của shop
     */
    long countByGianHangMaGianHang(Long maGianHang);

    /**
     * Đếm số combo đang diễn ra
     */
    @Query("SELECT COUNT(c) FROM ComboKhuyenMai c WHERE c.gianHang.maGianHang = :maGianHang " +
            "AND c.dangHoatDong = true AND :now >= c.ngayBatDau AND :now <= c.ngayKetThuc")
    long countDangChay(@Param("maGianHang") Long maGianHang, @Param("now") LocalDateTime now);

    /**
     * Đếm số combo sắp diễn ra
     */
    @Query("SELECT COUNT(c) FROM ComboKhuyenMai c WHERE c.gianHang.maGianHang = :maGianHang " +
            "AND c.dangHoatDong = true AND :now < c.ngayBatDau")
    long countSapChay(@Param("maGianHang") Long maGianHang, @Param("now") LocalDateTime now);
}