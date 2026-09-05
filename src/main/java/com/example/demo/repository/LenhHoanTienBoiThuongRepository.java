package com.example.demo.repository;

import com.example.demo.entity.LenhHoanTienBoiThuong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LenhHoanTienBoiThuongRepository extends JpaRepository<LenhHoanTienBoiThuong, Long> {

    Optional<LenhHoanTienBoiThuong> findByPhieuKhieuNai_MaPhieu(Long maPhieu);

    boolean existsByPhieuKhieuNai_MaPhieu(Long maPhieu);

    @Query("SELECT l FROM LenhHoanTienBoiThuong l " +
           "WHERE (:tuKhoa IS NULL OR :tuKhoa = '' OR " +
           "       LOWER(l.phieuKhieuNai.maCodePhieu) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR " +
           "       LOWER(l.phieuKhieuNai.donHangShop.maCodeDonShop) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR " +
           "       LOWER(l.nguoiNhanTien.hoVaTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR " +
           "       LOWER(l.nguoiNhanTien.email) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR " +
           "       LOWER(l.nguoiNhanTien.soDienThoai) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "  AND (:benChiuPhi IS NULL OR :benChiuPhi = '' OR l.benChiuPhi = :benChiuPhi) " +
           "  AND (:trangThai IS NULL OR :trangThai = '' OR l.trangThai = :trangThai) " +
           "  AND (:tuNgay IS NULL OR l.ngayThucHien >= :tuNgay) " +
           "  AND (:denNgay IS NULL OR l.ngayThucHien <= :denNgay) " +
           "ORDER BY l.ngayThucHien DESC")
    Page<LenhHoanTienBoiThuong> timKiemLenhHoanTien(
            @Param("tuKhoa") String tuKhoa,
            @Param("benChiuPhi") String benChiuPhi,
            @Param("trangThai") String trangThai,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(l.soTien), 0) FROM LenhHoanTienBoiThuong l " +
           "WHERE l.nguoiNhanTien.maNguoiDung = l.phieuKhieuNai.khachHang.maNguoiDung")
    BigDecimal sumTongTienHoanKhach();

    @Query("SELECT COALESCE(SUM(l.soTien), 0) FROM LenhHoanTienBoiThuong l " +
           "WHERE l.nguoiNhanTien.maNguoiDung <> l.phieuKhieuNai.khachHang.maNguoiDung")
    BigDecimal sumTongTienBoiThuongShop();

    long countByBenChiuPhi(String benChiuPhi);
}
