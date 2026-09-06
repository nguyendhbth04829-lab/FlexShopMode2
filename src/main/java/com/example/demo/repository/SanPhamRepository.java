package com.example.demo.repository;

import com.example.demo.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    
    boolean existsByDanhMuc_MaDanhMucAndDaXoaFalse(Long maDanhMuc);

    Page<SanPham> findByDaXoaFalse(Pageable pageable);

    @Query("SELECT s FROM SanPham s WHERE s.daXoa = false AND (s.tenSanPham LIKE %:tuKhoa% OR s.duongDanSlug LIKE %:tuKhoa%)")
    Page<SanPham> timKiemSanPham(String tuKhoa, Pageable pageable);
}
