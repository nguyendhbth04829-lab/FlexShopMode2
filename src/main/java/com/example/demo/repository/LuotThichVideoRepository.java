package com.example.demo.repository;

import com.example.demo.entity.LuotThichVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác bảng luot_thich_video (US-62)
 */
@Repository
public interface LuotThichVideoRepository extends JpaRepository<LuotThichVideo, Long> {

    boolean existsByVideo_MaVideoAndNguoiDung_MaNguoiDung(Long maVideo, Long maNguoiDung);

    Optional<LuotThichVideo> findByVideo_MaVideoAndNguoiDung_MaNguoiDung(Long maVideo, Long maNguoiDung);

    long countByVideo_MaVideo(Long maVideo);

    void deleteByVideo_MaVideoAndNguoiDung_MaNguoiDung(Long maVideo, Long maNguoiDung);
}
