package com.example.demo.repository;

import com.example.demo.entity.DanhMuc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
    
    List<DanhMuc> findByDaXoaFalse();

    Page<DanhMuc> findByDaXoaFalse(Pageable pageable);

    @Query("SELECT d FROM DanhMuc d WHERE d.daXoa = false AND d.tenDanhMuc LIKE %:tuKhoa%")
    Page<DanhMuc> timKiemDanhSach(String tuKhoa, Pageable pageable);

    Optional<DanhMuc> findByDuongDanSlugAndDaXoaFalse(String slug);
    
    // Tìm các danh mục con trực tiếp
    List<DanhMuc> findByDanhMucCha_MaDanhMucAndDaXoaFalse(Long maDanhMucCha);
}
