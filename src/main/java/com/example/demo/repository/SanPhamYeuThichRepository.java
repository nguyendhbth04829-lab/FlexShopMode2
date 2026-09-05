package com.example.demo.repository;

import com.example.demo.entity.SanPhamYeuThich;
import com.example.demo.entity.SanPhamYeuThichId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SanPhamYeuThichRepository extends JpaRepository<SanPhamYeuThich, SanPhamYeuThichId> {

    boolean existsByMaNguoiDungAndMaSanPham(Long maNguoiDung, Long maSanPham);

    Optional<SanPhamYeuThich> findByMaNguoiDungAndMaSanPham(Long maNguoiDung, Long maSanPham);

    @Modifying
    @Query("DELETE FROM SanPhamYeuThich y WHERE y.maNguoiDung = :maNguoiDung AND y.maSanPham = :maSanPham")
    void xoaSanPhamYeuThich(@Param("maNguoiDung") Long maNguoiDung, @Param("maSanPham") Long maSanPham);

    long countByMaNguoiDung(Long maNguoiDung);

    long countByMaSanPham(Long maSanPham);

    @Query(value = "SELECT y FROM SanPhamYeuThich y " +
                   "JOIN FETCH y.sanPham sp " +
                   "LEFT JOIN FETCH sp.gianHang gh " +
                   "WHERE y.maNguoiDung = :maNguoiDung " +
                   "AND (:maDanhMuc IS NULL OR sp.danhMuc.maDanhMuc = :maDanhMuc) " +
                   "AND (:tuKhoa IS NULL OR TRIM(:tuKhoa) = '' OR sp.tenSanPham LIKE :tuKhoaPattern) " +
                   "ORDER BY y.ngayTao DESC",
           countQuery = "SELECT COUNT(y) FROM SanPhamYeuThich y " +
                        "WHERE y.maNguoiDung = :maNguoiDung " +
                        "AND (:maDanhMuc IS NULL OR y.sanPham.danhMuc.maDanhMuc = :maDanhMuc) " +
                        "AND (:tuKhoa IS NULL OR TRIM(:tuKhoa) = '' OR y.sanPham.tenSanPham LIKE :tuKhoaPattern)")
    Page<SanPhamYeuThich> timKiemWishlistChoKhachHang(
            @Param("maNguoiDung") Long maNguoiDung,
            @Param("maDanhMuc") Long maDanhMuc,
            @Param("tuKhoa") String tuKhoa,
            @Param("tuKhoaPattern") String tuKhoaPattern,
            Pageable pageable
    );
}
