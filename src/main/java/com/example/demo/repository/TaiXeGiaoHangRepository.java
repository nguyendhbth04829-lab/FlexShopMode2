package com.example.demo.repository;

import com.example.demo.entity.TaiXeGiaoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaiXeGiaoHangRepository extends JpaRepository<TaiXeGiaoHang, Long> {
    Optional<TaiXeGiaoHang> findByNguoiDung_MaNguoiDung(Long maNguoiDung);
    Optional<TaiXeGiaoHang> findByBienSoXe(String bienSoXe);
}
