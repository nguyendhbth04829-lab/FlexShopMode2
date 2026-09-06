package com.example.demo.service;

import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.DonHangTong;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.DonHangTongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Xử lý nghiệp vụ chọn phương thức & thanh toán đơn hàng
 * =====================================================================
 * Nghiệp vụ chi tiết:
 *   - Quản lý trạng thái thanh toán của Đơn hàng tổng (don_hang_tong).
 *   - Xử lý chọn COD: cập nhật trạng thái CHUA_THANH_TOAN, kích hoạt đơn shop con CHO_XAC_NHAN.
 *   - Xử lý Mock Online Payment: cập nhật DA_THANH_TOAN, mở đường đưa tiền vào Ký quỹ Escrow (US-42).
 *   - Xử lý thất bại giao dịch trực tuyến: chuyển trạng thái THANH_TOAN_THAT_BAI để người mua thanh toán lại.
 * =====================================================================
 */
@Service
public class ThanhToanService {

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    public Page<DonHangTong> getDanhSachDonHang(String keyword, String trangThaiThanhToan, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size);
        return donHangTongRepository.searchAndFilter(keyword, trangThaiThanhToan, pageable);
    }

    public DonHangTong getDonHangTong(Long id) {
        return donHangTongRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng tổng có mã ID: " + id));
    }

    /**
     * US-26: Khách hàng chọn phương thức COD (Thanh toán khi nhận hàng)
     */
    @Transactional
    public DonHangTong xuLyChonCOD(Long maDonHangTong) {
        DonHangTong donHang = getDonHangTong(maDonHangTong);

        donHang.setPhuongThucThanhToan("COD");
        donHang.setTrangThaiThanhToan("CHUA_THANH_TOAN");
        donHang.setTrangThaiDonHang("CHO_XU_LY");

        // Cập nhật trạng thái các đơn hàng shop con chuyển sang CHO_XAC_NHAN
        if (donHang.getDanhSachShopOrder() != null) {
            for (DonHangShop shopOrder : donHang.getDanhSachShopOrder()) {
                if ("CHO_XAC_NHAN".equals(shopOrder.getTrangThai()) || shopOrder.getTrangThai() == null) {
                    shopOrder.setTrangThai("CHO_XAC_NHAN");
                    donHangShopRepository.save(shopOrder);
                }
            }
        }

        return donHangTongRepository.save(donHang);
    }

    /**
     * US-26: Giả lập thanh toán trực tuyến Mock Online Payment thành công
     */
    @Transactional
    public DonHangTong xuLyMockOnlineThanhCong(Long maDonHangTong, String maNganHang, String soThe) {
        DonHangTong donHang = getDonHangTong(maDonHangTong);

        donHang.setPhuongThucThanhToan("MOCK_ONLINE");
        donHang.setTrangThaiThanhToan("DA_THANH_TOAN");
        donHang.setTrangThaiDonHang("CHO_XU_LY");

        // Kích hoạt trạng thái sẵn sàng cho các shop
        if (donHang.getDanhSachShopOrder() != null) {
            for (DonHangShop shopOrder : donHang.getDanhSachShopOrder()) {
                shopOrder.setTrangThai("CHO_XAC_NHAN");
                donHangShopRepository.save(shopOrder);
            }
        }

        return donHangTongRepository.save(donHang);
    }

    /**
     * US-26: Mô phỏng thanh toán Mock Online thất bại (thẻ hết tiền, lỗi OTP,...)
     */
    @Transactional
    public DonHangTong xuLyMockOnlineThatBai(Long maDonHangTong, String lyDo) {
        DonHangTong donHang = getDonHangTong(maDonHangTong);

        donHang.setPhuongThucThanhToan("MOCK_ONLINE");
        donHang.setTrangThaiThanhToan("THANH_TOAN_THAT_BAI");

        return donHangTongRepository.save(donHang);
    }
}
