package com.example.demo.repository;

import com.example.demo.entity.ThuocTinhSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThuocTinhSanPhamRepository extends JpaRepository<ThuocTinhSanPham, Long> {
    List<ThuocTinhSanPham> findBySanPham_MaSanPham(Long maSanPham);
}
