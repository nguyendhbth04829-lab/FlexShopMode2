package com.example.demo.repository;

import com.example.demo.entity.LichSuTinNhanTuDong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LichSuTinNhanTuDongRepository extends JpaRepository<LichSuTinNhanTuDong, Long> {

    /**
     * Đếm số lần cấu hình này đã tự động gửi cho khách hàng trong khoảng thời gian (dùng để chống spam trong ngày)
     */
    long countByCauHinh_MaCauHinhAndKhachHang_MaNguoiDungAndThoiGianGuiBetween(
            Long maCauHinh,
            Long maKhachHang,
            LocalDateTime batDau,
            LocalDateTime ketThuc
    );

    @Query("SELECT COUNT(l) FROM LichSuTinNhanTuDong l WHERE l.cauHinh.loaiTinNhanTuDong = 'CHAO_MUNG' " +
            "AND l.gianHang.maGianHang = :maGianHang AND l.khachHang.maNguoiDung = :maKhachHang " +
            "AND l.thoiGianGui BETWEEN :batDau AND :ketThuc")
    long demSoLanNhanTinChaoMungHomNay(
            @Param("maGianHang") Long maGianHang,
            @Param("maKhachHang") Long maKhachHang,
            @Param("batDau") LocalDateTime batDau,
            @Param("ketThuc") LocalDateTime ketThuc
    );

    Page<LichSuTinNhanTuDong> findByGianHang_MaGianHangOrderByThoiGianGuiDesc(Long maGianHang, Pageable pageable);

    @Query("SELECT COUNT(l) FROM LichSuTinNhanTuDong l WHERE l.gianHang.maGianHang = :maGianHang AND l.voucherDaTang IS NOT NULL")
    long demSoVoucherDaPhatChoKhach(@Param("maGianHang") Long maGianHang);
}
