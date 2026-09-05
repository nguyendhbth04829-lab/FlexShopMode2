package com.example.demo;

import com.example.demo.dto.DoiChieuBaBenDTO;
import com.example.demo.dto.PhanLoaiTicketForm;
import com.example.demo.dto.ThongKeDashboardCskhDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.CskhService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CskhServiceTest {

    @Autowired
    private CskhService cskhService;

    @Autowired
    private PhieuKhieuNaiRepository phieuKhieuNaiRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private GhiChuNoiBoKhieuNaiRepository ghiChuNoiBoKhieuNaiRepository;

    private PhieuKhieuNai phieuMau;
    private NguoiDung cskhMau;

    @BeforeEach
    void setUp() {
        cskhMau = nguoiDungRepository.findAll().stream()
                .filter(u -> u.getEmail().contains("cskh"))
                .findFirst()
                .orElseGet(() -> {
                    NguoiDung u = new NguoiDung();
                    u.setHoVaTen("Ngô Thị CSKH");
                    u.setEmail("cskh.test@flexshop.vn");
                    u.setSoDienThoai("0933112233");
                    u.setMatKhauMaHoa("password");
                    u.setTrangThai("HOAT_DONG");
                    return nguoiDungRepository.save(u);
                });

        List<PhieuKhieuNai> danhSach = phieuKhieuNaiRepository.findAll();
        if (!danhSach.isEmpty()) {
            phieuMau = danhSach.get(0);
        } else {
            NguoiDung khach = new NguoiDung();
            khach.setHoVaTen("Khách Hàng Test");
            khach.setEmail("khach.test@flexshop.vn");
            khach.setSoDienThoai("0912345678");
            khach.setMatKhauMaHoa("password");
            khach = nguoiDungRepository.save(khach);

            GianHang shop = new GianHang();
            shop.setTenGianHang("Shop Test US46");
            shop.setChuSoHuu(khach);
            shop.setSdtKho("0988776655");
            shop.setDiaChiKho("Hà Nội");
            shop = gianHangRepository.save(shop);

            DonHangTong donTong = new DonHangTong();
            donTong.setMaCodeDonTong("TONG-US46-" + System.currentTimeMillis());
            donTong.setKhachHang(khach);
            donTong.setTongTienHang(new BigDecimal("500000"));
            donTong.setTongPhiVanChuyen(new BigDecimal("30000"));
            donTong.setTongThanhToanCuoi(new BigDecimal("530000"));
            donTong = donHangTongRepository.save(donTong);

            DonHangShop donShop = new DonHangShop();
            donShop.setMaCodeDonShop("SHOP-US46-" + System.currentTimeMillis());
            donShop.setDonHangTong(donTong);
            donShop.setGianHang(shop);
            donShop.setTienHangShop(new BigDecimal("500000"));
            donShop.setPhiVanChuyen(new BigDecimal("30000"));
            donShop.setTongTienShopNhan(new BigDecimal("530000"));
            donShop.setTrangThai("DA_GIAO");
            donShop.setMaVanDon("VNPOST-TEST-88");
            donShop = donHangShopRepository.save(donShop);

            phieuMau = new PhieuKhieuNai();
            phieuMau.setMaCodePhieu("KN-TEST-" + System.currentTimeMillis());
            phieuMau.setKhachHang(khach);
            phieuMau.setDonHangShop(donShop);
            phieuMau.setGianHang(shop);
            phieuMau.setLoaiKhieuNai("HONG_VO");
            phieuMau.setMucDoUuTien("CAO");
            phieuMau.setTrangThai("MO_MOI");
            phieuMau.setNoiDungMoTa("Tai nghe test bị nứt vỏ hộp khi nhận.");
            phieuMau.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
            phieuMau.setSoTienHoanTra(new BigDecimal("500000"));
            phieuMau = phieuKhieuNaiRepository.save(phieuMau);
        }
    }

    @Test
    @DisplayName("US-46: Thống kê KPI Dashboard CSKH thời gian thực từ database")
    void testLayThongKeDashboardCskh() {
        ThongKeDashboardCskhDTO kpi = cskhService.layThongKeDashboard();
        assertNotNull(kpi);
        assertTrue(kpi.getTongSoTicket() >= 1, "Tổng số ticket phải >= 1");
        assertTrue(kpi.getTicketUuTienCao() >= 0);
        assertTrue(kpi.getKhieuNaiVanChuyenPod() >= 0);
    }

    @Test
    @DisplayName("US-46: Tìm kiếm và lọc danh sách Ticket toàn sàn cho CSKH")
    void testTimKiemTicketCskh() {
        Page<PhieuKhieuNai> ketQua = cskhService.layDanhSachTicketCskh(
                null, null, null, null, "TAT_CA", null, null, null, 0, 10
        );
        assertNotNull(ketQua);
        assertFalse(ketQua.isEmpty(), "Danh sách ticket CSKH không được rỗng");
    }

    @Test
    @DisplayName("US-46: Tiếp nhận Ticket nhanh bởi CSKH")
    void testTiepNhanTicket() {
        phieuMau.setTrangThai("MO_MOI");
        phieuMau.setCskhXuLy(null);
        phieuMau = phieuKhieuNaiRepository.save(phieuMau);

        PhieuKhieuNai sauTiepNhan = cskhService.tiepNhanTicket(phieuMau.getMaPhieu(), cskhMau.getMaNguoiDung());

        assertNotNull(sauTiepNhan);
        assertNotNull(sauTiepNhan.getCskhXuLy());
        assertEquals(cskhMau.getMaNguoiDung(), sauTiepNhan.getCskhXuLy().getMaNguoiDung());
        assertEquals("DANG_XU_LY", sauTiepNhan.getTrangThai());

        List<GhiChuNoiBoKhieuNai> dsGhiChu = ghiChuNoiBoKhieuNaiRepository.findAllByPhieuKhieuNai_MaPhieuOrderByNgayTaoDesc(phieuMau.getMaPhieu());
        assertFalse(dsGhiChu.isEmpty());
        assertTrue(dsGhiChu.get(0).getNoiDung().contains("tiếp nhận"));
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn tiếp nhận ticket đã đóng hoặc đã hủy")
    void testValidateTiepNhanTicketDaDong() {
        phieuMau.setTrangThai("DONG_PHIEU");
        phieuKhieuNaiRepository.save(phieuMau);

        assertThrows(IllegalStateException.class, () -> {
            cskhService.tiepNhanTicket(phieuMau.getMaPhieu(), cskhMau.getMaNguoiDung());
        }, "Hệ thống phải chặn tiếp nhận ticket đã đóng");
    }

    @Test
    @DisplayName("US-46: Phân loại Ticket và cập nhật mức độ ưu tiên hợp lệ")
    void testPhanLoaiTicket() {
        PhanLoaiTicketForm form = new PhanLoaiTicketForm();
        form.setLoaiKhieuNai("HANG_GIA");
        form.setMucDoUuTien("KHAN_CAP");
        form.setTrangThai("CHO_SHOP_PHAN_HOI");
        form.setMaCskhPhuTrach(cskhMau.getMaNguoiDung());
        form.setGhiChuXuLy("Chuyển mức Khẩn cấp và yêu cầu Shop xuất trình chứng từ xuất xứ.");

        PhieuKhieuNai sauPhanLoai = cskhService.phanLoaiTicket(phieuMau.getMaPhieu(), form, cskhMau.getMaNguoiDung());

        assertEquals("HANG_GIA", sauPhanLoai.getLoaiKhieuNai());
        assertEquals("KHAN_CAP", sauPhanLoai.getMucDoUuTien());
        assertEquals("CHO_SHOP_PHAN_HOI", sauPhanLoai.getTrangThai());
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn phân loại với nhóm sự cố hoặc trạng thái không hợp lệ")
    void testValidatePhanLoaiKhongHopLe() {
        PhanLoaiTicketForm formSaiLoai = new PhanLoaiTicketForm();
        formSaiLoai.setLoaiKhieuNai("LOAI_BAY_BA");
        formSaiLoai.setMucDoUuTien("CAO");
        formSaiLoai.setTrangThai("DANG_XU_LY");

        assertThrows(IllegalArgumentException.class, () -> {
            cskhService.phanLoaiTicket(phieuMau.getMaPhieu(), formSaiLoai, cskhMau.getMaNguoiDung());
        }, "Hệ thống phải chặn loại khiếu nại không hợp lệ");

        PhanLoaiTicketForm formSaiTrangThai = new PhanLoaiTicketForm();
        formSaiTrangThai.setLoaiKhieuNai("HONG_VO");
        formSaiTrangThai.setMucDoUuTien("CAO");
        formSaiTrangThai.setTrangThai("TRANG_THAI_KHONG_TON_TAI");

        assertThrows(IllegalArgumentException.class, () -> {
            cskhService.phanLoaiTicket(phieuMau.getMaPhieu(), formSaiTrangThai, cskhMau.getMaNguoiDung());
        }, "Hệ thống phải chặn trạng thái ticket không hợp lệ");
    }

    @Test
    @DisplayName("US-46: Thêm ghi chú điều tra nội bộ bảo mật")
    void testThemGhiChuNoiBo() {
        String noiDung = "Đã liên hệ tài xế xác minh vị trí đặt kiện hàng lúc giao.";
        GhiChuNoiBoKhieuNai ghiChu = cskhService.themGhiChuNoiBo(phieuMau.getMaPhieu(), cskhMau.getMaNguoiDung(), noiDung);

        assertNotNull(ghiChu.getMaGhiChu());
        assertEquals(noiDung, ghiChu.getNoiDung());
        assertEquals(cskhMau.getMaNguoiDung(), ghiChu.getNhanVien().getMaNguoiDung());
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn ghi chú điều tra quá ngắn hoặc chỉ chứa khoảng trắng")
    void testValidateThemGhiChuQuaNgan() {
        assertThrows(IllegalArgumentException.class, () -> {
            cskhService.themGhiChuNoiBo(phieuMau.getMaPhieu(), cskhMau.getMaNguoiDung(), "123");
        }, "Chặn nội dung < 5 ký tự");

        assertThrows(IllegalArgumentException.class, () -> {
            cskhService.themGhiChuNoiBo(phieuMau.getMaPhieu(), cskhMau.getMaNguoiDung(), "       ");
        }, "Chặn nội dung chỉ chứa khoảng trắng");
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn ghi chú khi mã nhân viên không tồn tại")
    void testValidateNhanVienKhongTonTai() {
        assertThrows(IllegalArgumentException.class, () -> {
            cskhService.themGhiChuNoiBo(phieuMau.getMaPhieu(), 999999L, "Ghi chú hợp lệ");
        }, "Chặn nhân viên không tồn tại");
    }

    @Test
    @DisplayName("US-46: Tra cứu đối chiếu 3 bên (Khách hàng - Shop - Shipper POD & Lịch sử đơn) đầy đủ 100%")
    void testLayDuLieuDoiChieuBaBen() {
        DoiChieuBaBenDTO doiChieu = cskhService.layDuLieuDoiChieuBaBen(phieuMau.getMaPhieu());

        assertNotNull(doiChieu);
        // Bên 1: Khách hàng
        assertNotNull(doiChieu.getPhieuKhieuNai(), "Phải có thông tin phiếu khiếu nại (Khách hàng)");
        assertFalse(doiChieu.getDanhSachBangChungKhachHang().isEmpty(), "Phải có bằng chứng khiếu nại của khách");

        // Bên 2: Người bán / Gian hàng
        assertNotNull(doiChieu.getDonHangShop(), "Phải có thông tin đơn hàng shop (Người bán)");
        assertFalse(doiChieu.getDanhSachChiTietDonHang().isEmpty(), "Phải có danh sách sản phẩm Shop đóng gói gửi đi");

        // Bên 3: Vận chuyển & Shipper POD
        assertNotNull(doiChieu.getNhiemVuGiaoHang(), "Phải có nhiệm vụ giao hàng của logistics");
        assertNotNull(doiChieu.getNhiemVuGiaoHang().getLinkAnhBangChungPod(), "Phải có ảnh bằng chứng POD của shipper");
        assertNotNull(doiChieu.getTaiXe(), "Phải có thông tin tài xế giao vận");

        // Timeline & Ghi chú nội bộ
        assertFalse(doiChieu.getDanhSachLichSuTrangThai().isEmpty(), "Phải có danh sách lịch sử hành trình đơn");
        assertNotNull(doiChieu.getDanhSachGhiChuNoiBo(), "Phải có danh sách ghi chú nội bộ");
    }
}
