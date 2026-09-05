package com.example.demo.repository;

import com.example.demo.entity.DanhGiaSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DanhGiaSanPhamRepository extends JpaRepository<DanhGiaSanPham, Long> {

    boolean existsByChiTietDonHang_MaChiTietDon(Long maChiTietDon);

    Optional<DanhGiaSanPham> findByChiTietDonHang_MaChiTietDon(Long maChiTietDon);

    long countBySanPham_MaSanPhamAndBiAnFalse(Long maSanPham);

    long countBySanPham_MaSanPhamAndSoSaoAndBiAnFalse(Long maSanPham, Integer soSao);

    @Query("SELECT COUNT(d) FROM DanhGiaSanPham d WHERE d.sanPham.maSanPham = :maSanPham AND d.biAn = false AND SIZE(d.danhSachHinhAnh) > 0")
    long demDanhGiaCoHinhAnh(@Param("maSanPham") Long maSanPham);

    @Query("SELECT AVG(CAST(d.soSao as double)) FROM DanhGiaSanPham d WHERE d.sanPham.maSanPham = :maSanPham AND d.biAn = false")
    Double tinhDiemTrungBinhSanPham(@Param("maSanPham") Long maSanPham);

    @Query("SELECT AVG(CAST(d.soSao as double)) FROM DanhGiaSanPham d WHERE d.gianHang.maGianHang = :maGianHang AND d.biAn = false")
    Double tinhDiemTrungBinhGianHang(@Param("maGianHang") Long maGianHang);

    @Query("SELECT COUNT(d) FROM DanhGiaSanPham d WHERE d.gianHang.maGianHang = :maGianHang AND d.biAn = false")
    Long demTongDanhGiaGianHang(@Param("maGianHang") Long maGianHang);

    @Query("SELECT d FROM DanhGiaSanPham d WHERE d.sanPham.maSanPham = :maSanPham AND d.biAn = false " +
           "AND (:soSao IS NULL OR d.soSao = :soSao) " +
           "AND (:coHinhAnh IS NULL OR (:coHinhAnh = true AND SIZE(d.danhSachHinhAnh) > 0) OR (:coHinhAnh = false AND SIZE(d.danhSachHinhAnh) = 0)) " +
           "ORDER BY d.ngayTao DESC")
    Page<DanhGiaSanPham> timKiemDanhGiaSanPham(
            @Param("maSanPham") Long maSanPham,
            @Param("soSao") Integer soSao,
            @Param("coHinhAnh") Boolean coHinhAnh,
            Pageable pageable
    );

    Page<DanhGiaSanPham> findAllByNguoiDung_MaNguoiDungOrderByNgayTaoDesc(Long maNguoiDung, Pageable pageable);

    long countByGianHang_MaGianHangAndBiAnFalse(Long maGianHang);

    long countByGianHang_MaGianHangAndSoSaoAndBiAnFalse(Long maGianHang, Integer soSao);

    @Query("SELECT COUNT(d) FROM DanhGiaSanPham d WHERE d.gianHang.maGianHang = :maGianHang AND d.biAn = false AND d.phanHoiCuaShop IS NOT NULL AND TRIM(d.phanHoiCuaShop) <> ''")
    long demDanhGiaDaPhanHoi(@Param("maGianHang") Long maGianHang);

    @Query("SELECT COUNT(d) FROM DanhGiaSanPham d WHERE d.gianHang.maGianHang = :maGianHang AND d.biAn = false AND (d.phanHoiCuaShop IS NULL OR TRIM(d.phanHoiCuaShop) = '')")
    long demDanhGiaChuaPhanHoi(@Param("maGianHang") Long maGianHang);

    @Query("SELECT d FROM DanhGiaSanPham d WHERE d.gianHang.maGianHang = :maGianHang AND d.biAn = false " +
           "AND (:soSao IS NULL OR d.soSao = :soSao) " +
           "AND (:trangThaiPhanHoi IS NULL OR :trangThaiPhanHoi = '' OR :trangThaiPhanHoi = 'TAT_CA' " +
           "     OR (:trangThaiPhanHoi = 'DA_PHAN_HOI' AND d.phanHoiCuaShop IS NOT NULL AND TRIM(d.phanHoiCuaShop) <> '') " +
           "     OR (:trangThaiPhanHoi = 'CHUA_PHAN_HOI' AND (d.phanHoiCuaShop IS NULL OR TRIM(d.phanHoiCuaShop) = ''))) " +
           "AND (:tuKhoa IS NULL OR TRIM(:tuKhoa) = '' " +
           "     OR d.sanPham.tenSanPham LIKE :tuKhoaPattern " +
           "     OR d.noiDung LIKE :tuKhoaPattern " +
           "     OR d.nguoiDung.hoVaTen LIKE :tuKhoaPattern) " +
           "ORDER BY d.ngayTao DESC")
    Page<DanhGiaSanPham> timKiemDanhGiaChoSeller(
            @Param("maGianHang") Long maGianHang,
            @Param("soSao") Integer soSao,
            @Param("trangThaiPhanHoi") String trangThaiPhanHoi,
            @Param("tuKhoa") String tuKhoa,
            @Param("tuKhoaPattern") String tuKhoaPattern,
            Pageable pageable
    );
}
