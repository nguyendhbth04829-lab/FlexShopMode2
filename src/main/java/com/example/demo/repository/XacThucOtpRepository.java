package com.example.demo.repository;

import com.example.demo.entity.XacThucOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface XacThucOtpRepository extends JpaRepository<XacThucOtp, Long> {

    /**
     * Tìm mã OTP mới nhất chưa sử dụng của người nhận theo loại OTP
     */
    Optional<XacThucOtp> findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc(String nguoiNhan, String loaiOtp);

    /**
     * Tìm bản ghi OTP mới nhất để kiểm tra trạng thái khóa 24h
     */
    Optional<XacThucOtp> findTopByNguoiNhanOrderByNgayTaoDesc(String nguoiNhan);

    /**
     * Tìm bản ghi OTP đang còn hiệu lực khóa (thời gian khóa trong tương lai)
     */
    Optional<XacThucOtp> findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(String nguoiNhan, LocalDateTime now);

    /**
     * Rate Limit US-03: Đếm số lần gửi OTP trong vòng 1 giờ gần nhất
     */
    @Query("SELECT COUNT(o) FROM XacThucOtp o WHERE o.nguoiNhan = :nguoiNhan AND o.loaiOtp = :loaiOtp AND o.ngayTao >= :thoiGianBatDau")
    long demSoLanGuiTrongKhoangThoiGian(@Param("nguoiNhan") String nguoiNhan,
                                       @Param("loaiOtp") String loaiOtp,
                                       @Param("thoiGianBatDau") LocalDateTime thoiGianBatDau);
}
