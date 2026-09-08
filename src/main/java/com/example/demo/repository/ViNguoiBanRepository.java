package com.example.demo.repository;

import com.example.demo.entity.ViNguoiBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & PHÍ SÀN (DEV 5 - MINH)
 * USER STORY: US-42 & US-43 - Repository quản lý ví người bán (vi_nguoi_ban)
 * =====================================================================
 */
@Repository
public interface ViNguoiBanRepository extends JpaRepository<ViNguoiBan, Long> {

    Optional<ViNguoiBan> findByGianHang_MaGianHang(Long maGianHang);
}
