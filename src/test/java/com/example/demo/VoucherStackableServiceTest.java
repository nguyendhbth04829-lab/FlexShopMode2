package com.example.demo;

import com.example.demo.dto.ApDungVoucherRequestDTO;
import com.example.demo.dto.KetQuaApDungVoucherDTO;
import com.example.demo.dto.KetQuaPhanBoShopDTO;
import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.DonHangTong;
import com.example.demo.entity.LichSuDungMaGiamGia;
import com.example.demo.entity.MaGiamGia;
import com.example.demo.entity.NguoiDung;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.DonHangTongRepository;
import com.example.demo.repository.LichSuDungMaGiamGiaRepository;
import com.example.demo.repository.MaGiamGiaRepository;
import com.example.demo.service.VoucherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho US-52 (Module: VOUCHER):
 * Thuật toán Voucher lồng nhau 3 tầng (Stackable Vouchers) & Validate khắt khe 100%.
 */
@SpringBootTest
public class VoucherStackableServiceTest {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private LichSuDungMaGiamGiaRepository lichSuDungMaGiamGiaRepository;

    private Long testDonHangTongId;
    private static final Long MA_KHACH_HANG_TEST = 4L;

    @BeforeEach
    public void setup() {
        // Tìm đơn hàng mẫu kiểm thử US-52
        DonHangTong donTong = donHangTongRepository.findByMaCodeDonTong("MASTER-US52-STACKABLE").orElse(null);
        if (donTong != null) {
            testDonHangTongId = donTong.getMaDonHangTong();
        } else {
            List<DonHangTong> all = donHangTongRepository.findAll();
            if (!all.isEmpty()) {
                testDonHangTongId = all.get(0).getMaDonHangTong();
            }
        }
    }

    @Test
    @DisplayName("Test 1: Áp dụng thành công đồng thời cả 3 tầng voucher (Freeship Sàn + Voucher Sàn + Voucher Shop)")
    public void testApDungDongThoi3TangVoucherThanhCong() {
        assertNotNull(testDonHangTongId, "Cần có đơn hàng để kiểm thử");

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaFreeshipSan("FREESHIP30K"); // Tầng 3
        request.setMaVoucherSan("FLEXSAN100K");   // Tầng 2

        Map<Long, String> shopMap = new HashMap<>();
        shopMap.put(1L, "TECHZONE50K"); // Tầng 1 (Shop 1)
        shopMap.put(2L, "FASHION20K");  // Tầng 1 (Shop 2)
        request.setMaVoucherShopMap(shopMap);

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        // Khẳng định trạng thái hợp lệ
        assertTrue(ketQua.isHopLe(), "Áp dụng cả 3 tầng voucher phải hợp lệ 100%");
        assertTrue(ketQua.getDanhSachLoi().isEmpty(), "Không được có bất kỳ lỗi nào");

        // Kiểm tra số tiền giảm từng tầng
        assertEquals(new BigDecimal("30000.00"), ketQua.getGiamGiaFreeshipSan(), "Freeship sàn phải giảm đúng 30.000đ");
        assertEquals(new BigDecimal("100000.00"), ketQua.getGiamGiaVoucherSan(), "Voucher sàn phải giảm đúng 100.000đ");
        assertEquals(new BigDecimal("70000.00"), ketQua.getTongGiamGiaShop(), "Tổng voucher shop phải giảm đúng 70.000đ (50k + 20k)");

        // Kiểm tra nguồn tiền tài trợ
        assertEquals(new BigDecimal("130000.00"), ketQua.getTongTaiTroSan(), "Sàn phải tài trợ 130.000đ (100k voucher + 30k ship)");
        assertEquals(new BigDecimal("70000.00"), ketQua.getTongTaiTroShop(), "Các shop phải tài trợ 70.000đ");
        assertEquals(new BigDecimal("200000.00"), ketQua.getTongTietKiem(), "Tổng khách tiết kiệm được là 200.000đ");
    }

    @Test
    @DisplayName("Test 2: Kiểm định bảo toàn cân bằng tài chính 100% (Khách trả + Sàn tài trợ + Shop tài trợ == Tổng gốc)")
    public void testBaoToanDongTienTaiChinhTuyetDoi() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaFreeshipSan("FREESHIP50K");
        request.setMaVoucherSan("FLEXSAN10PCT"); // Giảm 10%

        Map<Long, String> shopMap = new HashMap<>();
        shopMap.put(1L, "TECHZONE50K");
        request.setMaVoucherShopMap(shopMap);

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        assertTrue(ketQua.isHopLe());
        assertTrue(ketQua.isBaoToanTaiChinh(), "Dòng tiền phải được bảo toàn chính xác 100%");

