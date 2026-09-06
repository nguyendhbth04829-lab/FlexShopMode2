package com.example.demo.repository;

import com.example.demo.entity.YeuCauRutTien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & RÚT TIỀN (DEV 5 - MINH)
 * USER STORY: US-44 - Repository Quản Lý Yêu Cầu Rút Tiền Người Bán
 * =====================================================================
 */
@Repository
public interface YeuCauRutTienRepository extends JpaRepository<YeuCauRutTien, Long> {

    List<YeuCauRutTien> findByGianHang_MaGianHangOrderByNgayTaoDesc(Long maGianHang);

    List<YeuCauRutTien> findByTrangThaiOrderByNgayTaoDesc(String trangThai);

    /**
     * Tìm kiếm và lọc nâng cao đa tiêu chí: Từ khóa, Trạng thái duyệt, Gian hàng
     */
    @Query("SELECT y FROM YeuCauRutTien y WHERE " +
           "(:keyword IS NULL OR LOWER(y.gianHang.tenGianHang) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(y.soTaiKhoan) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(y.tenChuTaiKhoan) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(y.tenNganHang) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR y.trangThai = :trangThai) " +
           "AND (:maGianHang IS NULL OR y.gianHang.maGianHang = :maGianHang)")
    Page<YeuCauRutTien> timKiemNangCao(
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            @Param("maGianHang") Long maGianHang,
            Pageable pageable
    );

    /**
     * Tính tổng tiền rút theo trạng thái (CHO_DUYET / DA_DUYET / TU_CHOI)
     */
    @Query("SELECT COALESCE(SUM(y.soTienRut), 0) FROM YeuCauRutTien y WHERE y.trangThai = :trangThai")
    BigDecimal tinhTongTienTheoTrangThai(@Param("trangThai") String trangThai);

    /**
     * Đếm số lượng yêu cầu theo trạng thái
     */
    @Query("SELECT COUNT(y) FROM YeuCauRutTien y WHERE y.trangThai = :trangThai")
    long demSoLuongTheoTrangThai(@Param("trangThai") String trangThai);
}
