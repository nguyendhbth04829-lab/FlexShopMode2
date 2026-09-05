package com.example.demo.repository;

import com.example.demo.entity.HoiDapSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HoiDapSanPhamRepository extends JpaRepository<HoiDapSanPham, Long> {

    long countBySanPham_MaSanPham(Long maSanPham);

    @Query("SELECT COUNT(h) FROM HoiDapSanPham h WHERE h.sanPham.maSanPham = :maSanPham AND h.cauTraLoi IS NOT NULL AND TRIM(h.cauTraLoi) <> ''")
    long demCauHoiDaTraLoi(@Param("maSanPham") Long maSanPham);

    @Query("SELECT COUNT(h) FROM HoiDapSanPham h WHERE h.sanPham.maSanPham = :maSanPham AND (h.cauTraLoi IS NULL OR TRIM(h.cauTraLoi) = '')")
    long demCauHoiChoTraLoi(@Param("maSanPham") Long maSanPham);

    long countByNguoiHoi_MaNguoiDung(Long maNguoiDung);

    @Query(value = "SELECT h FROM HoiDapSanPham h " +
                   "JOIN FETCH h.nguoiHoi nh " +
                   "LEFT JOIN FETCH h.nguoiTraLoi nt " +
                   "WHERE h.sanPham.maSanPham = :maSanPham " +
                   "AND (:trangThai IS NULL OR :trangThai = '' OR :trangThai = 'TAT_CA' " +
                   "     OR (:trangThai = 'DA_TRA_LOI' AND h.cauTraLoi IS NOT NULL AND TRIM(h.cauTraLoi) <> '') " +
                   "     OR (:trangThai = 'CHUA_TRA_LOI' AND (h.cauTraLoi IS NULL OR TRIM(h.cauTraLoi) = ''))) " +
                   "AND (:tuKhoa IS NULL OR TRIM(:tuKhoa) = '' " +
                   "     OR h.cauHoi LIKE :tuKhoaPattern " +
                   "     OR h.cauTraLoi LIKE :tuKhoaPattern) " +
                   "ORDER BY h.ngayHoi DESC",
           countQuery = "SELECT COUNT(h) FROM HoiDapSanPham h " +
                        "WHERE h.sanPham.maSanPham = :maSanPham " +
                        "AND (:trangThai IS NULL OR :trangThai = '' OR :trangThai = 'TAT_CA' " +
                        "     OR (:trangThai = 'DA_TRA_LOI' AND h.cauTraLoi IS NOT NULL AND TRIM(h.cauTraLoi) <> '') " +
                        "     OR (:trangThai = 'CHUA_TRA_LOI' AND (h.cauTraLoi IS NULL OR TRIM(h.cauTraLoi) = ''))) " +
                        "AND (:tuKhoa IS NULL OR TRIM(:tuKhoa) = '' " +
                        "     OR h.cauHoi LIKE :tuKhoaPattern " +
                        "     OR h.cauTraLoi LIKE :tuKhoaPattern)")
    Page<HoiDapSanPham> timKiemHoiDapChoSanPham(
            @Param("maSanPham") Long maSanPham,
            @Param("trangThai") String trangThai,
            @Param("tuKhoa") String tuKhoa,
            @Param("tuKhoaPattern") String tuKhoaPattern,
            Pageable pageable
    );
}
