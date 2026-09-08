package com.example.demo.repository;

import com.example.demo.entity.TiepThiLienKet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - Repository Tiếp Thị Liên Kết (Affiliate)
 * =====================================================================
 */
@Repository
public interface TiepThiLienKetRepository extends JpaRepository<TiepThiLienKet, Long> {

    List<TiepThiLienKet> findByKoc_MaNguoiDungOrderByNgayTaoDesc(Long maKoc);

    Optional<TiepThiLienKet> findByMaLinkAffiliate(String maLinkAffiliate);

    boolean existsByMaLinkAffiliate(String maLinkAffiliate);

    int countByKoc_MaNguoiDung(Long maKoc);

    List<TiepThiLienKet> findAllByOrderByNgayTaoDesc();
}
