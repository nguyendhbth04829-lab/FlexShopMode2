package com.example.demo.repository;

import com.example.demo.entity.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {
    List<BienTheSanPham> findBySanPham_MaSanPhamAndDaXoaFalse(Long maSanPham);
    boolean existsByMaSku(String sku);
    boolean existsByMaSkuAndMaBienTheNot(String sku, Long maBienThe);
    Optional<BienTheSanPham> findByMaSku(String maSku);
    List<BienTheSanPham> findBySanPham_MaSanPham(Long maSanPham);
    List<BienTheSanPham> findBySanPham_GianHang_MaGianHangAndDaXoaFalse(Long maGianHang);
}
