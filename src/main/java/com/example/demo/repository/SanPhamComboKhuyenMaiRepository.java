package com.example.demo.repository;

import com.example.demo.entity.SanPhamComboKhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác bảng san_pham_combo_khuyen_mai (US-54 - PROMOTION)
 */
@Repository
public interface SanPhamComboKhuyenMaiRepository extends JpaRepository<SanPhamComboKhuyenMai, Long> {

    List<SanPhamComboKhuyenMai> findByComboKhuyenMaiMaCombo(Long maCombo);

    List<SanPhamComboKhuyenMai> findByComboKhuyenMaiMaComboAndVaiTro(Long maCombo, String vaiTro);

    boolean existsByComboKhuyenMaiMaComboAndBienTheSanPhamMaBienThe(Long maCombo, Long maBienThe);

    Optional<SanPhamComboKhuyenMai> findByComboKhuyenMaiMaComboAndBienTheSanPhamMaBienThe(Long maCombo, Long maBienThe);

    /**
     * Dành cho Khách Hàng: Tìm tất cả phụ kiện B giảm giá 50% mua kèm sản phẩm chính A
     * Điều kiện: Cùng combo, combo đang diễn ra (dangHoatDong = true, now trong khoảng thời gian),
     * có sản phẩm chính là maBienTheChinh, và lấy các sản phẩm có vai trò MUA_KEM_DEAL_SOC.
     */
    @Query("SELECT b FROM SanPhamComboKhuyenMai b " +
            "WHERE b.vaiTro = 'MUA_KEM_DEAL_SOC' " +
            "AND b.comboKhuyenMai.dangHoatDong = true " +
            "AND :now >= b.comboKhuyenMai.ngayBatDau AND :now <= b.comboKhuyenMai.ngayKetThuc " +
            "AND b.comboKhuyenMai.maCombo IN (" +
            "    SELECT a.comboKhuyenMai.maCombo FROM SanPhamComboKhuyenMai a " +
            "    WHERE a.vaiTro = 'SAN_PHAM_CHINH' AND a.bienTheSanPham.maBienThe = :maBienTheChinh" +
            ")")
    List<SanPhamComboKhuyenMai> timPhuKienMuaKemTheoSanPhamChinh(
            @Param("maBienTheChinh") Long maBienTheChinh,
            @Param("now") LocalDateTime now
    );

    /**
     * Đếm tổng số sản phẩm tham gia các combo của gian hàng
     */
    @Query("SELECT COUNT(sp) FROM SanPhamComboKhuyenMai sp WHERE sp.comboKhuyenMai.gianHang.maGianHang = :maGianHang")
    long countSanPhamThamGiaTheoShop(@Param("maGianHang") Long maGianHang);

    /**
     * Tổng số lượng sản phẩm ưu đãi đã bán
     */
    @Query("SELECT COALESCE(SUM(sp.soLuongDaBan), 0) FROM SanPhamComboKhuyenMai sp WHERE sp.comboKhuyenMai.gianHang.maGianHang = :maGianHang")
    long tinhTongSoLuongDaBanTheoShop(@Param("maGianHang") Long maGianHang);

    /**
     * Tính tổng doanh thu thu về từ các sản phẩm combo / deal sốc
     */
    @Query("SELECT COALESCE(SUM(sp.soLuongDaBan * sp.giaUuDai), 0) FROM SanPhamComboKhuyenMai sp WHERE sp.comboKhuyenMai.gianHang.maGianHang = :maGianHang AND sp.giaUuDai IS NOT NULL")
    BigDecimal tinhTongDoanhThuComboTheoShop(@Param("maGianHang") Long maGianHang);
}