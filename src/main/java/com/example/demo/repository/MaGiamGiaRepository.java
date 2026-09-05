package com.example.demo.repository;

import com.example.demo.entity.MaGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác bảng ma_giam_gia (US-52 - VOUCHER)
 */
@Repository
public interface MaGiamGiaRepository extends JpaRepository<MaGiamGia, Long> {

    /**
     * Tìm voucher theo mã code (không phân biệt chữ hoa thường, chưa bị xóa)
     */
    Optional<MaGiamGia> findByMaCodeVoucherIgnoreCaseAndDaXoaFalse(String maCodeVoucher);

    /**
     * Lấy tất cả voucher đang hoạt động và chưa bị xóa
     */
    List<MaGiamGia> findByDangHoatDongTrueAndDaXoaFalse();

    /**
     * Lấy danh sách voucher toàn sàn (bao gồm cả Freeship và Giảm giá sàn)
     */
    List<MaGiamGia> findByGianHangIsNullAndDangHoatDongTrueAndDaXoaFalse();

    /**
     * Lấy danh sách voucher của một shop cụ thể
     */
    List<MaGiamGia> findByGianHangMaGianHangAndDangHoatDongTrueAndDaXoaFalse(Long maGianHang);

    /**
     * Đếm tổng số voucher đang hoạt động
     */
    long countByDangHoatDongTrueAndDaXoaFalse();

    /**
     * Đếm voucher Sàn FlexShop (không phải Freeship)
     */
    @Query("SELECT COUNT(v) FROM MaGiamGia v WHERE v.gianHang IS NULL AND v.loaiVoucher <> 'FREESHIP' AND v.dangHoatDong = true AND v.daXoa = false")
    long demVoucherSanFlexShop();

    /**
     * Đếm mã Freeship Sàn
     */
    @Query("SELECT COUNT(v) FROM MaGiamGia v WHERE v.gianHang IS NULL AND v.loaiVoucher = 'FREESHIP' AND v.dangHoatDong = true AND v.daXoa = false")
    long demVoucherFreeshipSan();

    /**
     * Đếm voucher các Gian hàng
     */
    @Query("SELECT COUNT(v) FROM MaGiamGia v WHERE v.gianHang IS NOT NULL AND v.dangHoatDong = true AND v.daXoa = false")
    long demVoucherShop();

    /**
     * Tìm kiếm và lọc danh sách voucher trong Kho Voucher có phân trang
     * Hỗ trợ lọc theo tab:
     * - TAT_CA: Lấy tất cả
     * - FREESHIP: Chỉ lấy mã Freeship sàn
     * - SAN: Chỉ lấy voucher sàn FlexShop (không phải freeship)
     * - SHOP: Chỉ lấy voucher của gian hàng
     */
    @Query("SELECT v FROM MaGiamGia v WHERE v.dangHoatDong = true AND v.daXoa = false " +
            "AND (:tab = 'TAT_CA' " +
            "     OR (:tab = 'FREESHIP' AND v.gianHang IS NULL AND v.loaiVoucher = 'FREESHIP') " +
            "     OR (:tab = 'SAN' AND v.gianHang IS NULL AND v.loaiVoucher <> 'FREESHIP') " +
            "     OR (:tab = 'SHOP' AND v.gianHang IS NOT NULL)) " +
            "AND (:tuKhoa IS NULL OR :tuKhoa = '' " +
            "     OR LOWER(v.maCodeVoucher) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
            "     OR LOWER(v.tenVoucher) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
            "ORDER BY v.ngayBatDau DESC")
    Page<MaGiamGia> timKiemKhoVoucher(
            @Param("tab") String tab,
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable
    );
}
