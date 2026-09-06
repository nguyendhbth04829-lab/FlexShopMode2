package com.example.demo;

import com.example.demo.dto.DeXuatTraGiaForm;
import com.example.demo.dto.ThongKeChatDTO;
import com.example.demo.dto.XuLyTraGiaForm;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho US-59 (Module: CHAT)
 * Live Chat trực tiếp với Shop và Trả giá sản phẩm (Make an Offer) trong khung Chat.
 */
@SpringBootTest
public class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private CuocTroChuyenRepository cuocTroChuyenRepository;

    @Autowired
    private TinNhanRepository tinNhanRepository;

    @Autowired
    private DeXuatTraGiaRepository deXuatTraGiaRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    private NguoiDung layKhachHangTest() {
        return nguoiDungRepository.findByEmail("khachhang@flexshop.vn")
                .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElseThrow());
    }

    private GianHang layGianHangTest() {
        return gianHangRepository.findAll().stream().findFirst().orElseThrow();
    }

    private SanPham laySanPhamTest(Long maGianHang) {
        return sanPhamRepository.findByGianHang_MaGianHang(maGianHang).stream()
                .findFirst()
                .orElseGet(() -> sanPhamRepository.findAll().stream().findFirst().orElseThrow());
    }

    @org.junit.jupiter.api.BeforeEach
    public void lamSachDeXuatChoDuyet() {
        NguoiDung khach = layKhachHangTest();
        GianHang shop = layGianHangTest();
        cuocTroChuyenRepository.findByKhachHang_MaNguoiDungAndGianHang_MaGianHang(khach.getMaNguoiDung(), shop.getMaGianHang())
                .ifPresent(ctc -> {
                    deXuatTraGiaRepository.deleteAll(deXuatTraGiaRepository.findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoDesc(ctc.getMaCuocTroChuyen()));
                });
    }

    @Test
    @Transactional
    @DisplayName("Test 1: Lấy hoặc tạo mới cuộc trò chuyện giữa Khách hàng và Shop")
    public void testLayHoacTaoCuocTroChuyen() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();

        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());
        assertNotNull(ctc);
        assertNotNull(ctc.getMaCuocTroChuyen());
        assertEquals(khachHang.getMaNguoiDung(), ctc.getKhachHang().getMaNguoiDung());
        assertEquals(gianHang.getMaGianHang(), ctc.getGianHang().getMaGianHang());
    }

    @Test
    @Transactional
    @DisplayName("Test 2: Gửi tin nhắn văn bản thông thường và cập nhật hội thoại")
    public void testGuiTinNhanVanBan() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        String noiDung = "Chào Shop, sản phẩm này còn màu đen không ạ?";
        TinNhan tn = chatService.guiTinNhan(ctc.getMaCuocTroChuyen(), khachHang.getMaNguoiDung(), "KHACH_HANG", noiDung);

        assertNotNull(tn);
        assertEquals("VAN_BAN", tn.getLoaiTinNhan());
        assertEquals(noiDung, tn.getNoiDung());
        assertFalse(tn.getDaXem());

        CuocTroChuyen updatedCtc = cuocTroChuyenRepository.findById(ctc.getMaCuocTroChuyen()).orElseThrow();
        assertEquals(noiDung, updatedCtc.getTinNhanCuoiCung());
        assertTrue(updatedCtc.getSoTinChuaDocShop() > 0);
    }

    @Test
    @Transactional
    @DisplayName("Test 3: Gửi Thẻ Sản Phẩm (Product Card) vào khung chat")
    public void testGuiTheSanPham() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();
        SanPham sanPham = laySanPhamTest(gianHang.getMaGianHang());
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        TinNhan tn = chatService.guiTheSanPham(ctc.getMaCuocTroChuyen(), khachHang.getMaNguoiDung(), "KHACH_HANG", sanPham.getMaSanPham(), null);

        assertNotNull(tn);
        assertEquals("THE_SAN_PHAM", tn.getLoaiTinNhan());
        assertTrue(tn.getNoiDung().contains(sanPham.getTenSanPham()));
        assertNotNull(tn.getDuLieuDinhKemJson());
        assertTrue(tn.getDuLieuDinhKemJson().contains(sanPham.getTenSanPham()));
    }

    @Test
    @Transactional
    @DisplayName("Test 4: Tạo Đề Xuất Trả Giá (Make an Offer) thành công")
    public void testTaoDeXuatTraGia_ThanhCong() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();
        SanPham sanPham = laySanPhamTest(gianHang.getMaGianHang());
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        // Giá gốc 1.250.000đ -> Trả giá 1.100.000đ (giảm ~12%, hợp lệ)
        BigDecimal giaGoc = sanPham.getGiaCoBan();
        BigDecimal giaDeXuat = giaGoc.multiply(new BigDecimal("0.85")).setScale(0, BigDecimal.ROUND_HALF_UP);

        DeXuatTraGiaForm form = new DeXuatTraGiaForm();
        form.setMaCuocTroChuyen(ctc.getMaCuocTroChuyen());
        form.setMaSanPham(sanPham.getMaSanPham());
        form.setSoLuong(1);
        form.setGiaDeXuat(giaDeXuat);
        form.setGhiChuKhach("Shop bớt xíu mình lấy luôn!");

        DeXuatTraGia deXuat = chatService.taoDeXuatTraGia(form, khachHang.getMaNguoiDung());

        assertNotNull(deXuat);
        assertEquals("CHO_DUYET", deXuat.getTrangThai());
        assertEquals(giaDeXuat, deXuat.getGiaDeXuat());
        assertEquals(1, deXuat.getSoLuong());
    }

    @Test
    @Transactional
    @DisplayName("Test 5: Chặn Trả Giá nếu mức giá đề xuất >= Giá niêm yết")
    public void testTaoDeXuatTraGia_GiaCaoHonGiaGoc() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();
        SanPham sanPham = laySanPhamTest(gianHang.getMaGianHang());
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        BigDecimal giaGoc = sanPham.getGiaCoBan();

        DeXuatTraGiaForm form = new DeXuatTraGiaForm();
        form.setMaCuocTroChuyen(ctc.getMaCuocTroChuyen());
        form.setMaSanPham(sanPham.getMaSanPham());
        form.setSoLuong(1);
        form.setGiaDeXuat(giaGoc.add(new BigDecimal("10000"))); // Cao hơn giá gốc

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            chatService.taoDeXuatTraGia(form, khachHang.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("phải nhỏ hơn giá niêm yết hiện tại"));
    }

    @Test
    @Transactional
    @DisplayName("Test 6: Chặn Trả Giá nếu mức giá đề xuất giảm quá sâu (> 50%)")
    public void testTaoDeXuatTraGia_GiamQuaSau50PhanTram() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();
        SanPham sanPham = laySanPhamTest(gianHang.getMaGianHang());
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        BigDecimal giaGoc = sanPham.getGiaCoBan();
        BigDecimal giaDeXuat = giaGoc.multiply(new BigDecimal("0.30")).setScale(0, BigDecimal.ROUND_HALF_UP); // Giảm 70%

        DeXuatTraGiaForm form = new DeXuatTraGiaForm();
        form.setMaCuocTroChuyen(ctc.getMaCuocTroChuyen());
        form.setMaSanPham(sanPham.getMaSanPham());
        form.setSoLuong(1);
        form.setGiaDeXuat(giaDeXuat);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            chatService.taoDeXuatTraGia(form, khachHang.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("tối đa được giảm là 50%"));
    }

    @Test
    @Transactional
    @DisplayName("Test 7: Chặn gửi Đề Xuất Trả Giá trùng lặp khi đang có đề xuất chờ duyệt")
    public void testTaoDeXuatTraGia_ChanTrungLap() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();
        SanPham sanPham = laySanPhamTest(gianHang.getMaGianHang());
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        BigDecimal giaGoc = sanPham.getGiaCoBan();
        BigDecimal giaDeXuat1 = giaGoc.multiply(new BigDecimal("0.85")).setScale(0, BigDecimal.ROUND_HALF_UP);

        DeXuatTraGiaForm form1 = new DeXuatTraGiaForm();
        form1.setMaCuocTroChuyen(ctc.getMaCuocTroChuyen());
        form1.setMaSanPham(sanPham.getMaSanPham());
        form1.setSoLuong(1);
        form1.setGiaDeXuat(giaDeXuat1);
        chatService.taoDeXuatTraGia(form1, khachHang.getMaNguoiDung());

        // Đề xuất thứ 2 cho cùng sản phẩm
        BigDecimal giaDeXuat2 = giaGoc.multiply(new BigDecimal("0.80")).setScale(0, BigDecimal.ROUND_HALF_UP);
        DeXuatTraGiaForm form2 = new DeXuatTraGiaForm();
        form2.setMaCuocTroChuyen(ctc.getMaCuocTroChuyen());
        form2.setMaSanPham(sanPham.getMaSanPham());
        form2.setSoLuong(1);
        form2.setGiaDeXuat(giaDeXuat2);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            chatService.taoDeXuatTraGia(form2, khachHang.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("đang có một đề xuất trả giá"));
    }

    @Test
    @Transactional
    @DisplayName("Test 8: Shop Đồng Ý đề xuất trả giá và tự động phát sinh tin nhắn hệ thống")
    public void testShopDuyetTraGia_DongY() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();
        SanPham sanPham = laySanPhamTest(gianHang.getMaGianHang());
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        BigDecimal giaGoc = sanPham.getGiaCoBan();
        BigDecimal giaDeXuat = giaGoc.multiply(new BigDecimal("0.85")).setScale(0, BigDecimal.ROUND_HALF_UP);

        DeXuatTraGiaForm form = new DeXuatTraGiaForm();
        form.setMaCuocTroChuyen(ctc.getMaCuocTroChuyen());
        form.setMaSanPham(sanPham.getMaSanPham());
        form.setSoLuong(1);
        form.setGiaDeXuat(giaDeXuat);
        DeXuatTraGia deXuat = chatService.taoDeXuatTraGia(form, khachHang.getMaNguoiDung());

        // Shop duyệt đồng ý
        XuLyTraGiaForm duyetForm = new XuLyTraGiaForm();
        duyetForm.setMaDeXuat(deXuat.getMaDeXuat());
        duyetForm.setHanhDong("DONG_Y");
        duyetForm.setPhanHoiShop("Shop đồng ý giá cho bạn nhé!");

        DeXuatTraGia result = chatService.xuLyDuyetTraGia(duyetForm, 2L);

        assertEquals("DONG_Y", result.getTrangThai());
        assertNotNull(result.getNgayCapNhat());

        // Kiểm tra tin nhắn thông báo hệ thống
        List<TinNhan> tinNhans = tinNhanRepository.findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoAsc(ctc.getMaCuocTroChuyen());
        TinNhan tinCuoi = tinNhans.get(tinNhans.size() - 1);
        assertEquals("HE_THONG", tinCuoi.getLoaiTinNhan());
        assertTrue(tinCuoi.getNoiDung().contains("ĐỒNG Ý"));
    }

    @Test
    @Transactional
    @DisplayName("Test 9: Shop Từ Chối đề xuất trả giá kèm lý do")
    public void testShopDuyetTraGia_TuChoi() {
        NguoiDung khachHang = layKhachHangTest();
        GianHang gianHang = layGianHangTest();
        SanPham sanPham = laySanPhamTest(gianHang.getMaGianHang());
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        BigDecimal giaGoc = sanPham.getGiaCoBan();
        BigDecimal giaDeXuat = giaGoc.multiply(new BigDecimal("0.85")).setScale(0, BigDecimal.ROUND_HALF_UP);

        DeXuatTraGiaForm form = new DeXuatTraGiaForm();
        form.setMaCuocTroChuyen(ctc.getMaCuocTroChuyen());
        form.setMaSanPham(sanPham.getMaSanPham());
        form.setSoLuong(1);
        form.setGiaDeXuat(giaDeXuat);
        DeXuatTraGia deXuat = chatService.taoDeXuatTraGia(form, khachHang.getMaNguoiDung());

        // Shop duyệt từ chối
        XuLyTraGiaForm duyetForm = new XuLyTraGiaForm();
        duyetForm.setMaDeXuat(deXuat.getMaDeXuat());
        duyetForm.setHanhDong("TU_CHOI");
        duyetForm.setPhanHoiShop("Giá này Shop đã bán lỗ, không giảm thêm được!");

        DeXuatTraGia result = chatService.xuLyDuyetTraGia(duyetForm, 2L);

        assertEquals("TU_CHOI", result.getTrangThai());
        assertEquals("Giá này Shop đã bán lỗ, không giảm thêm được!", result.getPhanHoiShop());
    }

    @Test
    @Transactional
    @DisplayName("Test 10: Thống kê Chat và tỷ lệ chốt deal trả giá cho Shop")
    public void testThongKeChatShop() {
        GianHang gianHang = layGianHangTest();
        ThongKeChatDTO thongKe = chatService.layThongKeChatShop(gianHang.getMaGianHang());

        assertNotNull(thongKe);
        assertTrue(thongKe.getTongHoiThoai() >= 0);
        assertTrue(thongKe.getTyLeDongYPhanTram() >= 0.0 && thongKe.getTyLeDongYPhanTram() <= 100.0);
    }
}
