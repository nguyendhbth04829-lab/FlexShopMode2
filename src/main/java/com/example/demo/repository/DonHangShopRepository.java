package com.example.demo.repository;

import com.example.demo.entity.DonHangShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Repository truy vấn đơn hàng theo từng Shop
 * =====================================================================
 */
@Repository
public interface DonHangShopRepository extends JpaRepository<DonHangShop, Long> {

    List<DonHangShop> findByTrangThai(String trangThai);

    List<DonHangShop> findByDonHangTong_KhachHang_MaNguoiDungOrderByNgayTaoDesc(Long maKhachHang);

    List<DonHangShop> findAllByOrderByNgayTaoDesc();

    /**
     * [US-58] Tính tổng GMV (tiền hàng shop) theo khoảng thời gian và gian hàng
     */
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(d.tienHangShop), 0) FROM DonHangShop d WHERE " +
            "(:maGianHang IS NULL OR d.gianHang.maGianHang = :maGianHang) AND " +
            "(:tuNgay IS NULL OR d.ngayTao >= :tuNgay) AND " +
            "(:denNgay IS NULL OR d.ngayTao <= :denNgay)")
    java.math.BigDecimal tinhTongGmv(
            @org.springframework.data.repository.query.Param("maGianHang") Long maGianHang,
            @org.springframework.data.repository.query.Param("tuNgay") java.time.LocalDateTime tuNgay,
            @org.springframework.data.repository.query.Param("denNgay") java.time.LocalDateTime denNgay
    );

    /**
     * [US-58] Tính tổng doanh thu thực nhận của Shop (không tính đơn hủy)
     */
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(d.tongTienShopNhan), 0) FROM DonHangShop d WHERE " +
            "(:maGianHang IS NULL OR d.gianHang.maGianHang = :maGianHang) AND " +
            "(:tuNgay IS NULL OR d.ngayTao >= :tuNgay) AND " +
            "(:denNgay IS NULL OR d.ngayTao <= :denNgay) AND " +
            "d.trangThai != 'DA_HUY'")
    java.math.BigDecimal tinhDoanhThuShopThucNhan(
            @org.springframework.data.repository.query.Param("maGianHang") Long maGianHang,
            @org.springframework.data.repository.query.Param("tuNgay") java.time.LocalDateTime tuNgay,
            @org.springframework.data.repository.query.Param("denNgay") java.time.LocalDateTime denNgay
    );

    /**
     * [US-58] Đếm tổng số đơn hàng
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHangShop d WHERE " +
            "(:maGianHang IS NULL OR d.gianHang.maGianHang = :maGianHang) AND " +
            "(:tuNgay IS NULL OR d.ngayTao >= :tuNgay) AND " +
            "(:denNgay IS NULL OR d.ngayTao <= :denNgay)")
    long demTongSoDon(
            @org.springframework.data.repository.query.Param("maGianHang") Long maGianHang,
            @org.springframework.data.repository.query.Param("tuNgay") java.time.LocalDateTime tuNgay,
            @org.springframework.data.repository.query.Param("denNgay") java.time.LocalDateTime denNgay
    );

    /**
     * [US-58] Đếm số đơn theo trạng thái
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DonHangShop d WHERE " +
            "(:maGianHang IS NULL OR d.gianHang.maGianHang = :maGianHang) AND " +
            "(:tuNgay IS NULL OR d.ngayTao >= :tuNgay) AND " +
            "(:denNgay IS NULL OR d.ngayTao <= :denNgay) AND " +
            "d.trangThai = :trangThai")
    long demSoDonTheoTrangThai(
            @org.springframework.data.repository.query.Param("maGianHang") Long maGianHang,
            @org.springframework.data.repository.query.Param("tuNgay") java.time.LocalDateTime tuNgay,
            @org.springframework.data.repository.query.Param("denNgay") java.time.LocalDateTime denNgay,
            @org.springframework.data.repository.query.Param("trangThai") String trangThai
    );

    /**
     * [US-58] Thống kê doanh số theo ngày phục vụ vẽ biểu đồ Chart.js (Native Query)
     */
    @org.springframework.data.jpa.repository.Query(value =
            "SELECT CAST(ngay_tao AS DATE) AS ngay, SUM(tien_hang_shop) AS tong_tien, COUNT(ma_don_hang_shop) AS so_don " +
            "FROM don_hang_shop WHERE " +
            "(:maGianHang IS NULL OR ma_gian_hang = :maGianHang) AND " +
            "(:tuNgay IS NULL OR ngay_tao >= :tuNgay) AND " +
            "(:denNgay IS NULL OR ngay_tao <= :denNgay) " +
            "GROUP BY CAST(ngay_tao AS DATE) ORDER BY ngay ASC",
            nativeQuery = true)
    List<Object[]> thongKeDoanhThuTungNgay(
            @org.springframework.data.repository.query.Param("maGianHang") Long maGianHang,
            @org.springframework.data.repository.query.Param("tuNgay") java.time.LocalDateTime tuNgay,
            @org.springframework.data.repository.query.Param("denNgay") java.time.LocalDateTime denNgay
    );
}
