package com.example.demo.repository;

import com.example.demo.entity.ViTriKeKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViTriKeKhoRepository extends JpaRepository<ViTriKeKho, Long> {
    List<ViTriKeKho> findByKhoHang_MaKho(Long maKho);
}
