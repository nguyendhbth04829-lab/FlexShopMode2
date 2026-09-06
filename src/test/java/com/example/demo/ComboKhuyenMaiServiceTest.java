package com.example.demo;

import com.example.demo.dto.ComboKhuyenMaiForm;
import com.example.demo.dto.DealSocMuaKemDTO;
import com.example.demo.dto.ThemSanPhamComboForm;
import com.example.demo.dto.ThongKeComboDTO;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.ComboKhuyenMai;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.SanPhamComboKhuyenMai;
import com.example.demo.repository.BienTheSanPhamRepository;
import com.example.demo.repository.ComboKhuyenMaiRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.SanPhamComboKhuyenMaiRepository;
import com.example.demo.service.ComboKhuyenMaiService;
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

/**
 * Bộ kiểm thử tự động toàn diện cho US-54 (Module: PROMOTION):
 * Quản lý Combo Khuyến Mãi & Mua Kèm Deal Sốc (Add-on Deals: Mua A giảm 50% cho B).
 */
@SpringBootTest
public class ComboKhuyenMaiServiceTest {

    @Autowired
    private ComboKhuyenMaiService comboKhuyenMaiService;

    @Autowired
    private ComboKhuyenMaiRepository comboKhuyenMaiRepository;

    @Autowired
    private SanPhamComboKhuyenMaiRepository sanPhamComboKhuyenMaiRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    private Long layMaGianHangTest() {
        return gianHangRepository.findAll().stream()
                .findFirst()
                .map(GianHang::getMaGianHang)
                .orElse(1L);
    }

    private BienTheSanPham layBienTheCuaGianHang(Long maGianHang, int index) {
        List<BienTheSanPham> list = bienTheSanPhamRepository.findBySanPham_GianHang_MaGianHangAndDaXoaFalse(maGianHang);
        if (list.isEmpty()) {
            list = bienTheSanPhamRepository.findAll();
        }
        return list.get(Math.min(index, list.size() - 1));
    }

    @Test
    @Transactional
    @DisplayName("Test 1: Tạo thành công chương trình Combo & Deal Sốc Mua Kèm với giảm giá 50%")
    public void testTaoComboThanhCong() {
        Long maGianHang = layMaGianHangTest();

        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Mua Điện Thoại Giảm 50% Củ Sạc GaN");
        form.setLoaiCombo("DEAL_SOC_MUA_KEM");
        form.setGiaTriGiam(new BigDecimal("50.00"));
        form.setSoLuongToiThieu(1);
        form.setThoiGianBatDau(LocalDateTime.now().plusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(5));
        form.setDangHoatDong(true);

        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        assertNotNull(combo.getMaCombo());
        assertEquals("Mua Điện Thoại Giảm 50% Củ Sạc GaN", combo.getTenCombo());
        assertEquals("DEAL_SOC_MUA_KEM", combo.getLoaiCombo());
        assertEquals(0, new BigDecimal("50.00").compareTo(combo.getGiaTriGiam()));
        assertTrue(combo.getDangHoatDong());
        assertEquals(maGianHang, combo.getGianHang().getMaGianHang());
    }

