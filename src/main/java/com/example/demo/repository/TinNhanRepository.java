package com.example.demo.repository;

import com.example.demo.entity.TinNhan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TinNhanRepository extends JpaRepository<TinNhan, Long> {

    // Lấy toàn bộ tin nhắn trong cuộc trò chuyện theo thứ tự tăng dần thời gian
    List<TinNhan> findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoAsc(Long maCuocTroChuyen);

    // Lấy tin nhắn theo phân trang (để cuộn xem tin cũ)
    Page<TinNhan> findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoDesc(Long maCuocTroChuyen, Pageable pageable);

    // Lấy các tin nhắn mới hơn mã tin nhắn cuối (phục vụ Real-time / Polling Fallback)
    List<TinNhan> findByCuocTroChuyen_MaCuocTroChuyenAndMaTinNhanGreaterThanOrderByNgayTaoAsc(Long maCuocTroChuyen, Long maTinNhanCuoi);

    // Đánh dấu toàn bộ tin nhắn từ phía đối phương là đã xem
    @Modifying
    @Query("UPDATE TinNhan t SET t.daXem = true WHERE t.cuocTroChuyen.maCuocTroChuyen = :maCuocTroChuyen AND t.loaiNguoiGui = :loaiNguoiGuiVaDaGui")
    void danhDauDaXem(@Param("maCuocTroChuyen") Long maCuocTroChuyen, @Param("loaiNguoiGuiVaDaGui") String loaiNguoiGuiVaDaGui);

    // Đếm tổng số tin nhắn của một cuộc trò chuyện
    long countByCuocTroChuyen_MaCuocTroChuyen(Long maCuocTroChuyen);

    // Đếm số tin nhắn chưa xem của một cuộc trò chuyện
    long countByCuocTroChuyen_MaCuocTroChuyenAndLoaiNguoiGuiAndDaXemFalse(Long maCuocTroChuyen, String loaiNguoiGui);
}
