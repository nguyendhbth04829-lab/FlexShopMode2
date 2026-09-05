package com.example.demo.repository;

import com.example.demo.entity.LichSuGiaoDichVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuGiaoDichViRepository extends JpaRepository<LichSuGiaoDichVi, Long> {

    List<LichSuGiaoDichVi> findAllByViNguoiBan_MaViOrderByNgayTaoDesc(Long maVi);

    List<LichSuGiaoDichVi> findAllByMaThamChieuOrderByNgayTaoDesc(String maThamChieu);
}
