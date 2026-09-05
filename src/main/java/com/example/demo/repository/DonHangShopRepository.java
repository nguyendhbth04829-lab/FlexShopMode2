package com.example.demo.repository;

import com.example.demo.entity.DonHangShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonHangShopRepository extends JpaRepository<DonHangShop, Long> {

    @Query("SELECT d FROM DonHangShop d JOIN FETCH d.gianHang JOIN FETCH d.donHangTong t JOIN FETCH t.khachHang WHERE t.khachHang.maNguoiDung = :maKhachHang ORDER BY d.ngayTao DESC")
    List<DonHangShop> findAllByKhachHangId(@Param("maKhachHang") Long maKhachHang);

    @Query("SELECT d FROM DonHangShop d JOIN FETCH d.gianHang JOIN FETCH d.donHangTong t JOIN FETCH t.khachHang WHERE t.khachHang.maNguoiDung = :maKhachHang AND d.trangThai = :trangThai ORDER BY d.ngayTao DESC")
    List<DonHangShop> findAllByKhachHangIdAndTrangThai(@Param("maKhachHang") Long maKhachHang, @Param("trangThai") String trangThai);

    Optional<DonHangShop> findByMaCodeDonShop(String maCodeDonShop);

    List<DonHangShop> findAllByGianHang_MaGianHang(Long maGianHang);

    List<DonHangShop> findByDonHangTongMaDonHangTong(Long maDonHangTong);
}
