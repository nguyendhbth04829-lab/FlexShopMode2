package com.example.demo.repository;

import com.example.demo.entity.DiaChiNguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Repository truy vấn sổ địa chỉ giao hàng
 * =====================================================================
 */
@Repository
public interface DiaChiNguoiDungRepository extends JpaRepository<DiaChiNguoiDung, Long> {

    List<DiaChiNguoiDung> findByNguoiDung_MaNguoiDung(Long maNguoiDung);
}
