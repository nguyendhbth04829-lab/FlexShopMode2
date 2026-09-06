package com.example.demo.repository;

import com.example.demo.entity.PhieuNhapXuatKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuNhapXuatKhoRepository extends JpaRepository<PhieuNhapXuatKho, Long> {
}
