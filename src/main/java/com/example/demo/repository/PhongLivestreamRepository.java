package com.example.demo.repository;

import com.example.demo.entity.PhongLivestream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác bảng phong_livestream (US-61)
 */
@Repository
public interface PhongLivestreamRepository extends JpaRepository<PhongLivestream, Long> {

    // Lấy danh sách phòng live của gian hàng có phân trang
    Page<PhongLivestream> findByGianHang_MaGianHangOrderByThoiGianBatDauDesc(Long maGianHang, Pageable pageable);

    Page<PhongLivestream> findByGianHang_MaGianHangAndTrangThaiOrderByThoiGianBatDauDesc(Long maGianHang, String trangThai, Pageable pageable);

    @Query("SELECT p FROM PhongLivestream p WHERE p.gianHang.maGianHang = :maGianHang " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR p.trangThai = :trangThai) " +
           "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(p.tieuDe) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "ORDER BY p.thoiGianBatDau DESC")
    Page<PhongLivestream> timKiemSeller(
            @Param("maGianHang") Long maGianHang,
            @Param("trangThai") String trangThai,
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable
    );

    // Khách hàng: Lấy danh sách phòng đang live hoặc sắp diễn ra
    @Query("SELECT p FROM PhongLivestream p WHERE p.trangThai IN ('DANG_LIVE', 'SAP_DIEN_RA') " +
           "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(p.tieuDe) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "     OR LOWER(p.gianHang.tenGianHang) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "ORDER BY CASE WHEN p.trangThai = 'DANG_LIVE' THEN 0 ELSE 1 END, p.soNguoiXemHienTai DESC, p.thoiGianBatDau DESC")
    Page<PhongLivestream> timKiemKhamPha(
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable
    );

    List<PhongLivestream> findByTrangThai(String trangThai);

    long countByGianHang_MaGianHang(Long maGianHang);

    long countByGianHang_MaGianHangAndTrangThai(Long maGianHang, String trangThai);

    @Query("SELECT COALESCE(SUM(p.tongLuotXem), 0) FROM PhongLivestream p WHERE p.gianHang.maGianHang = :maGianHang")
    long tongLuotXemCuaShop(@Param("maGianHang") Long maGianHang);
}
