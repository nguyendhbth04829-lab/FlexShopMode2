package com.example.demo.repository;

import com.example.demo.entity.ViNguoiBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViNguoiBanRepository extends JpaRepository<ViNguoiBan, Long> {

    Optional<ViNguoiBan> findByGianHang_MaGianHang(Long maGianHang);
}
