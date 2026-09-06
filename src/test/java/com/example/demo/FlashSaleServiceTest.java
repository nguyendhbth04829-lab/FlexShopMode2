package com.example.demo;

import com.example.demo.dto.DangKyFlashSaleForm;
import com.example.demo.dto.KhungGioFlashSaleForm;
import com.example.demo.dto.ThongKeFlashSaleDTO;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.KhungGioFlashSale;
import com.example.demo.entity.SanPhamFlashSale;
import com.example.demo.repository.BienTheSanPhamRepository;
import com.example.demo.repository.KhungGioFlashSaleRepository;
import com.example.demo.repository.SanPhamFlashSaleRepository;
import com.example.demo.service.FlashSaleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho US-53 (Module: PROMOTION):
 * Quản lý Khung Giờ Flash Sale (0h, 12h, 21h), Giá Giảm Sốc, Giới Hạn Số Lượng & Đếm Ngược.
 */
@SpringBootTest
public class FlashSaleServiceTest {

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private KhungGioFlashSaleRepository khungGioFlashSaleRepository;

    @Autowired
    private SanPhamFlashSaleRepository sanPhamFlashSaleRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Test
    @Transactional
    @DisplayName("Test 1: Tạo thành công các khung giờ Flash Sale chuẩn (0h, 12h, 21h)")
    public void testTaoKhungGioChuan0h12h21h() {
        LocalDate testDate = LocalDate.now().plusDays(10); // Chọn ngày tương lai để không trùng lặp

        // Tạo mốc 0h
        KhungGioFlashSale slot0h = flashSaleService.taoKhungGioChuan("0H", testDate);
        assertNotNull(slot0h.getMaFlashSale());
        assertTrue(slot0h.getTieuDe().contains("0h"));
        assertEquals(0, slot0h.getThoiGianBatDau().getHour());
        assertEquals(2, slot0h.getThoiGianKetThuc().getHour());

        // Tạo mốc 12h
        KhungGioFlashSale slot12h = flashSaleService.taoKhungGioChuan("12H", testDate);
        assertNotNull(slot12h.getMaFlashSale());
        assertTrue(slot12h.getTieuDe().contains("12h"));
        assertEquals(12, slot12h.getThoiGianBatDau().getHour());
        assertEquals(14, slot12h.getThoiGianKetThuc().getHour());

        // Tạo mốc 21h
        KhungGioFlashSale slot21h = flashSaleService.taoKhungGioChuan("21H", testDate);
        assertNotNull(slot21h.getMaFlashSale());
        assertTrue(slot21h.getTieuDe().contains("21h"));
        assertEquals(21, slot21h.getThoiGianBatDau().getHour());
        assertEquals(23, slot21h.getThoiGianKetThuc().getHour());
    }

