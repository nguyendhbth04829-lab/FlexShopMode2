package com.example.demo.repository;

import com.example.demo.entity.DonHangTiepThi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - Repository Đơn Hàng Tiếp Thị Liên Kết
 * =====================================================================
 */
@Repository
public interface DonHangTiepThiRepository extends JpaRepository<DonHangTiepThi, Long> {

    List<DonHangTiepThi> findAllByOrderByNgayGhiNhanDesc();

    List<DonHangTiepThi> findByTiepThiLienKet_Koc_MaNguoiDungOrderByNgayGhiNhanDesc(Long maKoc);

    List<DonHangTiepThi> findByTiepThiLienKet_MaAffiliateOrderByNgayGhiNhanDesc(Long maAffiliate);

    List<DonHangTiepThi> findByTrangThaiOrderByNgayGhiNhanDesc(String trangThai);

    List<DonHangTiepThi> findByTiepThiLienKet_Koc_MaNguoiDungAndTrangThaiOrderByNgayGhiNhanDesc(Long maKoc, String trangThai);

    boolean existsByDonHangShop_MaDonHangShop(Long maDonHangShop);

    int countByTiepThiLienKet_Koc_MaNguoiDung(Long maKoc);

    @Query("SELECT COALESCE(SUM(d.hoaHongDuocNhan), 0) FROM DonHangTiepThi d " +
           "WHERE d.tiepThiLienKet.koc.maNguoiDung = :maKoc AND d.trangThai = :trangThai")
    BigDecimal tinhTongHoaHongTheoTrangThaiVaKoc(@Param("maKoc") Long maKoc, @Param("trangThai") String trangThai);

    @Query("SELECT COALESCE(SUM(d.hoaHongDuocNhan), 0) FROM DonHangTiepThi d WHERE d.trangThai = :trangThai")
    BigDecimal tinhTongHoaHongHeThongTheoTrangThai(@Param("trangThai") String trangThai);

    @Query("SELECT COALESCE(SUM(d.donHangShop.tienHangShop), 0) FROM DonHangTiepThi d " +
           "WHERE d.tiepThiLienKet.koc.maNguoiDung = :maKoc AND d.trangThai <> 'DA_HUY'")
    BigDecimal tinhTongDoanhThuDonHangCuaKoc(@Param("maKoc") Long maKoc);
}
