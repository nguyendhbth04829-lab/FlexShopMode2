package com.example.demo.repository;

import com.example.demo.entity.PhieuKhieuNai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuKhieuNaiRepository extends JpaRepository<PhieuKhieuNai, Long> {

    Optional<PhieuKhieuNai> findByMaCodePhieu(String maCodePhieu);

    List<PhieuKhieuNai> findAllByDonHangShop_MaDonHangShop(Long maDonHangShop);

    boolean existsByDonHangShop_MaDonHangShopAndTrangThaiNotIn(Long maDonHangShop, List<String> danhSachTrangThaiLoaiTru);

    // Tìm kiếm và lọc nâng cao đa tiêu chí
    @Query("SELECT p FROM PhieuKhieuNai p WHERE p.khachHang.maNguoiDung = :maKhachHang " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR p.trangThai = :trangThai) " +
           "AND (:loaiKhieuNai IS NULL OR :loaiKhieuNai = '' OR p.loaiKhieuNai = :loaiKhieuNai) " +
           "AND (:mucDoUuTien IS NULL OR :mucDoUuTien = '' OR p.mucDoUuTien = :mucDoUuTien) " +
           "AND (:tuNgay IS NULL OR p.ngayTao >= :tuNgay) " +
           "AND (:denNgay IS NULL OR p.ngayTao <= :denNgay) " +
           "AND (:tuKhoa IS NULL OR :tuKhoa = '' " +
           "     OR LOWER(p.maCodePhieu) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "     OR LOWER(p.donHangShop.maCodeDonShop) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
           "     OR LOWER(p.gianHang.tenGianHang) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "ORDER BY p.ngayTao DESC")
    Page<PhieuKhieuNai> timKiemKhieuNaiNangCao(
            @Param("maKhachHang") Long maKhachHang,
            @Param("tuKhoa") String tuKhoa,
            @Param("trangThai") String trangThai,
            @Param("loaiKhieuNai") String loaiKhieuNai,
            @Param("mucDoUuTien") String mucDoUuTien,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable);

    // Thống kê đồng bộ từ database
    long countByKhachHang_MaNguoiDung(Long maKhachHang);

    long countByKhachHang_MaNguoiDungAndTrangThai(Long maKhachHang, String trangThai);

    @Query("SELECT COALESCE(SUM(p.soTienHoanTra), 0) FROM PhieuKhieuNai p " +
           "WHERE p.khachHang.maNguoiDung = :maKhachHang AND p.trangThai IN :danhSachTrangThai")
    BigDecimal tinhTongTienTheoDanhSachTrangThai(
            @Param("maKhachHang") Long maKhachHang,
            @Param("danhSachTrangThai") List<String> danhSachTrangThai);
}
