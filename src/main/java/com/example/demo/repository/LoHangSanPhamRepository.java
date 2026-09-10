package com.example.demo.repository;

import com.example.demo.entity.LoHangSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoHangSanPhamRepository extends JpaRepository<LoHangSanPham, Long> {
    List<LoHangSanPham> findByBienThe_MaBienThe(Long maBienThe);
    List<LoHangSanPham> findByHanSuDungBefore(LocalDate date);
}
