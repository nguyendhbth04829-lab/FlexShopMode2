package com.example.demo.repository;

import com.example.demo.entity.CanhBaoGianLan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & AN NINH SÀN (DEV 5 - MINH)
 * USER STORY: US-68 - Repository Cảnh Báo Gian Lận (Anti-Fraud Repository)
 * =====================================================================
 */
@Repository
public interface CanhBaoGianLanRepository extends JpaRepository<CanhBaoGianLan, Long> {

    List<CanhBaoGianLan> findAllByOrderByNgayTaoDesc();

    List<CanhBaoGianLan> findByTrangThaiOrderByNgayTaoDesc(String trangThai);

    List<CanhBaoGianLan> findByLoaiDoiTuongOrderByNgayTaoDesc(String loaiDoiTuong);

    List<CanhBaoGianLan> findByLoaiDoiTuongAndTrangThaiOrderByNgayTaoDesc(String loaiDoiTuong, String trangThai);

    int countByTrangThai(String trangThai);

    int countByDiemRuiRoGreaterThanEqual(int minDiem);

    boolean existsByLoaiDoiTuongAndMaDoiTuongAndTrangThai(String loaiDoiTuong, Long maDoiTuong, String trangThai);

    @Query("SELECT COALESCE(AVG(c.diemRuiRo * 1.0), 0.0) FROM CanhBaoGianLan c")
    Double tinhDiemRuiRoTrungBinh();
}
