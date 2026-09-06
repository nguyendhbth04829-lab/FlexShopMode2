package com.example.demo.repository;

import com.example.demo.entity.LichSuGiaoDichXu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository thao tác bảng Lịch Sử Giao Dịch Xu (US-55 - Coin Reward System)
 */
@Repository
public interface LichSuGiaoDichXuRepository extends JpaRepository<LichSuGiaoDichXu, Long> {

    /**
     * Tìm kiếm và phân trang lịch sử giao dịch xu theo loại (Tất cả, Cộng xu, Trừ xu, Hoặc mã loại cụ thể)
     */
    @Query("SELECT ls FROM LichSuGiaoDichXu ls WHERE ls.nguoiDung.maNguoiDung = :maNguoiDung " +
            "AND (:loai = 'TAT_CA' OR (:loai = 'CONG_XU' AND ls.soXuThayDoi > 0) OR (:loai = 'TRU_XU' AND ls.soXuThayDoi < 0) OR ls.loaiGiaoDich = :loai) " +
            "ORDER BY ls.maGiaoDichXu DESC")
    Page<LichSuGiaoDichXu> timKiemLichSuGiaoDich(
            @Param("maNguoiDung") Long maNguoiDung,
            @Param("loai") String loai,
            Pageable pageable
    );

    /**
     * Kiểm tra người dùng đã điểm danh nhận xu trong ngày hôm nay chưa
     */
    @Query("SELECT COUNT(ls) > 0 FROM LichSuGiaoDichXu ls " +
            "WHERE ls.nguoiDung.maNguoiDung = :maNguoiDung " +
            "AND ls.loaiGiaoDich = 'DIEM_DANH_HANG_NGAY' " +
            "AND ls.ngayTao >= :dauNgay AND ls.ngayTao < :cuoiNgay")
    boolean daDiemDanhHomNay(
            @Param("maNguoiDung") Long maNguoiDung,
            @Param("dauNgay") LocalDateTime dauNgay,
            @Param("cuoiNgay") LocalDateTime cuoiNgay
    );

    /**
     * Tính tổng số xu đã tiêu (dùng giảm giá) trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(ABS(ls.soXuThayDoi)), 0) FROM LichSuGiaoDichXu ls " +
            "WHERE ls.nguoiDung.maNguoiDung = :maNguoiDung " +
            "AND ls.soXuThayDoi < 0 " +
            "AND ls.ngayTao >= :tuNgay")
    Long tinhTongXuDaTieu(
            @Param("maNguoiDung") Long maNguoiDung,
            @Param("tuNgay") LocalDateTime tuNgay
    );

    /**
     * Tính tổng số xu đã tích lũy trong tháng
     */
    @Query("SELECT COALESCE(SUM(ls.soXuThayDoi), 0) FROM LichSuGiaoDichXu ls " +
            "WHERE ls.nguoiDung.maNguoiDung = :maNguoiDung " +
            "AND ls.soXuThayDoi > 0 " +
            "AND ls.ngayTao >= :tuNgay")
    Long tinhTongXuDaTichTrongThang(
            @Param("maNguoiDung") Long maNguoiDung,
            @Param("tuNgay") LocalDateTime tuNgay
    );

    List<LichSuGiaoDichXu> findTop5ByNguoiDung_MaNguoiDungOrderByMaGiaoDichXuDesc(Long maNguoiDung);
}
