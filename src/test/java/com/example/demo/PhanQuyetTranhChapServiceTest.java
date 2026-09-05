package com.example.demo;

import com.example.demo.dto.PhanQuyetTranhChapForm;
import com.example.demo.dto.ThongKePhanQuyetDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.PhanQuyetTranhChapService;
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
public class PhanQuyetTranhChapServiceTest {

    @Autowired
    private PhanQuyetTranhChapService phanQuyetTranhChapService;

    @Autowired
    private PhieuKhieuNaiRepository phieuKhieuNaiRepository;

    @Autowired
    private LenhHoanTienBoiThuongRepository lenhHoanTienBoiThuongRepository;

    @Autowired
    private ViNguoiBanRepository viNguoiBanRepository;

    @Autowired
    private LichSuGiaoDichViRepository lichSuGiaoDichViRepository;

    @Autowired
    private LichSuTrangThaiDonRepository lichSuTrangThaiDonRepository;

    @Autowired
    private GhiChuNoiBoKhieuNaiRepository ghiChuNoiBoKhieuNaiRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    private NguoiDung khachHang;
    private NguoiDung cskh;
    private NguoiDung chuShop;
    private GianHang gianHang;
    private DonHangShop donHangShop;
    private PhieuKhieuNai phieuDangXuLy;

    @BeforeEach
    void setUp() {
        // 1. Tạo hoặc lấy tài khoản test
        khachHang = nguoiDungRepository.findAll().stream()
                .filter(u -> "khachhang@flexshop.vn".equals(u.getEmail()))
                .findFirst().orElseGet(() -> {
                    NguoiDung u = new NguoiDung();
                    u.setEmail("khachhang_test@flexshop.vn");
                    u.setSoDienThoai("0901112233");
                    u.setHoVaTen("Khách Hàng Test US47");
                    u.setMatKhauMaHoa("password");
                    u.setTrangThai("HOAT_DONG");
                    u.setDaXoa(false);
                    return nguoiDungRepository.save(u);
                });

        cskh = nguoiDungRepository.findAll().stream()
                .filter(u -> "cskh@flexshop.vn".equals(u.getEmail()))
                .findFirst().orElseGet(() -> {
                    NguoiDung u = new NguoiDung();
                    u.setEmail("cskh_test@flexshop.vn");
                    u.setSoDienThoai("0909998877");
                    u.setHoVaTen("CSKH Test US47");
                    u.setMatKhauMaHoa("password");
                    u.setTrangThai("HOAT_DONG");
                    u.setDaXoa(false);
                    return nguoiDungRepository.save(u);
                });

        chuShop = nguoiDungRepository.findAll().stream()
                .filter(u -> "seller@flexshop.vn".equals(u.getEmail()))
                .findFirst().orElseGet(() -> {
                    NguoiDung u = new NguoiDung();
                    u.setEmail("seller_test@flexshop.vn");
                    u.setSoDienThoai("0903334455");
                    u.setHoVaTen("Chủ Gian Hàng Test");
                    u.setMatKhauMaHoa("password");
                    u.setTrangThai("HOAT_DONG");
                    u.setDaXoa(false);
                    return nguoiDungRepository.save(u);
                });

        // 2. Gian hàng test
        gianHang = gianHangRepository.findAll().stream().findFirst().orElseGet(() -> {
            GianHang g = new GianHang();
            g.setTenGianHang("Gian Hàng Kiểm Thử US47");
            g.setChuSoHuu(chuShop);
            g.setDuongDanSlug("gian-hang-test-" + System.currentTimeMillis());
            g.setDiaChiKho("Hà Nội");
            g.setSdtKho("0901234567");
            g.setTrangThai("HOAT_DONG");
            g.setNgayTao(LocalDateTime.now());
            return gianHangRepository.save(g);
        });

        // 3. Đơn hàng shop test
        DonHangTong donTong = donHangTongRepository.findAll().stream().findFirst().orElseGet(() -> {
            DonHangTong dt = new DonHangTong();
            dt.setMaCodeDonTong("DT-TEST-" + System.currentTimeMillis());
            dt.setKhachHang(khachHang);
            dt.setTongTienHang(BigDecimal.valueOf(1000000));
            dt.setTongPhiVanChuyen(BigDecimal.valueOf(30000));
            dt.setTongThanhToanCuoi(BigDecimal.valueOf(1030000));
            dt.setPhuongThucThanhToan("VNPAY");
            dt.setTrangThaiThanhToan("DA_THANH_TOAN");
            return donHangTongRepository.save(dt);
        });

        donHangShop = new DonHangShop();
        donHangShop.setMaCodeDonShop("SHOP-TEST-US47-" + System.currentTimeMillis());
        donHangShop.setDonHangTong(donTong);
        donHangShop.setGianHang(gianHang);
        donHangShop.setTienHangShop(BigDecimal.valueOf(500000));
        donHangShop.setPhiVanChuyen(BigDecimal.valueOf(30000));
        donHangShop.setTongTienShopNhan(BigDecimal.valueOf(500000));
        donHangShop.setTrangThai("DA_GIAO");
        donHangShop = donHangShopRepository.save(donHangShop);

        // 4. Ticket khiếu nại test
        phieuDangXuLy = new PhieuKhieuNai();
        phieuDangXuLy.setMaCodePhieu("KN-TEST-US47-" + System.currentTimeMillis());
        phieuDangXuLy.setKhachHang(khachHang);
        phieuDangXuLy.setDonHangShop(donHangShop);
        phieuDangXuLy.setGianHang(gianHang);
        phieuDangXuLy.setLoaiKhieuNai("HONG_VO");
        phieuDangXuLy.setMucDoUuTien("CAO");
        phieuDangXuLy.setTrangThai("DANG_XU_LY");
        phieuDangXuLy.setNoiDungMoTa("Sản phẩm bị nứt vỡ trong quá trình vận chuyển, cần hoàn tiền.");
        phieuDangXuLy.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        phieuDangXuLy.setSoTienHoanTra(BigDecimal.valueOf(500000));
        phieuDangXuLy.setCskhXuLy(cskh);
        phieuDangXuLy = phieuKhieuNaiRepository.save(phieuDangXuLy);
    }

