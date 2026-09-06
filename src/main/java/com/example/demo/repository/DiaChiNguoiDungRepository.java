package com.example.demo.repository;

import com.example.demo.entity.DiaChiNguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiNguoiDungRepository extends JpaRepository<DiaChiNguoiDung, Long> {

    List<DiaChiNguoiDung> findByNguoiDung_MaNguoiDung(Long maNguoiDung);
}
