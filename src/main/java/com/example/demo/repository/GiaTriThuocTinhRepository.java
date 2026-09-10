package com.example.demo.repository;

import com.example.demo.entity.GiaTriThuocTinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiaTriThuocTinhRepository extends JpaRepository<GiaTriThuocTinh, Long> {
    List<GiaTriThuocTinh> findByThuocTinh_MaThuocTinh(Long maThuocTinh);
}
