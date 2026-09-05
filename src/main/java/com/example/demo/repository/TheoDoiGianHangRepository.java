package com.example.demo.repository;

import com.example.demo.entity.TheoDoiGianHang;
import com.example.demo.entity.TheoDoiGianHangId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TheoDoiGianHangRepository extends JpaRepository<TheoDoiGianHang, TheoDoiGianHangId> {

    boolean existsByMaNguoiDungAndMaGianHang(Long maNguoiDung, Long maGianHang);

    Optional<TheoDoiGianHang> findByMaNguoiDungAndMaGianHang(Long maNguoiDung, Long maGianHang);

    @Modifying
    @Query("DELETE FROM TheoDoiGianHang t WHERE t.maNguoiDung = :maNguoiDung AND t.maGianHang = :maGianHang")
    void huyTheoDoiGianHang(@Param("maNguoiDung") Long maNguoiDung, @Param("maGianHang") Long maGianHang);

    long countByMaNguoiDung(Long maNguoiDung);

    long countByMaGianHang(Long maGianHang);

    @Query(value = "SELECT t FROM TheoDoiGianHang t " +
                   "JOIN FETCH t.gianHang gh " +
                   "WHERE t.maNguoiDung = :maNguoiDung " +
                   "AND (:tuKhoa IS NULL OR TRIM(:tuKhoa) = '' OR gh.tenGianHang LIKE :tuKhoaPattern) " +
                   "ORDER BY t.ngayTao DESC",
           countQuery = "SELECT COUNT(t) FROM TheoDoiGianHang t " +
                        "WHERE t.maNguoiDung = :maNguoiDung " +
                        "AND (:tuKhoa IS NULL OR TRIM(:tuKhoa) = '' OR t.gianHang.tenGianHang LIKE :tuKhoaPattern)")
    Page<TheoDoiGianHang> timKiemGianHangTheoDoi(
            @Param("maNguoiDung") Long maNguoiDung,
            @Param("tuKhoa") String tuKhoa,
            @Param("tuKhoaPattern") String tuKhoaPattern,
            Pageable pageable
    );
}