    @Test
    @DisplayName("US-47 Test 1: Duyệt hoàn tiền cho Khách hàng, Shop chịu phí thành công")
    void testDuyetHoanTienKhach_NguoiBanChiuPhi_ThanhCong() {
        PhanQuyetTranhChapForm form = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("DUYET_HOAN_TIEN_KHACH")
                .soTien(BigDecimal.valueOf(500000))
                .benChiuPhi("NGUOI_BAN")
                .ghiChuPhanQuyet("Đối chiếu ảnh POD và bằng chứng video xác nhận lỗi do đóng gói sơ sài, duyệt hoàn tiền.")
                .build();

        LenhHoanTienBoiThuong lenh = phanQuyetTranhChapService.raPhanQuyet(form, cskh.getMaNguoiDung());

        assertNotNull(lenh);
        assertEquals(BigDecimal.valueOf(500000), lenh.getSoTien());
        assertEquals("NGUOI_BAN", lenh.getBenChiuPhi());
        assertEquals("DA_CHUYEN_TIEN", lenh.getTrangThai());
        assertEquals(khachHang.getMaNguoiDung(), lenh.getNguoiNhanTien().getMaNguoiDung());

        // Kiểm tra cập nhật phiếu
        PhieuKhieuNai updated = phieuKhieuNaiRepository.findById(phieuDangXuLy.getMaPhieu()).orElseThrow();
        assertEquals("CHAP_NHAN_HOAN_TIEN", updated.getTrangThai());
        assertEquals(cskh.getMaNguoiDung(), updated.getNguoiPhanQuyet().getMaNguoiDung());

        // Kiểm tra ghi lịch sử đơn hàng
        boolean coLichSu = lichSuTrangThaiDonRepository.findAllByDonHangShop_MaDonHangShopOrderByThoiGianAsc(donHangShop.getMaDonHangShop())
                .stream().anyMatch(ls -> "DA_HOAN_TIEN".equals(ls.getTrangThaiMoi()));
        assertTrue(coLichSu);

        // Kiểm tra ghi chú nội bộ
        boolean coGhiChu = ghiChuNoiBoKhieuNaiRepository.findAllByPhieuKhieuNai_MaPhieuOrderByNgayTaoDesc(phieuDangXuLy.getMaPhieu())
                .stream().anyMatch(gn -> gn.getNoiDung().contains("[Phán quyết - Duyệt hoàn tiền]"));
        assertTrue(coGhiChu);
    }

