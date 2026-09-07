package com.example.demo.repository;

import com.example.demo.entity.ThuongHieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Long> {
    List<ThuongHieu> findByDaXoaFalse();
    Page<ThuongHieu> findByDaXoaFalse(Pageable pageable);
    Optional<ThuongHieu> findByTenThuongHieuAndDaXoaFalse(String ten);
    @Query("SELECT t FROM ThuongHieu t WHERE t.daXoa = false AND t.tenThuongHieu LIKE %:tuKhoa%")
    Page<ThuongHieu> timKiem(String tuKhoa, Pageable pageable);
}
