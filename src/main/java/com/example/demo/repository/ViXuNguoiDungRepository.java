package com.example.demo.repository;

import com.example.demo.entity.ViXuNguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác bảng Ví Xu Người Dùng (US-55 - Coin Reward System)
 */
@Repository
public interface ViXuNguoiDungRepository extends JpaRepository<ViXuNguoiDung, Long> {

    Optional<ViXuNguoiDung> findByMaNguoiDung(Long maNguoiDung);

    Optional<ViXuNguoiDung> findByNguoiDung_Email(String email);
}