    @Test
    @Transactional
    @DisplayName("Test 2: Validate chặt chẽ thời gian bắt đầu / kết thúc và thời lượng tối thiểu")
    public void testValidateThoiGianCombo() {
        Long maGianHang = layMaGianHangTest();

        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Combo Lỗi Thời Gian");
        form.setLoaiCombo("DEAL_SOC_MUA_KEM");
        form.setGiaTriGiam(new BigDecimal("50.00"));
        // Lỗi: kết thúc trước bắt đầu
        form.setThoiGianBatDau(LocalDateTime.now().plusDays(2));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            comboKhuyenMaiService.taoCombo(form, maGianHang);
        });
        assertTrue(ex.getMessage().contains("Thời gian kết thúc phải sau"));

        // Lỗi: thời lượng quá ngắn (< 30 phút)
        form.setThoiGianBatDau(LocalDateTime.now().plusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusHours(1).plusMinutes(15));
        IllegalArgumentException exShort = assertThrows(IllegalArgumentException.class, () -> {
            comboKhuyenMaiService.taoCombo(form, maGianHang);
        });
        assertTrue(exShort.getMessage().contains("tối thiểu 30 phút"));
    }

    @Test
    @Transactional
    @DisplayName("Test 3: Gán Sản Phẩm Chính A vào Combo (bán theo giá niêm yết chuẩn, % giảm = 0)")
    public void testThemSanPhamChinhAVaoCombo() {
        Long maGianHang = layMaGianHangTest();
        BienTheSanPham btChinh = layBienTheCuaGianHang(maGianHang, 0);

        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Combo Sản Phẩm Chính A");
        form.setLoaiCombo("DEAL_SOC_MUA_KEM");
        form.setGiaTriGiam(new BigDecimal("50.00"));
        form.setThoiGianBatDau(LocalDateTime.now().plusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(3));
        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        ThemSanPhamComboForm addForm = new ThemSanPhamComboForm();
        addForm.setMaCombo(combo.getMaCombo());
        addForm.setMaBienThe(btChinh.getMaBienThe());
        addForm.setVaiTro("SAN_PHAM_CHINH");

        SanPhamComboKhuyenMai spChinh = comboKhuyenMaiService.themSanPhamVaoCombo(addForm, maGianHang);

        assertNotNull(spChinh.getMaSanPhamCombo());
        assertEquals("SAN_PHAM_CHINH", spChinh.getVaiTro());
        assertEquals(BigDecimal.ZERO, spChinh.getPhanTramGiam());
        assertEquals(0, btChinh.getGiaBan().compareTo(spChinh.getGiaUuDai()));
    }

    @Test
    @Transactional
    @DisplayName("Test 4: Gán Phụ Kiện B vào Combo theo chiết khấu 50% chuẩn (US-54)")
    public void testThemPhuKienBTheoPhanTram50PhanTram() {
        Long maGianHang = layMaGianHangTest();
        List<BienTheSanPham> dsBienThe = bienTheSanPhamRepository.findBySanPham_GianHang_MaGianHangAndDaXoaFalse(maGianHang);
        assertTrue(dsBienThe.size() >= 2, "Cần tối thiểu 2 sản phẩm để test combo");

        BienTheSanPham btPhuKien = dsBienThe.get(1);

        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Combo Giảm 50% Phụ Kiện");
        form.setLoaiCombo("DEAL_SOC_MUA_KEM");
        form.setGiaTriGiam(new BigDecimal("50.00"));
        form.setThoiGianBatDau(LocalDateTime.now().plusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(3));
        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        ThemSanPhamComboForm addForm = new ThemSanPhamComboForm();
        addForm.setMaCombo(combo.getMaCombo());
        addForm.setMaBienThe(btPhuKien.getMaBienThe());
        addForm.setVaiTro("MUA_KEM_DEAL_SOC");
        addForm.setPhanTramGiam(new BigDecimal("50.00"));
        addForm.setGioiHanMuaKemMoiDon(1);
        addForm.setSoLuongToiDa(50);

        SanPhamComboKhuyenMai spB = comboKhuyenMaiService.themSanPhamVaoCombo(addForm, maGianHang);

        assertNotNull(spB.getMaSanPhamCombo());
        assertEquals("MUA_KEM_DEAL_SOC", spB.getVaiTro());
        assertEquals(0, new BigDecimal("50.00").compareTo(spB.getPhanTramGiam()));

        // Giá sau giảm 50% = Giá gốc / 2
        BigDecimal giaGoc = btPhuKien.getGiaBan();
        BigDecimal giaExpected = giaGoc.multiply(new BigDecimal("0.50")).setScale(0, java.math.RoundingMode.HALF_UP);
        assertEquals(0, giaExpected.compareTo(spB.getGiaUuDai()));
        assertEquals(0, giaGoc.subtract(giaExpected).compareTo(spB.getTietKiem()));
    }

    @Test
    @Transactional
    @DisplayName("Test 5: Gán Phụ Kiện B khi người bán nhập giá ưu đãi trực tiếp và tự động quy đổi %")
    public void testThemPhuKienBTheoGiaUuDaiTrucTiep() {
        Long maGianHang = layMaGianHangTest();
        List<BienTheSanPham> dsBienThe = bienTheSanPhamRepository.findBySanPham_GianHang_MaGianHangAndDaXoaFalse(maGianHang);
        BienTheSanPham btPhuKien = dsBienThe.get(1);

        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Combo Giá Trực Tiếp");
        form.setLoaiCombo("DEAL_SOC_MUA_KEM");
        form.setGiaTriGiam(new BigDecimal("50.00"));
        form.setThoiGianBatDau(LocalDateTime.now().plusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(3));
        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        // Giả sử giá gốc là X, người bán nhập giá bán ưu đãi là 100,000 đ (nếu nhỏ hơn X)
        BigDecimal giaGoc = btPhuKien.getGiaBan();
        BigDecimal giaDeal = giaGoc.divide(new BigDecimal("2"), 0, java.math.RoundingMode.HALF_UP);

        ThemSanPhamComboForm addForm = new ThemSanPhamComboForm();
        addForm.setMaCombo(combo.getMaCombo());
        addForm.setMaBienThe(btPhuKien.getMaBienThe());
        addForm.setVaiTro("MUA_KEM_DEAL_SOC");
        addForm.setGiaUuDai(giaDeal);

        SanPhamComboKhuyenMai spB = comboKhuyenMaiService.themSanPhamVaoCombo(addForm, maGianHang);

        assertEquals(0, giaDeal.compareTo(spB.getGiaUuDai()));
        assertTrue(spB.getPhanTramGiam().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Transactional
    @DisplayName("Test 6: Chặn giá ưu đãi phụ kiện mua kèm lớn hơn hoặc bằng giá gốc niêm yết")
    public void testChanGiaUuDaiCaoHonGiaGoc() {
        Long maGianHang = layMaGianHangTest();
        List<BienTheSanPham> dsBienThe = bienTheSanPhamRepository.findBySanPham_GianHang_MaGianHangAndDaXoaFalse(maGianHang);
        BienTheSanPham btPhuKien = dsBienThe.get(1);

        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Combo Giá Bất Hợp Lý");
        form.setThoiGianBatDau(LocalDateTime.now().plusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(3));
        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        ThemSanPhamComboForm addForm = new ThemSanPhamComboForm();
        addForm.setMaCombo(combo.getMaCombo());
        addForm.setMaBienThe(btPhuKien.getMaBienThe());
        addForm.setVaiTro("MUA_KEM_DEAL_SOC");
        // Lỗi: giá ưu đãi cao hơn giá gốc
        addForm.setGiaUuDai(btPhuKien.getGiaBan().add(new BigDecimal("50000")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            comboKhuyenMaiService.themSanPhamVaoCombo(addForm, maGianHang);
        });
        assertTrue(ex.getMessage().contains("phải nhỏ hơn giá niêm yết"));
    }

    @Test
    @Transactional
    @DisplayName("Test 7: Bảo mật phân quyền - Chặn Seller thêm sản phẩm thuộc gian hàng khác")
    public void testChanThemSanPhamGianHangKhac() {
        Long maGianHang = layMaGianHangTest();
        // Giả lập gian hàng giả mạo #9999
        Long maGianHangGiaMao = 9999L;

        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Combo Test Phân Quyền");
        form.setThoiGianBatDau(LocalDateTime.now().plusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(3));
        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        BienTheSanPham bt = layBienTheCuaGianHang(maGianHang, 0);

        ThemSanPhamComboForm addForm = new ThemSanPhamComboForm();
        addForm.setMaCombo(combo.getMaCombo());
        addForm.setMaBienThe(bt.getMaBienThe());

        // Seller gian hàng #9999 cố tình thao tác
        assertThrows(IllegalArgumentException.class, () -> {
            comboKhuyenMaiService.themSanPhamVaoCombo(addForm, maGianHangGiaMao);
        });
    }

    @Test
    @Transactional
    @DisplayName("Test 8: Chặn thêm trùng lặp cùng một sản phẩm/biến thể vào một Combo")
    public void testChanTrungLapSanPham() {
        Long maGianHang = layMaGianHangTest();
        BienTheSanPham bt = layBienTheCuaGianHang(maGianHang, 0);

        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Combo Chặn Trùng");
        form.setThoiGianBatDau(LocalDateTime.now().plusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(3));
        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        ThemSanPhamComboForm addForm = new ThemSanPhamComboForm();
        addForm.setMaCombo(combo.getMaCombo());
        addForm.setMaBienThe(bt.getMaBienThe());
        addForm.setVaiTro("SAN_PHAM_CHINH");

        // Lần 1: Thành công
        comboKhuyenMaiService.themSanPhamVaoCombo(addForm, maGianHang);

        // Lần 2: Phải ném Exception chặn trùng lặp
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            comboKhuyenMaiService.themSanPhamVaoCombo(addForm, maGianHang);
        });
        assertTrue(ex.getMessage().contains("đã có mặt trong chương trình"));
    }

    @Test
    @Transactional
    @DisplayName("Test 9: Khách hàng truy vấn danh sách Phụ Kiện B mua kèm theo Sản Phẩm Chính A")
    public void testLayDanhSachDealSocChoKhachHang() {
        Long maGianHang = layMaGianHangTest();
        List<BienTheSanPham> dsBienThe = bienTheSanPhamRepository.findBySanPham_GianHang_MaGianHangAndDaXoaFalse(maGianHang);
        assertTrue(dsBienThe.size() >= 2);

        BienTheSanPham btChinh = dsBienThe.get(0);
        BienTheSanPham btPhuKien = dsBienThe.get(1);

        // Tạo combo đang diễn ra ngay bây giờ
        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Deal Sốc Mua Kèm Live");
        form.setLoaiCombo("DEAL_SOC_MUA_KEM");
        form.setGiaTriGiam(new BigDecimal("50.00"));
        form.setThoiGianBatDau(LocalDateTime.now().minusHours(1));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(2));
        form.setDangHoatDong(true);
        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        // Thêm SP chính A
        ThemSanPhamComboForm addA = new ThemSanPhamComboForm();
        addA.setMaCombo(combo.getMaCombo());
        addA.setMaBienThe(btChinh.getMaBienThe());
        addA.setVaiTro("SAN_PHAM_CHINH");
        comboKhuyenMaiService.themSanPhamVaoCombo(addA, maGianHang);

        // Thêm Phụ kiện B giảm 50%
        ThemSanPhamComboForm addB = new ThemSanPhamComboForm();
        addB.setMaCombo(combo.getMaCombo());
        addB.setMaBienThe(btPhuKien.getMaBienThe());
        addB.setVaiTro("MUA_KEM_DEAL_SOC");
        addB.setPhanTramGiam(new BigDecimal("50.00"));
        comboKhuyenMaiService.themSanPhamVaoCombo(addB, maGianHang);

        // Khách hàng truy vấn deal sốc khi xem sản phẩm chính btChinh
        List<DealSocMuaKemDTO> dsDealSoc = comboKhuyenMaiService.layDanhSachDealSocChoKhachHang(btChinh.getMaBienThe());

        assertNotNull(dsDealSoc);
        assertFalse(dsDealSoc.isEmpty());
        DealSocMuaKemDTO dto = dsDealSoc.get(0);
        assertEquals(btPhuKien.getMaBienThe(), dto.getMaBienThe());
        assertEquals(0, new BigDecimal("50.00").compareTo(dto.getPhanTramGiam()));
        assertTrue(dto.getTietKiem().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Transactional
    @DisplayName("Test 10: Thống kê KPI, Phân trang và Đổi trạng thái Bật/Tắt Combo")
    public void testThongKeVaDoiTrangThaiCombo() {
        Long maGianHang = layMaGianHangTest();

        // 1. Thống kê ban đầu
        ThongKeComboDTO kpiBefore = comboKhuyenMaiService.layThongKeCombo(maGianHang);
        assertNotNull(kpiBefore);

        // 2. Tạo combo mới
        ComboKhuyenMaiForm form = new ComboKhuyenMaiForm();
        form.setTenCombo("Combo Kiểm Thử KPI");
        form.setLoaiCombo("DEAL_SOC_MUA_KEM");
        form.setGiaTriGiam(new BigDecimal("50.00"));
        form.setThoiGianBatDau(LocalDateTime.now().minusHours(2));
        form.setThoiGianKetThuc(LocalDateTime.now().plusDays(2));
        form.setDangHoatDong(true);
        ComboKhuyenMai combo = comboKhuyenMaiService.taoCombo(form, maGianHang);

        // 3. Phân trang tìm kiếm
        Page<ComboKhuyenMai> page = comboKhuyenMaiService.layDanhSachCombo(
                maGianHang, "TAT_CA", "TAT_CA", "Kiểm Thử KPI", 0, 10
        );
        assertTrue(page.getTotalElements() >= 1);

        // 4. Bật / Tắt trạng thái
        assertTrue(combo.getDangHoatDong());
        comboKhuyenMaiService.doiTrangThaiCombo(combo.getMaCombo(), maGianHang);
        ComboKhuyenMai comboUpdated = comboKhuyenMaiService.layChiTietCombo(combo.getMaCombo(), maGianHang);
        assertFalse(comboUpdated.getDangHoatDong());
    }
}
