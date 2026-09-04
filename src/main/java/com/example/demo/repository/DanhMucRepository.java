package com.example.demo.repository;

import com.example.demo.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
    Optional<DanhMuc> findByDuongDanSlug(String duongDanSlug);
}
