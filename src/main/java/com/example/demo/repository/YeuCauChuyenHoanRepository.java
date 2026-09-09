package com.example.demo.repository;

import com.example.demo.entity.YeuCauChuyenHoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YeuCauChuyenHoanRepository extends JpaRepository<YeuCauChuyenHoan, Long> {
    // US-40: mỗi ShopOrder tối đa 1 yêu cầu chuyển hoàn
    Optional<YeuCauChuyenHoan> findByDonHangShop_MaDonHangShop(Long maDonHangShop);
    boolean existsByDonHangShop_MaDonHangShop(Long maDonHangShop);
}
