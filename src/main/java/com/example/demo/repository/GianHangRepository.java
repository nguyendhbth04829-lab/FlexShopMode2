package com.example.demo.repository;

import com.example.demo.entity.GianHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GianHangRepository extends JpaRepository<GianHang, Long> {

    /**
     * Tìm gian hàng theo mã chủ sở hữu còn hiệu lực
     */
    Optional<GianHang> findByMaChuSoHuuAndDaXoaFalse(Long maChuSoHuu);

    /**
     * Tìm gian hàng mới nhất theo mã chủ sở hữu (cho trường hợp hồ sơ đăng ký)
     */
    Optional<GianHang> findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(Long maChuSoHuu);

    /**
     * Tìm gian hàng theo đường dẫn slug
     */
    Optional<GianHang> findByDuongDanSlugAndDaXoaFalse(String duongDanSlug);

    /**
     * Kiểm tra tên gian hàng đã tồn tại hay chưa (không phân biệt hoa thường)
     */
    boolean existsByTenGianHangIgnoreCaseAndDaXoaFalse(String tenGianHang);

    /**
     * Kiểm tra slug gian hàng đã tồn tại hay chưa
     */
    boolean existsByDuongDanSlugIgnoreCaseAndDaXoaFalse(String duongDanSlug);

    /**
     * Kiểm tra tên gian hàng đã tồn tại ngoại trừ gian hàng hiện tại (cho cập nhật)
     */
    boolean existsByTenGianHangIgnoreCaseAndMaGianHangNotAndDaXoaFalse(String tenGianHang, Long maGianHang);

    /**
     * Kiểm tra slug gian hàng đã tồn tại ngoại trừ gian hàng hiện tại (cho cập nhật)
     */
    boolean existsByDuongDanSlugIgnoreCaseAndMaGianHangNotAndDaXoaFalse(String duongDanSlug, Long maGianHang);

    /**
     * Tìm kiếm và phân trang danh sách gian hàng (cho Admin / quản trị)
     */
    @Query("SELECT g FROM GianHang g WHERE g.daXoa = false " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "     OR LOWER(g.tenGianHang) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(g.duongDanSlug) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(g.sdtKho) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(g.diaChiKho) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR EXISTS (SELECT u FROM NguoiDung u WHERE u.maNguoiDung = g.maChuSoHuu AND (" +
           "         LOWER(u.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "         OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "         OR LOWER(u.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
           "     ))) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR g.trangThai = :trangThai)")
    Page<GianHang> timKiemPhanTrang(@Param("keyword") String keyword, @Param("trangThai") String trangThai, Pageable pageable);

    /**
     * Đếm số lượng gian hàng theo trạng thái
     */
    long countByTrangThaiAndDaXoaFalse(String trangThai);

    /**
     * Đếm tổng số gian hàng còn hiệu lực
     */
    long countByDaXoaFalse();
}
