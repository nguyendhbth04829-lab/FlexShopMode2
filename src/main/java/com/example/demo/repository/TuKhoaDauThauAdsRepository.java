package com.example.demo.repository;

import com.example.demo.entity.TuKhoaDauThauAds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - Repository Quản Lý Từ Khóa Đấu Thầu Ads
 * =====================================================================
 */
@Repository
public interface TuKhoaDauThauAdsRepository extends JpaRepository<TuKhoaDauThauAds, Long> {

    List<TuKhoaDauThauAds> findByChienDichQuangCao_MaChienDichOrderByGiaThauMoiClickCpcDesc(Long maChienDich);

    /**
     * Tìm kiếm các từ khóa đấu thầu phù hợp với từ khóa người dùng tìm kiếm,
     * chỉ lấy các từ khóa đang kích hoạt thuộc các chiến dịch đang chạy,
     * sắp xếp theo giá thầu CPC cao nhất lên đầu (Auction Ranking).
     */
    @Query("SELECT t FROM TuKhoaDauThauAds t WHERE " +
           "t.dangKichHoat = true AND t.chienDichQuangCao.trangThai = 'DANG_CHAY' " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "     OR LOWER(:keyword) LIKE LOWER(CONCAT('%', t.tuKhoa, '%')) " +
           "     OR LOWER(t.tuKhoa) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.giaThauMoiClickCpc DESC")
    List<TuKhoaDauThauAds> timTuKhoaDauThauKhop(@Param("keyword") String keyword);
}
