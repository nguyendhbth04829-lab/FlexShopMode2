package com.example.demo.repository;

import com.example.demo.entity.GiaoDichKyQuy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & PHÍ SÀN (DEV 5 - MINH)
 * USER STORY: US-42 & US-43 - Repository truy vấn giao dịch ký quỹ Escrow (Shopee Guarantee)
 * =====================================================================
 * Bổ sung:
 *   - Tìm kiếm, lọc đa tiêu chí phân trang nâng cao
 *   - Các câu truy vấn tính toán thống kê trực tiếp đồng bộ tại CSDL
 * =====================================================================
 */
@Repository
public interface GiaoDichKyQuyRepository extends JpaRepository<GiaoDichKyQuy, Long> {

    Optional<GiaoDichKyQuy> findByDonHangShop_MaDonHangShop(Long maDonHangShop);

    List<GiaoDichKyQuy> findByGianHang_MaGianHangOrderByNgayTaoDesc(Long maGianHang);

    List<GiaoDichKyQuy> findByTrangThaiAndNgayDuKienNhaTienBefore(String trangThai, LocalDateTime now);

    Page<GiaoDichKyQuy> findAllByOrderByNgayTaoDesc(Pageable pageable);

    /**
     * Tìm kiếm và lọc nâng cao đa tiêu chí: Từ khóa, Trạng thái, Gian hàng, Lọc quá hạn
     */
    @Query("SELECT g FROM GiaoDichKyQuy g WHERE " +
           "(:keyword IS NULL OR LOWER(g.donHangShop.maCodeDonShop) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(g.gianHang.tenGianHang) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR g.trangThai = :trangThai) " +
           "AND (:maGianHang IS NULL OR g.gianHang.maGianHang = :maGianHang) " +
           "AND (:chiLayQuaHan = false OR (g.trangThai = 'DANG_TAM_GIU' AND g.ngayDuKienNhaTien <= :now))")
    Page<GiaoDichKyQuy> timKiemNangCao(
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            @Param("maGianHang") Long maGianHang,
            @Param("chiLayQuaHan") boolean chiLayQuaHan,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    /**
     * Tính tổng tiền thực nhận theo trạng thái (DANG_TAM_GIU / DA_GIAI_NGAN)
     */
    @Query("SELECT COALESCE(SUM(g.tienThucNhanVeVi), 0) FROM GiaoDichKyQuy g WHERE g.trangThai = :trangThai")
    BigDecimal tinhTongTienTheoTrangThai(@Param("trangThai") String trangThai);

    /**
     * Tính tổng phí sàn 3% đã thu được trên toàn hệ thống (US-43)
     */
    @Query("SELECT COALESCE(SUM(g.tienPhiSan), 0) FROM GiaoDichKyQuy g")
    BigDecimal tinhTongPhiSanDaThu();

    /**
     * Đếm số lượng đơn theo trạng thái
     */
    @Query("SELECT COUNT(g) FROM GiaoDichKyQuy g WHERE g.trangThai = :trangThai")
    long demSoDonTheoTrangThai(@Param("trangThai") String trangThai);

    /**
     * Đếm số lượng đơn đang tạm giữ nhưng đã quá hạn 3 ngày bảo vệ
     */
    @Query("SELECT COUNT(g) FROM GiaoDichKyQuy g WHERE g.trangThai = 'DANG_TAM_GIU' AND g.ngayDuKienNhaTien <= :now")
    long demSoDonQuaHanChuaGiaiNgan(@Param("now") LocalDateTime now);
}