    @Test
    @DisplayName("US-47 Test 2: Bồi thường cho Shop từ quỹ Sàn FlexShop thành công")
    void testBoiThuongShop_SanFlexShopChiuPhi_ThanhCong() {
        PhanQuyetTranhChapForm form = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("BOI_THUONG_SHOP")
                .soTien(BigDecimal.valueOf(400000))
                .benChiuPhi("SAN_FLEXSHOP")
                .ghiChuPhanQuyet("Kiện hàng bị hỏng do thiên tai ngập lụt, quỹ bảo hiểm Sàn FlexShop chi trả bồi thường cho Shop.")
                .build();

        LenhHoanTienBoiThuong lenh = phanQuyetTranhChapService.raPhanQuyet(form, cskh.getMaNguoiDung());

        assertNotNull(lenh);
        assertEquals(BigDecimal.valueOf(400000), lenh.getSoTien());
        assertEquals("SAN_FLEXSHOP", lenh.getBenChiuPhi());
        assertEquals(gianHang.getChuSoHuu().getMaNguoiDung(), lenh.getNguoiNhanTien().getMaNguoiDung());

        // Kiểm tra cập nhật phiếu
        PhieuKhieuNai updated = phieuKhieuNaiRepository.findById(phieuDangXuLy.getMaPhieu()).orElseThrow();
        assertEquals("BOI_THUONG_SHOP", updated.getTrangThai());

        // Kiểm tra ví người bán được cộng tiền
        ViNguoiBan vi = viNguoiBanRepository.findByGianHang_MaGianHang(gianHang.getMaGianHang()).orElseThrow();
        assertTrue(vi.getSoDuKhaDung().compareTo(BigDecimal.valueOf(400000)) >= 0);
    }

    @Test
    @DisplayName("US-47 Test 3: Bác bỏ khiếu nại của Khách hàng, bảo vệ Shop thành công")
    void testBacBoKhieuNai_ThanhCong() {
        PhanQuyetTranhChapForm form = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("BAC_BO_KHIEU_NAI")
                .soTien(BigDecimal.ZERO)
                .benChiuPhi("")
                .ghiChuPhanQuyet("Ảnh POD chứng minh Shipper đã giao kiện hàng nguyên vẹn, khách không có video mở hộp đối chứng. Bác bỏ khiếu nại.")
                .build();

        LenhHoanTienBoiThuong lenh = phanQuyetTranhChapService.raPhanQuyet(form, cskh.getMaNguoiDung());

        // Bác bỏ không tạo lệnh hoàn tiền
        assertNull(lenh);

        PhieuKhieuNai updated = phieuKhieuNaiRepository.findById(phieuDangXuLy.getMaPhieu()).orElseThrow();
        assertEquals("TU_CHOI_KHIEU_NAI", updated.getTrangThai());
        assertEquals(BigDecimal.ZERO, updated.getSoTienHoanTra());
        assertEquals(cskh.getMaNguoiDung(), updated.getNguoiPhanQuyet().getMaNguoiDung());
    }

    @Test
    @DisplayName("US-47 Test 4: Chặn phán quyết số tiền vượt quá tổng giá trị đơn hàng")
    void testChan_SoTienVuotQuaTongTienDon() {
        PhanQuyetTranhChapForm form = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("DUYET_HOAN_TIEN_KHACH")
                .soTien(BigDecimal.valueOf(9999999)) // Vượt quá 500k của đơn
                .benChiuPhi("NGUOI_BAN")
                .ghiChuPhanQuyet("Lý do hợp lệ trên 10 ký tự...")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            phanQuyetTranhChapService.raPhanQuyet(form, cskh.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("không được vượt quá tổng giá trị đơn hàng"));
    }

    @Test
    @DisplayName("US-47 Test 5: Chặn phán quyết số tiền nhỏ hơn 1.000 VNĐ")
    void testChan_SoTienNhoHon1000() {
        PhanQuyetTranhChapForm form = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("DUYET_HOAN_TIEN_KHACH")
                .soTien(BigDecimal.valueOf(500)) // Nhỏ hơn 1000
                .benChiuPhi("NGUOI_BAN")
                .ghiChuPhanQuyet("Lý do hợp lệ trên 10 ký tự...")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            phanQuyetTranhChapService.raPhanQuyet(form, cskh.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("tối thiểu từ 1.000 VNĐ"));
    }

