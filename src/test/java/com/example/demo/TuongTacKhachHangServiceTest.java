package com.example.demo;

import com.example.demo.dto.ThongKeHoiDapSanPhamDTO;
import com.example.demo.dto.ThongKeTuongTacKhachHangDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.TuongTacKhachHangService;
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
public class TuongTacKhachHangServiceTest {

    @Autowired
    private TuongTacKhachHangService tuongTacKhachHangService;

    @Autowired
    private SanPhamYeuThichRepository sanPhamYeuThichRepository;

    @Autowired
    private TheoDoiGianHangRepository theoDoiGianHangRepository;

    @Autowired
    private HoiDapSanPhamRepository hoiDapSanPhamRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DanhMucRepository danhMucRepository;

    private NguoiDung khachHang;
    private NguoiDung seller;
    private GianHang gianHang;
    private SanPham sanPham;
    private DanhMuc danhMuc;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();

        // 1. Tạo Khách hàng
        khachHang = new NguoiDung();
        khachHang.setHoVaTen("Nguyễn Văn An US51");
        khachHang.setEmail("khach_us51_" + timestamp + "@flexshop.vn");
        khachHang.setSoDienThoai("09" + String.valueOf(timestamp).substring(5));
        khachHang.setMatKhauMaHoa("$2a$10$hashPassword123");
        khachHang.setTrangThai("HOAT_DONG");
        khachHang.setDaXoa(false);
        khachHang.setNgayTao(LocalDateTime.now());
        khachHang = nguoiDungRepository.save(khachHang);

        // 2. Tạo Seller
        seller = new NguoiDung();
        seller.setHoVaTen("Trần Minh Đức US51");
        seller.setEmail("seller_us51_" + (timestamp + 1) + "@flexshop.vn");
        seller.setSoDienThoai("08" + String.valueOf(timestamp + 1).substring(5));
        seller.setMatKhauMaHoa("$2a$10$hashPassword123");
        seller.setTrangThai("HOAT_DONG");
        seller.setDaXoa(false);
        seller.setNgayTao(LocalDateTime.now());
        seller = nguoiDungRepository.save(seller);

        // 3. Tạo Gian hàng thuộc Seller
        gianHang = new GianHang();
        gianHang.setTenGianHang("TechZone Flagship " + timestamp);
        gianHang.setChuSoHuu(seller);
        gianHang.setDuongDanSlug("techzone-flagship-" + timestamp);
        gianHang.setDiaChiKho("Hà Nội");
        gianHang.setSdtKho("0901234567");
        gianHang.setTrangThai("DA_DUYET");
        gianHang.setDiemDanhGiaTb(BigDecimal.valueOf(5.0));
        gianHang.setTongDanhGia(0);
        gianHang.setNgayTao(LocalDateTime.now());
        gianHang = gianHangRepository.save(gianHang);

        // 4. Tạo Danh mục & Sản phẩm
        danhMuc = danhMucRepository.findAll().stream().findFirst().orElseGet(() -> {
            DanhMuc d = new DanhMuc();
            d.setTenDanhMuc("Thiết bị công nghệ");
            d.setDuongDanSlug("thiet-bi-cong-nghe-" + timestamp);
            d.setDangHoatDong(true);
            return danhMucRepository.save(d);
        });

