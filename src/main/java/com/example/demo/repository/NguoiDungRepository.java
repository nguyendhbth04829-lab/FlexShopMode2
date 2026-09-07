package com.example.demo.repository;

import com.example.demo.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {

    Optional<NguoiDung> findByEmailIgnoreCase(String email);

    Optional<NguoiDung> findBySoDienThoai(String soDienThoai);

    @Query("SELECT u FROM NguoiDung u WHERE LOWER(u.email) = LOWER(:identifier) OR u.soDienThoai = :identifier")
    Optional<NguoiDung> findByIdentifier(@Param("identifier") String identifier);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    /**
     * US-04: Kiểm tra Số điện thoại đã được đăng ký bởi người dùng khác hay chưa
     */
    @Query("SELECT COUNT(u) > 0 FROM NguoiDung u WHERE u.soDienThoai = :soDienThoai AND u.maNguoiDung != :maNguoiDung")
    boolean existsBySoDienThoaiAndMaNguoiDungNot(@Param("soDienThoai") String soDienThoai, @Param("maNguoiDung") Long maNguoiDung);
}
