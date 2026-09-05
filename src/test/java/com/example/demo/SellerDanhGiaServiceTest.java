package com.example.demo;

import com.example.demo.dto.ThongKeDanhGiaSellerDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.DanhGiaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SellerDanhGiaServiceTest {

    @Autowired
    private DanhGiaService danhGiaService;

    @Autowired
    private DanhGiaSanPhamRepository danhGiaSanPhamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private DiaChiNguoiDungRepository diaChiNguoiDungRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    private NguoiDung sellerHopLe;
    private NguoiDung sellerKhac;
    private NguoiDung khachHang;
    private GianHang gianHangCuaSeller;
    private GianHang gianHangKhac;
    private SanPham sanPham;
    private DanhGiaSanPham danhGiaChuaPhanHoi;
    private DanhGiaSanPham danhGiaDaPhanHoi;
    private DanhGiaSanPham danhGiaBiAn;

    @BeforeEach
    void setUp() {
        // 1. Tạo Seller chính & Seller khác
        long timestamp = System.currentTimeMillis();
        sellerHopLe = new NguoiDung();
        sellerHopLe.setHoVaTen("Trần Minh Đức");
        sellerHopLe.setEmail("seller_techzone_" + timestamp + "@flexshop.vn");
        sellerHopLe.setSoDienThoai("09" + String.valueOf(timestamp).substring(5));
        sellerHopLe.setMatKhauMaHoa("$2a$10$hashPassword123");
        sellerHopLe.setTrangThai("HOAT_DONG");
        sellerHopLe.setDaXoa(false);
        sellerHopLe.setNgayTao(LocalDateTime.now());
        sellerHopLe = nguoiDungRepository.save(sellerHopLe);

        sellerKhac = new NguoiDung();
        sellerKhac.setHoVaTen("Lê Thu Hà");
        sellerKhac.setEmail("seller_khac_" + (timestamp + 1) + "@flexshop.vn");
        sellerKhac.setSoDienThoai("08" + String.valueOf(timestamp + 1).substring(5));
        sellerKhac.setMatKhauMaHoa("$2a$10$hashPassword123");
        sellerKhac.setTrangThai("HOAT_DONG");
        sellerKhac.setDaXoa(false);
        sellerKhac.setNgayTao(LocalDateTime.now());
        sellerKhac = nguoiDungRepository.save(sellerKhac);

        // 2. Tạo Khách hàng
        khachHang = new NguoiDung();
        khachHang.setHoVaTen("Nguyễn Văn Khách");
        khachHang.setEmail("khach_us50_" + (timestamp + 2) + "@flexshop.vn");
        khachHang.setSoDienThoai("07" + String.valueOf(timestamp + 2).substring(5));
        khachHang.setMatKhauMaHoa("$2a$10$hashPassword123");
        khachHang.setTrangThai("HOAT_DONG");
        khachHang.setDaXoa(false);
        khachHang.setNgayTao(LocalDateTime.now());
        khachHang = nguoiDungRepository.save(khachHang);

        // 3. Tạo Gian hàng cho Seller chính & Gian hàng của Seller khác
        gianHangCuaSeller = new GianHang();
        gianHangCuaSeller.setTenGianHang("TechZone Store " + System.currentTimeMillis());
        gianHangCuaSeller.setChuSoHuu(sellerHopLe);
        gianHangCuaSeller.setDuongDanSlug("techzone-store-" + System.currentTimeMillis());
        gianHangCuaSeller.setDiaChiKho("Hà Nội");
        gianHangCuaSeller.setSdtKho("0901112233");
        gianHangCuaSeller.setTrangThai("HOAT_DONG");
        gianHangCuaSeller.setDiemDanhGiaTb(BigDecimal.valueOf(5.0));
        gianHangCuaSeller.setTongDanhGia(0);
        gianHangCuaSeller.setNgayTao(LocalDateTime.now());
        gianHangCuaSeller = gianHangRepository.save(gianHangCuaSeller);

        gianHangKhac = new GianHang();
        gianHangKhac.setTenGianHang("Fashion Shop " + System.currentTimeMillis());
        gianHangKhac.setChuSoHuu(sellerKhac);
        gianHangKhac.setDuongDanSlug("fashion-shop-" + System.currentTimeMillis());
        gianHangKhac.setDiaChiKho("TP HCM");
        gianHangKhac.setSdtKho("0904445566");
        gianHangKhac.setTrangThai("HOAT_DONG");
        gianHangKhac.setDiemDanhGiaTb(BigDecimal.valueOf(5.0));
        gianHangKhac.setTongDanhGia(0);
        gianHangKhac.setNgayTao(LocalDateTime.now());
        gianHangKhac = gianHangRepository.save(gianHangKhac);

        // 4. Sản phẩm của Seller chính
        DanhMuc dm = danhMucRepository.findAll().stream().findFirst().orElseGet(() -> {
            DanhMuc d = new DanhMuc();
            d.setTenDanhMuc("Thiết bị công nghệ");
            d.setDuongDanSlug("thiet-bi-cong-nghe-" + System.currentTimeMillis());
            d.setDangHoatDong(true);
            return danhMucRepository.save(d);
        });

        sanPham = new SanPham();
        sanPham.setTenSanPham("Tai nghe Sony Test US50");
        sanPham.setGianHang(gianHangCuaSeller);
        sanPham.setDanhMuc(dm);
        sanPham.setDuongDanSlug("tai-nghe-sony-test-" + System.currentTimeMillis());
        sanPham.setGiaCoBan(BigDecimal.valueOf(1250000));
        sanPham.setDanhGiaTb(BigDecimal.valueOf(5.0));
        sanPham.setNgayTao(LocalDateTime.now());
        sanPham = sanPhamRepository.save(sanPham);

        // 4.1. Tạo Đơn hàng & Chi tiết đơn cho các đánh giá
        DiaChiNguoiDung dc = diaChiNguoiDungRepository.findAll().stream().findFirst().orElseGet(() -> {
            DiaChiNguoiDung d = new DiaChiNguoiDung();
            d.setNguoiDung(khachHang);
            d.setTenNguoiNhan("Khách Test");
            d.setSoDienThoai("09" + String.valueOf(timestamp + 5).substring(5));
            d.setTinhThanh("Hà Nội");
            d.setQuanHuyen("Cầu Giấy");
            d.setXaPhuong("Dịch Vọng");
            d.setDiaChiChiTiet("Số 10 Cầu Giấy");
            d.setLaMacDinh(true);
            return diaChiNguoiDungRepository.save(d);
        });

        DonHangTong dht = new DonHangTong();
        dht.setKhachHang(khachHang);
        dht.setDiaChiGiao(dc);
        dht.setMaCodeDonTong("DHT-US50-" + timestamp);
        dht.setTongTienHang(BigDecimal.valueOf(1500000));
        dht.setTongPhiVanChuyen(BigDecimal.valueOf(30000));
        dht.setTongThanhToanCuoi(BigDecimal.valueOf(1530000));
        dht.setPhuongThucThanhToan("COD");
        dht.setTrangThaiThanhToan("DA_THANH_TOAN");
        dht.setTrangThaiDonHang("HOAN_TAT");
        dht.setNgayTao(LocalDateTime.now());
        dht = donHangTongRepository.save(dht);

        DonHangShop dhs = new DonHangShop();
        dhs.setDonHangTong(dht);
        dhs.setGianHang(gianHangCuaSeller);
        dhs.setMaCodeDonShop("DHS-US50-" + timestamp);
        dhs.setTienHangShop(BigDecimal.valueOf(1500000));
        dhs.setPhiVanChuyen(BigDecimal.valueOf(30000));
        dhs.setTongTienShopNhan(BigDecimal.valueOf(1500000));
        dhs.setTrangThai("DA_GIAO");
        dhs.setNgayTao(LocalDateTime.now());
        dhs = donHangShopRepository.save(dhs);

        ChiTietDonHang ct1 = new ChiTietDonHang();
        ct1.setDonHangShop(dhs);
        ct1.setMaBienThe(1L);
        ct1.setTenSanPham(sanPham.getTenSanPham());
        ct1.setTenBienThe("Bản Chuẩn");
        ct1.setMaSku("SKU-US50-01-" + timestamp);
        ct1.setDonGia(BigDecimal.valueOf(500000));
        ct1.setSoLuong(1);
        ct1.setTongTien(BigDecimal.valueOf(500000));
        ct1 = chiTietDonHangRepository.save(ct1);

        ChiTietDonHang ct2 = new ChiTietDonHang();
        ct2.setDonHangShop(dhs);
        ct2.setMaBienThe(1L);
        ct2.setTenSanPham(sanPham.getTenSanPham());
        ct2.setTenBienThe("Bản Chuẩn");
        ct2.setMaSku("SKU-US50-02-" + timestamp);
        ct2.setDonGia(BigDecimal.valueOf(500000));
        ct2.setSoLuong(1);
        ct2.setTongTien(BigDecimal.valueOf(500000));
        ct2 = chiTietDonHangRepository.save(ct2);

        ChiTietDonHang ct3 = new ChiTietDonHang();
        ct3.setDonHangShop(dhs);
        ct3.setMaBienThe(1L);
        ct3.setTenSanPham(sanPham.getTenSanPham());
        ct3.setTenBienThe("Bản Chuẩn");
        ct3.setMaSku("SKU-US50-03-" + timestamp);
        ct3.setDonGia(BigDecimal.valueOf(500000));
        ct3.setSoLuong(1);
        ct3.setTongTien(BigDecimal.valueOf(500000));
        ct3 = chiTietDonHangRepository.save(ct3);

        // 5. Tạo đánh giá chưa phản hồi
        danhGiaChuaPhanHoi = new DanhGiaSanPham();
        danhGiaChuaPhanHoi.setChiTietDonHang(ct1);
        danhGiaChuaPhanHoi.setSanPham(sanPham);
        danhGiaChuaPhanHoi.setGianHang(gianHangCuaSeller);
        danhGiaChuaPhanHoi.setNguoiDung(khachHang);
        danhGiaChuaPhanHoi.setSoSao(5);
        danhGiaChuaPhanHoi.setNoiDung("Tai nghe nghe rất hay, chống ồn rất đỉnh!");
        danhGiaChuaPhanHoi.setAnDanh(false);
        danhGiaChuaPhanHoi.setBiAn(false);
        danhGiaChuaPhanHoi.setNgayTao(LocalDateTime.now().minusHours(2));
        danhGiaChuaPhanHoi = danhGiaSanPhamRepository.save(danhGiaChuaPhanHoi);

        // 6. Tạo đánh giá đã có phản hồi
        danhGiaDaPhanHoi = new DanhGiaSanPham();
        danhGiaDaPhanHoi.setChiTietDonHang(ct2);
        danhGiaDaPhanHoi.setSanPham(sanPham);
        danhGiaDaPhanHoi.setGianHang(gianHangCuaSeller);
        danhGiaDaPhanHoi.setNguoiDung(khachHang);
        danhGiaDaPhanHoi.setSoSao(4);
        danhGiaDaPhanHoi.setNoiDung("Chất âm tốt nhưng hộp đựng hơi to.");
        danhGiaDaPhanHoi.setAnDanh(false);
        danhGiaDaPhanHoi.setBiAn(false);
        danhGiaDaPhanHoi.setPhanHoiCuaShop("Shop cảm ơn bạn đã góp ý nhé!");
        danhGiaDaPhanHoi.setNgayShopPhanHoi(LocalDateTime.now().minusHours(1));
        danhGiaDaPhanHoi.setNgayTao(LocalDateTime.now().minusHours(3));
        danhGiaDaPhanHoi = danhGiaSanPhamRepository.save(danhGiaDaPhanHoi);

        // 7. Tạo đánh giá bị ẩn do vi phạm
        danhGiaBiAn = new DanhGiaSanPham();
        danhGiaBiAn.setChiTietDonHang(ct3);
        danhGiaBiAn.setSanPham(sanPham);
        danhGiaBiAn.setGianHang(gianHangCuaSeller);
        danhGiaBiAn.setNguoiDung(khachHang);
        danhGiaBiAn.setSoSao(1);
        danhGiaBiAn.setNoiDung("Nội dung xúc phạm sàn vi phạm chính sách.");
        danhGiaBiAn.setAnDanh(false);
        danhGiaBiAn.setBiAn(true);
        danhGiaBiAn.setNgayTao(LocalDateTime.now().minusDays(1));
        danhGiaBiAn = danhGiaSanPhamRepository.save(danhGiaBiAn);
    }

    @Test
    @DisplayName("US-50: Seller gửi phản hồi cho khách hàng thành công và lưu thời gian")
    void testSellerGuiPhanHoiThanhCong() {
        String noiDungPhanHoi = "Dạ TechZone cảm ơn quý khách đã tin tưởng và đánh giá 5 sao cho shop ạ!";
        
        DanhGiaSanPham saved = danhGiaService.sellerPhanHoiDanhGia(
                danhGiaChuaPhanHoi.getMaDanhGia(), noiDungPhanHoi, sellerHopLe.getMaNguoiDung()
        );

        assertNotNull(saved);
        assertEquals(noiDungPhanHoi, saved.getPhanHoiCuaShop());
        assertNotNull(saved.getNgayShopPhanHoi());
    }

    @Test
    @DisplayName("US-50: Seller cập nhật (chỉnh sửa) phản hồi đã gửi thành công")
    void testSellerCapNhatPhanHoiDaCo() {
        String phanHoiMoi = "Dạ Shop đã cập nhật mẫu hộp đựng nhỏ gọn hơn, cảm ơn bạn nhiều nhé!";

        DanhGiaSanPham updated = danhGiaService.sellerPhanHoiDanhGia(
                danhGiaDaPhanHoi.getMaDanhGia(), phanHoiMoi, sellerHopLe.getMaNguoiDung()
        );

        assertEquals(phanHoiMoi, updated.getPhanHoiCuaShop());
        assertNotNull(updated.getNgayShopPhanHoi());
    }

    @Test
    @DisplayName("US-50 Security: Chặn Seller phản hồi đánh giá của Gian hàng khác")
    void testChanSellerPhanHoiShopNguoiKhac() {
        SecurityException ex = assertThrows(SecurityException.class, () ->
                danhGiaService.sellerPhanHoiDanhGia(
                        danhGiaChuaPhanHoi.getMaDanhGia(), 
                        "Cố tình trả lời đánh giá của shop đối thủ.", 
                        sellerKhac.getMaNguoiDung()
                )
        );
        assertTrue(ex.getMessage().contains("không có quyền phản hồi"));
    }

    @Test
    @DisplayName("US-50 Validation: Chặn nội dung phản hồi rỗng hoặc chỉ có khoảng trắng")
    void testChanPhanHoiRongHoacToanKhoangTrang() {
        assertThrows(IllegalArgumentException.class, () ->
                danhGiaService.sellerPhanHoiDanhGia(
                        danhGiaChuaPhanHoi.getMaDanhGia(), "", sellerHopLe.getMaNguoiDung()
                )
        );

        assertThrows(IllegalArgumentException.class, () ->
                danhGiaService.sellerPhanHoiDanhGia(
                        danhGiaChuaPhanHoi.getMaDanhGia(), "    ", sellerHopLe.getMaNguoiDung()
                )
        );
    }

    @Test
    @DisplayName("US-50 Validation: Chặn nội dung phản hồi dưới 5 ký tự")
    void testChanPhanHoiDuoi5KyTu() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                danhGiaService.sellerPhanHoiDanhGia(
                        danhGiaChuaPhanHoi.getMaDanhGia(), "Tks!", sellerHopLe.getMaNguoiDung()
                )
        );
        assertTrue(ex.getMessage().contains("từ 5 đến 1.000 ký tự"));
    }

    @Test
    @DisplayName("US-50 Validation: Chặn nội dung phản hồi vượt quá 1000 ký tự")
    void testChanPhanHoiVuotQua1000KyTu() {
        String noiDungQuaDai = "A".repeat(1005);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                danhGiaService.sellerPhanHoiDanhGia(
                        danhGiaChuaPhanHoi.getMaDanhGia(), noiDungQuaDai, sellerHopLe.getMaNguoiDung()
                )
        );
        assertTrue(ex.getMessage().contains("không được vượt quá 1.000 ký tự"));
    }

    @Test
    @DisplayName("US-50 Validation: Chặn phản hồi trên đánh giá đã bị ẩn do vi phạm")
    void testChanPhanHoiDanhGiaBiAn() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                danhGiaService.sellerPhanHoiDanhGia(
                        danhGiaBiAn.getMaDanhGia(), "Shop giải trình vi phạm.", sellerHopLe.getMaNguoiDung()
                )
        );
        assertTrue(ex.getMessage().contains("đã bị ẩn"));
    }

    @Test
    @DisplayName("US-50 Thống kê: Tính toán chính xác KPI tỷ lệ phản hồi và số lượng chưa phản hồi")
    void testThongKeDanhGiaSeller() {
        ThongKeDanhGiaSellerDTO thongKe = danhGiaService.layThongKeDanhGiaChoSeller(gianHangCuaSeller.getMaGianHang());

        assertNotNull(thongKe);
        // Có 2 đánh giá không bị ẩn: 1 đã phản hồi, 1 chưa phản hồi
        assertEquals(2, thongKe.getTongDanhGia());
        assertEquals(1, thongKe.getSoLuongDaPhanHoi());
        assertEquals(1, thongKe.getSoLuongChuaPhanHoi());
        assertEquals(50, thongKe.getTyLePhanHoi()); // 1/2 = 50%
        assertEquals(1, thongKe.getSoLuong5Sao());
        assertEquals(1, thongKe.getSoLuong4Sao());
    }

    @Test
    @DisplayName("US-50 Lọc & Phân trang: Kiểm tra lọc theo trạng thái phản hồi và tìm kiếm từ khóa")
    void testTimKiemVaLocDanhGiaChoSeller() {
        // 1. Lọc theo trạng thái CHƯA PHẢN HỒI
        Page<DanhGiaSanPham> pageChuaPhanHoi = danhGiaService.layDanhSachDanhGiaChoSeller(
                gianHangCuaSeller.getMaGianHang(), null, "CHUA_PHAN_HOI", null, 0, 10
        );
        assertEquals(1, pageChuaPhanHoi.getTotalElements());
        assertEquals(danhGiaChuaPhanHoi.getMaDanhGia(), pageChuaPhanHoi.getContent().get(0).getMaDanhGia());

        // 2. Lọc theo trạng thái ĐÃ PHẢN HỒI
        Page<DanhGiaSanPham> pageDaPhanHoi = danhGiaService.layDanhSachDanhGiaChoSeller(
                gianHangCuaSeller.getMaGianHang(), null, "DA_PHAN_HOI", null, 0, 10
        );
        assertEquals(1, pageDaPhanHoi.getTotalElements());
        assertEquals(danhGiaDaPhanHoi.getMaDanhGia(), pageDaPhanHoi.getContent().get(0).getMaDanhGia());

        // 3. Tìm kiếm theo từ khóa nhận xét
        Page<DanhGiaSanPham> pageTimKiem = danhGiaService.layDanhSachDanhGiaChoSeller(
                gianHangCuaSeller.getMaGianHang(), null, null, "chống ồn", 0, 10
        );
        assertEquals(1, pageTimKiem.getTotalElements());
    }
}