        BigDecimal tongGoc = ketQua.getTongTienHangGoc().add(ketQua.getTongPhiVanChuyenGoc());
        BigDecimal tongKhachVaTroGia = ketQua.getTongThanhToanCuoi()
                .add(ketQua.getTongTaiTroSan())
                .add(ketQua.getTongTaiTroShop());

        assertEquals(0, tongGoc.compareTo(tongKhachVaTroGia), "Tổng gốc phải bằng Khách trả + Sàn tài trợ + Shop tài trợ");
    }

    @Test
    @DisplayName("Test 3: Kiểm tra phân bổ tỷ trọng Voucher Sàn cho từng Shop theo tỷ lệ tiền hàng")
    public void testPhanBoTyTrongVoucherSanChinhXac() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaVoucherSan("FLEXSAN100K"); // 100k sàn phân bổ cho 2 shop

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        assertTrue(ketQua.isHopLe());
        assertFalse(ketQua.getDanhSachPhanBoShop().isEmpty(), "Phải có danh sách phân bổ từng shop");

        BigDecimal tongPhanBo = BigDecimal.ZERO;
        for (KetQuaPhanBoShopDTO pb : ketQua.getDanhSachPhanBoShop()) {
            tongPhanBo = tongPhanBo.add(pb.getGiamGiaVoucherSan());
            // Doanh thu shop nhận không bị trừ khoản sàn tài trợ
            assertEquals(pb.getTienHangGoc().subtract(pb.getGiamGiaVoucherShop()), pb.getTongTienShopNhan(),
                    "Tiền shop nhận chỉ trừ voucher của shop đó, không bị trừ voucher sàn");
        }

        assertEquals(0, ketQua.getGiamGiaVoucherSan().compareTo(tongPhanBo),
                "Tổng tiền sàn phân bổ cho các shop phải bằng chính xác 100% voucher sàn");
    }

    @Test
    @DisplayName("Test 4: Validate chặn mã voucher đã hết hạn sử dụng")
    public void testValidateChanVoucherHetHan() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaVoucherSan("HETHAN100K"); // Đã hết hạn

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        assertFalse(ketQua.isHopLe(), "Mã hết hạn không được phép áp dụng");
        assertFalse(ketQua.getDanhSachLoi().isEmpty());
        assertTrue(ketQua.getDanhSachLoi().get(0).contains("đã hết hạn sử dụng"),
                "Thông báo lỗi phải nêu rõ mã đã hết hạn sử dụng");
    }

    @Test
    @DisplayName("Test 5: Validate chặn mã voucher đã hết lượt sử dụng trên hệ thống")
    public void testValidateChanVoucherHetLuotDungHeThong() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaVoucherSan("HETLUOT50K"); // Đã dùng hết 10/10

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        assertFalse(ketQua.isHopLe(), "Mã hết lượt dùng không được phép áp dụng");
        assertFalse(ketQua.getDanhSachLoi().isEmpty());
        assertTrue(ketQua.getDanhSachLoi().get(0).contains("đã hết lượt sử dụng trên hệ thống"),
                "Thông báo lỗi phải nêu rõ mã đã hết lượt dùng");
    }

    @Test
    @DisplayName("Test 6: Validate chặn đơn hàng chưa đạt giá trị đơn tối thiểu")
    public void testValidateChanDonHangChuaDatGiaTriToiThieu() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaVoucherSan("DONCAO2TR"); // Đơn tối thiểu 2.000.000đ trong khi đơn mẫu là 1.410.000đ

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        assertFalse(ketQua.isHopLe(), "Đơn chưa đủ giá trị tối thiểu không được áp dụng");
        assertFalse(ketQua.getDanhSachLoi().isEmpty());
        assertTrue(ketQua.getDanhSachLoi().get(0).contains("chưa đạt giá trị tối thiểu"),
                "Thông báo lỗi phải chỉ rõ chưa đạt giá trị tối thiểu");
    }

    @Test
    @DisplayName("Test 7: Validate chặn dùng voucher của Shop A cho Shop B")
    public void testValidateChanDungVoucherShopChoShopKhac() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);

        Map<Long, String> shopMap = new HashMap<>();
        // Shop 2 là Flex Fashion nhưng lại điền mã TECHZONE50K của Shop 1
        shopMap.put(2L, "TECHZONE50K");
        request.setMaVoucherShopMap(shopMap);

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        assertFalse(ketQua.isHopLe(), "Không được dùng voucher shop này cho shop khác");
        assertFalse(ketQua.getDanhSachLoi().isEmpty());
        assertTrue(ketQua.getDanhSachLoi().get(0).contains("không thể áp dụng cho gian hàng"),
                "Thông báo lỗi phải cảnh báo sai gian hàng");
    }

    @Test
    @DisplayName("Test 8: Validate chặn dùng sai loại voucher (nhập voucher tiền hàng vào ô Freeship)")
    public void testValidateChanDungSaiLoaiVoucher() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaFreeshipSan("FLEXSAN100K"); // Điền mã tiền hàng vào ô Freeship

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        assertFalse(ketQua.isHopLe(), "Không được nhập mã tiền hàng vào ô Freeship");
        assertFalse(ketQua.getDanhSachLoi().isEmpty());
        assertTrue(ketQua.getDanhSachLoi().get(0).contains("không phải là mã Freeship"),
                "Thông báo lỗi phải nhắc nhở chọn đúng ô Freeship");
    }

    @Test
    @DisplayName("Test 9: Validate chặn dùng mã Freeship vào ô Voucher Sàn")
    public void testValidateChanDungFreeshipChoVoucherSan() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaVoucherSan("FREESHIP30K"); // Điền mã Freeship vào ô Voucher Sàn

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, MA_KHACH_HANG_TEST);

        assertFalse(ketQua.isHopLe(), "Không được nhập mã Freeship vào ô Voucher Sàn");
        assertFalse(ketQua.getDanhSachLoi().isEmpty());
        assertTrue(ketQua.getDanhSachLoi().get(0).contains("là mã miễn phí vận chuyển"),
                "Thông báo lỗi phải cảnh báo mã miễn phí vận chuyển");
    }

    @Test
    @Transactional
    @DisplayName("Test 10: Xác nhận chốt đơn: Cập nhật DB, lưu LichSuDungMaGiamGia và tăng so_luong_da_dung")
    public void testXacNhanDonHangVaLuuLichSuThanhCong() {
        assertNotNull(testDonHangTongId);

        ApDungVoucherRequestDTO request = new ApDungVoucherRequestDTO();
        request.setMaDonHangTong(testDonHangTongId);
        request.setMaFreeshipSan("FREESHIP30K");
        request.setMaVoucherSan("FLEXSAN100K");

        Map<Long, String> shopMap = new HashMap<>();
        shopMap.put(1L, "TECHZONE50K");
        request.setMaVoucherShopMap(shopMap);

        // Thực hiện chốt đơn
        KetQuaApDungVoucherDTO ketQua = voucherService.xacNhanApDungVoucherChoDonHang(request, MA_KHACH_HANG_TEST);
        assertTrue(ketQua.isHopLe());

        // 1. Kiểm tra DonHangTong được cập nhật trong DB
        DonHangTong donTongCapNhat = donHangTongRepository.findById(testDonHangTongId).orElse(null);
        assertNotNull(donTongCapNhat);
        assertEquals(0, ketQua.getTongTaiTroSan().compareTo(donTongCapNhat.getTongGiamGiaSan()));
        assertEquals(0, ketQua.getTongTaiTroShop().compareTo(donTongCapNhat.getTongGiamGiaShop()));
        assertEquals(0, ketQua.getTongThanhToanCuoi().compareTo(donTongCapNhat.getTongThanhToanCuoi()));

        // 2. Kiểm tra lịch sử sử dụng được ghi nhận
        List<LichSuDungMaGiamGia> lichSuList = lichSuDungMaGiamGiaRepository.findByNguoiDungMaNguoiDungOrderByNgaySuDungDesc(MA_KHACH_HANG_TEST);
        assertFalse(lichSuList.isEmpty(), "Phải có ít nhất 1 bản ghi lịch sử sử dụng voucher");

        // 3. Kiểm tra số lượng đã dùng của voucher tăng lên
        MaGiamGia vSan = maGiamGiaRepository.findByMaCodeVoucherIgnoreCaseAndDaXoaFalse("FLEXSAN100K").orElse(null);
        assertNotNull(vSan);
        assertTrue(vSan.getSoLuongDaDung() > 0, "Số lượng đã dùng của voucher Sàn phải tăng lên");
    }

    @Test
    @DisplayName("Test 11: Kiểm tra tính tương thích đồng bộ của getHoTen và getHoVaTen trên Entity NguoiDung")
    public void testNguoiDungHoTenVaHoVaTenDongBo() {
        NguoiDung user = new NguoiDung();
        user.setHoVaTen("Nguyễn Văn Khách Hàng");
        assertEquals("Nguyễn Văn Khách Hàng", user.getHoTen(), "getHoTen phải trả về cùng giá trị với getHoVaTen");

        user.setHoTen("Trần Thị Người Mua");
        assertEquals("Trần Thị Người Mua", user.getHoVaTen(), "setHoTen phải cập nhật vào trường hoVaTen");
    }
}
