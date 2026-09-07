package com.example.demo.repository;

import com.example.demo.entity.SanPhamLivestream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác bảng san_pham_livestream (US-61)
 */
@Repository
public interface SanPhamLivestreamRepository extends JpaRepository<SanPhamLivestream, Long> {

    List<SanPhamLivestream> findByPhongLivestream_MaLiveOrderByThuTuHienThiAsc(Long maLive);

    Optional<SanPhamLivestream> findFirstByPhongLivestream_MaLiveAndLaSanPhamDangGhimTrue(Long maLive);

    Optional<SanPhamLivestream> findByPhongLivestream_MaLiveAndSanPham_MaSanPham(Long maLive, Long maSanPham);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE SanPhamLivestream sp SET sp.laSanPhamDangGhim = false WHERE sp.phongLivestream.maLive = :maLive")
    void boGhimTatCaTrongPhong(@Param("maLive") Long maLive);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM SanPhamLivestream sp WHERE sp.phongLivestream.maLive = :maLive AND sp.sanPham.maSanPham = :maSanPham")
    void xoaSanPhamKhoiLive(@Param("maLive") Long maLive, @Param("maSanPham") Long maSanPham);

    long countByPhongLivestream_MaLive(Long maLive);
}
