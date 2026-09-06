package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DiaChiNguoiDungRepository diaChiNguoiDungRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Override
    public void run(String... args) {
        try {
            if (nguoiDungRepository.count() == 0) {
                // 1. Tạo Khách hàng mẫu
                NguoiDung khachHang = new NguoiDung();
                khachHang.setEmail("khachhang.demo@flexshop.com");
                khachHang.setSoDienThoai("0912345678");
                khachHang.setMatKhauMaHoa("$2a$10$e8y1d8f1h2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9a0b1c2d3e");
                khachHang.setHoVaTen("Nguyễn Văn Khách");
                khachHang.setTrangThai("HOAT_DONG");
                khachHang = nguoiDungRepository.save(khachHang);

                // 2. Tạo Địa chỉ giao hàng mẫu cho khách
                DiaChiNguoiDung diaChi = new DiaChiNguoiDung();
                diaChi.setNguoiDung(khachHang);
                diaChi.setTenNguoiNhan("Nguyễn Văn Khách");
                diaChi.setSoDienThoai("0912345678");
                diaChi.setTinhThanh("TP. Hà Nội");
                diaChi.setQuanHuyen("Quận Cầu Giấy");
                diaChi.setXaPhuong("Phường Dịch Vọng Hậu");
                diaChi.setDiaChiChiTiet("Số 86 Phố Duy Tân, Tòa nhà FPT");
                diaChi.setLaMacDinh(true);
                diaChi = diaChiNguoiDungRepository.save(diaChi);

                // 3. Tạo Chủ shop mẫu 1 & 2
                NguoiDung chuShop1 = new NguoiDung();
                chuShop1.setEmail("seller.anker@flexshop.com");
                chuShop1.setSoDienThoai("0987654321");
                chuShop1.setMatKhauMaHoa("$2a$10$e8y1d8f1h2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9a0b1c2d3e");
                chuShop1.setHoVaTen("Trần Anker Owner");
                chuShop1.setTrangThai("HOAT_DONG");
                chuShop1 = nguoiDungRepository.save(chuShop1);

                NguoiDung chuShop2 = new NguoiDung();
                chuShop2.setEmail("seller.logitech@flexshop.com");
                chuShop2.setSoDienThoai("0977112233");
                chuShop2.setMatKhauMaHoa("$2a$10$e8y1d8f1h2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9a0b1c2d3e");
                chuShop2.setHoVaTen("Lê Logitech Owner");
                chuShop2.setTrangThai("HOAT_DONG");
                chuShop2 = nguoiDungRepository.save(chuShop2);

                // 4. Tạo Gian hàng mẫu
                GianHang shopAnker = new GianHang();
                shopAnker.setChuSoHuu(chuShop1);
                shopAnker.setTenGianHang("Anker Official Store");
                shopAnker.setDuongDanSlug("anker-official-store");
                shopAnker.setMoTa("Phụ kiện cáp sạc pin dự phòng chính hãng");
                shopAnker.setDiaChiKho("Tòa Keangnam, Cầu Giấy, Hà Nội");
                shopAnker.setSdtKho("02431112222");
                shopAnker.setTrangThai("HOAT_DONG");
                shopAnker = gianHangRepository.save(shopAnker);

                GianHang shopLogi = new GianHang();
                shopLogi.setChuSoHuu(chuShop2);
                shopLogi.setTenGianHang("Logitech Flagship Store");
                shopLogi.setDuongDanSlug("logitech-flagship-store");
                shopLogi.setMoTa("Chuột phím tai nghe gaming văn phòng Logitech");
                shopLogi.setDiaChiKho("Quận 1, TP. Hồ Chí Minh");
                shopLogi.setSdtKho("02839998888");
                shopLogi.setTrangThai("HOAT_DONG");
                shopLogi = gianHangRepository.save(shopLogi);

                // 5. Đơn hàng tổng 1: ĐANG CHỜ THANH TOÁN (để test chọn COD hoặc Mock Online)
                DonHangTong don1 = new DonHangTong();
                don1.setMaCodeDonTong("DHT-2026-PAY01");
                don1.setKhachHang(khachHang);
                don1.setDiaChiGiao(diaChi);
                don1.setTongTienHang(new BigDecimal("1250000"));
                don1.setTongPhiVanChuyen(new BigDecimal("30000"));
                don1.setTongThanhToanCuoi(new BigDecimal("1280000"));
                don1.setPhuongThucThanhToan("CHUA_CHON");
                don1.setTrangThaiThanhToan("CHUA_THANH_TOAN");
                don1.setTrangThaiDonHang("CHO_XU_LY");
                don1.setGhiChu("Giao giờ hành chính giúp tôi");
                don1.setNgayTao(LocalDateTime.now().minusMinutes(25));
                don1 = donHangTongRepository.save(don1);

                // ShopOrder con của đơn 1
                DonHangShop shopOrder1 = new DonHangShop();
                shopOrder1.setMaCodeDonShop("DHS-ANKER-01");
                shopOrder1.setDonHangTong(don1);
                shopOrder1.setGianHang(shopAnker);
                shopOrder1.setTienHangShop(new BigDecimal("850000"));
                shopOrder1.setPhiVanChuyen(new BigDecimal("15000"));
                shopOrder1.setTongTienShopNhan(new BigDecimal("865000"));
                shopOrder1.setTrangThai("CHO_XAC_NHAN");
                shopOrder1 = donHangShopRepository.save(shopOrder1);

                ChiTietDonHang ct1 = new ChiTietDonHang();
                ct1.setDonHangShop(shopOrder1);
                ct1.setMaBienThe(1L);
                ct1.setTenSanPham("Củ sạc GaN Anker 65W Pod 3");
                ct1.setTenBienThe("Màu Đen");
                ct1.setMaSku("ANK-65W-BLK");
                ct1.setDonGia(new BigDecimal("850000"));
                ct1.setSoLuong(1);
                ct1.setTongTien(new BigDecimal("850000"));
                chiTietDonHangRepository.save(ct1);

                DonHangShop shopOrder2 = new DonHangShop();
                shopOrder2.setMaCodeDonShop("DHS-LOGI-01");
                shopOrder2.setDonHangTong(don1);
                shopOrder2.setGianHang(shopLogi);
                shopOrder2.setTienHangShop(new BigDecimal("400000"));
                shopOrder2.setPhiVanChuyen(new BigDecimal("15000"));
                shopOrder2.setTongTienShopNhan(new BigDecimal("415000"));
                shopOrder2.setTrangThai("CHO_XAC_NHAN");
                shopOrder2 = donHangShopRepository.save(shopOrder2);

                ChiTietDonHang ct2 = new ChiTietDonHang();
                ct2.setDonHangShop(shopOrder2);
                ct2.setMaBienThe(2L);
                ct2.setTenSanPham("Bàn phím không dây Logitech K380");
                ct2.setTenBienThe("Màu Xám Không Gian");
                ct2.setMaSku("LOGI-K380-GRY");
                ct2.setDonGia(new BigDecimal("400000"));
                ct2.setSoLuong(1);
                ct2.setTongTien(new BigDecimal("400000"));
                chiTietDonHangRepository.save(ct2);

                // 6. Đơn hàng tổng 2: Đã thanh toán Mock Online thành công trước đó (để so sánh)
                DonHangTong don2 = new DonHangTong();
                don2.setMaCodeDonTong("DHT-2026-PAY02");
                don2.setKhachHang(khachHang);
                don2.setDiaChiGiao(diaChi);
                don2.setTongTienHang(new BigDecimal("2100000"));
                don2.setTongPhiVanChuyen(new BigDecimal("40000"));
                don2.setTongThanhToanCuoi(new BigDecimal("2140000"));
                don2.setPhuongThucThanhToan("MOCK_ONLINE");
                don2.setTrangThaiThanhToan("DA_THANH_TOAN");
                don2.setTrangThaiDonHang("CHO_XU_LY");
                don2.setNgayTao(LocalDateTime.now().minusHours(2));
                donHangTongRepository.save(don2);
            }
        } catch (Exception e) {
            System.err.println("DataInitializer US-26: " + e.getMessage());
        }
    }
}
