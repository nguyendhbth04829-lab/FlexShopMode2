package com.example.demo.repository;

import com.example.demo.entity.TonKhoChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TonKhoChiTietRepository extends JpaRepository<TonKhoChiTiet, Long> {
    Optional<TonKhoChiTiet> findByKhoHang_MaKhoAndBienThe_MaBienThe(Long maKho, Long maBienThe);
}
