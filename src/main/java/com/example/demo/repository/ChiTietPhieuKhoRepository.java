package com.example.demo.repository;

import com.example.demo.entity.ChiTietPhieuKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietPhieuKhoRepository extends JpaRepository<ChiTietPhieuKho, Long> {
    List<ChiTietPhieuKho> findByPhieuKho_MaPhieuKho(Long maPhieuKho);
}
