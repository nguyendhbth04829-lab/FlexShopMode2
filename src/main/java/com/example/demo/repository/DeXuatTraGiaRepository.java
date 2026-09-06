package com.example.demo.repository;

import com.example.demo.entity.DeXuatTraGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeXuatTraGiaRepository extends JpaRepository<DeXuatTraGia, Long> {

    List<DeXuatTraGia> findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoDesc(Long maCuocTroChuyen);

    List<DeXuatTraGia> findByGianHang_MaGianHangOrderByNgayTaoDesc(Long maGianHang);

    Page<DeXuatTraGia> findByGianHang_MaGianHangOrderByNgayTaoDesc(Long maGianHang, Pageable pageable);

    Page<DeXuatTraGia> findByGianHang_MaGianHangAndTrangThaiOrderByNgayTaoDesc(Long maGianHang, String trangThai, Pageable pageable);

    // Kiểm tra xem khách hàng có đề xuất nào đang chờ duyệt cho sản phẩm này không
    Optional<DeXuatTraGia> findTopByKhachHang_MaNguoiDungAndSanPham_MaSanPhamAndTrangThai(Long maKhachHang, Long maSanPham, String trangThai);

    // Đếm số đề xuất theo trạng thái của Gian hàng
    long countByGianHang_MaGianHang(Long maGianHang);

    long countByGianHang_MaGianHangAndTrangThai(Long maGianHang, String trangThai);

    // Lấy danh sách mã cuộc trò chuyện có đề xuất trả giá đang chờ duyệt
    @Query("SELECT DISTINCT d.cuocTroChuyen.maCuocTroChuyen FROM DeXuatTraGia d WHERE d.gianHang.maGianHang = :maGianHang AND d.trangThai = 'CHO_DUYET'")
    List<Long> findMaCuocTroChuyenCoTraGiaChoDuyet(@Param("maGianHang") Long maGianHang);
}
