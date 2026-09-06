package com.example.demo.repository;

import com.example.demo.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    Optional<SanPham> findByDuongDanSlug(String duongDanSlug);
    java.util.List<SanPham> findByGianHang_MaGianHang(Long maGianHang);
    java.util.List<SanPham> findByGianHang_MaGianHangAndTrangThai(Long maGianHang, String trangThai);
}
