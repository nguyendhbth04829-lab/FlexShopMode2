package com.example.demo.repository;

import com.example.demo.entity.KyThanhToanTraSau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - Repository Quản lý Kỳ Thanh Toán Trả Sau
 * =====================================================================
 */
@Repository
public interface KyThanhToanTraSauRepository extends JpaRepository<KyThanhToanTraSau, Long> {

    List<KyThanhToanTraSau> findByHopDongTraSau_MaHopDongOrderByKySoAsc(Long maHopDong);

    List<KyThanhToanTraSau> findByHopDongTraSau_TaiKhoanTraSau_MaTkTraSauOrderByHanChotThanhToanAsc(Long maTkTraSau);

    /**
     * Tìm tất cả các kỳ chưa trả và đã quá hạn thanh toán
     */
    @Query("SELECT k FROM KyThanhToanTraSau k WHERE k.trangThai <> 'DA_TRA' AND k.hanChotThanhToan < :hienTai")
    List<KyThanhToanTraSau> timDanhSachKyQuaHan(@Param("hienTai") LocalDate hienTai);

    /**
     * Tìm các kỳ chưa trả của tài khoản đến hạn trong khoảng thời gian
     */
    @Query("SELECT k FROM KyThanhToanTraSau k WHERE k.hopDongTraSau.taiKhoanTraSau.maTkTraSau = :maTkTraSau " +
           "AND k.trangThai <> 'DA_TRA' AND k.hanChotThanhToan BETWEEN :tuNgay AND :denNgay " +
           "ORDER BY k.hanChotThanhToan ASC")
    List<KyThanhToanTraSau> timKyDenHan(
            @Param("maTkTraSau") Long maTkTraSau,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    /**
     * Tính tổng số tiền nợ còn lại (chưa trả) của tài khoản trả sau
     */
    @Query("SELECT COALESCE(SUM(k.soTienCanTra - k.soTienDaTra), 0) FROM KyThanhToanTraSau k " +
           "WHERE k.hopDongTraSau.taiKhoanTraSau.maTkTraSau = :maTkTraSau AND k.trangThai <> 'DA_TRA'")
    BigDecimal tinhTongTienNoConLaiCuaTaiKhoan(@Param("maTkTraSau") Long maTkTraSau);

    /**
     * Đếm số kỳ quá hạn của tài khoản trả sau
     */
    @Query("SELECT COUNT(k) FROM KyThanhToanTraSau k " +
           "WHERE k.hopDongTraSau.taiKhoanTraSau.maTkTraSau = :maTkTraSau " +
           "AND (k.trangThai = 'QUA_HAN' OR (k.trangThai <> 'DA_TRA' AND k.hanChotThanhToan < :hienTai))")
    long demSoKyQuaHanCuaTaiKhoan(@Param("maTkTraSau") Long maTkTraSau, @Param("hienTai") LocalDate hienTai);

    /**
     * Tính tổng tiền nợ quá hạn của tài khoản trả sau
     */
    @Query("SELECT COALESCE(SUM(k.soTienCanTra - k.soTienDaTra), 0) FROM KyThanhToanTraSau k " +
           "WHERE k.hopDongTraSau.taiKhoanTraSau.maTkTraSau = :maTkTraSau " +
           "AND (k.trangThai = 'QUA_HAN' OR (k.trangThai <> 'DA_TRA' AND k.hanChotThanhToan < :hienTai))")
    BigDecimal tinhTongTienQuaHanCuaTaiKhoan(@Param("maTkTraSau") Long maTkTraSau, @Param("hienTai") LocalDate hienTai);
}
