package com.example.demo.repository;

import com.example.demo.entity.KhungGioFlashSale;
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
 * Repository thao tác bảng khung_gio_flash_sale (US-53 - PROMOTION)
 */
@Repository
public interface KhungGioFlashSaleRepository extends JpaRepository<KhungGioFlashSale, Long> {

    /**
     * Tìm khung giờ đang diễn ra tại thời điểm hiện tại
     */
    @Query("SELECT k FROM KhungGioFlashSale k WHERE :now >= k.thoiGianBatDau AND :now <= k.thoiGianKetThuc AND (k.trangThai IS NULL OR k.trangThai <> 'TAM_KHOA') ORDER BY k.thoiGianBatDau ASC")
    List<KhungGioFlashSale> timKhungGioDangDienRa(@Param("now") LocalDateTime now);

    /**
     * Lấy các khung giờ sắp diễn ra
     */
    @Query("SELECT k FROM KhungGioFlashSale k WHERE k.thoiGianBatDau > :now AND (k.trangThai IS NULL OR k.trangThai <> 'TAM_KHOA') ORDER BY k.thoiGianBatDau ASC")
    List<KhungGioFlashSale> timKhungGioSapDienRa(@Param("now") LocalDateTime now);

    /**
     * Lấy tất cả khung giờ trong ngày (để hiển thị các mốc 0h, 12h, 21h trên thanh Tab)
     */
    @Query("SELECT k FROM KhungGioFlashSale k WHERE k.thoiGianBatDau >= :startOfDay AND k.thoiGianBatDau <= :endOfDay AND (k.trangThai IS NULL OR k.trangThai <> 'TAM_KHOA') ORDER BY k.thoiGianBatDau ASC")
    List<KhungGioFlashSale> timKhungGioTrongNgay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Lấy danh sách tất cả khung giờ hiển thị cho khách hàng (Đang diễn ra + Sắp diễn ra + Đã diễn ra hôm nay)
     */
    @Query("SELECT k FROM KhungGioFlashSale k WHERE (k.trangThai IS NULL OR (k.trangThai <> 'TAM_KHOA' AND k.trangThai <> 'DA_HUY')) " +
            "AND (k.thoiGianKetThuc >= :startOfToday) " +
            "ORDER BY k.thoiGianBatDau ASC")
    List<KhungGioFlashSale> timKhungGioChoKhachHang(@Param("startOfToday") LocalDateTime startOfToday);

    /**
     * Kiểm tra xem khoảng thời gian mới có bị trùng lặp / chồng chéo (overlap) với khung giờ đang hoạt động không
     */
    @Query("SELECT COUNT(k) > 0 FROM KhungGioFlashSale k WHERE (:excludeId IS NULL OR k.maFlashSale <> :excludeId) " +
            "AND (k.trangThai = 'DANG_DIEN_RA' OR k.trangThai = 'SAP_DIEN_RA') " +
            "AND (k.thoiGianBatDau < :end AND k.thoiGianKetThuc > :start)")
    boolean kiemTraChongCheoThoiGian(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") Long excludeId
    );

    /**
     * Tìm kiếm và phân trang khung giờ cho màn hình quản trị (Sắp xếp mới nhất lên đầu)
     */
    @Query("SELECT k FROM KhungGioFlashSale k WHERE (:trangThai = 'TAT_CA' OR k.trangThai = :trangThai) " +
            "AND (:tuKhoa IS NULL OR :tuKhoa = '' OR LOWER(k.tieuDe) LIKE LOWER(CONCAT('%', :tuKhoa, '%'))) " +
            "ORDER BY k.maFlashSale DESC")
    Page<KhungGioFlashSale> timKiemKhungGio(
            @Param("trangThai") String trangThai,
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable
    );
}
