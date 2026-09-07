package com.example.demo;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.LivestreamService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho US-61 (FlexShop Live & Ghim sản phẩm giảm giá sốc)
 */
@SpringBootTest
public class LivestreamServiceTest {

    @Autowired
    private LivestreamService livestreamService;

    @Autowired
    private PhongLivestreamRepository phongLivestreamRepository;

    @Autowired
    private SanPhamLivestreamRepository sanPhamLivestreamRepository;

    @Autowired
    private BinhLuanLivestreamRepository binhLuanLivestreamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    private NguoiDung laySellerTest() {
        return nguoiDungRepository.findByEmail("techzone@flexshop.vn")
                .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElseThrow());
    }

    private NguoiDung layKhachHangTest() {
        return nguoiDungRepository.findByEmail("khachhang@flexshop.vn")
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> !u.getEmail().contains("techzone"))
                        .findFirst()
                        .orElseThrow());
    }

    private GianHang layGianHangTest() {
        return gianHangRepository.findAll().stream().findFirst().orElseThrow();
    }

    @Test
    @DisplayName("US-61 [TEST-01]: Lấy danh sách phòng live và thống kê KPI Seller")
    public void testDanhSachVaThongKeLivestream() {
        GianHang gianHang = layGianHangTest();
        Page<PhongLivestream> trangLive = livestreamService.timKiemSeller(
                gianHang.getMaGianHang(), null, null, PageRequest.of(0, 10));

        assertNotNull(trangLive, "Trang livestream không được null");
        assertFalse(trangLive.isEmpty(), "Cần có ít nhất 1 phòng livestream mẫu trong CSDL");

        ThongKeLivestreamDTO thongKe = livestreamService.layThongKeSeller(gianHang.getMaGianHang());
        assertNotNull(thongKe, "Thống kê không được null");
        assertTrue(thongKe.getTongSoPhienLive() > 0, "Tổng số phiên live phải > 0");

        System.out.println("✅ [TEST-01 PASS] Tổng phiên live: " + thongKe.getTongSoPhienLive() + ", Đang live: " + thongKe.getSoPhienDangLive());
    }

    @Test
    @Transactional
    @DisplayName("US-61 [TEST-02]: Chức năng CỐT LÕI - Ghim sản phẩm giảm giá sốc lên màn hình Live")
    public void testGhimVaBoGhimSanPham() {
        List<PhongLivestream> phongs = phongLivestreamRepository.findByTrangThai("DANG_LIVE");
        assertFalse(phongs.isEmpty(), "Cần có phòng đang phát live");
        PhongLivestream phong = phongs.get(0);

        List<SanPhamLivestream> spList = livestreamService.layDanhSachSanPhamLive(phong.getMaLive());
        assertFalse(spList.isEmpty(), "Phòng live phải có sản phẩm");
        SanPhamLivestream spMucTieu = spList.get(0);
        Long maSp = spMucTieu.getSanPham().getMaSanPham();

        // Thao tác GHIM sản phẩm
        SanPhamGhimDTO ghimDTO = livestreamService.ghimSanPham(phong.getMaLive(), maSp);
        assertNotNull(ghimDTO, "DTO sản phẩm ghim không được null");
        assertTrue(ghimDTO.getLaSanPhamDangGhim(), "Sản phẩm phải ở trạng thái đang ghim");
        assertEquals(maSp, ghimDTO.getMaSanPham(), "Mã sản phẩm ghim phải khớp");

        // Kiểm tra trong CSDL
        Optional<SanPhamLivestream> optDangGhim = livestreamService.laySanPhamDangGhim(phong.getMaLive());
        assertTrue(optDangGhim.isPresent(), "Phải tìm thấy sản phẩm đang ghim trong phòng live");
        assertEquals(maSp, optDangGhim.get().getSanPham().getMaSanPham(), "Mã sản phẩm đang ghim trong DB phải khớp");

        // Thao tác BỎ GHIM
        livestreamService.boGhimSanPham(phong.getMaLive(), maSp);
        Optional<SanPhamLivestream> optSauBoGhim = livestreamService.laySanPhamDangGhim(phong.getMaLive());
        assertTrue(optSauBoGhim.isEmpty() || !Boolean.TRUE.equals(optSauBoGhim.get().getLaSanPhamDangGhim()), "Sau khi bỏ ghim không còn sản phẩm ghim");

        System.out.println("✅ [TEST-02 PASS] Ghim và bỏ ghim sản phẩm thành công: " + ghimDTO.getTenSanPham());
    }

    @Test
    @Transactional
    @DisplayName("US-61 [TEST-03]: Khách hàng gửi bình luận trực tiếp và Thả tim tương tác")
    public void testBinhLuanVaThaTim() {
        List<PhongLivestream> phongs = phongLivestreamRepository.findByTrangThai("DANG_LIVE");
        assertFalse(phongs.isEmpty());
        PhongLivestream phong = phongs.get(0);
        NguoiDung khach = layKhachHangTest();

        // 1. Gửi bình luận
        String noiDungTest = "Shop ơi tai nghe Sony có được freeship không?";
        BinhLuanLiveDTO blDTO = livestreamService.guiBinhLuan(phong.getMaLive(), khach, noiDungTest, false);
        assertNotNull(blDTO, "DTO bình luận không được null");
        assertEquals(noiDungTest, blDTO.getNoiDung());
        assertFalse(blDTO.getLaNguoiBan(), "Người gửi là khách hàng");

        // 2. Thả tim
        int timBanDau = phong.getSoLuotThich() != null ? phong.getSoLuotThich() : 0;
        int timMoi = livestreamService.thaTim(phong.getMaLive());
        assertEquals(timBanDau + 1, timMoi, "Số lượt thích phải tăng thêm 1");

        System.out.println("✅ [TEST-03 PASS] Gửi bình luận & Thả tim thành công. Số tim mới: " + timMoi);
    }

    @Test
    @Transactional
    @DisplayName("US-61 [TEST-04]: Khách hàng Đặt Hàng Nhanh trực tiếp trên Live với giá độc quyền")
    public void testDatHangNhanhLive() {
        List<PhongLivestream> phongs = phongLivestreamRepository.findByTrangThai("DANG_LIVE");
        assertFalse(phongs.isEmpty());
        PhongLivestream phong = phongs.get(0);

        List<SanPhamLivestream> spList = livestreamService.layDanhSachSanPhamLive(phong.getMaLive());
        assertFalse(spList.isEmpty());
        SanPhamLivestream sp = spList.get(0);

        int daBanBanDau = sp.getSoLuongDaBan() != null ? sp.getSoLuongDaBan() : 0;
        int conLaiBanDau = sp.getSoLuongConLai();
        assertTrue(conLaiBanDau > 0, "Suất live phải còn hàng để test");

        NguoiDung khach = layKhachHangTest();
        DatHangLiveForm form = DatHangLiveForm.builder()
                .maLive(phong.getMaLive())
                .maSanPham(sp.getSanPham().getMaSanPham())
                .soLuong(1)
                .hoTenNguoiNhan("Trần Thị Hoa")
                .soDienThoai("0987654321")
                .diaChiGiaoHang("123 Nguyễn Trãi, Thanh Xuân, Hà Nội")
                .build();

        String thongBao = livestreamService.datHangNhanhLive(form, khach);
        assertNotNull(thongBao);
        assertTrue(thongBao.contains("thành công"));

        // Kiểm tra số lượng đã bán tăng và suất còn lại giảm
        SanPhamLivestream spSauDat = sanPhamLivestreamRepository.findById(sp.getMaSpLive()).orElseThrow();
        assertEquals(daBanBanDau + 1, spSauDat.getSoLuongDaBan());
        assertEquals(conLaiBanDau - 1, spSauDat.getSoLuongConLai());

        System.out.println("✅ [TEST-04 PASS] Chốt đơn nhanh trên Live thành công, suất còn lại: " + spSauDat.getSoLuongConLai());
    }

    @Test
    @Transactional
    @DisplayName("US-61 [TEST-05]: Tạo mới phòng Live và Chuyển trạng thái Kết thúc")
    public void testTaoVaKetThucLive() {
        NguoiDung seller = laySellerTest();
        GianHang gianHang = layGianHangTest();
        List<SanPham> sps = sanPhamRepository.findByGianHang_MaGianHang(gianHang.getMaGianHang());
        assertFalse(sps.isEmpty());

        TaoPhongLiveForm form = TaoPhongLiveForm.builder()
                .tieuDe("TEST LIVE TỰ ĐỘNG - SALE SẬP SÀN")
                .moTa("Phiên live kiểm thử tự động")
                .danhSachMaSanPham(List.of(sps.get(0).getMaSanPham()))
                .build();

        PhongLivestream phongMoi = livestreamService.taoPhongLive(form, seller);
        assertNotNull(phongMoi.getMaLive());
        assertEquals("DANG_LIVE", phongMoi.getTrangThai());

        // Kiểm tra sản phẩm được ghim mặc định
        Optional<SanPhamLivestream> spGhim = livestreamService.laySanPhamDangGhim(phongMoi.getMaLive());
        assertTrue(spGhim.isPresent(), "Sản phẩm đầu tiên phải được ghim tự động");

        // Chuyển trạng thái kết thúc live
        PhongLivestream phongKetThuc = livestreamService.chuyenTrangThai(phongMoi.getMaLive(), "DA_KET_THUC");
        assertEquals("DA_KET_THUC", phongKetThuc.getTrangThai());
        assertNotNull(phongKetThuc.getThoiGianKetThuc());

        System.out.println("✅ [TEST-05 PASS] Tạo phòng và kết thúc Live thành công!");
    }
}
