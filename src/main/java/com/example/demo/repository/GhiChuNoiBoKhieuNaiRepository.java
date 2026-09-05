package com.example.demo.repository;

import com.example.demo.entity.GhiChuNoiBoKhieuNai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GhiChuNoiBoKhieuNaiRepository extends JpaRepository<GhiChuNoiBoKhieuNai, Long> {
    List<GhiChuNoiBoKhieuNai> findAllByPhieuKhieuNai_MaPhieuOrderByNgayTaoDesc(Long maPhieu);
}
