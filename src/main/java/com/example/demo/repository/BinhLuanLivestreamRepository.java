package com.example.demo.repository;

import com.example.demo.entity.BinhLuanLivestream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác bảng binh_luan_livestream (US-61)
 */
@Repository
public interface BinhLuanLivestreamRepository extends JpaRepository<BinhLuanLivestream, Long> {

    List<BinhLuanLivestream> findTop50ByPhongLivestream_MaLiveOrderByThoiGianGuiAsc(Long maLive);

    List<BinhLuanLivestream> findByPhongLivestream_MaLiveOrderByThoiGianGuiAsc(Long maLive);

    long countByPhongLivestream_MaLive(Long maLive);
}