        sanPham = new SanPham();
        sanPham.setTenSanPham("Tai nghe không dây Bluetooth chống ồn Sony US51");
        sanPham.setGianHang(gianHang);
        sanPham.setDanhMuc(danhMuc);
        sanPham.setDuongDanSlug("tai-nghe-sony-us51-" + timestamp);
        sanPham.setGiaCoBan(BigDecimal.valueOf(1250000));
        sanPham.setDanhGiaTb(BigDecimal.valueOf(5.0));
        sanPham.setTrangThai("HOAT_DONG");
        sanPham.setDaXoa(false);
        sanPham.setBiKhoa(false);
        sanPham.setNgayTao(LocalDateTime.now());
        sanPham = sanPhamRepository.save(sanPham);
    }

    // =========================================================================
    // 1. WISHLIST TESTS
    // =========================================================================

    @Test
    @DisplayName("US-51: Khách hàng thêm sản phẩm vào danh sách Yêu thích (Wishlist) thành công")
    void testThemVaoWishlistThanhCong() {
        SanPhamYeuThich yt = tuongTacKhachHangService.themVaoYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham());

        assertNotNull(yt);
        assertTrue(tuongTacKhachHangService.kiemTraDaYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham()));
        assertEquals(1, tuongTacKhachHangService.demLuotYeuThichSanPham(sanPham.getMaSanPham()));
    }

    @Test
    @DisplayName("US-51: Bật/Tắt (Toggle) Wishlist chính xác khi bấm nhiều lần")
    void testToggleWishlist() {
        // Lần 1: Thêm vào Wishlist
        boolean ketQua1 = tuongTacKhachHangService.toggleYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham());
        assertTrue(ketQua1);
        assertTrue(tuongTacKhachHangService.kiemTraDaYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham()));

        // Lần 2: Bỏ khỏi Wishlist
        boolean ketQua2 = tuongTacKhachHangService.toggleYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham());
        assertFalse(ketQua2);
        assertFalse(tuongTacKhachHangService.kiemTraDaYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham()));
    }

    @Test
    @DisplayName("US-51: Chặn lưu sản phẩm đã bị xóa hoặc tạm khóa vào Wishlist")
    void testChanThemWishlistSanPhamDaXoaHoacKhoa() {
        sanPham.setBiKhoa(true);
        sanPhamRepository.save(sanPham);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                tuongTacKhachHangService.themVaoYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham())
        );
        assertTrue(ex.getMessage().contains("Không thể lưu sản phẩm đã bị xóa hoặc tạm khóa"));
    }

    @Test
    @DisplayName("US-51: Xóa sản phẩm khỏi danh sách yêu thích và phân trang tìm kiếm")
    void testXoaVaTimKiemWishlist() {
        tuongTacKhachHangService.themVaoYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham());

        Page<SanPhamYeuThich> page = tuongTacKhachHangService.layDanhSachWishlist(
                khachHang.getMaNguoiDung(), null, "Sony", 0, 10
        );
        assertEquals(1, page.getTotalElements());

        tuongTacKhachHangService.xoaKhoiYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham());
        assertFalse(tuongTacKhachHangService.kiemTraDaYeuThich(khachHang.getMaNguoiDung(), sanPham.getMaSanPham()));
    }

    // =========================================================================
    // 2. FOLLOW SHOP TESTS
    // =========================================================================

    @Test
    @DisplayName("US-51: Khách hàng theo dõi gian hàng thành công")
    void testTheoDoiGianHangThanhCong() {
        TheoDoiGianHang td = tuongTacKhachHangService.theoDoiGianHang(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        assertNotNull(td);
        assertTrue(tuongTacKhachHangService.kiemTraDaTheoDoi(khachHang.getMaNguoiDung(), gianHang.getMaGianHang()));
        assertEquals(1, tuongTacKhachHangService.demLuotTheoDoiGianHang(gianHang.getMaGianHang()));
    }

    @Test
    @DisplayName("US-51 Security: Chặn chủ sở hữu gian hàng tự theo dõi chính shop của mình")
    void testChanChuShopTuTheoDoiChinhMinh() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tuongTacKhachHangService.theoDoiGianHang(seller.getMaNguoiDung(), gianHang.getMaGianHang())
        );
        assertTrue(ex.getMessage().contains("không thể tự theo dõi gian hàng của chính mình"));
    }

    @Test
    @DisplayName("US-51: Bật/Tắt (Toggle) Theo dõi gian hàng chính xác")
    void testToggleTheoDoiGianHang() {
        // Lần 1: Follow
        boolean ketQua1 = tuongTacKhachHangService.toggleTheoDoiGianHang(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());
        assertTrue(ketQua1);
        assertTrue(tuongTacKhachHangService.kiemTraDaTheoDoi(khachHang.getMaNguoiDung(), gianHang.getMaGianHang()));

        // Lần 2: Unfollow
        boolean ketQua2 = tuongTacKhachHangService.toggleTheoDoiGianHang(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());
        assertFalse(ketQua2);
        assertFalse(tuongTacKhachHangService.kiemTraDaTheoDoi(khachHang.getMaNguoiDung(), gianHang.getMaGianHang()));
    }

    // =========================================================================
    // 3. Q&A (HỎI - ĐÁP) TESTS
    // =========================================================================

    @Test
    @DisplayName("US-51: Khách hàng đặt câu hỏi về sản phẩm thành công")
    void testDatCauHoiThanhCong() {
        String noiDung = "Tai nghe này có hỗ trợ chống ồn chủ động ANC tốt không shop?";
        HoiDapSanPham hd = tuongTacKhachHangService.datCauHoi(sanPham.getMaSanPham(), khachHang.getMaNguoiDung(), noiDung);

        assertNotNull(hd);
        assertNotNull(hd.getMaHoiDap());
        assertEquals(noiDung, hd.getCauHoi());
        assertNull(hd.getCauTraLoi());
        assertFalse(hd.isDaTraLoi());
        assertNotNull(hd.getNgayHoi());
    }

    @Test
    @DisplayName("US-51 Validation: Chặn câu hỏi dưới 10 ký tự hoặc vượt quá 1000 ký tự")
    void testValidateDoDaiCauHoi() {
        // Chặn câu hỏi quá ngắn (< 10 ký tự)
        IllegalArgumentException exShort = assertThrows(IllegalArgumentException.class, () ->
                tuongTacKhachHangService.datCauHoi(sanPham.getMaSanPham(), khachHang.getMaNguoiDung(), "Có tốt?")
        );
        assertTrue(exShort.getMessage().contains("ít nhất 10 ký tự"));

        // Chặn câu hỏi quá dài (> 1000 ký tự)
        String cauHoiDai = "A".repeat(1005);
        IllegalArgumentException exLong = assertThrows(IllegalArgumentException.class, () ->
                tuongTacKhachHangService.datCauHoi(sanPham.getMaSanPham(), khachHang.getMaNguoiDung(), cauHoiDai)
        );
        assertTrue(exLong.getMessage().contains("không được vượt quá 1.000 ký tự"));
    }

    @Test
    @DisplayName("US-51: Người bán (Seller) trả lời câu hỏi và nhận diện đúng nhãn Chủ Shop")
    void testTraLoiCauHoiBoiNguoiBan() {
        HoiDapSanPham hd = tuongTacKhachHangService.datCauHoi(
                sanPham.getMaSanPham(), khachHang.getMaNguoiDung(), 
                "Tai nghe này dùng được mấy ngày cho 1 lần sạc đầy ạ?"
        );

        String cauTraLoi = "Dạ tai nghe dùng được 30 tiếng liên tục khi bật chống ồn, tương đương khoảng 4-5 ngày sử dụng bạn nhé!";
        HoiDapSanPham daTraLoi = tuongTacKhachHangService.traLoiCauHoi(hd.getMaHoiDap(), seller.getMaNguoiDung(), cauTraLoi);

        assertTrue(daTraLoi.isDaTraLoi());
        assertEquals(cauTraLoi, daTraLoi.getCauTraLoi());
        assertNotNull(daTraLoi.getNgayTraLoi());
        assertTrue(daTraLoi.isNguoiBanTraLoi()); // Nhận diện chính xác Chủ Shop phản hồi
    }

    @Test
    @DisplayName("US-51: Thống kê và lọc Q&A theo trạng thái, từ khóa tiếng Việt")
    void testThongKeVaLocHoiDap() {
        // Tạo 1 câu hỏi đã trả lời
        HoiDapSanPham hd1 = tuongTacKhachHangService.datCauHoi(
                sanPham.getMaSanPham(), khachHang.getMaNguoiDung(), 
                "Sản phẩm có kèm dây cắm 3.5mm không shop?"
        );
        tuongTacKhachHangService.traLoiCauHoi(hd1.getMaHoiDap(), seller.getMaNguoiDung(), "Dạ có kèm đầy đủ dây 3.5mm trong hộp ạ!");

        // Tạo 1 câu hỏi chưa trả lời
        tuongTacKhachHangService.datCauHoi(
                sanPham.getMaSanPham(), khachHang.getMaNguoiDung(), 
                "Bảo hành chính hãng tại trung tâm Sony đúng không ạ?"
        );

        // Kiểm tra thống kê
        ThongKeHoiDapSanPhamDTO tk = tuongTacKhachHangService.layThongKeHoiDap(sanPham.getMaSanPham());
        assertEquals(2, tk.getTongSoCauHoi());
        assertEquals(1, tk.getSoCauHoiDaTraLoi());
        assertEquals(1, tk.getSoCauHoiChoTraLoi());
        assertEquals(50, tk.getTyLeDaTraLoi()); // 1/2 = 50%

        // Lọc trạng thái ĐÃ TRẢ LỜI
        Page<HoiDapSanPham> pageDaTraLoi = tuongTacKhachHangService.layDanhSachHoiDapChoSanPham(
                sanPham.getMaSanPham(), "DA_TRA_LOI", null, 0, 10
        );
        assertEquals(1, pageDaTraLoi.getTotalElements());

        // Tìm kiếm theo từ khóa tiếng Việt có dấu
        Page<HoiDapSanPham> pageTimKiem = tuongTacKhachHangService.layDanhSachHoiDapChoSanPham(
                sanPham.getMaSanPham(), null, "dây cắm", 0, 10
        );
        assertEquals(1, pageTimKiem.getTotalElements());

        // Kiểm tra thống kê tương tác khách hàng
        ThongKeTuongTacKhachHangDTO tkKhach = tuongTacKhachHangService.layThongKeTuongTacKhachHang(khachHang.getMaNguoiDung());
        assertEquals(2, tkKhach.getTongSoCauHoiDaDat());
    }
}
