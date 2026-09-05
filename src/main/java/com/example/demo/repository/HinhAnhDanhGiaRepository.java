package com.example.demo.repository;

import com.example.demo.entity.HinhAnhDanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HinhAnhDanhGiaRepository extends JpaRepository<HinhAnhDanhGia, Long> {
    List<HinhAnhDanhGia> findAllByDanhGiaSanPham_MaDanhGia(Long maDanhGia);
}
