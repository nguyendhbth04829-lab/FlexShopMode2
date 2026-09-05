package com.example.demo.repository;

import com.example.demo.entity.NhiemVuGiaoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhiemVuGiaoHangRepository extends JpaRepository<NhiemVuGiaoHang, Long> {

    List<NhiemVuGiaoHang> findAllByDonHangShop_MaDonHangShop(Long maDonHangShop);

    @Query("SELECT n FROM NhiemVuGiaoHang n WHERE n.donHangShop.maDonHangShop = :maDonHangShop ORDER BY n.ngayTao DESC")
    List<NhiemVuGiaoHang> timNhiemVuTheoDonHangSapXepMoiNhat(@Param("maDonHangShop") Long maDonHangShop);

    Optional<NhiemVuGiaoHang> findFirstByDonHangShop_MaDonHangShopAndLinkAnhBangChungPodIsNotNullOrderByNgayTaoDesc(Long maDonHangShop);
}
