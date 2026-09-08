package com.example.demo.repository;

import com.example.demo.entity.NguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Repository truy vấn tài khoản người dùng
 * =====================================================================
 */
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<NguoiDung> findByEmailIgnoreCase(String email);

    Optional<NguoiDung> findBySoDienThoai(String soDienThoai);

    @Query("SELECT u FROM NguoiDung u WHERE LOWER(u.email) = LOWER(:identifier) OR u.soDienThoai = :identifier")
    Optional<NguoiDung> findByIdentifier(@Param("identifier") String identifier);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    /**
     * US-04: Kiểm tra Số điện thoại đã được đăng ký bởi người dùng khác hay chưa
     */
    @Query("SELECT COUNT(u) > 0 FROM NguoiDung u WHERE u.soDienThoai = :soDienThoai AND u.maNguoiDung != :maNguoiDung")
    boolean existsBySoDienThoaiAndMaNguoiDungNot(@Param("soDienThoai") String soDienThoai, @Param("maNguoiDung") Long maNguoiDung);

    /**
     * US-06: Tìm kiếm, lọc và phân trang người dùng cho Admin
     */
    @Query("""
        SELECT DISTINCT u FROM NguoiDung u
        LEFT JOIN u.danhSachVaiTro vt
        WHERE u.daXoa = false
        AND (:keyword IS NULL OR :keyword = ''
             OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(u.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR u.soDienThoai LIKE CONCAT('%', :keyword, '%'))
        AND (:vaiTro IS NULL OR :vaiTro = ''
             OR vt.tenVaiTro = :vaiTro
             OR vt.tenVaiTro = CONCAT('ROLE_', :vaiTro))
        AND (:trangThai IS NULL OR :trangThai = ''
             OR u.trangThai = :trangThai)
    """)
    Page<NguoiDung> timKiemPhanTrang(
            @Param("keyword") String keyword,
            @Param("vaiTro") String vaiTro,
            @Param("trangThai") String trangThai,
            Pageable pageable
    );

    /**
     * US-06: Các hàm đếm số liệu thống kê KPI
     */
    long countByDaXoaFalse();

    long countByTrangThaiAndDaXoaFalse(String trangThai);

    @Query("""
        SELECT COUNT(DISTINCT u) FROM NguoiDung u
        JOIN u.danhSachVaiTro vt
        WHERE u.daXoa = false
        AND (vt.tenVaiTro = :tenVaiTro OR vt.tenVaiTro = CONCAT('ROLE_', :tenVaiTro))
    """)
    long demSoLuongTheoVaiTro(@Param("tenVaiTro") String tenVaiTro);

    @Query("SELECT COUNT(u) FROM NguoiDung u WHERE u.daXoa = false AND u.ngayTao >= :tuThoiGian")
    long demSoNguoiDungMoiTu(@Param("tuThoiGian") LocalDateTime tuThoiGian);
}
