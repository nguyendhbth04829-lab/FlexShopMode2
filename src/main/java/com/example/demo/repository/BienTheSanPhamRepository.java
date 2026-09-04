package com.example.demo.repository;

import com.example.demo.entity.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Long> {
    Optional<BienTheSanPham> findByMaSku(String maSku);
}
