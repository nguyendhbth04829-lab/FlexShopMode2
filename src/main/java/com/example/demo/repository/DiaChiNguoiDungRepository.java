package com.example.demo.repository;

import com.example.demo.entity.DiaChiNguoiDung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiNguoiDungRepository extends JpaRepository<DiaChiNguoiDung, Long> {

    /**
     * US-05: Đếm tổng số địa chỉ còn hiệu lực của người dùng (tối đa 20)
     */
    long countByMaNguoiDungAndDaXoaFalse(Long maNguoiDung);

    /**
     * Tìm địa chỉ theo ID và người dùng (kiểm tra quyền sở hữu)
     */
    Optional<DiaChiNguoiDung> findByMaDiaChiAndMaNguoiDungAndDaXoaFalse(Long maDiaChi, Long maNguoiDung);

    /**
     * Tìm địa chỉ mặc định hiện tại của người dùng
     */
    Optional<DiaChiNguoiDung> findByMaNguoiDungAndLaMacDinhTrueAndDaXoaFalse(Long maNguoiDung);

    /**
     * Tìm địa chỉ gần nhất còn hiệu lực để tự động gán mặc định thay thế khi xóa
     */
    Optional<DiaChiNguoiDung> findTopByMaNguoiDungAndDaXoaFalseOrderByNgayTaoDesc(Long maNguoiDung);

    /**
     * US-05 Nghiệp vụ cốt lõi:
     * Chuyển tất cả các địa chỉ khác của người dùng về la_mac_dinh = false trong 1 câu lệnh cập nhật
     */
    @Modifying
    @Query("UPDATE DiaChiNguoiDung d SET d.laMacDinh = false WHERE d.maNguoiDung = :maNguoiDung AND d.maDiaChi != :maDiaChiMoi AND d.daXoa = false")
    void huyMacDinhCacDiaChiKhac(@Param("maNguoiDung") Long maNguoiDung, @Param("maDiaChiMoi") Long maDiaChiMoi);

    /**
     * Lấy toàn bộ danh sách địa chỉ chưa xóa của người dùng, ưu tiên địa chỉ mặc định lên đầu
     */
    List<DiaChiNguoiDung> findByMaNguoiDungAndDaXoaFalseOrderByLaMacDinhDescNgayTaoDesc(Long maNguoiDung);

    /**
     * Mở rộng: Tìm kiếm, lọc và phân trang chuyên nghiệp
     */
    @Query("SELECT d FROM DiaChiNguoiDung d WHERE d.maNguoiDung = :maNguoiDung AND d.daXoa = false " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(d.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(d.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(d.diaChiChiTiet) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:tinhThanh IS NULL OR :tinhThanh = '' OR LOWER(d.tinhThanh) = LOWER(:tinhThanh)) " +
            "AND (:laMacDinh IS NULL OR d.laMacDinh = :laMacDinh)")
    Page<DiaChiNguoiDung> timKiemPhanTrang(
            @Param("maNguoiDung") Long maNguoiDung,
            @Param("keyword") String keyword,
            @Param("tinhThanh") String tinhThanh,
            @Param("laMacDinh") Boolean laMacDinh,
            Pageable pageable);

    /**
     * Lấy danh sách các tỉnh thành mà người dùng đã từng lưu địa chỉ phục vụ bộ lọc
     */
    @Query("SELECT DISTINCT d.tinhThanh FROM DiaChiNguoiDung d WHERE d.maNguoiDung = :maNguoiDung AND d.daXoa = false ORDER BY d.tinhThanh")
    List<String> layDanhSachTinhThanhCuaNguoiDung(@Param("maNguoiDung") Long maNguoiDung);
}
