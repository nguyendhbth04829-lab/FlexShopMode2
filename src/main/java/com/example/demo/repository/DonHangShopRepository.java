package com.example.demo.repository;

import com.example.demo.entity.DonHangShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Repository truy vấn đơn hàng theo từng Shop
 * =====================================================================
 */
@Repository
public interface DonHangShopRepository extends JpaRepository<DonHangShop, Long> {

    List<DonHangShop> findByTrangThai(String trangThai);

    List<DonHangShop> findByDonHangTong_KhachHang_MaNguoiDungOrderByNgayTaoDesc(Long maKhachHang);

    List<DonHangShop> findAllByOrderByNgayTaoDesc();
}
