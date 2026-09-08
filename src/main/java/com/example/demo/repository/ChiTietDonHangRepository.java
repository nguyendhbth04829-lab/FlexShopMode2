package com.example.demo.repository;

import com.example.demo.entity.ChiTietDonHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Repository truy vấn chi tiết sản phẩm trong đơn hàng
 * =====================================================================
 */
@Repository
public interface ChiTietDonHangRepository extends JpaRepository<ChiTietDonHang, Long> {

    List<ChiTietDonHang> findByDonHangShop_MaDonHangShop(Long maDonHangShop);

    /**
     * [US-58] Tìm top sản phẩm bán chạy theo số lượng bán ra
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT c.tenSanPham, c.tenBienThe, c.maSku, SUM(c.soLuong), SUM(c.tongTien) " +
            "FROM ChiTietDonHang c WHERE " +
            "(:maGianHang IS NULL OR c.donHangShop.gianHang.maGianHang = :maGianHang) AND " +
            "(:tuNgay IS NULL OR c.donHangShop.ngayTao >= :tuNgay) AND " +
            "(:denNgay IS NULL OR c.donHangShop.ngayTao <= :denNgay) AND " +
            "c.donHangShop.trangThai != 'DA_HUY' " +
            "GROUP BY c.tenSanPham, c.tenBienThe, c.maSku " +
            "ORDER BY SUM(c.soLuong) DESC")
    List<Object[]> timTopSanPhamBanChay(
            @org.springframework.data.repository.query.Param("maGianHang") Long maGianHang,
            @org.springframework.data.repository.query.Param("tuNgay") java.time.LocalDateTime tuNgay,
            @org.springframework.data.repository.query.Param("denNgay") java.time.LocalDateTime denNgay,
            org.springframework.data.domain.Pageable pageable
    );
    List<ChiTietDonHang> findAllByDonHangShop_MaDonHangShop(Long maDonHangShop);
}
