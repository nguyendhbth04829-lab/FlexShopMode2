package com.example.demo;

import com.example.demo.dto.ThongKeKhieuNaiDTO;
import com.example.demo.dto.YeuCauKhieuNaiForm;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.PhieuKhieuNaiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class KhieuNaiServiceTest {

    @Autowired
    private PhieuKhieuNaiService phieuKhieuNaiService;

    @Autowired
    private PhieuKhieuNaiRepository phieuKhieuNaiRepository;

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
    @DisplayName("US-45: Tạo khiếu nại thành công kèm ảnh/video và hủy phiếu hợp lệ")
    void testTaoPhieuKhieuNaiVaHuyPhieu() throws Exception {
        DonHangShop donHang = taoDonHangTest("DA_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

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
        assertNotNull(phieuMoi.getMaPhieu(), "Mã phiếu ID không được null");
        assertNotNull(phieuMoi.getMaCodePhieu(), "Mã code phiếu phải được sinh tự động");
        assertTrue(phieuMoi.getMaCodePhieu().startsWith("KN-"), "Mã code phiếu phải có tiền tố KN-");
        assertEquals("MO_MOI", phieuMoi.getTrangThai(), "Trạng thái ban đầu phải là MO_MOI");
        assertEquals("HONG_VO", phieuMoi.getLoaiKhieuNai());
        assertEquals(2, phieuMoi.getDanhSachBangChung().size(), "Phải lưu đủ 2 bằng chứng ảnh và video");

        // 2. Kiểm tra chi tiết phiếu
        PhieuKhieuNai chiTiet = phieuKhieuNaiService.layChiTietPhieu(phieuMoi.getMaPhieu());
        assertNotNull(chiTiet);
        assertEquals(phieuMoi.getMaCodePhieu(), chiTiet.getMaCodePhieu());

        // 3. Kiểm tra thống kê dashboard
        ThongKeKhieuNaiDTO thongKe = phieuKhieuNaiService.layThongKeKhieuNai(khachHang.getMaNguoiDung());
        assertNotNull(thongKe);
        assertTrue(thongKe.getTongSoPhieu() > 0, "Tổng số phiếu phải > 0");

        // 4. Kiểm tra hủy phiếu
        boolean huyThanhCong = phieuKhieuNaiService.huyKhieuNai(phieuMoi.getMaPhieu(), khachHang.getMaNguoiDung());
        assertTrue(huyThanhCong, "Hủy phiếu khi đang ở MO_MOI phải thành công");

        PhieuKhieuNai phieuSauHuy = phieuKhieuNaiService.layChiTietPhieu(phieuMoi.getMaPhieu());
        assertEquals("DA_HUY", phieuSauHuy.getTrangThai(), "Trạng thái sau khi hủy phải là DA_HUY");
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn số tiền hoàn vượt quá tổng tiền đơn hàng")
    void testValidateTienHoanVuotQuaDonHang() {
        DonHangShop donHang = taoDonHangTest("DA_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        MockMultipartFile anh = new MockMultipartFile("filesBangChung", "anh.jpg", "image/jpeg", "content".getBytes());

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(donHang.getMaDonHangShop());
        form.setLoaiKhieuNai("HONG_VO");
        form.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form.setSoTienHoanTra(donHang.getTongTienShopNhan().add(new BigDecimal("100000"))); // Vượt quá tổng tiền
        form.setNoiDungMoTa("Mô tả test hợp lệ đủ 10 ký tự chi tiết");
        form.setFilesBangChung(new MultipartFile[]{anh});

        assertThrows(IllegalArgumentException.class, () -> {
            phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form);
        }, "Hệ thống phải chặn số tiền hoàn vượt quá giá trị đơn hàng");
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn khiếu nại đối với đơn hàng chưa giao thành công")
    void testValidateDonHangChuaGiaoKhongDuocKhieuNai() {
        DonHangShop donHang = taoDonHangTest("DANG_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(donHang.getMaDonHangShop());
        form.setLoaiKhieuNai("HONG_VO");
        form.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form.setNoiDungMoTa("Mô tả test đơn hàng chưa giao");

        assertThrows(IllegalStateException.class, () -> {
            phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form);
        }, "Hệ thống phải chặn khiếu nại đối với đơn hàng chưa giao thành công");
    }

    @Test
    @DisplayName("Validate khắc khe: Bắt buộc đính kèm bằng chứng ảnh/video khi khiếu nại hỏng vỡ")
    void testValidateBatBuocBangChungKhiHongVo() {
        DonHangShop donHang = taoDonHangTest("DA_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(donHang.getMaDonHangShop());
        form.setLoaiKhieuNai("HONG_VO");
        form.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form.setNoiDungMoTa("Tai nghe nhận được bị nứt vỡ nhưng cố tình không gửi ảnh.");
        form.setFilesBangChung(null); // Không có file

        assertThrows(IllegalArgumentException.class, () -> {
            phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form);
        }, "Hệ thống phải chặn khiếu nại hỏng vỡ mà không có ảnh/video bằng chứng");
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn khiếu nại trùng lặp trên cùng một đơn hàng")
    void testValidateChongKhieuNaiTrungLap() throws Exception {
        DonHangShop donHang = taoDonHangTest("DA_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        MockMultipartFile anh = new MockMultipartFile("filesBangChung", "anh.jpg", "image/jpeg", "content".getBytes());

        YeuCauKhieuNaiForm form1 = new YeuCauKhieuNaiForm();
        form1.setMaDonHangShop(donHang.getMaDonHangShop());
        form1.setLoaiKhieuNai("HONG_VO");
        form1.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form1.setNoiDungMoTa("Khiếu nại lần 1 hợp lệ");
        form1.setFilesBangChung(new MultipartFile[]{anh});

        // Tạo phiếu 1 thành công
        phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form1);

        // Cố tình tạo phiếu 2 trên cùng đơn hàng
        YeuCauKhieuNaiForm form2 = new YeuCauKhieuNaiForm();
        form2.setMaDonHangShop(donHang.getMaDonHangShop());
        form2.setLoaiKhieuNai("HONG_VO");
        form2.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form2.setNoiDungMoTa("Khiếu nại lần 2 cố tình spam");
        form2.setFilesBangChung(new MultipartFile[]{anh});

        assertThrows(IllegalStateException.class, () -> {
            phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form2);
        }, "Hệ thống phải chặn khiếu nại trùng lặp khi đơn đã có phiếu đang mở");
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn tệp tin định dạng độc hại (.exe, .sh)")
    void testValidateChanTepTinDocHai() {
        DonHangShop donHang = taoDonHangTest("DA_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        MockMultipartFile fileDocHai = new MockMultipartFile(
                "filesBangChung",
                "virus_payload.exe",
                "application/x-msdownload",
                "evil content".getBytes()
        );

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(donHang.getMaDonHangShop());
        form.setLoaiKhieuNai("HONG_VO");
        form.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form.setNoiDungMoTa("Cố tình tải file exe độc hại");
        form.setFilesBangChung(new MultipartFile[]{fileDocHai});

        assertThrows(IllegalArgumentException.class, () -> {
            phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form);
        }, "Hệ thống phải chặn tệp tin độc hại không đúng định dạng cho phép");
    }

    @Test
    @DisplayName("Validate khắc khe: Chặn khách hàng hủy phiếu đã có phán quyết hoàn tiền")
    void testValidateChanHuyPhieuDaPhanQuyet() throws Exception {
        DonHangShop donHang = taoDonHangTest("DA_GIAO");
        NguoiDung khachHang = donHang.getDonHangTong().getKhachHang();

        MockMultipartFile anh = new MockMultipartFile("filesBangChung", "anh.jpg", "image/jpeg", "content".getBytes());

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        form.setMaDonHangShop(donHang.getMaDonHangShop());
        form.setLoaiKhieuNai("HONG_VO");
        form.setGiaiPhapYeuCau("HOAN_TIEN_TRA_HANG");
        form.setNoiDungMoTa("Khiếu nại hợp lệ để test hủy phiếu");
        form.setFilesBangChung(new MultipartFile[]{anh});

        PhieuKhieuNai p = phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(khachHang.getMaNguoiDung(), form);

        // Giả lập phiếu đã được CSKH chấp thuận hoàn tiền
        p.setTrangThai("CHAP_NHAN_HOAN_TIEN");
        phieuKhieuNaiRepository.save(p);

        assertThrows(IllegalStateException.class, () -> {
            phieuKhieuNaiService.huyKhieuNai(p.getMaPhieu(), khachHang.getMaNguoiDung());
        }, "Hệ thống phải chặn hủy phiếu khi đã có phán quyết chấp nhận hoàn tiền");
    }
}
