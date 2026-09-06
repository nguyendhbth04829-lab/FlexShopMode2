package com.example.demo.repository;

import com.example.demo.entity.LichSuSaoQuaTa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuSaoQuaTaRepository extends JpaRepository<LichSuSaoQuaTa, Long> {
    List<LichSuSaoQuaTa> findByMaGianHangOrderByNgayTaoDesc(Long maGianHang);
}
