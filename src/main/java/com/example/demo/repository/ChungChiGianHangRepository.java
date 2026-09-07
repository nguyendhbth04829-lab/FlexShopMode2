package com.example.demo.repository;

import com.example.demo.entity.ChungChiGianHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChungChiGianHangRepository extends JpaRepository<ChungChiGianHang, Long> {

    /**
     * Lấy danh sách chứng chỉ / giấy tờ của gian hàng
     */
    List<ChungChiGianHang> findByMaGianHang(Long maGianHang);

    /**
     * Lấy giấy tờ mới nhất của gian hàng theo loại giấy tờ
     */
    Optional<ChungChiGianHang> findFirstByMaGianHangAndLoaiGiayToOrderByMaChungChiDesc(Long maGianHang, String loaiGiayTo);

    /**
     * Lấy giấy tờ mới nhất của gian hàng
     */
    Optional<ChungChiGianHang> findFirstByMaGianHangOrderByMaChungChiDesc(Long maGianHang);

    /**
     * Kiểm tra số giấy tờ / mã số thuế đã tồn tại hay chưa
     */
    boolean existsBySoGiayTo(String soGiayTo);

    /**
     * Kiểm tra số giấy tờ đã tồn tại ngoại trừ gian hàng hiện tại
     */
    boolean existsBySoGiayToAndMaGianHangNot(String soGiayTo, Long maGianHang);

    /**
     * Lấy danh sách chứng chỉ của gian hàng sắp xếp giảm dần theo mã
     */
    List<ChungChiGianHang> findByMaGianHangOrderByMaChungChiDesc(Long maGianHang);

    /**
     * Tìm chứng chỉ theo mã chứng chỉ và mã gian hàng
     */
    Optional<ChungChiGianHang> findByMaChungChiAndMaGianHang(Long maChungChi, Long maGianHang);

    /**
     * Đếm số lượng chứng chỉ của gian hàng theo trạng thái duyệt
     */
    long countByMaGianHangAndTrangThaiDuyet(Long maGianHang, String trangThaiDuyet);

    /**
     * Đếm tổng số lượng chứng chỉ của gian hàng
     */
    long countByMaGianHang(Long maGianHang);
}
