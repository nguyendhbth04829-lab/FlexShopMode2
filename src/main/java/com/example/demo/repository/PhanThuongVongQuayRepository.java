package com.example.demo.repository;

import com.example.demo.entity.PhanThuongVongQuay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhanThuongVongQuayRepository extends JpaRepository<PhanThuongVongQuay, Long> {

    List<PhanThuongVongQuay> findByDangHoatDongTrueOrderByThuTuOAsc();

    Optional<PhanThuongVongQuay> findByThuTuO(Integer thuTuO);
}
