package com.example.demo.repository;

import com.example.demo.entity.HinhAnhSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HinhAnhSanPhamRepository extends JpaRepository<HinhAnhSanPham, Long> {
    List<HinhAnhSanPham> findBySanPham_MaSanPhamOrderByThuTuHienThiAsc(Long maSanPham);
    int countBySanPham_MaSanPham(Long maSanPham);
}
