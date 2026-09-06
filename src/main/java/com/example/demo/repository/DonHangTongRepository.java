package com.example.demo.repository;

import com.example.demo.entity.DonHangTong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonHangTongRepository extends JpaRepository<DonHangTong, Long> {

    Optional<DonHangTong> findByMaCodeDonTong(String maCodeDonTong);

    List<DonHangTong> findAllByOrderByNgayTaoDesc();

    Page<DonHangTong> findAllByOrderByNgayTaoDesc(Pageable pageable);

    @Query("SELECT d FROM DonHangTong d WHERE " +
           "(:keyword IS NULL OR LOWER(d.maCodeDonTong) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.khachHang.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:trangThaiThanhToan IS NULL OR :trangThaiThanhToan = '' OR d.trangThaiThanhToan = :trangThaiThanhToan) " +
           "ORDER BY d.ngayTao DESC")
    Page<DonHangTong> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("trangThaiThanhToan") String trangThaiThanhToan,
            Pageable pageable
    );
}
