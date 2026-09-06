package com.example.demo;

import com.example.demo.dto.ThongKeViXuDTO;
import com.example.demo.entity.DonHangTong;
import com.example.demo.entity.LichSuGiaoDichXu;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.ViXuNguoiDung;
import com.example.demo.repository.DonHangTongRepository;
import com.example.demo.repository.LichSuGiaoDichXuRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.ViXuNguoiDungRepository;
import com.example.demo.service.ViXuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho US-55 (Module: COIN / REWARD):
 * Tích lũy & Sử dụng Điểm thưởng/Xu (Coin Reward) để giảm trừ trực tiếp vào đơn hàng.
 */
@SpringBootTest
public class ViXuServiceTest {

    @Autowired
    private ViXuService viXuService;

    @Autowired
    private ViXuNguoiDungRepository viXuNguoiDungRepository;

    @Autowired
    private LichSuGiaoDichXuRepository lichSuGiaoDichXuRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    private NguoiDung layNguoiDungTest() {
        return nguoiDungRepository.findByEmail("khachhang@flexshop.vn")
                .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElseThrow());
    }

    private DonHangTong layDonHangTest(Long maNguoiDung) {
        DonHangTong dh = donHangTongRepository.findByKhachHangMaNguoiDungOrderByNgayTaoDesc(maNguoiDung).stream()
                .findFirst()
                .orElseGet(() -> donHangTongRepository.findAll().stream().findFirst().orElseThrow());
        if (dh.getSoXuDaDung() != null && dh.getSoXuDaDung() > 0) {
            dh.setTongThanhToanCuoi(dh.getTongThanhToanCuoi().add(dh.getSoTienGiamTuXu()));
            dh.setSoXuDaDung(0L);
            dh.setSoTienGiamTuXu(BigDecimal.ZERO);
            dh = donHangTongRepository.saveAndFlush(dh);
        }
        return dh;
    }

    @Test
    @Transactional
    @DisplayName("Test 1: Khởi tạo và lấy thông tin Ví Xu người dùng")
    public void testLayHoacTaoViXu() {
        NguoiDung user = layNguoiDungTest();

        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());

        assertNotNull(viXu);
        assertEquals(user.getMaNguoiDung(), viXu.getMaNguoiDung());
        assertNotNull(viXu.getSoXuHienTai());
        assertEquals(viXu.getSoXuHienTai(), viXu.getGiaTriQuyDoiVnd());
    }

    @Test
    @Transactional
    @DisplayName("Test 2: Điểm danh nhận thưởng xu hàng ngày (+1.000 Xu)")
    public void testDiemDanhNhanXuThanhCong() {
        NguoiDung user = layNguoiDungTest();
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        long soXuTruoc = viXu.getSoXuHienTai();

        // Xóa lịch sử điểm danh hôm nay nếu có để đảm bảo test chạy độc lập
        LocalDateTime dauNgay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime cuoiNgay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        // Nếu hôm nay đã điểm danh, test qua logic chặn
        if (!lichSuGiaoDichXuRepository.daDiemDanhHomNay(user.getMaNguoiDung(), dauNgay, cuoiNgay)) {
            Long nhanDuoc = viXuService.diemDanhNhanXu(user.getMaNguoiDung());
            assertEquals(1000L, nhanDuoc);

            ViXuNguoiDung viSau = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
            assertEquals(soXuTruoc + 1000L, viSau.getSoXuHienTai());
        } else {
            // Đã điểm danh rồi thì phải ném lỗi
            assertThrows(IllegalStateException.class, () -> {
                viXuService.diemDanhNhanXu(user.getMaNguoiDung());
            });
        }
    }

    @Test
    @Transactional
    @DisplayName("Test 3: Chặn điểm danh nhận xu lần thứ 2 trong cùng một ngày")
    public void testChanDiemDanhLanHaiTrongCungNgay() {
        NguoiDung user = layNguoiDungTest();
        LocalDateTime dauNgay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime cuoiNgay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        // Đảm bảo đã có 1 lần điểm danh hôm nay
        if (!lichSuGiaoDichXuRepository.daDiemDanhHomNay(user.getMaNguoiDung(), dauNgay, cuoiNgay)) {
            viXuService.diemDanhNhanXu(user.getMaNguoiDung());
        }

        // Lần gọi tiếp theo phải bị chặn
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            viXuService.diemDanhNhanXu(user.getMaNguoiDung());
        });
        assertTrue(ex.getMessage().contains("đã điểm danh nhận xu"));
    }

    @Test
    @Transactional
    @DisplayName("Test 4: Áp dụng Xu vào Đơn hàng thành công (Giảm trừ trực tiếp tổng thanh toán)")
    public void testApDungXuVaoDonHangThanhCong() {
        NguoiDung user = layNguoiDungTest();
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());

        // Nạp đủ xu để test
        viXu.setSoXuHienTai(50000L);
        viXuNguoiDungRepository.save(viXu);

        DonHangTong donHang = layDonHangTest(user.getMaNguoiDung());
        BigDecimal tongThanhToanTruoc = donHang.getTongThanhToanCuoi();

        // Dùng 10.000 Xu (tương đương giảm 10.000 đ)
        long soXuDung = 10000L;
        DonHangTong donSauKhiApXu = viXuService.apDungXuVaoDonHang(user.getMaNguoiDung(), donHang.getMaCodeDonTong(), soXuDung);

        assertEquals(soXuDung, donSauKhiApXu.getSoXuDaDung());
        assertEquals(0, BigDecimal.valueOf(soXuDung).compareTo(donSauKhiApXu.getSoTienGiamTuXu()));
        assertEquals(0, tongThanhToanTruoc.subtract(BigDecimal.valueOf(soXuDung)).compareTo(donSauKhiApXu.getTongThanhToanCuoi()));

        // Kiểm tra số dư ví giảm chính xác
        ViXuNguoiDung viSau = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        assertEquals(40000L, viSau.getSoXuHienTai());
    }

    @Test
    @Transactional
    @DisplayName("Test 5: Chặn sử dụng xu vượt quá số dư khả dụng trong ví")
    public void testChanDungXuVuotQuaSoDuVi() {
        NguoiDung user = layNguoiDungTest();
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        viXu.setSoXuHienTai(5000L);
        viXuNguoiDungRepository.save(viXu);

        DonHangTong donHang = layDonHangTest(user.getMaNguoiDung());

        // Cố tình dùng 20.000 xu trong khi ví chỉ có 5.000 xu
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            viXuService.apDungXuVaoDonHang(user.getMaNguoiDung(), donHang.getMaCodeDonTong(), 20000L);
        });
        assertTrue(ex.getMessage().contains("vượt quá số dư"));
    }

    @Test
    @Transactional
    @DisplayName("Test 6: Chặn sử dụng xu vượt quá 50% giá trị đơn hàng")
    public void testChanDungXuVuotQua50PhanTramDonHang() {
        NguoiDung user = layNguoiDungTest();
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        viXu.setSoXuHienTai(100000000L); // 100 triệu xu
        viXuNguoiDungRepository.save(viXu);

        DonHangTong donHang = layDonHangTest(user.getMaNguoiDung());
        BigDecimal tongTien = donHang.getTongThanhToanCuoi();
        long vuot50PhanTram = tongTien.multiply(new BigDecimal("0.60")).longValue();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            viXuService.apDungXuVaoDonHang(user.getMaNguoiDung(), donHang.getMaCodeDonTong(), vuot50PhanTram);
        });
        assertTrue(ex.getMessage().contains("tối đa 50%"));
    }

    @Test
    @Transactional
    @DisplayName("Test 7: Hủy áp dụng xu và hoàn trả số dư về ví người dùng")
    public void testHuyApDungXuVaHoanTraVi() {
        NguoiDung user = layNguoiDungTest();
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        viXu.setSoXuHienTai(30000L);
        viXuNguoiDungRepository.save(viXu);

        DonHangTong donHang = layDonHangTest(user.getMaNguoiDung());
        BigDecimal tongTienGoc = donHang.getTongThanhToanCuoi();

        // 1. Áp dụng 15.000 xu
        viXuService.apDungXuVaoDonHang(user.getMaNguoiDung(), donHang.getMaCodeDonTong(), 15000L);
        assertEquals(15000L, viXuService.layHoacTaoViXu(user.getMaNguoiDung()).getSoXuHienTai());

        // 2. Hủy áp dụng xu
        DonHangTong donSauHuy = viXuService.huyApDungXu(user.getMaNguoiDung(), donHang.getMaCodeDonTong());
        assertEquals(0L, donSauHuy.getSoXuDaDung());
        assertEquals(0, BigDecimal.ZERO.compareTo(donSauHuy.getSoTienGiamTuXu()));
        assertEquals(0, tongTienGoc.compareTo(donSauHuy.getTongThanhToanCuoi()));

        // Ví được hoàn lại 30.000 xu ban đầu
        assertEquals(30000L, viXuService.layHoacTaoViXu(user.getMaNguoiDung()).getSoXuHienTai());
    }

    @Test
    @Transactional
    @DisplayName("Test 8: Tự động tích lũy 1% Xu khi đơn hàng hoàn thành")
    public void testTichXuKhiHoanThanhDonHang() {
        NguoiDung user = layNguoiDungTest();
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        long soXuTruoc = viXu.getSoXuHienTai();

        DonHangTong donHang = layDonHangTest(user.getMaNguoiDung());
        long xuExpected = (long) Math.floor(donHang.getTongTienHang().doubleValue() * 0.01);

        Long xuTich = viXuService.tichXuKhiHoanThanhDon(donHang.getMaCodeDonTong());
        assertEquals(xuExpected, xuTich);

        ViXuNguoiDung viSau = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        assertEquals(soXuTruoc + xuExpected, viSau.getSoXuHienTai());
    }

    @Test
    @Transactional
    @DisplayName("Test 9: Tính toán giới hạn số xu tối đa cho phép sử dụng trên đơn hàng")
    public void testTinhSoXuToiDaChoPhep() {
        NguoiDung user = layNguoiDungTest();
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        viXu.setSoXuHienTai(20000L);
        viXuNguoiDungRepository.save(viXu);

        DonHangTong donHang = layDonHangTest(user.getMaNguoiDung());
        Long toiDa = viXuService.tinhSoXuToiDaChoPhep(user.getMaNguoiDung(), donHang);

        assertNotNull(toiDa);
        assertTrue(toiDa <= viXu.getSoXuHienTai());
        assertTrue(toiDa <= donHang.getTongThanhToanCuoi().longValue());
    }

    @Test
    @Transactional
    @DisplayName("Test 10: Thống kê số dư Ví Xu và Phân trang Lịch sử giao dịch")
    public void testThongKeVaPhanTrangLichSu() {
        NguoiDung user = layNguoiDungTest();

        ThongKeViXuDTO thongKe = viXuService.layThongKeViXu(user.getMaNguoiDung());
        assertNotNull(thongKe);
        assertNotNull(thongKe.getDinhDangSoXuHienTai());
        assertNotNull(thongKe.getDinhDangGiaTriQuyDoi());

        Page<LichSuGiaoDichXu> pageAll = viXuService.layLichSuGiaoDichPhanTrang(user.getMaNguoiDung(), "TAT_CA", 0, 10);
        assertNotNull(pageAll);

        Page<LichSuGiaoDichXu> pageCong = viXuService.layLichSuGiaoDichPhanTrang(user.getMaNguoiDung(), "CONG_XU", 0, 10);
        assertNotNull(pageCong);
    }
}
