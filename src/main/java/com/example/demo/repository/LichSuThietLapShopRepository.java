package com.example.demo.repository;

import com.example.demo.entity.LichSuThietLapShop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuThietLapShopRepository extends JpaRepository<LichSuThietLapShop, Long> {

    List<LichSuThietLapShop> findByMaGianHangOrderByThoiGianDesc(Long maGianHang);

    Page<LichSuThietLapShop> findByMaGianHangOrderByThoiGianDesc(Long maGianHang, Pageable pageable);

    @Query("SELECT l FROM LichSuThietLapShop l WHERE l.maGianHang = :maGianHang " +
           "AND (:loaiThayDoi IS NULL OR :loaiThayDoi = '' OR l.loaiThayDoi = :loaiThayDoi) " +
           "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(l.noiDungThayDoi) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR LOWER(l.nguoiThucHien) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "ORDER BY l.thoiGian DESC")
    Page<LichSuThietLapShop> timKiemLichSu(@Param("maGianHang") Long maGianHang,
                                          @Param("loaiThayDoi") String loaiThayDoi,
                                          @Param("tuKhoa") String tuKhoa,
                                          Pageable pageable);

    long countByMaGianHang(Long maGianHang);
}
