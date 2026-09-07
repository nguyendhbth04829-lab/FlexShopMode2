package com.example.demo;

import com.example.demo.dto.BinhLuanVideoDTO;
import com.example.demo.dto.DangVideoNganForm;
import com.example.demo.dto.ThongKeVideoDTO;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.SanPham;
import com.example.demo.entity.VideoNganReview;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.SanPhamRepository;
import com.example.demo.repository.VideoNganReviewRepository;
import com.example.demo.service.VideoNganService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class VideoNganServiceTest {

    @Autowired
    private VideoNganService videoNganService;

    @Autowired
    private VideoNganReviewRepository videoNganReviewRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    private NguoiDung testUser;
    private SanPham testSanPham;

    @BeforeEach
    void setUp() {
        testUser = nguoiDungRepository.findAll().stream()
                .filter(u -> u.getEmail() != null)
                .findFirst()
                .orElseGet(() -> {
                    NguoiDung nd = new NguoiDung();
                    nd.setEmail("koc_test@flexshop.vn");
                    nd.setHoVaTen("KOC Test User");
                    nd.setMatKhauMaHoa("123456");
                    return nguoiDungRepository.save(nd);
                });

        testSanPham = sanPhamRepository.findAll().stream()
                .filter(sp -> !Boolean.TRUE.equals(sp.getDaXoa()) && !Boolean.TRUE.equals(sp.getBiKhoa()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cần ít nhất 1 sản phẩm trong DB để test"));
    }

    @Test
    @DisplayName("US-62: Test Đăng video ngắn review gắn link sản phẩm thành công")
    void testDangVideoThanhCong() {
        DangVideoNganForm form = new DangVideoNganForm();
        form.setTieuDe("Đập hộp và review thực tế sản phẩm đỉnh cao!");
        form.setMaSanPhamGanKem(testSanPham.getMaSanPham());
        form.setLinkVideo("https://example.com/videos/sample.mp4");
        form.setLinkAnhBia("https://example.com/poster.jpg");
        form.setMoTa("Sản phẩm xịn xò, đóng gói cẩn thận!");
        form.setHashtag("#review #unbox #flexshop");
        form.setThoiLuongGiay(45);

        VideoNganReview videoMoi = videoNganService.dangVideo(form, testUser);

        assertNotNull(videoMoi.getMaVideo(), "Mã video sau khi lưu không được null");
        assertEquals("Đập hộp và review thực tế sản phẩm đỉnh cao!", videoMoi.getTieuDe());
        assertEquals(testSanPham.getMaSanPham(), videoMoi.getSanPhamGanKem().getMaSanPham());
        assertEquals(0, videoMoi.getTongLuotTim());
        assertEquals(0, videoMoi.getTongLuotXem());
        assertEquals(0, videoMoi.getTongLuotBinhLuan());
        assertEquals("HOAT_DONG", videoMoi.getTrangThai());
        assertFalse(videoMoi.getDaXoa());
        System.out.println("✅ [TEST-01 PASS] Đăng video review thành công: maVideo=" + videoMoi.getMaVideo());
    }

    @Test
    @DisplayName("US-62: Test Validate khi thiếu sản phẩm gắn kèm")
    void testDangVideoThieuSanPham() {
        DangVideoNganForm form = new DangVideoNganForm();
        form.setTieuDe("Video không có sản phẩm");
        form.setMaSanPhamGanKem(999999L); // ID không tồn tại
        form.setLinkVideo("https://example.com/video.mp4");

        assertThrows(IllegalArgumentException.class, () -> {
            videoNganService.dangVideo(form, testUser);
        }, "Phải ném ngoại lệ khi sản phẩm gắn kèm không tồn tại");
        System.out.println("✅ [TEST-02 PASS] Bắt lỗi chính xác khi mã sản phẩm không tồn tại!");
    }

    @Test
    @DisplayName("US-62: Test Thả tim (Like) và Bỏ thích (Unlike) video review")
    void testThichVaBoThichVideo() {
        // Tạo video trước
        DangVideoNganForm form = new DangVideoNganForm();
        form.setTieuDe("Video test thả tim sản phẩm");
        form.setMaSanPhamGanKem(testSanPham.getMaSanPham());
        form.setLinkVideo("https://example.com/video.mp4");
        VideoNganReview video = videoNganService.dangVideo(form, testUser);

        // Thả tim lần 1 -> Đã thích, lượt tim = 1
        Map<String, Object> like1 = videoNganService.thichHoacBoThich(video.getMaVideo(), testUser);
        assertTrue((Boolean) like1.get("daThich"), "Lần 1 phải là đã thích");
        assertEquals(1, ((Number) like1.get("tongLuotTim")).intValue(), "Lượt tim phải tăng lên 1");
        assertTrue(videoNganService.daThichVideo(video.getMaVideo(), testUser.getMaNguoiDung()));

        // Thả tim lần 2 -> Bỏ thích, lượt tim = 0
        Map<String, Object> like2 = videoNganService.thichHoacBoThich(video.getMaVideo(), testUser);
        assertFalse((Boolean) like2.get("daThich"), "Lần 2 phải là bỏ thích");
        assertEquals(0, ((Number) like2.get("tongLuotTim")).intValue(), "Lượt tim phải giảm về 0");
        assertFalse(videoNganService.daThichVideo(video.getMaVideo(), testUser.getMaNguoiDung()));
        System.out.println("✅ [TEST-03 PASS] Thả tim và bỏ thích video hoạt động chính xác!");
    }

    @Test
    @DisplayName("US-62: Test Thêm bình luận và kiểm tra tăng bộ đếm bình luận")
    void testThemBinhLuanVaTangBoDem() {
        DangVideoNganForm form = new DangVideoNganForm();
        form.setTieuDe("Video test bình luận tương tác");
        form.setMaSanPhamGanKem(testSanPham.getMaSanPham());
        form.setLinkVideo("https://example.com/video.mp4");
        VideoNganReview video = videoNganService.dangVideo(form, testUser);

        BinhLuanVideoDTO cmt = videoNganService.themBinhLuan(video.getMaVideo(), "Sản phẩm dùng rất thích, cảm ơn KOC!", testUser);

        assertNotNull(cmt.getMaBinhLuan());
        assertEquals("Sản phẩm dùng rất thích, cảm ơn KOC!", cmt.getNoiDung());
        assertTrue(cmt.isLaToi());

        List<BinhLuanVideoDTO> ds = videoNganService.layDanhSachBinhLuan(video.getMaVideo(), testUser.getMaNguoiDung());
        assertEquals(1, ds.size());

        VideoNganReview videoCapNhat = videoNganService.layVideoChiTiet(video.getMaVideo());
        assertEquals(1, videoCapNhat.getTongLuotBinhLuan(), "Tổng lượt bình luận phải tăng lên 1");
        System.out.println("✅ [TEST-04 PASS] Thêm bình luận và tăng bộ đếm bình luận thành công!");
    }

    @Test
    @DisplayName("US-62: Test Thống kê tương tác video của KOC")
    void testLayThongKeCuaNguoiDung() {
        ThongKeVideoDTO tk = videoNganService.layThongKeCuaNguoiDung(testUser.getMaNguoiDung());
        assertNotNull(tk);
        assertTrue(tk.getTongSoVideo() >= 0);
        assertTrue(tk.getTongLuotXem() >= 0);
        assertTrue(tk.getTongLuotTim() >= 0);
        System.out.println("✅ [TEST-05 PASS] Thống kê tương tác KOC: " + tk);
    }
}

