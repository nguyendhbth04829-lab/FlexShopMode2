package com.example.demo.repository;

import com.example.demo.entity.CuocTroChuyen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuocTroChuyenRepository extends JpaRepository<CuocTroChuyen, Long> {

    Optional<CuocTroChuyen> findByKhachHang_MaNguoiDungAndGianHang_MaGianHang(Long maKhachHang, Long maGianHang);

    List<CuocTroChuyen> findByKhachHang_MaNguoiDungOrderByThoiGianTinCuoiDesc(Long maKhachHang);

    List<CuocTroChuyen> findByGianHang_MaGianHangOrderByThoiGianTinCuoiDesc(Long maGianHang);

    // Phân trang danh sách hội thoại của Shop
    Page<CuocTroChuyen> findByGianHang_MaGianHangOrderByThoiGianTinCuoiDesc(Long maGianHang, Pageable pageable);

    // Lọc hội thoại Shop có tin chưa đọc
    List<CuocTroChuyen> findByGianHang_MaGianHangAndSoTinChuaDocShopGreaterThanOrderByThoiGianTinCuoiDesc(Long maGianHang, Integer soTin);

    // Đếm tổng số cuộc hội thoại của Shop
    long countByGianHang_MaGianHang(Long maGianHang);

    // Đếm số cuộc trò chuyện có tin nhắn chưa đọc của Shop
    long countByGianHang_MaGianHangAndSoTinChuaDocShopGreaterThan(Long maGianHang, Integer soTin);

    // Tìm kiếm hội thoại theo tên khách hàng hoặc tên gian hàng
    @Query("SELECT c FROM CuocTroChuyen c WHERE c.khachHang.maNguoiDung = :maKhachHang " +
           "AND LOWER(c.gianHang.tenGianHang) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "ORDER BY c.thoiGianTinCuoi DESC")
    List<CuocTroChuyen> timKiemHoiThoaiKhachHang(@Param("maKhachHang") Long maKhachHang, @Param("tuKhoa") String tuKhoa);

    @Query("SELECT c FROM CuocTroChuyen c WHERE c.gianHang.maGianHang = :maGianHang " +
           "AND (LOWER(c.khachHang.hoVaTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "     OR LOWER(c.khachHang.email) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "ORDER BY c.thoiGianTinCuoi DESC")
    List<CuocTroChuyen> timKiemHoiThoaiShop(@Param("maGianHang") Long maGianHang, @Param("tuKhoa") String tuKhoa);
}