    @Test
    @DisplayName("US-47 Test 6: Chặn bồi thường cho Shop nhưng lại bắt chính Shop chịu phí")
    void testChan_BoiThuongShopNhungBatShopChiuPhi() {
        PhanQuyetTranhChapForm form = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("BOI_THUONG_SHOP")
                .soTien(BigDecimal.valueOf(300000))
                .benChiuPhi("NGUOI_BAN") // Bắt shop tự bồi thường cho chính mình -> Không hợp lý!
                .ghiChuPhanQuyet("Căn cứ hợp lệ để kiểm tra...")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            phanQuyetTranhChapService.raPhanQuyet(form, cskh.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("Shop không thể tự bồi thường cho chính mình"));
    }

    @Test
    @DisplayName("US-47 Test 7: Chặn phán quyết trùng lặp trên ticket đã có phán quyết")
    void testChan_PhanQuyetTrungLap() {
        // Phán quyết lần 1
        PhanQuyetTranhChapForm form1 = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("DUYET_HOAN_TIEN_KHACH")
                .soTien(BigDecimal.valueOf(300000))
                .benChiuPhi("SAN_FLEXSHOP")
                .ghiChuPhanQuyet("Phán quyết lần 1 hợp lệ...")
                .build();
        phanQuyetTranhChapService.raPhanQuyet(form1, cskh.getMaNguoiDung());

        // Phán quyết lần 2 trên cùng ticket -> Bị chặn!
        PhanQuyetTranhChapForm form2 = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("BOI_THUONG_SHOP")
                .soTien(BigDecimal.valueOf(200000))
                .benChiuPhi("DON_VI_VAN_CHUYEN")
                .ghiChuPhanQuyet("Phán quyết lần 2...")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            phanQuyetTranhChapService.raPhanQuyet(form2, cskh.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("Không được tạo giao dịch trùng lặp") || ex.getMessage().contains("đã được phân xử"));
    }

    @Test
    @DisplayName("US-47 Test 8: Chặn lý do phán quyết để trống hoặc quá ngắn dưới 10 ký tự")
    void testChan_LyDoPhanQuyetQuaNganHoacRong() {
        PhanQuyetTranhChapForm form = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("DUYET_HOAN_TIEN_KHACH")
                .soTien(BigDecimal.valueOf(200000))
                .benChiuPhi("NGUOI_BAN")
                .ghiChuPhanQuyet("Ngắn") // Dưới 10 ký tự
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            phanQuyetTranhChapService.raPhanQuyet(form, cskh.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("từ 10 đến 2.000 ký tự"));
    }

    @Test
    @DisplayName("US-47 Test 9: Thống kê KPI tài chính phán quyết và tìm kiếm phân trang")
    void testThongKeVaPhanTrangPhanQuyet() {
        // Thực hiện 1 lệnh hoàn tiền
        PhanQuyetTranhChapForm form = PhanQuyetTranhChapForm.builder()
                .maPhieu(phieuDangXuLy.getMaPhieu())
                .quyetDinh("DUYET_HOAN_TIEN_KHACH")
                .soTien(BigDecimal.valueOf(250000))
                .benChiuPhi("NGUOI_BAN")
                .ghiChuPhanQuyet("Duyệt hoàn tiền cho khách phục vụ kiểm tra KPI tài chính.")
                .build();
        phanQuyetTranhChapService.raPhanQuyet(form, cskh.getMaNguoiDung());

        // Kiểm tra KPI
        ThongKePhanQuyetDTO thongKe = phanQuyetTranhChapService.layThongKePhanQuyet();
        assertNotNull(thongKe);
        assertTrue(thongKe.getTongSoLenh() > 0);
        assertTrue(thongKe.getTongTienHoanKhach().compareTo(BigDecimal.ZERO) > 0);

        // Kiểm tra phân trang và tìm kiếm
        Page<LenhHoanTienBoiThuong> pageResult = phanQuyetTranhChapService.layDanhSachLenhHoanTien(
                null, null, null, null, null, 0, 10
        );
        assertNotNull(pageResult);
        assertFalse(pageResult.isEmpty());
    }
}
