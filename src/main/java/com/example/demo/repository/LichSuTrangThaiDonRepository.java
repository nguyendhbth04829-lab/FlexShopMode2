package com.example.demo.repository;

import com.example.demo.entity.LichSuTrangThaiDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuTrangThaiDonRepository extends JpaRepository<LichSuTrangThaiDon, Long> {
    List<LichSuTrangThaiDon> findAllByDonHangShop_MaDonHangShopOrderByThoiGianAsc(Long maDonHangShop);
}
