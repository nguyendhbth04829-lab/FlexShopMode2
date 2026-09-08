package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.KyQuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & THANH TOÁN (DEV 5 - MINH)
 * USER STORY: US-26 - Khởi tạo dữ liệu mẫu kiểm thử luồng thanh toán
 * =====================================================================
 * Tự động tạo:
 *   - Khách hàng mẫu & Địa chỉ nhận hàng mặc định.
 *   - 2 Gian hàng mẫu (Anker, Logitech).
 *   - Đơn hàng tổng mẫu cần thanh toán và đơn hàng đã thanh toán.
 * =====================================================================
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private DiaChiNguoiDungRepository diaChiNguoiDungRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;
    private GianHangRepository gianHangRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private ViNguoiBanRepository viNguoiBanRepository;

    @Autowired
    private GiaoDichKyQuyRepository giaoDichKyQuyRepository;

    @Autowired
    private KyQuyService kyQuyService;

    @Override
    public void run(String... args) throws Exception {
        // Khởi tạo dữ liệu mẫu nếu chưa có đơn hàng shop
        if (donHangShopRepository.count() == 0) {
            System.out.println("=== Bắt đầu khởi tạo dữ liệu mẫu kiểm thử FlexShop (US-45) ===");

            // 1. Khách hàng
            NguoiDung khachHang = nguoiDungRepository.findAll().stream().findFirst().orElseGet(() -> {
                NguoiDung nd = new NguoiDung();
                nd.setEmail("khachhang@flexshop.vn");
                nd.setHoVaTen("Nguyễn Văn Khách Hàng");
                nd.setSoDienThoai("0901234567");
                nd.setMatKhauMaHoa("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
                nd.setTrangThai("HOAT_DONG");
                return nguoiDungRepository.save(nd);
            });

            // 2. Địa chỉ nhận hàng
            DiaChiNguoiDung diaChi = diaChiNguoiDungRepository.findAllByNguoiDung_MaNguoiDung(khachHang.getMaNguoiDung())
                    .stream().findFirst().orElseGet(() -> {
                        DiaChiNguoiDung dc = new DiaChiNguoiDung();
                        dc.setNguoiDung(khachHang);
                        dc.setTenNguoiNhan(khachHang.getHoVaTen());
                        dc.setSoDienThoai(khachHang.getSoDienThoai() != null ? khachHang.getSoDienThoai() : "0901234567");
                        dc.setTinhThanh("Hà Nội");
                        dc.setQuanHuyen("Cầu Giấy");
                        dc.setXaPhuong("Dịch Vọng");
                        dc.setDiaChiChiTiet("Số 123 Đường Cầu Giấy, Tòa nhà FPT");
                        dc.setLaMacDinh(true);
                        return diaChiNguoiDungRepository.save(dc);
                    });

            // 3. Danh mục sản phẩm
            DanhMuc dmCongNghe = danhMucRepository.findByDuongDanSlug("thiet-bi-dien-tu").orElseGet(() -> {
                DanhMuc dm = new DanhMuc();
                dm.setTenDanhMuc("Thiết Bị Điện Tử & Âm Thanh");
                dm.setDuongDanSlug("thiet-bi-dien-tu");
                dm.setCapDo(1);
                dm.setDangHoatDong(true);
                return danhMucRepository.save(dm);
            });

            DanhMuc dmThoiTrang = danhMucRepository.findByDuongDanSlug("thoi-trang-nam").orElseGet(() -> {
                DanhMuc dm = new DanhMuc();
                dm.setTenDanhMuc("Thời Trang Nam");
                dm.setDuongDanSlug("thoi-trang-nam");
                dm.setCapDo(1);
                dm.setDangHoatDong(true);
                return danhMucRepository.save(dm);
            });
    public void run(String... args) {
        try {
            NguoiDung khachHang;
            DiaChiNguoiDung diaChi;
            GianHang shopAnker;
            GianHang shopLogi;

            // 1. Khách hàng mẫu
            if (nguoiDungRepository.count() == 0) {
                khachHang = new NguoiDung();
                khachHang.setEmail("khachhang.demo@flexshop.com");
                khachHang.setSoDienThoai("0912345678");
                khachHang.setMatKhauMaHoa("$2a$10$e8y1d8f1h2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9a0b1c2d3e");
                khachHang.setHoVaTen("Nguyễn Văn Khách");
                khachHang.setTrangThai("HOAT_DONG");
                khachHang = nguoiDungRepository.save(khachHang);
            // 4. Gian hàng 1: TechZone
            NguoiDung chuShop1 = nguoiDungRepository.findByEmail("techzone@flexshop.vn").orElseGet(() -> {
                NguoiDung nd = new NguoiDung();
                nd.setEmail("techzone@flexshop.vn");
                nd.setHoVaTen("Trần Minh Đức (TechZone)");
                nd.setSoDienThoai("0988889999");
                nd.setMatKhauMaHoa("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
                nd.setTrangThai("HOAT_DONG");
                return nguoiDungRepository.save(nd);
            });

                diaChi = new DiaChiNguoiDung();
                diaChi.setNguoiDung(khachHang);
                diaChi.setTenNguoiNhan("Nguyễn Văn Khách");
                diaChi.setSoDienThoai("0912345678");
                diaChi.setTinhThanh("TP. Hà Nội");
                diaChi.setQuanHuyen("Quận Cầu Giấy");
                diaChi.setXaPhuong("Phường Dịch Vọng Hậu");
                diaChi.setDiaChiChiTiet("Số 86 Phố Duy Tân, Tòa nhà FPT");
                diaChi.setLaMacDinh(true);
                diaChi = diaChiNguoiDungRepository.save(diaChi);
            GianHang gianHang1 = gianHangRepository.findByTenGianHang("TechZone Flagship Store").orElseGet(() -> {
                GianHang gh = new GianHang();
                gh.setChuSoHuu(chuShop1);
                gh.setTenGianHang("TechZone Flagship Store");
                gh.setDuongDanSlug("techzone-flagship-store");
                gh.setMoTa("Cửa hàng công nghệ, phụ kiện âm thanh chính hãng");
                gh.setDiaChiKho("Kho tổng Hà Nội, KCN Đài Tư, Long Biên");
                gh.setSdtKho("02438889999");
                gh.setTrangThai("DA_DUYET");
                gh.setHangGianHang("MALL");
                return gianHangRepository.save(gh);
            });

            // Sản phẩm 1
            SanPham spTaiNghe = sanPhamRepository.findByDuongDanSlug("tai-nghe-bluetooth-pro-max").orElseGet(() -> {
                SanPham sp = new SanPham();
                sp.setGianHang(gianHang1);
                sp.setDanhMuc(dmCongNghe);
                sp.setTenSanPham("Tai nghe không dây Bluetooth chống ồn Pro Max");
                sp.setDuongDanSlug("tai-nghe-bluetooth-pro-max");
                sp.setGiaCoBan(new BigDecimal("1250000.00"));
                sp.setTrangThai("HOAT_DONG");
                return sanPhamRepository.save(sp);
            });
                NguoiDung chuShop1 = new NguoiDung();
                chuShop1.setEmail("seller.anker@flexshop.com");
                chuShop1.setSoDienThoai("0987654321");
                chuShop1.setMatKhauMaHoa("$2a$10$e8y1d8f1h2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9a0b1c2d3e");
                chuShop1.setHoVaTen("Trần Anker Owner");
                chuShop1.setTrangThai("HOAT_DONG");
                chuShop1 = nguoiDungRepository.save(chuShop1);

            // Biến thể sản phẩm 1
            BienTheSanPham btTaiNghe = bienTheSanPhamRepository.findByMaSku("SKU-EAR-BLK-01").orElseGet(() -> {
                BienTheSanPham bt = new BienTheSanPham();
                bt.setSanPham(spTaiNghe);
                bt.setMaSku("SKU-EAR-BLK-01");
                bt.setTenBienThe("Màu Đen Nhám - Bluetooth 5.3");
                bt.setGiaBan(new BigDecimal("1250000.00"));
                return bienTheSanPhamRepository.save(bt);
            });
                NguoiDung chuShop2 = new NguoiDung();
                chuShop2.setEmail("seller.logitech@flexshop.com");
                chuShop2.setSoDienThoai("0977112233");
                chuShop2.setMatKhauMaHoa("$2a$10$e8y1d8f1h2j3k4l5m6n7o8p9q0r1s2t3u4v5w6x7y8z9a0b1c2d3e");
                chuShop2.setHoVaTen("Lê Logitech Owner");
                chuShop2.setTrangThai("HOAT_DONG");
                chuShop2 = nguoiDungRepository.save(chuShop2);

                shopAnker = new GianHang();
                shopAnker.setChuSoHuu(chuShop1);
                shopAnker.setTenGianHang("Anker Official Store");
                shopAnker.setDuongDanSlug("anker-official-store");
                shopAnker.setMoTa("Phụ kiện cáp sạc pin dự phòng chính hãng");
                shopAnker.setDiaChiKho("Tòa Keangnam, Cầu Giấy, Hà Nội");
                shopAnker.setSdtKho("02431112222");
                shopAnker.setTrangThai("HOAT_DONG");
                shopAnker = gianHangRepository.save(shopAnker);
            // 5. Gian hàng 2: Flex Fashion
            NguoiDung chuShop2 = nguoiDungRepository.findByEmail("flexfashion@flexshop.vn").orElseGet(() -> {
                NguoiDung nd = new NguoiDung();
                nd.setEmail("flexfashion@flexshop.vn");
                nd.setHoVaTen("Lê Thu Hà (Flex Fashion)");
                nd.setSoDienThoai("0977112233");
                nd.setMatKhauMaHoa("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
                nd.setTrangThai("HOAT_DONG");
                return nguoiDungRepository.save(nd);
            });

                shopLogi = new GianHang();
                shopLogi.setChuSoHuu(chuShop2);
                shopLogi.setTenGianHang("Logitech Flagship Store");
                shopLogi.setDuongDanSlug("logitech-flagship-store");
                shopLogi.setMoTa("Chuột phím tai nghe gaming văn phòng Logitech");
                shopLogi.setDiaChiKho("Quận 1, TP. Hồ Chí Minh");
                shopLogi.setSdtKho("02839998888");
                shopLogi.setTrangThai("HOAT_DONG");
                shopLogi = gianHangRepository.save(shopLogi);
            } else {
                khachHang = nguoiDungRepository.findAll().get(0);
                diaChi = diaChiNguoiDungRepository.findAll().isEmpty() ? null : diaChiNguoiDungRepository.findAll().get(0);
                if (diaChi == null) {
                    diaChi = new DiaChiNguoiDung();
                    diaChi.setNguoiDung(khachHang);
                    diaChi.setTenNguoiNhan(khachHang.getHoVaTen());
                    diaChi.setSoDienThoai("0912345678");
                    diaChi.setTinhThanh("TP. Hà Nội");
                    diaChi.setQuanHuyen("Quận Cầu Giấy");
                    diaChi.setXaPhuong("Phường Dịch Vọng Hậu");
                    diaChi.setDiaChiChiTiet("Số 86 Phố Duy Tân, Tòa nhà FPT");
                    diaChi.setLaMacDinh(true);
                    diaChi = diaChiNguoiDungRepository.save(diaChi);
                }
                var shops = gianHangRepository.findAll();
                shopAnker = shops.get(0);
                shopLogi = shops.size() > 1 ? shops.get(1) : shops.get(0);
            }
            GianHang gianHang2 = gianHangRepository.findByTenGianHang("Flex Fashion Official").orElseGet(() -> {
                GianHang gh = new GianHang();
                gh.setChuSoHuu(chuShop2);
                gh.setTenGianHang("Flex Fashion Official");
                gh.setDuongDanSlug("flex-fashion-official");
                gh.setMoTa("Thời trang công sở và dạo phố cao cấp");
                gh.setDiaChiKho("Kho TP.HCM, Quận Tân Bình");
                gh.setSdtKho("02837778888");
                gh.setTrangThai("DA_DUYET");
                gh.setHangGianHang("CHUAN");
                return gianHangRepository.save(gh);
            });

            // Sản phẩm 2
            SanPham spAoSoMi = sanPhamRepository.findByDuongDanSlug("ao-so-mi-nam-oxford").orElseGet(() -> {
                SanPham sp = new SanPham();
                sp.setGianHang(gianHang2);
                sp.setDanhMuc(dmThoiTrang);
                sp.setTenSanPham("Áo Sơ Mi Nam Oxford Form Slimfit Kháng Nhăn");
                sp.setDuongDanSlug("ao-so-mi-nam-oxford");
                sp.setGiaCoBan(new BigDecimal("280000.00"));
                sp.setTrangThai("HOAT_DONG");
                return sanPhamRepository.save(sp);
            });
            // 2. Tạo đơn hàng mẫu nếu chưa có
            if (donHangTongRepository.count() == 0) {
                // Đơn hàng 1: Đang chờ thanh toán (US-26)
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

            // Biến thể sản phẩm 2
            BienTheSanPham btAoSoMi = bienTheSanPhamRepository.findByMaSku("SKU-SOMI-BLU-L").orElseGet(() -> {
                BienTheSanPham bt = new BienTheSanPham();
                bt.setSanPham(spAoSoMi);
                bt.setMaSku("SKU-SOMI-BLU-L");
                bt.setTenBienThe("Màu Xanh Pastel - Size L");
                bt.setGiaBan(new BigDecimal("280000.00"));
                return bienTheSanPhamRepository.save(bt);
            });
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
            // 6. Đơn hàng tổng 1
            DonHangTong donTong1 = new DonHangTong();
            donTong1.setMaCodeDonTong("MASTER-20260901-001");
            donTong1.setKhachHang(khachHang);
            donTong1.setDiaChiGiao(diaChi);
            donTong1.setTongTienHang(new BigDecimal("1250000.00"));
            donTong1.setTongPhiVanChuyen(new BigDecimal("30000.00"));
            donTong1.setTongThanhToanCuoi(new BigDecimal("1280000.00"));
            donTong1.setPhuongThucThanhToan("COD");
            donTong1.setTrangThaiDonHang("HOAN_TAT");
            donTong1.setTrangThaiThanhToan("DA_THANH_TOAN");
            donTong1 = donHangTongRepository.save(donTong1);

            // Đơn hàng Shop 1 (thuộc TechZone)
            DonHangShop donShop1 = new DonHangShop();
            donShop1.setMaCodeDonShop("SHOP-TECH-20260901-88");
            donShop1.setDonHangTong(donTong1);
            donShop1.setGianHang(gianHang1);
            donShop1.setTienHangShop(new BigDecimal("1250000.00"));
            donShop1.setPhiVanChuyen(new BigDecimal("30000.00"));
            donShop1.setTongTienShopNhan(new BigDecimal("1280000.00"));
            donShop1.setTrangThai("DA_GIAO");
            donShop1.setMaVanDon("VNP98234123VN");
            donShop1 = donHangShopRepository.save(donShop1);

            // Chi tiết sản phẩm trong đơn 1
            ChiTietDonHang ct1 = new ChiTietDonHang();
            ct1.setDonHangShop(donShop1);
            ct1.setMaBienThe(btTaiNghe.getMaBienThe());
            ct1.setTenSanPham(spTaiNghe.getTenSanPham());
            ct1.setTenBienThe(btTaiNghe.getTenBienThe());
            ct1.setMaSku(btTaiNghe.getMaSku());
            ct1.setDonGia(new BigDecimal("1250000.00"));
            ct1.setSoLuong(1);
            ct1.setTongTien(new BigDecimal("1250000.00"));
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
            // 7. Đơn hàng tổng 2
            DonHangTong donTong2 = new DonHangTong();
            donTong2.setMaCodeDonTong("MASTER-20260903-002");
            donTong2.setKhachHang(khachHang);
            donTong2.setDiaChiGiao(diaChi);
            donTong2.setTongTienHang(new BigDecimal("560000.00"));
            donTong2.setTongPhiVanChuyen(new BigDecimal("25000.00"));
            donTong2.setTongThanhToanCuoi(new BigDecimal("585000.00"));
            donTong2.setPhuongThucThanhToan("VNPAY_QR");
            donTong2.setTrangThaiDonHang("HOAN_TAT");
            donTong2.setTrangThaiThanhToan("DA_THANH_TOAN");
            donTong2 = donHangTongRepository.save(donTong2);

            // Đơn hàng Shop 2 (thuộc Flex Fashion)
            DonHangShop donShop2 = new DonHangShop();
            donShop2.setMaCodeDonShop("SHOP-FASHION-20260903-99");
            donShop2.setDonHangTong(donTong2);
            donShop2.setGianHang(gianHang2);
            donShop2.setTienHangShop(new BigDecimal("560000.00"));
            donShop2.setPhiVanChuyen(new BigDecimal("25000.00"));
            donShop2.setTongTienShopNhan(new BigDecimal("585000.00"));
            donShop2.setTrangThai("DA_GIAO");
            donShop2.setMaVanDon("GHN88776655VN");
            donShop2 = donHangShopRepository.save(donShop2);

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
            ChiTietDonHang ct2 = new ChiTietDonHang();
            ct2.setDonHangShop(donShop2);
            ct2.setMaBienThe(btAoSoMi.getMaBienThe());
            ct2.setTenSanPham(spAoSoMi.getTenSanPham());
            ct2.setTenBienThe(btAoSoMi.getTenBienThe());
            ct2.setMaSku(btAoSoMi.getMaSku());
            ct2.setDonGia(new BigDecimal("280000.00"));
            ct2.setSoLuong(2);
            ct2.setTongTien(new BigDecimal("560000.00"));
            chiTietDonHangRepository.save(ct2);

                // Đơn hàng 2: Đã thanh toán Mock Online thành công
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
                don2 = donHangTongRepository.save(don2);

                DonHangShop shopOrder3 = new DonHangShop();
                shopOrder3.setMaCodeDonShop("DHS-ANKER-02");
                shopOrder3.setDonHangTong(don2);
                shopOrder3.setGianHang(shopAnker);
                shopOrder3.setTienHangShop(new BigDecimal("2100000"));
                shopOrder3.setPhiVanChuyen(new BigDecimal("40000"));
                shopOrder3.setTongTienShopNhan(new BigDecimal("2140000"));
                shopOrder3.setTrangThai("CHO_XAC_NHAN");
                shopOrder3 = donHangShopRepository.save(shopOrder3);

                ChiTietDonHang ct3 = new ChiTietDonHang();
                ct3.setDonHangShop(shopOrder3);
                ct3.setMaBienThe(3L);
                ct3.setTenSanPham("Trạm sạc Anker PowerHouse 757 GaN");
                ct3.setTenBienThe("Màu Xám Đen");
                ct3.setMaSku("ANK-POW-757");
                ct3.setDonGia(new BigDecimal("2100000"));
                ct3.setSoLuong(1);
                ct3.setTongTien(new BigDecimal("2100000"));
                chiTietDonHangRepository.save(ct3);
            }

            // 3. [US-42 & US-43] Đảm bảo Ví Người Bán luôn tồn tại
            if (viNguoiBanRepository.count() == 0) {
                var listShops = gianHangRepository.findAll();
                for (GianHang shop : listShops) {
                    kyQuyService.getOrCreateViNguoiBan(shop);
                }
            }

            // 4. [US-42 & US-43] Khởi tạo các Giao dịch Ký Quỹ Escrow mẫu
            if (giaoDichKyQuyRepository.count() == 0) {
                var listShopOrders = donHangShopRepository.findAll();
                if (!listShopOrders.isEmpty()) {
                    // Đơn 1: Đang tạm giữ Escrow (Shopee Guarantee còn hạn 3 ngày)
                    DonHangShop shopOrder1 = listShopOrders.get(0);
                    kyQuyService.taoGiaoDichKyQuy(shopOrder1);

                    // Đơn 2 (nếu có): Thiết lập thời gian hết hạn 3 ngày (quá hạn) để kiểm thử Quét tự động
                    if (listShopOrders.size() > 1) {
                        DonHangShop shopOrder2 = listShopOrders.get(1);
                        GiaoDichKyQuy kq2 = kyQuyService.taoGiaoDichKyQuy(shopOrder2);
                        kq2.setNgayDuKienNhaTien(LocalDateTime.now().minusDays(1)); // Đã quá hạn 1 ngày
                        kq2.setNgayTao(LocalDateTime.now().minusDays(4));
                        giaoDichKyQuyRepository.save(kq2);
                    }

                    // Đơn 3 (nếu có): Giả lập 1 đơn đã thanh toán online và đang bảo lưu
                    if (listShopOrders.size() > 2) {
                        DonHangShop shopOrder3 = listShopOrders.get(2);
                        kyQuyService.taoGiaoDichKyQuy(shopOrder3);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("DataInitializer US-26 & US-42: " + e.getMessage());
            System.out.println("=== Khởi tạo dữ liệu mẫu hoàn tất thành công! ===");
        }
    }
}
