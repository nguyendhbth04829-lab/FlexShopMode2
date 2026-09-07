package com.example.demo;

import com.example.demo.dto.KetQuaQuayThuongDTO;
import com.example.demo.dto.ThongKeVongQuayDTO;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.PhanThuongVongQuay;
import com.example.demo.entity.ViXuNguoiDung;
import com.example.demo.entity.VongQuayMayMan;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.PhanThuongVongQuayRepository;
import com.example.demo.repository.ViXuNguoiDungRepository;
import com.example.demo.repository.VongQuayMayManRepository;
import com.example.demo.service.ViXuService;
import com.example.demo.service.VongQuayMayManService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho US-66 (Vòng Quay May Mắn - Shopee Lucky Wheel)
 */
@SpringBootTest
public class VongQuayMayManServiceTest {

    @Autowired
    private VongQuayMayManService vongQuayMayManService;

    @Autowired
    private PhanThuongVongQuayRepository phanThuongVongQuayRepository;

    @Autowired
    private VongQuayMayManRepository vongQuayMayManRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private ViXuNguoiDungRepository viXuNguoiDungRepository;

    @Autowired
    private ViXuService viXuService;

    private NguoiDung layNguoiDungTest() {
        return nguoiDungRepository.findByEmail("khachhang@flexshop.vn")
                .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElseThrow());
    }

    @Test
    @DisplayName("TC-01: Kiểm tra cấu hình 8 ô phần thưởng vòng quay chuẩn xác suất 100%")
    public void testLayDanhSachPhanThuong() {
        List<PhanThuongVongQuay> ds = vongQuayMayManService.layDanhSachPhanThuong();
        assertNotNull(ds, "Danh sách phần thưởng không được null");
        assertEquals(8, ds.size(), "Vòng quay may mắn phải có đúng 8 ô phần thưởng");

        double tongTyLe = ds.stream().mapToDouble(PhanThuongVongQuay::getTyLeTrung).sum();
        assertEquals(100.0, tongTyLe, 0.01, "Tổng tỷ lệ trúng thưởng của 8 ô phải bằng 100%");

        // Kiểm tra từng ô có tên và màu sắc
        for (int i = 0; i < ds.size(); i++) {
            PhanThuongVongQuay pt = ds.get(i);
            assertEquals(i + 1, pt.getThuTuO(), "Thứ tự ô phải liên tục từ 1 đến 8");
            assertNotNull(pt.getTenPhanThuong(), "Tên phần thưởng không được để trống");
            assertNotNull(pt.getMauSacO(), "Màu sắc ô không được để trống");
        }
    }

    @Test
    @Transactional
    @DisplayName("TC-02: Kiểm tra thực hiện quay thưởng thành công và trả về DTO hợp lệ")
    public void testThucHienQuayThuongThanhCong() {
        NguoiDung user = layNguoiDungTest();

        // Đảm bảo có ví xu
        ViXuNguoiDung viXuTruoc = viXuService.layHoacTaoViXu(user.getMaNguoiDung());
        long soXuTruoc = viXuTruoc.getSoXuHienTai();

        // Đảm bảo user có ít nhất 1 lượt quay (nếu hết thì đổi xu để test)
        int conLai = vongQuayMayManService.tinhSoLuotQuayConLai(user.getMaNguoiDung());
        if (conLai <= 0) {
            vongQuayMayManService.doiXuLayLuotQuay(user.getMaNguoiDung());
        }

        KetQuaQuayThuongDTO ketQua = vongQuayMayManService.thucHienQuayThuong(user.getMaNguoiDung(), "127.0.0.1");

        assertNotNull(ketQua, "Kết quả quay không được null");
        assertTrue(ketQua.isThanhCong(), "Quá trình quay phải thành công");
        assertNotNull(ketQua.getTenPhanThuong(), "Phải có tên phần thưởng trúng");
        assertTrue(ketQua.getThuTuO() >= 1 && ketQua.getThuTuO() <= 8, "Vị trí ô trúng phải từ 1 đến 8");
        assertTrue(ketQua.getGocDung() > 0, "Góc dừng bánh xe phải lớn hơn 0");

        // Kiểm tra bản ghi trong DB
        VongQuayMayMan luotQuay = vongQuayMayManRepository.findById(ketQua.getMaLuotQuay()).orElse(null);
        assertNotNull(luotQuay, "Lượt quay phải được lưu vào CSDL");
        assertEquals(user.getMaNguoiDung(), luotQuay.getNguoiDung().getMaNguoiDung());
    }

    @Test
    @Transactional
    @DisplayName("TC-03: Kiểm tra đổi 1.000 Xu lấy thêm lượt quay")
    public void testDoiXuLayThemLuotQuay() {
        NguoiDung user = layNguoiDungTest();
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(user.getMaNguoiDung());

        // Đảm bảo đủ xu để test
        if (viXu.getSoXuHienTai() < 1000) {
            viXu.setSoXuHienTai(viXu.getSoXuHienTai() + 5000);
            viXuNguoiDungRepository.save(viXu);
        }

        long soXuTruoc = viXu.getSoXuHienTai();
        int soLuotTruoc = vongQuayMayManService.tinhSoLuotQuayConLai(user.getMaNguoiDung());

        Long soXuSau = vongQuayMayManService.doiXuLayLuotQuay(user.getMaNguoiDung());

        assertEquals(soXuTruoc - 1000, soXuSau, "Số dư xu phải bị trừ đúng 1.000 Xu");
        int soLuotSau = vongQuayMayManService.tinhSoLuotQuayConLai(user.getMaNguoiDung());
        assertEquals(soLuotTruoc + 1, soLuotSau, "Số lượt quay còn lại phải tăng thêm 1");
    }

    @Test
    @DisplayName("TC-04: Kiểm tra phân trang và lọc lịch sử quay thưởng cá nhân")
    public void testLayLichSuQuayVaPhanTrang() {
        NguoiDung user = layNguoiDungTest();

        Page<VongQuayMayMan> pageTatCa = vongQuayMayManService.layLichSuQuayCaNhan(user.getMaNguoiDung(), "TAT_CA", 0, 5);
        assertNotNull(pageTatCa, "Page lịch sử không được null");

        Page<VongQuayMayMan> pageXu = vongQuayMayManService.layLichSuQuayCaNhan(user.getMaNguoiDung(), "XU", 0, 5);
        assertNotNull(pageXu, "Page lịch sử xu không được null");
        pageXu.getContent().forEach(item -> assertEquals("XU", item.getLoaiPhanThuong(), "Chỉ chứa các lượt nhận Xu"));
    }

    @Test
    @DisplayName("TC-05: Kiểm tra số liệu thống kê tổng quan Vòng quay")
    public void testLayThongKeVongQuay() {
        NguoiDung user = layNguoiDungTest();

        ThongKeVongQuayDTO thongKe = vongQuayMayManService.layThongKeVongQuay(user.getMaNguoiDung());
        assertNotNull(thongKe, "Thống kê không được null");
        assertTrue(thongKe.getTongLuotQuayToanSan() >= 0, "Tổng lượt quay toàn sàn >= 0");
        assertTrue(thongKe.getTongXuDaPhat() >= 0, "Tổng xu đã phát >= 0");
        assertNotNull(thongKe.getSoXuHienTai(), "Số xu hiện tại không null");
    }
}