    @Test
    @DisplayName("Test 2: Validate chặn tạo khung giờ có thời gian kết thúc trước hoặc bằng thời gian bắt đầu")
    public void testValidateThoiGianKetThucTruocBatDau() {
        KhungGioFlashSaleForm form = new KhungGioFlashSaleForm();
        form.setTieuDe("Khung giờ lỗi thời gian");
        LocalDateTime start = LocalDateTime.now().plusDays(15).withHour(10).withMinute(0);
        form.setThoiGianBatDau(start);
        form.setThoiGianKetThuc(start.minusHours(1)); // Kết thúc trước bắt đầu!

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            flashSaleService.taoKhungGio(form);
        });

        assertTrue(ex.getMessage().contains("Thời gian kết thúc phải sau thời gian bắt đầu"));
    }

    @Test
    @Transactional
    @DisplayName("Test 3: Validate chặn tạo khung giờ bị chồng chéo (overlap) thời gian")
    public void testValidateChongCheoThoiGian() {
        LocalDate testDate = LocalDate.now().plusDays(20);

        // Tạo khung giờ 1: 08:00 - 10:00
        KhungGioFlashSaleForm form1 = new KhungGioFlashSaleForm();
        form1.setTieuDe("Khung Giờ Buổi Sáng");
        form1.setThoiGianBatDau(testDate.atTime(8, 0));
        form1.setThoiGianKetThuc(testDate.atTime(10, 0));
        flashSaleService.taoKhungGio(form1);

        // Tạo khung giờ 2 bị chồng lấn: 09:00 - 11:00
        KhungGioFlashSaleForm form2 = new KhungGioFlashSaleForm();
        form2.setTieuDe("Khung Giờ Chồng Chéo");
        form2.setThoiGianBatDau(testDate.atTime(9, 0));
        form2.setThoiGianKetThuc(testDate.atTime(11, 0));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            flashSaleService.taoKhungGio(form2);
        });

        assertTrue(ex.getMessage().contains("trùng lặp hoặc chồng chéo"));
    }

    @Test
    @Transactional
    @DisplayName("Test 4: Đăng ký sản phẩm Flash Sale thành công và tính đúng tỷ lệ % giảm giá")
    public void testDangKySanPhamFlashSaleThanhCong() {
        // Tạo khung giờ mới
        LocalDate testDate = LocalDate.now().plusDays(25);
        KhungGioFlashSale slot = flashSaleService.taoKhungGioChuan("12H", testDate);

        // Lấy biến thể sản phẩm 1 (Tai nghe Sony, giá bán 1.250.000đ)
        BienTheSanPham bt = bienTheSanPhamRepository.findById(1L).orElseThrow();

        DangKyFlashSaleForm form = new DangKyFlashSaleForm();
        form.setMaFlashSale(slot.getMaFlashSale());
        form.setMaBienThe(1L);
        form.setGiaFlashSale(new BigDecimal("799000.00")); // Giảm sốc
        form.setSoLuongGioiHan(50);
        form.setGioiHanMuaMoiKhach(1);

        SanPhamFlashSale sp = flashSaleService.dangKySanPhamFlashSale(form, null, true);

        assertNotNull(sp.getMaSanPhamFs());
        assertEquals(new BigDecimal("799000.00"), sp.getGiaFlashSale());
        assertEquals(50, sp.getSoLuongGioiHan());
        assertEquals(0, sp.getSoLuongDaBan());
        assertEquals(1, sp.getGioiHanMuaMoiKhach());

        // Kiểm tra tính tỷ lệ giảm giá: (1.250.000 - 799.000) / 1.250.000 = 36%
        assertEquals(36, sp.getPhanTramGiamGia(), "Tỷ lệ giảm giá phải tính chính xác 36%");
        assertFalse(sp.isChayHang());
        assertEquals(50, sp.getSoLuongConLai());
    }

    @Test
    @Transactional
    @DisplayName("Test 5: Validate chặn giá Flash Sale lớn hơn hoặc bằng giá niêm yết hiện tại")
    public void testValidateGiaFlashSalePhaiNhoHonGiaGoc() {
        LocalDate testDate = LocalDate.now().plusDays(30);
        KhungGioFlashSale slot = flashSaleService.taoKhungGioChuan("12H", testDate);

        BienTheSanPham bt = bienTheSanPhamRepository.findById(1L).orElseThrow(); // Giá gốc 1.250.000đ

        DangKyFlashSaleForm form = new DangKyFlashSaleForm();
        form.setMaFlashSale(slot.getMaFlashSale());
        form.setMaBienThe(1L);
        form.setGiaFlashSale(new BigDecimal("1300000.00")); // Lớn hơn giá gốc!
        form.setSoLuongGioiHan(20);
        form.setGioiHanMuaMoiKhach(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            flashSaleService.dangKySanPhamFlashSale(form, null, true);
        });

        assertTrue(ex.getMessage().contains("phải nhỏ hơn giá bán niêm yết"));
    }

    @Test
    @Transactional
    @DisplayName("Test 6: Validate chặn giá Flash Sale giảm chưa đủ 5% (không đạt tiêu chí giảm sốc)")
    public void testValidateGiaGiamToiThieu5PhanTram() {
        LocalDate testDate = LocalDate.now().plusDays(35);
        KhungGioFlashSale slot = flashSaleService.taoKhungGioChuan("12H", testDate);

        // Giá gốc 1.250.000đ, chỉ giảm 1.000đ xuống 1.249.000đ (chưa tới 5%)
        DangKyFlashSaleForm form = new DangKyFlashSaleForm();
        form.setMaFlashSale(slot.getMaFlashSale());
        form.setMaBienThe(1L);
        form.setGiaFlashSale(new BigDecimal("1249000.00"));
        form.setSoLuongGioiHan(20);
        form.setGioiHanMuaMoiKhach(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            flashSaleService.dangKySanPhamFlashSale(form, null, true);
        });

        assertTrue(ex.getMessage().contains("phải giảm tối thiểu 5%"));
    }

    @Test
    @Transactional
    @DisplayName("Test 7: Validate chặn đăng ký biến thể sản phẩm bị trùng lặp trong cùng 1 khung giờ")
    public void testValidateChongDangKyTrungBienThe() {
        LocalDate testDate = LocalDate.now().plusDays(40);
        KhungGioFlashSale slot = flashSaleService.taoKhungGioChuan("12H", testDate);

        DangKyFlashSaleForm form1 = new DangKyFlashSaleForm();
        form1.setMaFlashSale(slot.getMaFlashSale());
        form1.setMaBienThe(1L);
        form1.setGiaFlashSale(new BigDecimal("800000.00"));
        form1.setSoLuongGioiHan(10);
        form1.setGioiHanMuaMoiKhach(1);
        flashSaleService.dangKySanPhamFlashSale(form1, null, true);

        // Đăng ký lại biến thể 1 vào cùng khung giờ
        DangKyFlashSaleForm form2 = new DangKyFlashSaleForm();
        form2.setMaFlashSale(slot.getMaFlashSale());
        form2.setMaBienThe(1L);
        form2.setGiaFlashSale(new BigDecimal("750000.00"));
        form2.setSoLuongGioiHan(15);
        form2.setGioiHanMuaMoiKhach(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            flashSaleService.dangKySanPhamFlashSale(form2, null, true);
        });

        assertTrue(ex.getMessage().contains("đã được đăng ký trong khung giờ Flash Sale rồi"));
    }

    @Test
    @Transactional
    @DisplayName("Test 8: Bảo mật - Chặn Seller đăng ký sản phẩm thuộc gian hàng khác")
    public void testBaoMatSellerChiDangKySanPhamCuaShopMinh() {
        LocalDate testDate = LocalDate.now().plusDays(45);
        KhungGioFlashSale slot = flashSaleService.taoKhungGioChuan("12H", testDate);

        // Biến thể 1 thuộc Shop 1 (TechZone), nhưng Seller của Shop 2 cố tình đăng ký
        DangKyFlashSaleForm form = new DangKyFlashSaleForm();
        form.setMaFlashSale(slot.getMaFlashSale());
        form.setMaBienThe(1L);
        form.setGiaFlashSale(new BigDecimal("800000.00"));
        form.setSoLuongGioiHan(10);
        form.setGioiHanMuaMoiKhach(1);

        Long maShopCuaSeller2 = 2L;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            flashSaleService.dangKySanPhamFlashSale(form, maShopCuaSeller2, false);
        });

        assertTrue(ex.getMessage().contains("chỉ được phép đăng ký Flash Sale cho sản phẩm thuộc sở hữu của gian hàng mình"));
    }

    @Test
    @Transactional
    @DisplayName("Test 9: Ghi nhận bán hàng Flash Sale, tăng soLuongDaBan và chặn mua vượt giới hạn")
    public void testGhiNhanBanHangFlashSaleVaKiemTraGioiHan() {
        KhungGioFlashSale liveSlot = flashSaleService.layKhungGioHienTaiHoacGanNhat();
        assertNotNull(liveSlot, "Cần có khung giờ Flash Sale");
        assertTrue(liveSlot.isDangDienRa(), "Khung giờ phải đang diễn ra");

        List<SanPhamFlashSale> spList = sanPhamFlashSaleRepository.findByKhungGioFlashSale_MaFlashSale(liveSlot.getMaFlashSale());
        assertFalse(spList.isEmpty(), "Khung giờ phải có sản phẩm");
        SanPhamFlashSale sp = spList.get(0);
        int banBanDau = (sp.getSoLuongDaBan() != null) ? sp.getSoLuongDaBan() : 0;

        // Mua 1 cái hợp lệ (trong giới hạn mua mỗi khách)
        flashSaleService.ghiNhanBanHangFlashSale(sp.getMaSanPhamFs(), 1, 4L);

        SanPhamFlashSale spSauMua = sanPhamFlashSaleRepository.findById(sp.getMaSanPhamFs()).orElseThrow();
        assertEquals(banBanDau + 1, spSauMua.getSoLuongDaBan());

        // Thử mua số lượng vượt quá giới hạn mỗi khách -> Phải bị chặn
        int vuotGioiHan = sp.getGioiHanMuaMoiKhach() + 1;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            flashSaleService.ghiNhanBanHangFlashSale(sp.getMaSanPhamFs(), vuotGioiHan, 4L);
        });

        assertTrue(ex.getMessage().contains("Vượt quá giới hạn mua tối đa"));
    }

    @Test
    @DisplayName("Test 10: Thống kê Flash Sale tổng quan chính xác")
    public void testThongKeFlashSale() {
        ThongKeFlashSaleDTO thongKe = flashSaleService.layThongKeFlashSale();

        assertNotNull(thongKe);
        assertTrue(thongKe.getTongKhungGio() > 0, "Phải có ít nhất 1 khung giờ trong hệ thống");
        assertTrue(thongKe.getTongSanPhamThamGia() >= 0);
        assertNotNull(thongKe.getTongDoanhThuFlashSale());
    }

    @Test
    @Transactional
    @DisplayName("Test 11: Tạo khung giờ chọn ảnh banner từ máy tính thành công, lưu file và đồng bộ đường dẫn vào CSDL")
    public void testTaoKhungGioChonAnhBannerTuMayTinhThanhCong() {
        LocalDate testDate = LocalDate.now().plusDays(25);
        LocalDateTime start = LocalDateTime.of(testDate, java.time.LocalTime.of(8, 0));
        LocalDateTime end = LocalDateTime.of(testDate, java.time.LocalTime.of(11, 0));

        KhungGioFlashSaleForm form = new KhungGioFlashSaleForm();
        form.setTieuDe("⚡ Flash Sale Siêu Sale Cuối Tháng Có Banner Tải Từ Máy");
        form.setThoiGianBatDau(start);
        form.setThoiGianKetThuc(end);

        // Giả lập tệp ảnh banner tải từ máy tính của người dùng
        org.springframework.mock.web.MockMultipartFile mockFile = new org.springframework.mock.web.MockMultipartFile(
                "tepAnhBanner",
                "my_custom_banner.png",
                "image/png",
                "Dữ liệu mẫu ảnh banner flash sale từ máy tính".getBytes()
        );
        form.setTepAnhBanner(mockFile);

        KhungGioFlashSale slot = flashSaleService.taoKhungGio(form);

        assertNotNull(slot.getMaFlashSale());
        assertNotNull(slot.getLinkBanner(), "Đường dẫn banner không được null");
        assertTrue(slot.getLinkBanner().startsWith("/uploads/flash-sale/banner_"), "Đường dẫn banner phải lưu trong thư mục /uploads/flash-sale/");
        assertTrue(slot.getLinkBanner().endsWith(".png"), "Phần mở rộng phải giữ nguyên là .png");

        // Kiểm tra tệp tin thực sự đã được tạo trên ổ đĩa
        String relativePath = slot.getLinkBanner().replace("/uploads/", "uploads/");
        assertTrue(java.nio.file.Files.exists(java.nio.file.Paths.get(relativePath)), "File ảnh banner phải tồn tại trên hệ thống tệp");
    }

    @Test
    @DisplayName("Test 12: Validate chặn tệp tin ảnh banner không hợp lệ (.exe)")
    public void testValidateChanTepAnhBannerKhongHopLe() {
        LocalDate testDate = LocalDate.now().plusDays(26);
        LocalDateTime start = LocalDateTime.of(testDate, java.time.LocalTime.of(8, 0));
        LocalDateTime end = LocalDateTime.of(testDate, java.time.LocalTime.of(11, 0));

        KhungGioFlashSaleForm form = new KhungGioFlashSaleForm();
        form.setTieuDe("⚡ Flash Sale Test Banner Độc Hại");
        form.setThoiGianBatDau(start);
        form.setThoiGianKetThuc(end);

        // Giả lập tệp tin không phải ảnh (.exe)
        org.springframework.mock.web.MockMultipartFile mockBadFile = new org.springframework.mock.web.MockMultipartFile(
                "tepAnhBanner",
                "virus_banner.exe",
                "application/octet-stream",
                "malicious bytes".getBytes()
        );
        form.setTepAnhBanner(mockBadFile);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            flashSaleService.taoKhungGio(form);
        });

        assertTrue(ex.getMessage().contains("Định dạng ảnh không hợp lệ"));
    }

    @Test
    @Transactional
    @DisplayName("Test 13: Tạo khung giờ mới xuất hiện ngay đầu bảng danh sách và xóa khung giờ thành công")
    public void testTaoKhungGioHienThiDauBangVaXoaThanhCong() {
        LocalDate testDate = LocalDate.now().plusDays(30);
        KhungGioFlashSaleForm form = new KhungGioFlashSaleForm();
        form.setTieuDe("⚡ Flash Sale Mới Nhất Phải Lên Đầu Bảng");
        form.setThoiGianBatDau(testDate.atTime(15, 0));
        form.setThoiGianKetThuc(testDate.atTime(17, 0));

        KhungGioFlashSale slotMoi = flashSaleService.taoKhungGio(form);
        assertNotNull(slotMoi.getMaFlashSale());

        // Kiểm tra danh sách phân trang: slot mới tạo phải nằm ở vị trí đầu tiên (Index 0)
        org.springframework.data.domain.Page<KhungGioFlashSale> page = flashSaleService.layDanhSachKhungGio("TAT_CA", null, 0, 20);
        assertFalse(page.getContent().isEmpty());
        assertEquals(slotMoi.getMaFlashSale(), page.getContent().get(0).getMaFlashSale(),
                "Khung giờ mới tạo có ID lớn nhất phải nằm ngay dòng đầu tiên của bảng");

        // Xóa khung giờ
        flashSaleService.xoaKhungGio(slotMoi.getMaFlashSale());
        assertFalse(khungGioFlashSaleRepository.findById(slotMoi.getMaFlashSale()).isPresent(),
                "Khung giờ sau khi xóa không còn tồn tại trong CSDL");
    }

    @Test
    @Transactional
    @DisplayName("Test 14: Khung giờ mới tạo cho ngày mai hoặc tương lai phải xuất hiện trong danh sách hiển thị cho khách hàng")
    public void testKhungGioMoiHienThiTrenGiaoDienKhachHang() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        KhungGioFlashSaleForm form = new KhungGioFlashSaleForm();
        form.setTieuDe("⚡ Flash Sale Ngày Mai Mở Bán Cho Khách");
        form.setThoiGianBatDau(tomorrow.atTime(14, 0));
        form.setThoiGianKetThuc(tomorrow.atTime(16, 0));

        KhungGioFlashSale slotMoi = flashSaleService.taoKhungGio(form);
        assertNotNull(slotMoi.getMaFlashSale());

        // Lấy danh sách khung giờ dành cho khách hàng
        List<KhungGioFlashSale> dsChoKhach = flashSaleService.layDanhSachKhungGioChoKhachHang();
        assertFalse(dsChoKhach.isEmpty());

        boolean daCoTrongDanhSach = dsChoKhach.stream()
                .anyMatch(k -> k.getMaFlashSale().equals(slotMoi.getMaFlashSale()));
        assertTrue(daCoTrongDanhSach, "Khung giờ mới tạo cho ngày mai phải xuất hiện trong danh sách hiển thị cho khách hàng (/flash-sale)");
    }

    @Test
    @DisplayName("Test 15: Nhãn hiển thị thanh Tab (Tab Label) chuẩn hệ 24H (HH:mm, Mai HH:mm, dd/MM HH:mm)")
    public void testNhanThoiGianTabChuan24H() {
        LocalDate today = LocalDate.now();
        KhungGioFlashSale slotHomNay = new KhungGioFlashSale();
        slotHomNay.setThoiGianBatDau(today.atTime(21, 0));
        assertEquals("21:00", slotHomNay.getNhanThoiGianTab(), "Khung giờ hôm nay phải hiển thị chuẩn 24h không AM/PM (21:00)");

        KhungGioFlashSale slotNgayMai = new KhungGioFlashSale();
        slotNgayMai.setThoiGianBatDau(today.plusDays(1).atTime(9, 30));
        assertEquals("Mai 09:30", slotNgayMai.getNhanThoiGianTab(), "Khung giờ ngày mai phải có tiền tố Mai và giờ 24h (Mai 09:30)");

        KhungGioFlashSale slotKhac = new KhungGioFlashSale();
        LocalDate ngayKhac = today.plusDays(3);
        slotKhac.setThoiGianBatDau(ngayKhac.atTime(15, 0));
        String expected = ngayKhac.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + " 15:00";
        assertEquals(expected, slotKhac.getNhanThoiGianTab(), "Khung giờ ngày khác phải có ngày/tháng và giờ 24h");
    }
}
