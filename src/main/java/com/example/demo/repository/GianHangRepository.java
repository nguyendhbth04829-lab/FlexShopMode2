package com.example.demo.repository;

import com.example.demo.entity.GianHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Repository truy vấn thông tin gian hàng người bán
 * =====================================================================
 */
@Repository
public interface GianHangRepository extends JpaRepository<GianHang, Long> {

    Optional<GianHang> findByDuongDanSlug(String slug);
}
