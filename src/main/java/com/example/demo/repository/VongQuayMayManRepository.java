package com.example.demo.repository;

import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.VongQuayMayMan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VongQuayMayManRepository extends JpaRepository<VongQuayMayMan, Long> {

    /**
     * Đếm số lượt quay của người dùng trong khoảng thời gian (dùng để tính số lượt quay hôm nay)
     */
    long countByNguoiDungAndNgayQuayBetween(NguoiDung nguoiDung, LocalDateTime batDau, LocalDateTime ketThuc);

    /**
     * Lấy lịch sử quay của người dùng có phân trang
     */
    Page<VongQuayMayMan> findByNguoiDungOrderByNgayQuayDesc(NguoiDung nguoiDung, Pageable pageable);

    /**
     * Lọc lịch sử theo loại phần thưởng (XU, VOUCHER, MAY_MAN_LAN_SAU)
     */
    Page<VongQuayMayMan> findByNguoiDungAndLoaiPhanThuongOrderByNgayQuayDesc(
            NguoiDung nguoiDung, String loaiPhanThuong, Pageable pageable);

    /**
     * Lấy 10 lượt trúng thưởng mới nhất cho Live Ticker vinh danh
     */
    List<VongQuayMayMan> findTop10ByOrderByNgayQuayDesc();

    /**
     * Thống kê tổng số xu đã phát từ vòng quay
     */
    @Query("SELECT COALESCE(SUM(v.soXuNhan), 0) FROM VongQuayMayMan v WHERE v.loaiPhanThuong = 'XU'")
    Long sumTongXuDaPhat();

    /**
     * Thống kê tổng số voucher đã trao
     */
    @Query("SELECT COUNT(v) FROM VongQuayMayMan v WHERE v.loaiPhanThuong = 'VOUCHER'")
    Long countTongVoucherDaTrao();

    /**
     * Đếm tổng số lượt quay trong ngày hôm nay của toàn sàn
     */
    @Query("SELECT COUNT(v) FROM VongQuayMayMan v WHERE v.ngayQuay >= :dauNgay AND v.ngayQuay <= :cuoiNgay")
    Long countLuotQuayHomNayToanSan(@Param("dauNgay") LocalDateTime dauNgay, @Param("cuoiNgay") LocalDateTime cuoiNgay);

    /**
     * Tìm kiếm và lọc lịch sử quay toàn sàn cho Admin
     */
    @Query("SELECT v FROM VongQuayMayMan v " +
           "WHERE (:tuKhoa IS NULL OR :tuKhoa = '' OR " +
           "      LOWER(v.nguoiDung.hoVaTen) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR " +
           "      LOWER(v.nguoiDung.email) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR " +
           "      LOWER(v.phanThuong) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) OR " +
           "      LOWER(v.maCodeVoucher) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
           "AND (:loaiPhanThuong IS NULL OR :loaiPhanThuong = 'TAT_CA' OR v.loaiPhanThuong = :loaiPhanThuong) " +
           "AND (:tuNgay IS NULL OR v.ngayQuay >= :tuNgay) " +
           "AND (:denNgay IS NULL OR v.ngayQuay <= :denNgay) " +
           "ORDER BY v.ngayQuay DESC")
    Page<VongQuayMayMan> timKiemVaLocToanSan(
            @Param("tuKhoa") String tuKhoa,
            @Param("loaiPhanThuong") String loaiPhanThuong,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable
    );
}
