package com.example.demo;

import com.example.demo.dto.CauHinhTinNhanTuDongForm;
import com.example.demo.dto.ThongKeTinNhanTuDongDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.ChatService;
import com.example.demo.service.TinNhanTuDongService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử tự động toàn diện cho US-60 (Module: CHAT - Phân hệ Seller)
 * Thiết lập Tin nhắn tự động trả lời (Auto-responder) khi Shop vắng mặt
 */
@SpringBootTest
@Transactional
public class TinNhanTuDongServiceTest {

    @Autowired
    private TinNhanTuDongService tinNhanTuDongService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private CauHinhTinNhanTuDongRepository cauHinhRepository;

    @Autowired
    private LichSuTinNhanTuDongRepository lichSuRepository;

    @Autowired
    private CuocTroChuyenRepository cuocTroChuyenRepository;

    @Autowired
    private TinNhanRepository tinNhanRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    private GianHang layGianHangTest() {
        return gianHangRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy gian hàng mẫu trong DB!"));
    }

    private NguoiDung layKhachHangTest() {
        return nguoiDungRepository.findByEmail("khachhang@flexshop.vn")
                .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng mẫu!")));
    }

    @Test
    @DisplayName("US-60 TC01: Lấy thống kê Dashboard cấu hình tin nhắn tự động thành công")
    void testLayThongKe_ThanhCong() {
        GianHang gianHang = layGianHangTest();
        ThongKeTinNhanTuDongDTO thongKe = tinNhanTuDongService.layThongKe(gianHang.getMaGianHang());

        assertNotNull(thongKe);
        assertTrue(thongKe.getTongSoCauHinh() >= 0);
        assertTrue(thongKe.getSoCauHinhDangBat() >= 0);
    }

    @Test
    @DisplayName("US-60 TC02: Thêm mới kịch bản tin nhắn tự động hợp lệ thành công")
    void testLuuCauHinhMoi_ThanhCong() {
        GianHang gianHang = layGianHangTest();

        CauHinhTinNhanTuDongForm form = CauHinhTinNhanTuDongForm.builder()
                .tieuDe("Kịch bản kiểm thử tự động JUnit")
                .loaiTinNhanTuDong("CHAO_MUNG")
                .noiDungTinNhan("Chào mừng bạn đến với gian hàng của chúng tôi qua kiểm thử tự động!")
                .doTreGiay(1)
                .gioiHanGuiMoiKhachNgay(2)
                .kichHoat(true)
                .build();

        CauHinhTinNhanTuDong saved = tinNhanTuDongService.luuCauHinh(gianHang.getMaGianHang(), form);

        assertNotNull(saved);
        assertNotNull(saved.getMaCauHinh());
        assertEquals("Kịch bản kiểm thử tự động JUnit", saved.getTieuDe());
        assertEquals("CHAO_MUNG", saved.getLoaiTinNhanTuDong());
        assertTrue(saved.getKichHoat());
    }

