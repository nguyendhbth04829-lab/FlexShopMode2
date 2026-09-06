package com.example.demo.repository;

import com.example.demo.entity.CauHinhTinNhanTuDong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CauHinhTinNhanTuDongRepository extends JpaRepository<CauHinhTinNhanTuDong, Long> {

    List<CauHinhTinNhanTuDong> findByGianHang_MaGianHangOrderByNgayTaoDesc(Long maGianHang);

    List<CauHinhTinNhanTuDong> findByGianHang_MaGianHangAndKichHoatTrue(Long maGianHang);

    List<CauHinhTinNhanTuDong> findByGianHang_MaGianHangAndKichHoatTrueOrderByMaCauHinhDesc(Long maGianHang);

    Optional<CauHinhTinNhanTuDong> findByMaCauHinhAndGianHang_MaGianHang(Long maCauHinh, Long maGianHang);

    long countByGianHang_MaGianHang(Long maGianHang);

    long countByGianHang_MaGianHangAndKichHoatTrue(Long maGianHang);

    @Query("SELECT COALESCE(SUM(c.soLanDaGui), 0) FROM CauHinhTinNhanTuDong c WHERE c.gianHang.maGianHang = :maGianHang")
    long tongSoTinNhanTuDongDaGui(@Param("maGianHang") Long maGianHang);

    @Query("SELECT COUNT(c) FROM CauHinhTinNhanTuDong c WHERE c.gianHang.maGianHang = :maGianHang AND c.maGiamGia IS NOT NULL")
    long soCauHinhCoTangVoucher(@Param("maGianHang") Long maGianHang);

    /**
     * Tìm kiếm và phân trang cấu hình tin nhắn tự động theo bộ lọc
     */
    @Query("SELECT c FROM CauHinhTinNhanTuDong c WHERE c.gianHang.maGianHang = :maGianHang " +
            "AND (:loai IS NULL OR :loai = 'TAT_CA' OR c.loaiTinNhanTuDong = :loai) " +
            "AND (:trangThai IS NULL OR :trangThai = 'TAT_CA' OR (:trangThai = 'BAT' AND c.kichHoat = true) OR (:trangThai = 'TAT' AND c.kichHoat = false)) " +
            "AND (:tuKhoa IS NULL OR :tuKhoa = '' " +
            "     OR LOWER(c.tieuDe) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
            "     OR LOWER(c.noiDungTinNhan) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
            "     OR LOWER(c.tuKhoaKichHoat) LIKE LOWER(CONCAT('%', :tuKhoa, '%')))")
    Page<CauHinhTinNhanTuDong> timKiemVaLoc(
            @Param("maGianHang") Long maGianHang,
            @Param("loai") String loai,
            @Param("trangThai") String trangThai,
            @Param("tuKhoa") String tuKhoa,
            Pageable pageable
    );
}
