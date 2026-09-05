package com.example.demo;

import com.example.demo.dto.DanhGiaSanPhamForm;
import com.example.demo.dto.DonHangDanhGiaItemDTO;
import com.example.demo.dto.ThongKeDanhGiaDTO;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DanhGiaServiceTest {

    @Autowired
    private DanhGiaService danhGiaService;

    @Autowired
    private DanhGiaSanPhamRepository danhGiaSanPhamRepository;

    @Autowired
    private HinhAnhDanhGiaRepository hinhAnhDanhGiaRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DiaChiNguoiDungRepository diaChiNguoiDungRepository;

    private NguoiDung khachHangHopLe;
    private NguoiDung khachHangKhac;
    private GianHang gianHang;
    private SanPham sanPham;
    private BienTheSanPham bienThe;
    private DonHangShop donHangDaGiao;
    private DonHangShop donHangDangGiao;
    private ChiTietDonHang chiTietHopLe;
    private ChiTietDonHang chiTietChuaGiao;

    @BeforeEach
    void setUp() {
        // 1. Tạo người dùng kiểm thử
        khachHangHopLe = new NguoiDung();
        khachHangHopLe.setHoVaTen("Nguyễn Văn Kiểm Thử");
        khachHangHopLe.setEmail("kiemthu_us49_" + System.currentTimeMillis() + "@flexshop.vn");
        khachHangHopLe.setSoDienThoai("0988776655");
        khachHangHopLe.setMatKhauMaHoa("$2a$10$testHashPassword123");
        khachHangHopLe.setTrangThai("HOAT_DONG");
        khachHangHopLe.setDaXoa(false);
        khachHangHopLe.setNgayTao(LocalDateTime.now());
        khachHangHopLe = nguoiDungRepository.save(khachHangHopLe);

        khachHangKhac = new NguoiDung();
        khachHangKhac.setHoVaTen("Trần Thị Khác");
        khachHangKhac.setEmail("khac_us49_" + System.currentTimeMillis() + "@flexshop.vn");
        khachHangKhac.setSoDienThoai("0911223344");
        khachHangKhac.setMatKhauMaHoa("$2a$10$testHashPassword123");
        khachHangKhac.setTrangThai("HOAT_DONG");
        khachHangKhac.setDaXoa(false);
        khachHangKhac.setNgayTao(LocalDateTime.now());
        khachHangKhac = nguoiDungRepository.save(khachHangKhac);

        // 2. Lấy hoặc tạo gian hàng & sản phẩm
        gianHang = gianHangRepository.findAll().stream().findFirst().orElseGet(() -> {
            GianHang gh = new GianHang();
            gh.setTenGianHang("Shop Công Nghệ Test");
            gh.setTrangThai("HOAT_DONG");
            gh.setDiemDanhGiaTb(BigDecimal.valueOf(5.0));
            gh.setTongDanhGia(0);
            gh.setNgayTao(LocalDateTime.now());
            return gianHangRepository.save(gh);
        });

        sanPham = sanPhamRepository.findAll().stream().findFirst().orElseGet(() -> {
            SanPham sp = new SanPham();
            sp.setTenSanPham("Sản phẩm Test Đánh Giá");
            sp.setGianHang(gianHang);
            sp.setDanhGiaTb(BigDecimal.valueOf(5.0));
            sp.setNgayTao(LocalDateTime.now());
            return sanPhamRepository.save(sp);
        });

        bienThe = bienTheSanPhamRepository.findAll().stream().findFirst().orElseGet(() -> {
            BienTheSanPham bt = new BienTheSanPham();
            bt.setSanPham(sanPham);
            bt.setTenBienThe("Màu Đen - Bản Chuẩn");
            bt.setGiaBan(BigDecimal.valueOf(500000));
            return bienTheSanPhamRepository.save(bt);
        });

        // 3. Tạo Đơn hàng tổng & Đơn hàng shop đã giao (DA_GIAO)
        DiaChiNguoiDung diaChi = diaChiNguoiDungRepository.findAll().stream().findFirst().orElseGet(() -> {
            DiaChiNguoiDung dc = new DiaChiNguoiDung();
            dc.setNguoiDung(khachHangHopLe);
            dc.setTenNguoiNhan("Nguyễn Văn Test");
            dc.setSoDienThoai("0988776655");
            dc.setDiaChiChiTiet("Số 1 Đại Cồ Việt");
            dc.setXaPhuong("Bách Khoa");
            dc.setQuanHuyen("Hai Bà Trưng");
            dc.setTinhThanh("Hà Nội");
            dc.setLaMacDinh(true);
            return diaChiNguoiDungRepository.save(dc);
        });

        DonHangTong dhtHopLe = new DonHangTong();
        dhtHopLe.setKhachHang(khachHangHopLe);
        dhtHopLe.setDiaChiGiao(diaChi);
        dhtHopLe.setMaCodeDonTong("TEST-DHT-" + System.currentTimeMillis());
        dhtHopLe.setTongTienHang(BigDecimal.valueOf(500000));
        dhtHopLe.setTongPhiVanChuyen(BigDecimal.valueOf(30000));
        dhtHopLe.setTongThanhToanCuoi(BigDecimal.valueOf(530000));
        dhtHopLe.setPhuongThucThanhToan("COD");
        dhtHopLe.setTrangThaiThanhToan("DA_THANH_TOAN");
        dhtHopLe.setTrangThaiDonHang("HOAN_TAT");
        dhtHopLe.setNgayTao(LocalDateTime.now().minusDays(2));
        dhtHopLe = donHangTongRepository.save(dhtHopLe);

        donHangDaGiao = new DonHangShop();
        donHangDaGiao.setDonHangTong(dhtHopLe);
        donHangDaGiao.setGianHang(gianHang);
        donHangDaGiao.setMaCodeDonShop("TEST-DHS-DAGIAO-" + System.currentTimeMillis());
        donHangDaGiao.setTienHangShop(BigDecimal.valueOf(500000));
        donHangDaGiao.setPhiVanChuyen(BigDecimal.valueOf(30000));
        donHangDaGiao.setTongTienShopNhan(BigDecimal.valueOf(500000));
        donHangDaGiao.setTrangThai("DA_GIAO");
        donHangDaGiao.setNgayTao(LocalDateTime.now().minusDays(2));
        donHangDaGiao = donHangShopRepository.save(donHangDaGiao);

        chiTietHopLe = new ChiTietDonHang();
        chiTietHopLe.setDonHangShop(donHangDaGiao);
        chiTietHopLe.setMaBienThe(bienThe.getMaBienThe());
        chiTietHopLe.setTenSanPham(sanPham.getTenSanPham());
        chiTietHopLe.setTenBienThe(bienThe.getTenBienThe());
        chiTietHopLe.setMaSku("SKU-TEST-001");
        chiTietHopLe.setDonGia(BigDecimal.valueOf(500000));
        chiTietHopLe.setSoLuong(1);
        chiTietHopLe.setTongTien(BigDecimal.valueOf(500000));
        chiTietHopLe = chiTietDonHangRepository.save(chiTietHopLe);

        // 4. Tạo Đơn hàng shop đang giao (DANG_GIAO) để test validation
        donHangDangGiao = new DonHangShop();
        donHangDangGiao.setDonHangTong(dhtHopLe);
        donHangDangGiao.setGianHang(gianHang);
        donHangDangGiao.setMaCodeDonShop("TEST-DHS-DANGGIAO-" + System.currentTimeMillis());
        donHangDangGiao.setTienHangShop(BigDecimal.valueOf(500000));
        donHangDangGiao.setPhiVanChuyen(BigDecimal.valueOf(30000));
        donHangDangGiao.setTongTienShopNhan(BigDecimal.valueOf(500000));
        donHangDangGiao.setTrangThai("DANG_GIAO");
        donHangDangGiao.setNgayTao(LocalDateTime.now().minusDays(1));
        donHangDangGiao = donHangShopRepository.save(donHangDangGiao);

        chiTietChuaGiao = new ChiTietDonHang();
        chiTietChuaGiao.setDonHangShop(donHangDangGiao);
        chiTietChuaGiao.setMaBienThe(bienThe.getMaBienThe());
        chiTietChuaGiao.setTenSanPham(sanPham.getTenSanPham());
        chiTietChuaGiao.setTenBienThe(bienThe.getTenBienThe());
        chiTietChuaGiao.setMaSku("SKU-TEST-002");
        chiTietChuaGiao.setDonGia(BigDecimal.valueOf(500000));
        chiTietChuaGiao.setSoLuong(1);
        chiTietChuaGiao.setTongTien(BigDecimal.valueOf(500000));
        chiTietChuaGiao = chiTietDonHangRepository.save(chiTietChuaGiao);
    }

    @Test
    @DisplayName("US-49: Khách hàng gửi đánh giá sản phẩm thành công và tự động tính lại điểm Rating")
    void testGuiDanhGiaSanPhamThanhCong() {
        DanhGiaSanPhamForm form = new DanhGiaSanPhamForm();
        form.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        form.setSoSao(5);
        form.setNoiDung("Sản phẩm tuyệt vời, chất lượng vượt mong đợi!");
        form.setAnDanh(false);

        DanhGiaSanPham saved = danhGiaService.guiDanhGiaSanPham(form, khachHangHopLe.getMaNguoiDung());

        assertNotNull(saved.getMaDanhGia());
        assertEquals(5, saved.getSoSao());
        assertEquals("Sản phẩm tuyệt vời, chất lượng vượt mong đợi!", saved.getNoiDung());
        assertFalse(saved.getAnDanh());
        assertEquals(khachHangHopLe.getMaNguoiDung(), saved.getNguoiDung().getMaNguoiDung());

        // Kiểm tra điểm rating sản phẩm và gian hàng được cập nhật
        SanPham spCapNhat = sanPhamRepository.findById(sanPham.getMaSanPham()).orElseThrow();
        assertNotNull(spCapNhat.getDanhGiaTb());
        assertTrue(spCapNhat.getDanhGiaTb().compareTo(BigDecimal.ZERO) > 0);

        GianHang ghCapNhat = gianHangRepository.findById(gianHang.getMaGianHang()).orElseThrow();
        assertNotNull(ghCapNhat.getDiemDanhGiaTb());
        assertTrue(ghCapNhat.getTongDanhGia() > 0);
    }

    @Test
    @DisplayName("US-49: Đánh giá ẩn danh hiển thị tên mã hóa (ví dụ: N***u)")
    void testDanhGiaAnDanh() {
        DanhGiaSanPhamForm form = new DanhGiaSanPhamForm();
        form.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        form.setSoSao(4);
        form.setNoiDung("Sản phẩm rất tốt nhưng muốn ẩn danh tính.");
        form.setAnDanh(true);

        DanhGiaSanPham saved = danhGiaService.guiDanhGiaSanPham(form, khachHangHopLe.getMaNguoiDung());

        assertTrue(saved.getAnDanh());
        String tenDisplay = saved.getTenNguoiDanhGiaDisplay();
        assertTrue(tenDisplay.contains("***"), "Tên ẩn danh phải chứa dấu hoa thị bảo mật: " + tenDisplay);
    }

    @Test
    @DisplayName("US-49 Validation: Chặn đánh giá khi đơn hàng chưa giao thành công")
    void testChanDanhGiaKhiDonHangChuaGiaoThanhCong() {
        DanhGiaSanPhamForm form = new DanhGiaSanPhamForm();
        form.setMaChiTietDon(chiTietChuaGiao.getMaChiTietDon());
        form.setSoSao(5);
        form.setNoiDung("Cố tình đánh giá đơn đang giao hàng.");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                danhGiaService.guiDanhGiaSanPham(form, khachHangHopLe.getMaNguoiDung())
        );
        assertTrue(ex.getMessage().contains("chỉ có thể đánh giá"));
    }

    @Test
    @DisplayName("US-49 Validation: Chặn đánh giá trùng lặp cùng một món hàng")
    void testChanDanhGiaTrungLap() {
        DanhGiaSanPhamForm form = new DanhGiaSanPhamForm();
        form.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        form.setSoSao(5);
        form.setNoiDung("Đánh giá lần thứ nhất rất tốt!");
        danhGiaService.guiDanhGiaSanPham(form, khachHangHopLe.getMaNguoiDung());

        // Cố tình gửi đánh giá lần 2 cho cùng món hàng đó
        DanhGiaSanPhamForm formLan2 = new DanhGiaSanPhamForm();
        formLan2.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        formLan2.setSoSao(4);
        formLan2.setNoiDung("Đánh giá lần thứ hai gian lận.");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                danhGiaService.guiDanhGiaSanPham(formLan2, khachHangHopLe.getMaNguoiDung())
        );
        assertTrue(ex.getMessage().contains("đã được quý khách đánh giá trước đó"));
    }

    @Test
    @DisplayName("US-49 Security: Chặn khách hàng đánh giá chéo đơn hàng của tài khoản khác")
    void testChanDanhGiaCheoDonHangNguoiKhac() {
        DanhGiaSanPhamForm form = new DanhGiaSanPhamForm();
        form.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        form.setSoSao(5);
        form.setNoiDung("Đánh giá trộm đơn hàng của người khác.");

        SecurityException ex = assertThrows(SecurityException.class, () ->
                danhGiaService.guiDanhGiaSanPham(form, khachHangKhac.getMaNguoiDung())
        );
        assertTrue(ex.getMessage().contains("không có quyền đánh giá sản phẩm của đơn hàng không thuộc về mình"));
    }

    @Test
    @DisplayName("US-49 Validation: Kiểm tra số sao hợp lệ từ 1 đến 5 sao")
    void testValidateSoSaoBatBuocTu1Den5() {
        // Sao nhỏ hơn 1
        DanhGiaSanPhamForm form0Sao = new DanhGiaSanPhamForm();
        form0Sao.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        form0Sao.setSoSao(0);
        form0Sao.setNoiDung("Đánh giá 0 sao không hợp lệ.");

        assertThrows(IllegalArgumentException.class, () ->
                danhGiaService.guiDanhGiaSanPham(form0Sao, khachHangHopLe.getMaNguoiDung())
        );

        // Sao lớn hơn 5
        DanhGiaSanPhamForm form6Sao = new DanhGiaSanPhamForm();
        form6Sao.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        form6Sao.setSoSao(6);
        form6Sao.setNoiDung("Đánh giá 6 sao vượt ngưỡng quy định.");

        assertThrows(IllegalArgumentException.class, () ->
                danhGiaService.guiDanhGiaSanPham(form6Sao, khachHangHopLe.getMaNguoiDung())
        );
    }

    @Test
    @DisplayName("US-49 Validation: Kiểm tra nội dung nhận xét tối thiểu 5 ký tự và chống rỗng")
    void testValidateNoiDungNhanXetToiThieu5KyTu() {
        DanhGiaSanPhamForm formNgan = new DanhGiaSanPhamForm();
        formNgan.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        formNgan.setSoSao(5);
        formNgan.setNoiDung("Tốt"); // Chỉ 3 ký tự

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                danhGiaService.guiDanhGiaSanPham(formNgan, khachHangHopLe.getMaNguoiDung())
        );
        assertTrue(ex.getMessage().contains("từ 5 đến 2.000 ký tự"));
    }

    @Test
    @DisplayName("US-49 Thống kê & Phân trang: Kiểm tra phân tích tỷ lệ số sao và bộ lọc")
    void testThongKeVaPhanTrangDanhGia() {
        // Gửi 1 đánh giá
        DanhGiaSanPhamForm form = new DanhGiaSanPhamForm();
        form.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        form.setSoSao(5);
        form.setNoiDung("Đánh giá thử nghiệm để lấy số liệu thống kê.");
        danhGiaService.guiDanhGiaSanPham(form, khachHangHopLe.getMaNguoiDung());

        ThongKeDanhGiaDTO thongKe = danhGiaService.layThongKeDanhGiaSanPham(sanPham.getMaSanPham());
        assertNotNull(thongKe);
        assertTrue(thongKe.getTongSoDanhGia() >= 1);
        assertTrue(thongKe.getSoLuong5Sao() >= 1);

        // Kiểm tra phân trang và lọc theo sao
        Page<DanhGiaSanPham> page5Sao = danhGiaService.layDanhSachDanhGiaSanPham(sanPham.getMaSanPham(), 5, null, 0, 10);
        assertNotNull(page5Sao);
        assertFalse(page5Sao.isEmpty());
        assertEquals(5, page5Sao.getContent().get(0).getSoSao());
    }

    @Test
    @DisplayName("US-49 DTO: Kiểm tra danh sách sản phẩm trong đơn hàng kèm trạng thái đã đánh giá")
    void testLayDanhSachSanPhamDonHangDeDanhGia() {
        List<DonHangDanhGiaItemDTO> dsItem = danhGiaService.layDanhSachSanPhamDonHangDeDanhGia(
                donHangDaGiao.getMaDonHangShop(), khachHangHopLe.getMaNguoiDung()
        );
        assertNotNull(dsItem);
        assertFalse(dsItem.isEmpty());
        assertFalse(dsItem.get(0).isDaDanhGia());

        // Đánh giá sản phẩm
        DanhGiaSanPhamForm form = new DanhGiaSanPhamForm();
        form.setMaChiTietDon(chiTietHopLe.getMaChiTietDon());
        form.setSoSao(5);
        form.setNoiDung("Sản phẩm dùng rất tốt, đánh giá để kiểm tra cờ daDanhGia.");
        danhGiaService.guiDanhGiaSanPham(form, khachHangHopLe.getMaNguoiDung());

        // Truy vấn lại
        List<DonHangDanhGiaItemDTO> dsItemSauKhiDanhGia = danhGiaService.layDanhSachSanPhamDonHangDeDanhGia(
                donHangDaGiao.getMaDonHangShop(), khachHangHopLe.getMaNguoiDung()
        );
        assertTrue(dsItemSauKhiDanhGia.get(0).isDaDanhGia());
    }
}
