package com.example.demo.repository;

import com.example.demo.entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Long> {
    Optional<GioHang> findByMaNguoiDung(Long maNguoiDung);
    boolean existsByMaNguoiDung(Long maNguoiDung);
}
