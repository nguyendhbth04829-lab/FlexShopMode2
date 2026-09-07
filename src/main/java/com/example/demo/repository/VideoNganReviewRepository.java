package com.example.demo.repository;

import com.example.demo.entity.VideoNganReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác bảng video_ngan_review (US-62)
 */
@Repository
public interface VideoNganReviewRepository extends JpaRepository<VideoNganReview, Long> {

    Optional<VideoNganReview> findByMaVideoAndDaXoaFalse(Long maVideo);

    Page<VideoNganReview> findByDaXoaFalseAndTrangThaiOrderByNgayDangDesc(String trangThai, Pageable pageable);

    Page<VideoNganReview> findByNguoiDang_MaNguoiDungAndDaXoaFalseOrderByNgayDangDesc(Long maNguoiDung, Pageable pageable);

    List<VideoNganReview> findTop10BySanPhamGanKem_MaSanPhamAndDaXoaFalseAndTrangThaiOrderByTongLuotTimDesc(
            Long maSanPham, String trangThai
    );

    @Query("SELECT v FROM VideoNganReview v WHERE v.daXoa = false " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR v.trangThai = :trangThai) " +
           "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(v.tieuDe) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR LOWER(v.hashtag) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "ORDER BY v.ngayDang DESC")
    Page<VideoNganReview> timKiemFeed(
            @Param("trangThai") String trangThai,
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable
    );

    @Query("SELECT v FROM VideoNganReview v WHERE v.nguoiDang.maNguoiDung = :maNguoiDung AND v.daXoa = false " +
           "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(v.tieuDe) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR LOWER(v.hashtag) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "ORDER BY v.ngayDang DESC")
    Page<VideoNganReview> timKiemCuaToi(
            @Param("maNguoiDung") Long maNguoiDung,
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable
    );

    long countByNguoiDang_MaNguoiDungAndDaXoaFalse(Long maNguoiDung);

    @Query("SELECT COALESCE(SUM(v.tongLuotXem), 0) FROM VideoNganReview v WHERE v.nguoiDang.maNguoiDung = :maNguoiDung AND v.daXoa = false")
    Long tongLuotXemCuaNguoiDung(@Param("maNguoiDung") Long maNguoiDung);

    @Query("SELECT COALESCE(SUM(v.tongLuotTim), 0) FROM VideoNganReview v WHERE v.nguoiDang.maNguoiDung = :maNguoiDung AND v.daXoa = false")
    Long tongLuotTimCuaNguoiDung(@Param("maNguoiDung") Long maNguoiDung);

    @Query("SELECT COALESCE(SUM(v.tongLuotBinhLuan), 0) FROM VideoNganReview v WHERE v.nguoiDang.maNguoiDung = :maNguoiDung AND v.daXoa = false")
    Long tongLuotBinhLuanCuaNguoiDung(@Param("maNguoiDung") Long maNguoiDung);

    @Query("SELECT COALESCE(SUM(v.tongLuotChiaSe), 0) FROM VideoNganReview v WHERE v.nguoiDang.maNguoiDung = :maNguoiDung AND v.daXoa = false")
    Long tongLuotChiaSeCuaNguoiDung(@Param("maNguoiDung") Long maNguoiDung);

    @Modifying
    @Query("UPDATE VideoNganReview v SET v.tongLuotXem = v.tongLuotXem + 1 WHERE v.maVideo = :maVideo")
    void tangLuotXem(@Param("maVideo") Long maVideo);
}
