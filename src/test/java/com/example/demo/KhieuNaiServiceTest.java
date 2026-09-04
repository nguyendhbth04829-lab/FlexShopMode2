package com.example.demo;

import com.example.demo.dto.ThongKeKhieuNaiDTO;
import com.example.demo.dto.YeuCauKhieuNaiForm;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.PhieuKhieuNaiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@Transactional
class KhieuNaiServiceTest {

    @Autowired
    private PhieuKhieuNaiService phieuKhieuNaiService;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private DiaChiNguoiDungRepository diaChiNguoiDungRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    private DonHangShop taoDonHangTest(String trangThaiDon) {
        NguoiDung user = nguoiDungRepository.findAll().get(0);
        GianHang shop = gianHangRepository.findAll().get(0);
        DiaChiNguoiDung diaChi = diaChiNguoiDungRepository.findAll().get(0);

        DonHangTong master = new DonHangTong();
        master.setMaCodeDonTong("TEST-ORDER-" + UUID.randomUUID().toString().substring(0, 8));
        master.setKhachHang(user);
        master.setDiaChiGiao(diaChi);
        master.setTongTienHang(new BigDecimal("500000.00"));
        master.setTongPhiVanChuyen(new BigDecimal("30000.00"));
        master.setTongThanhToanCuoi(new BigDecimal("530000.00"));
        master.setPhuongThucThanhToan("COD");
        master.setTrangThaiDonHang("HOAN_TAT");
        master = donHangTongRepository.save(master);

        DonHangShop shopOrder = new DonHangShop();
        shopOrder.setMaCodeDonShop("SHOP-TEST-" + UUID.randomUUID().toString().substring(0, 8));
        shopOrder.setDonHangTong(master);
        shopOrder.setGianHang(shop);
        shopOrder.setTienHangShop(new BigDecimal("500000.00"));
        shopOrder.setPhiVanChuyen(new BigDecimal("30000.00"));
        shopOrder.setTongTienShopNhan(new BigDecimal("530000.00"));
        shopOrder.setTrangThai(trangThaiDon);
        return donHangShopRepository.save(shopOrder);
    }

    @Test
    void testTaoPhieuKhieuNaiVaHuyPhieu() throws Exception {
        DonHangShop donHang = taoDonHangTest("DA_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        // Giả lập tải lên 1 ảnh và 1 video bằng chứng lỗi
        MockMultipartFile anhLoi = new MockMultipartFile(
                "filesBangChung",
                "hop_bi_vo.png",
                "image/png",
                "fake image content".getBytes()
        );

        MockMultipartFile videoLoi = new MockMultipartFile(
                "filesBangChung",
                "video_mo_hop.mp4",
                "video/mp4",
                "fake video content".getBytes()
        );

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(donHang.getMaDonHangShop());
        form.setLoaiKhieuNai("HONG_VO");
        form.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form.setMucDoUuTien("CAO");
        form.setSoTienHoanTra(new BigDecimal("500000.00"));
        form.setNoiDungMoTa("Kiện hàng bị móp méo nghiêm trọng khi nhận từ shipper, sản phẩm bên trong bị nứt vỡ màn hình.");
        form.setFilesBangChung(new MultipartFile[]{anhLoi, videoLoi});

        // 1. Thực hiện tạo khiếu nại US-45
        PhieuKhieuNai phieuMoi = phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form);

        // Kiểm tra kết quả
        Assertions.assertNotNull(phieuMoi.getMaPhieu(), "Mã phiếu ID không được null");
        Assertions.assertNotNull(phieuMoi.getMaCodePhieu(), "Mã code phiếu phải được sinh tự động");
        Assertions.assertTrue(phieuMoi.getMaCodePhieu().startsWith("KN-"), "Mã code phiếu phải có tiền tố KN-");
        Assertions.assertEquals("MO_MOI", phieuMoi.getTrangThai(), "Trạng thái ban đầu phải là MO_MOI");
        Assertions.assertEquals("HONG_VO", phieuMoi.getLoaiKhieuNai());
        Assertions.assertEquals(2, phieuMoi.getDanhSachBangChung().size(), "Phải lưu đủ 2 bằng chứng ảnh và video");

        // 2. Kiểm tra chi tiết phiếu
        PhieuKhieuNai chiTiet = phieuKhieuNaiService.layChiTietPhieu(phieuMoi.getMaPhieu());
        Assertions.assertNotNull(chiTiet);
        Assertions.assertEquals(phieuMoi.getMaCodePhieu(), chiTiet.getMaCodePhieu());

        // 3. Kiểm tra thống kê dashboard
        ThongKeKhieuNaiDTO thongKe = phieuKhieuNaiService.layThongKeKhieuNai(khachHang.getMaNguoiDung());
        Assertions.assertNotNull(thongKe);
        Assertions.assertTrue(thongKe.getTongSoPhieu() > 0, "Tổng số phiếu phải > 0");

        // 4. Kiểm tra hủy phiếu
        boolean huyThanhCong = phieuKhieuNaiService.huyKhieuNai(phieuMoi.getMaPhieu(), khachHang.getMaNguoiDung());
        Assertions.assertTrue(huyThanhCong, "Hủy phiếu khi đang ở MO_MOI phải thành công");

        PhieuKhieuNai phieuSauHuy = phieuKhieuNaiService.layChiTietPhieu(phieuMoi.getMaPhieu());
        Assertions.assertEquals("DA_HUY", phieuSauHuy.getTrangThai(), "Trạng thái sau khi hủy phải là DA_HUY");
    }

    @Test
    void testValidateTienHoanVuotQuaDonHang() {
        DonHangShop donHang = taoDonHangTest("DA_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(donHang.getMaDonHangShop());
        form.setLoaiKhieuNai("HONG_VO");
        form.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form.setSoTienHoanTra(donHang.getTongTienShopNhan().add(new BigDecimal("100000"))); // Vượt quá tổng tiền
        form.setNoiDungMoTa("Mô tả test hợp lệ đủ 10 ký tự chi tiết");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form);
        }, "Hệ thống phải chặn số tiền hoàn vượt quá giá trị đơn hàng");
    }

    @Test
    void testValidateDonHangChuaGiaoKhongDuocKhieuNai() {
        DonHangShop donHang = taoDonHangTest("DANG_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(donHang.getMaDonHangShop());
        form.setLoaiKhieuNai("HONG_VO");
        form.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form.setNoiDungMoTa("Mô tả test đơn hàng chưa giao");

        Assertions.assertThrows(IllegalStateException.class, () -> {
            phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form);
        }, "Hệ thống phải chặn khiếu nại đối với đơn hàng chưa giao thành công");
    }
}
