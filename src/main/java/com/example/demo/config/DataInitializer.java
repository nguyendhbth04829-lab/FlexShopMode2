package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.KyQuyService;
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
    private GianHangRepository gianHangRepository;

    @Autowired
    private DiaChiNguoiDungRepository diaChiNguoiDungRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

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
        if (donHangShopRepository.count() == 0) {
            System.out.println("=== Bat dau khoi tao du lieu mau kiem thu FlexShop ===");

            // 1. Khach hang
            NguoiDung khachHang = nguoiDungRepository.findAll().stream().findFirst().orElseGet(() -> {
                NguoiDung nd = new NguoiDung();
                nd.setEmail("khachhang@flexshop.vn");
                nd.setHoVaTen("Nguyen Van Khach Hang");
                nd.setSoDienThoai("0901234567");
                nd.setMatKhauMaHoa("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
                nd.setTrangThai("HOAT_DONG");
                return nguoiDungRepository.save(nd);
            });

            // 2. Dia chi nhan hang
            DiaChiNguoiDung diaChi = diaChiNguoiDungRepository.findAllByNguoiDung_MaNguoiDung(khachHang.getMaNguoiDung())
                    .stream().findFirst().orElseGet(() -> {
                        DiaChiNguoiDung dc = new DiaChiNguoiDung();
                        dc.setNguoiDung(khachHang);
                        dc.setTenNguoiNhan(khachHang.getHoVaTen());
                        dc.setSoDienThoai(khachHang.getSoDienThoai() != null ? khachHang.getSoDienThoai() : "0901234567");
                        dc.setTinhThanh("Ha Noi");
                        dc.setQuanHuyen("Cau Giay");
                        dc.setXaPhuong("Dich Vong");
                        dc.setDiaChiChiTiet("So 123 Duong Cau Giay, Toa nha FPT");
                        dc.setLaMacDinh(true);
                        return diaChiNguoiDungRepository.save(dc);
                    });

            // 3. Danh muc san pham
            DanhMuc dmCongNghe = danhMucRepository.findByDuongDanSlug("thiet-bi-dien-tu").orElseGet(() -> {
                DanhMuc dm = new DanhMuc();
                dm.setTenDanhMuc("Thiet Bi Dien Tu & Am Thanh");
                dm.setDuongDanSlug("thiet-bi-dien-tu");
                dm.setCapDo(1);
                dm.setDangHoatDong(true);
                return danhMucRepository.save(dm);
            });

            DanhMuc dmThoiTrang = danhMucRepository.findByDuongDanSlug("thoi-trang-nam").orElseGet(() -> {
                DanhMuc dm = new DanhMuc();
                dm.setTenDanhMuc("Thoi Trang Nam");
                dm.setDuongDanSlug("thoi-trang-nam");
                dm.setCapDo(1);
                dm.setDangHoatDong(true);
                return danhMucRepository.save(dm);
            });

            // 4. Gian hang 1: TechZone
            NguoiDung chuShop1 = nguoiDungRepository.findByEmailIgnoreCase("techzone@flexshop.vn").orElseGet(() -> {
                NguoiDung nd = new NguoiDung();
                nd.setEmail("techzone@flexshop.vn");
                nd.setHoVaTen("Tran Minh Duc (TechZone)");
                nd.setSoDienThoai("0988889999");
                nd.setMatKhauMaHoa("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
                nd.setTrangThai("HOAT_DONG");
                return nguoiDungRepository.save(nd);
            });

            GianHang gianHang1 = gianHangRepository.findByTenGianHang("TechZone Flagship Store").orElseGet(() -> {
                GianHang gh = new GianHang();
                gh.setChuSoHuu(chuShop1);
                gh.setTenGianHang("TechZone Flagship Store");
                gh.setDuongDanSlug("techzone-flagship-store");
                gh.setMoTa("Cua hang cong nghe, phu kien am thanh chinh hang");
                gh.setDiaChiKho("Kho tong Ha Noi, KCN Dai Tu, Long Bien");
                gh.setSdtKho("02438889999");
                gh.setTrangThai("DA_DUYET");
                gh.setHangGianHang("MALL");
                return gianHangRepository.save(gh);
            });

            // 5. Gian hang 2: Flex Fashion
            NguoiDung chuShop2 = nguoiDungRepository.findByEmailIgnoreCase("flexfashion@flexshop.vn").orElseGet(() -> {
                NguoiDung nd = new NguoiDung();
                nd.setEmail("flexfashion@flexshop.vn");
                nd.setHoVaTen("Le Thu Ha (Flex Fashion)");
                nd.setSoDienThoai("0977112233");
                nd.setMatKhauMaHoa("$2a$10$abcdefghijklmnopqrstuvwxyz123456");
                nd.setTrangThai("HOAT_DONG");
                return nguoiDungRepository.save(nd);
            });

            GianHang gianHang2 = gianHangRepository.findByTenGianHang("Flex Fashion Official").orElseGet(() -> {
                GianHang gh = new GianHang();
                gh.setChuSoHuu(chuShop2);
                gh.setTenGianHang("Flex Fashion Official");
                gh.setDuongDanSlug("flex-fashion-official");
                gh.setMoTa("Thoi trang cong so va dao pho cao cap");
                gh.setDiaChiKho("Kho TP.HCM, Quan Tan Binh");
                gh.setSdtKho("02837778888");
                gh.setTrangThai("DA_DUYET");
                gh.setHangGianHang("CHUAN");
                return gianHangRepository.save(gh);
            });

            // San pham 1
            SanPham spTaiNghe = sanPhamRepository.findByDuongDanSlug("tai-nghe-bluetooth-pro-max").orElseGet(() -> {
                SanPham sp = new SanPham();
                sp.setGianHang(gianHang1);
                sp.setDanhMuc(dmCongNghe);
                sp.setTenSanPham("Tai nghe khong day Bluetooth chong on Pro Max");
                sp.setDuongDanSlug("tai-nghe-bluetooth-pro-max");
                sp.setGiaCoBan(new BigDecimal("1250000.00"));
                sp.setTrangThai("HOAT_DONG");
                return sanPhamRepository.save(sp);
            });

            BienTheSanPham btTaiNghe = bienTheSanPhamRepository.findByMaSku("SKU-EAR-BLK-01").orElseGet(() -> {
                BienTheSanPham bt = new BienTheSanPham();
                bt.setSanPham(spTaiNghe);
                bt.setMaSku("SKU-EAR-BLK-01");
                bt.setTenBienThe("Mau Den Nham - Bluetooth 5.3");
                bt.setGiaBan(new BigDecimal("1250000.00"));
                return bienTheSanPhamRepository.save(bt);
            });

            // San pham 2
            SanPham spAoSoMi = sanPhamRepository.findByDuongDanSlug("ao-so-mi-nam-oxford").orElseGet(() -> {
                SanPham sp = new SanPham();
                sp.setGianHang(gianHang2);
                sp.setDanhMuc(dmThoiTrang);
                sp.setTenSanPham("Ao So Mi Nam Oxford Form Slimfit Khang Nhan");
                sp.setDuongDanSlug("ao-so-mi-nam-oxford");
                sp.setGiaCoBan(new BigDecimal("280000.00"));
                sp.setTrangThai("HOAT_DONG");
                return sanPhamRepository.save(sp);
            });

            BienTheSanPham btAoSoMi = bienTheSanPhamRepository.findByMaSku("SKU-SOMI-BLU-L").orElseGet(() -> {
                BienTheSanPham bt = new BienTheSanPham();
                bt.setSanPham(spAoSoMi);
                bt.setMaSku("SKU-SOMI-BLU-L");
                bt.setTenBienThe("Mau Xanh Pastel - Size L");
                bt.setGiaBan(new BigDecimal("280000.00"));
                return bienTheSanPhamRepository.save(bt);
            });

            // 6. Don hang tong 1 - COD, da giao
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

            // 7. Don hang tong 2 - VNPAY, da giao
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
            ct2.setDonHangShop(donShop2);
            ct2.setMaBienThe(btAoSoMi.getMaBienThe());
            ct2.setTenSanPham(spAoSoMi.getTenSanPham());
            ct2.setTenBienThe(btAoSoMi.getTenBienThe());
            ct2.setMaSku(btAoSoMi.getMaSku());
            ct2.setDonGia(new BigDecimal("280000.00"));
            ct2.setSoLuong(2);
            ct2.setTongTien(new BigDecimal("560000.00"));
            chiTietDonHangRepository.save(ct2);

            // 3. US-42 & US-43: Dam bao Vi Nguoi Ban luon ton tai
            if (viNguoiBanRepository.count() == 0) {
                var listShops = gianHangRepository.findAll();
                for (GianHang shop : listShops) {
                    kyQuyService.getOrCreateViNguoiBan(shop);
                }
            }

            // 4. US-42 & US-43: Khoi tao Giao dich Ky Quy Escrow mau
            if (giaoDichKyQuyRepository.count() == 0) {
                var listShopOrders = donHangShopRepository.findAll();
                if (!listShopOrders.isEmpty()) {
                    DonHangShop shopOrder1 = listShopOrders.get(0);
                    kyQuyService.taoGiaoDichKyQuy(shopOrder1);

                    if (listShopOrders.size() > 1) {
                        DonHangShop shopOrder2 = listShopOrders.get(1);
                        GiaoDichKyQuy kq2 = kyQuyService.taoGiaoDichKyQuy(shopOrder2);
                        kq2.setNgayDuKienNhaTien(LocalDateTime.now().minusDays(1));
                        kq2.setNgayTao(LocalDateTime.now().minusDays(4));
                        giaoDichKyQuyRepository.save(kq2);
                    }

                    if (listShopOrders.size() > 2) {
                        DonHangShop shopOrder3 = listShopOrders.get(2);
                        kyQuyService.taoGiaoDichKyQuy(shopOrder3);
                    }
                }
            }

            System.out.println("=== Khoi tao du lieu mau hoan tat! ===");
        }
    }
}
