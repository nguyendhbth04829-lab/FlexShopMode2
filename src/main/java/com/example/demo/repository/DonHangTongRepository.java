package com.example.demo.repository;

import com.example.demo.entity.DonHangTong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonHangTongRepository extends JpaRepository<DonHangTong, Long> {
    Optional<DonHangTong> findByMaCodeDonTong(String maCodeDonTong);

    java.util.List<DonHangTong> findByKhachHangMaNguoiDungOrderByNgayTaoDesc(Long maKhachHang);
}
