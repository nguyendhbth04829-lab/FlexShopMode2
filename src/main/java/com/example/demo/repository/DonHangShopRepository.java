package com.example.demo.repository;

import com.example.demo.entity.DonHangShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonHangShopRepository extends JpaRepository<DonHangShop, Long> {

    List<DonHangShop> findByTrangThai(String trangThai);

    List<DonHangShop> findByDonHangTong_KhachHang_MaNguoiDungOrderByNgayTaoDesc(Long maKhachHang);

    List<DonHangShop> findAllByOrderByNgayTaoDesc();
}
