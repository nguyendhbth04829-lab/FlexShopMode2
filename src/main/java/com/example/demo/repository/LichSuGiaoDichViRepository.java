package com.example.demo.repository;

import com.example.demo.entity.LichSuGiaoDichVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & PHÍ SÀN (DEV 5 - MINH)
 * USER STORY: US-42 & US-43 - Repository truy vấn lịch sử biến động số dư ví người bán
 * =====================================================================
 */
@Repository
public interface LichSuGiaoDichViRepository extends JpaRepository<LichSuGiaoDichVi, Long> {

    List<LichSuGiaoDichVi> findByViNguoiBan_MaViOrderByNgayTaoDesc(Long maVi);
}
