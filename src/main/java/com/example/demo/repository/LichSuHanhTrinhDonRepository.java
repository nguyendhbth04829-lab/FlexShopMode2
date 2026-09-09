package com.example.demo.repository;

import com.example.demo.entity.LichSuHanhTrinhDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuHanhTrinhDonRepository extends JpaRepository<LichSuHanhTrinhDon, Long> {
    // US-33: timeline kiện hàng mới nhất trước
    List<LichSuHanhTrinhDon> findAllByDonHangShop_MaDonHangShopOrderByThoiGianDesc(Long maDonHangShop);
}
