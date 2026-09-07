package com.example.demo.repository;

import com.example.demo.entity.BinhLuanVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác bảng binh_luan_video (US-62)
 */
@Repository
public interface BinhLuanVideoRepository extends JpaRepository<BinhLuanVideo, Long> {

    List<BinhLuanVideo> findByVideo_MaVideoAndDaXoaFalseOrderByThoiGianGuiDesc(Long maVideo);

    Page<BinhLuanVideo> findByVideo_MaVideoAndDaXoaFalseOrderByThoiGianGuiDesc(Long maVideo, Pageable pageable);

    long countByVideo_MaVideoAndDaXoaFalse(Long maVideo);
}
