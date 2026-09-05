package com.example.demo.repository;

import com.example.demo.entity.LichSuDungMaGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository thao tác bảng lich_su_dung_ma_giam_gia (US-52 - VOUCHER)
 */
@Repository
public interface LichSuDungMaGiamGiaRepository extends JpaRepository<LichSuDungMaGiamGia, Long> {

    /**
     * Đếm số lần một khách hàng đã sử dụng một mã voucher nhất định (để kiểm tra giới hạn lượt dùng)
     */
    long countByVoucherMaVoucherAndNguoiDungMaNguoiDung(Long maVoucher, Long maNguoiDung);

    /**
     * Lấy toàn bộ lịch sử dùng voucher của một khách hàng
     */
    List<LichSuDungMaGiamGia> findByNguoiDungMaNguoiDungOrderByNgaySuDungDesc(Long maNguoiDung);

    /**
     * Lấy lịch sử dùng voucher của khách hàng có phân trang
     */
    Page<LichSuDungMaGiamGia> findByNguoiDungMaNguoiDungOrderByNgaySuDungDesc(Long maNguoiDung, Pageable pageable);

    /**
     * Đếm tổng số lần khách hàng đã sử dụng voucher
     */
    long countByNguoiDungMaNguoiDung(Long maNguoiDung);

    /**
     * Tính tổng số tiền khách hàng đã tiết kiệm được từ trước đến nay
     */
    @Query("SELECT COALESCE(SUM(l.soTienDaGiam), 0) FROM LichSuDungMaGiamGia l WHERE l.nguoiDung.maNguoiDung = :maNguoiDung")
    BigDecimal tongTienTietKiemCuaKhachHang(@Param("maNguoiDung") Long maNguoiDung);
}
