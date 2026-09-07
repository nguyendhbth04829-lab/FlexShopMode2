package com.example.demo.repository;

import com.example.demo.entity.TaiKhoanTraSau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - Repository Quản lý Tài Khoản Trả Sau / SPayLater
 * =====================================================================
 */
@Repository
public interface TaiKhoanTraSauRepository extends JpaRepository<TaiKhoanTraSau, Long> {

    Optional<TaiKhoanTraSau> findByNguoiDung_MaNguoiDung(Long maNguoiDung);

    boolean existsByNguoiDung_MaNguoiDung(Long maNguoiDung);
}