    @Test
    @DisplayName("US-60 TC03: Validate bắt buộc giờ bắt đầu và kết thúc khi chọn Ngoài giờ làm việc")
    void testLuuCauHinh_ThieuGioNgoaiGio_BaoLoi() {
        GianHang gianHang = layGianHangTest();

        CauHinhTinNhanTuDongForm form = CauHinhTinNhanTuDongForm.builder()
                .tieuDe("Ngoài giờ làm việc thiếu giờ")
                .loaiTinNhanTuDong("NGOAI_GIO_LAM_VIEC")
                .noiDungTinNhan("Hiện tại shop đang ngoài giờ làm việc.")
                .gioBatDau(null)
                .gioKetThuc(null)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            tinNhanTuDongService.luuCauHinh(gianHang.getMaGianHang(), form);
        });

        assertTrue(ex.getMessage().contains("ngoài giờ làm việc bắt buộc phải chọn Giờ bắt đầu"));
    }

    @Test
    @DisplayName("US-60 TC04: Validate bắt buộc nhập từ khóa khi chọn loại Từ khóa")
    void testLuuCauHinh_ThieuTuKhoa_BaoLoi() {
        GianHang gianHang = layGianHangTest();

        CauHinhTinNhanTuDongForm form = CauHinhTinNhanTuDongForm.builder()
                .tieuDe("Kịch bản từ khóa thiếu từ khóa")
                .loaiTinNhanTuDong("TU_KHOA")
                .noiDungTinNhan("Trả lời câu hỏi từ khóa.")
                .tuKhoaKichHoat("")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            tinNhanTuDongService.luuCauHinh(gianHang.getMaGianHang(), form);
        });

        assertTrue(ex.getMessage().contains("từ khóa bắt buộc phải nhập ít nhất 1 từ khóa"));
    }

    @Test
    @DisplayName("US-60 TC05: Bật/Tắt trạng thái kích hoạt kịch bản thành công")
    void testDoiTrangThaiKichHoat() {
        GianHang gianHang = layGianHangTest();

        CauHinhTinNhanTuDongForm form = CauHinhTinNhanTuDongForm.builder()
                .tieuDe("Kịch bản test switch toggle")
                .loaiTinNhanTuDong("CHAO_MUNG")
                .noiDungTinNhan("Test toggle switch on/off")
                .kichHoat(true)
                .build();

        CauHinhTinNhanTuDong saved = tinNhanTuDongService.luuCauHinh(gianHang.getMaGianHang(), form);
        assertTrue(saved.getKichHoat());

        boolean trangThaiMoi = tinNhanTuDongService.doiTrangThai(saved.getMaCauHinh(), gianHang.getMaGianHang());
        assertFalse(trangThaiMoi);

        boolean trangThaiLai = tinNhanTuDongService.doiTrangThai(saved.getMaCauHinh(), gianHang.getMaGianHang());
        assertTrue(trangThaiLai);
    }

    @Test
    @DisplayName("US-60 TC06: Kích hoạt tự động phản hồi chào mừng và đính kèm Voucher khi khách nhắn tin")
    void testXuLyTuDongPhanHoi_ChaoMungKemVoucher() {
        GianHang gianHang = layGianHangTest();
        NguoiDung khachHang = layKhachHangTest();

        // Lấy hoặc tạo cuộc trò chuyện
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        // Lấy voucher của shop nếu có
        MaGiamGia voucher = maGiamGiaRepository.findByGianHangMaGianHangAndDangHoatDongTrueAndDaXoaFalse(gianHang.getMaGianHang())
                .stream().findFirst().orElse(null);

        // Tạo cấu hình chào mừng kích hoạt
        CauHinhTinNhanTuDongForm form = CauHinhTinNhanTuDongForm.builder()
                .tieuDe("Chào mừng tự động có Voucher")
                .loaiTinNhanTuDong("CHAO_MUNG")
                .noiDungTinNhan("Dạ chào bạn! Shop gửi tặng bạn mã giảm giá ưu đãi nhé!")
                .maVoucher(voucher != null ? voucher.getMaVoucher() : null)
                .kichHoat(true)
                .doTreGiay(0)
                .gioiHanGuiMoiKhachNgay(5)
                .build();
        tinNhanTuDongService.luuCauHinh(gianHang.getMaGianHang(), form);

        // Khách gửi tin nhắn
        TinNhan tinNhanKhach = new TinNhan();
        tinNhanKhach.setCuocTroChuyen(ctc);
        tinNhanKhach.setLoaiNguoiGui("KHACH_HANG");
        tinNhanKhach.setMaNguoiGui(khachHang.getMaNguoiDung());
        tinNhanKhach.setNoiDung("Xin chào shop ơi!");

        // Kích hoạt auto-responder
        Optional<TinNhan> ketQua = tinNhanTuDongService.xuLyTuDongPhanHoi(ctc, tinNhanKhach);

        assertTrue(ketQua.isPresent());
        TinNhan autoMsg = ketQua.get();
        assertEquals("SHOP", autoMsg.getLoaiNguoiGui());
        assertTrue(autoMsg.getNoiDung().contains("Dạ chào bạn! Shop gửi tặng bạn mã giảm giá ưu đãi nhé!"));

        if (voucher != null) {
            assertEquals("VOUCHER", autoMsg.getLoaiTinNhan());
            assertNotNull(autoMsg.getDuLieuDinhKemJson());
            assertTrue(autoMsg.getDuLieuDinhKemJson().contains(voucher.getMaCodeVoucher()));
        }
    }

    @Test
    @DisplayName("US-60 TC07: Cơ chế chống Spam không gửi vượt quá giới hạn cấu hình trong ngày")
    void testChongSpam_KhongGuiQuaGioiHanNgay() {
        GianHang gianHang = layGianHangTest();
        NguoiDung khachHang = layKhachHangTest();
        CuocTroChuyen ctc = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHang.getMaGianHang());

        // Tạo cấu hình giới hạn 1 lần/ngày
        CauHinhTinNhanTuDongForm form = CauHinhTinNhanTuDongForm.builder()
                .tieuDe("Chào mừng giới hạn 1 lần")
                .loaiTinNhanTuDong("CHAO_MUNG")
                .noiDungTinNhan("Tin nhắn chỉ gửi 1 lần duy nhất trong ngày.")
                .kichHoat(true)
                .doTreGiay(0)
                .gioiHanGuiMoiKhachNgay(1)
                .build();
        tinNhanTuDongService.luuCauHinh(gianHang.getMaGianHang(), form);

        TinNhan tinNhanKhach1 = new TinNhan();
        tinNhanKhach1.setCuocTroChuyen(ctc);
        tinNhanKhach1.setLoaiNguoiGui("KHACH_HANG");
        tinNhanKhach1.setMaNguoiGui(khachHang.getMaNguoiDung());
        tinNhanKhach1.setNoiDung("Tin nhắn số 1");

        Optional<TinNhan> lan1 = tinNhanTuDongService.xuLyTuDongPhanHoi(ctc, tinNhanKhach1);
        assertTrue(lan1.isPresent());

        // Khách gửi tiếp tin nhắn thứ 2 ngay sau đó
        TinNhan tinNhanKhach2 = new TinNhan();
        tinNhanKhach2.setCuocTroChuyen(ctc);
        tinNhanKhach2.setLoaiNguoiGui("KHACH_HANG");
        tinNhanKhach2.setMaNguoiGui(khachHang.getMaNguoiDung());
        tinNhanKhach2.setNoiDung("Tin nhắn số 2");

        Optional<TinNhan> lan2 = tinNhanTuDongService.xuLyTuDongPhanHoi(ctc, tinNhanKhach2);
        // Do đã đạt giới hạn 1 lần/ngày cho kịch bản chào mừng, lần 2 sẽ không gửi trùng lặp
        assertFalse(lan2.isPresent(), "Hệ thống chống spam phải chặn không gửi lặp lại tin chào mừng");
    }
}
