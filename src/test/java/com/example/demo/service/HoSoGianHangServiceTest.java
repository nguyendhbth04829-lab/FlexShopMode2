package com.example.demo.service;

import com.example.demo.dto.request.CapNhatThongTinShopRequest;
import com.example.demo.dto.request.ThemChungChiRequest;
import com.example.demo.dto.response.ChungChiGianHangResponse;
import com.example.demo.dto.response.HoSoGianHangDayDuResponse;
import com.example.demo.entity.ChungChiGianHang;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.NguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.ChungChiGianHangRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoSoGianHangServiceTest {

    @Mock
    private GianHangRepository gianHangRepository;

    @Mock
    private ChungChiGianHangRepository chungChiGianHangRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private DangKyGianHangService dangKyGianHangService;

    @InjectMocks
    private HoSoGianHangService hoSoGianHangService;

    private Long userId;
    private Long shopId;
    private GianHang mockShop;
    private NguoiDung mockUser;
    private ChungChiGianHang mockCert;

    @BeforeEach
    void setUp() {
        userId = 2L;
        shopId = 4L;

        mockUser = NguoiDung.builder()
                .maNguoiDung(userId)
                .email("seller@flexshop.vn")
                .hoVaTen("Trần Người Bán")
                .soDienThoai("0912345678")
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .build();

        mockShop = GianHang.builder()
                .maGianHang(shopId)
                .maChuSoHuu(userId)
                .tenGianHang("Flex Fashion 1")
                .duongDanSlug("flex-fashion-1")
                .moTa("Shop thời trang cao cấp")
                .linkLogo("/uploads/shop-logos/logo.png")
                .diaChiKho("88 Cầu Giấy, Hà Nội")
                .sdtKho("0912345678")
                .trangThai("HOAT_DONG")
                .hangGianHang("UY_TIN")
                .diemSaoQuaTa(0)
                .diemDanhGiaTb(BigDecimal.valueOf(4.9))
                .tongDanhGia(120)
                .tongDonHang(350)
                .tyLePhanHoiChat(BigDecimal.valueOf(99.0))
                .daXoa(false)
                .ngayTao(LocalDateTime.now().minusDays(10))
                .build();

        mockCert = ChungChiGianHang.builder()
                .maChungChi(10L)
                .maGianHang(shopId)
                .loaiGiayTo("GIAY_PHEP_KINH_DOANH")
                .soGiayTo("0101234567")
                .linkAnhGiayTo("/uploads/giay-phep-kd/test.pdf")
                .trangThaiDuyet("CHO_DUYET")
                .ngayTao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Lấy hồ sơ gian hàng thành công khi người dùng đã có gian hàng")
    void layHoSoGianHangCuaToi_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(chungChiGianHangRepository.findByMaGianHangOrderByMaChungChiDesc(shopId))
                .thenReturn(List.of(mockCert));
        when(chungChiGianHangRepository.countByMaGianHang(shopId)).thenReturn(1L);
        when(chungChiGianHangRepository.countByMaGianHangAndTrangThaiDuyet(shopId, "DA_DUYET")).thenReturn(0L);
        when(chungChiGianHangRepository.countByMaGianHangAndTrangThaiDuyet(shopId, "CHO_DUYET")).thenReturn(1L);
        when(chungChiGianHangRepository.countByMaGianHangAndTrangThaiDuyet(shopId, "TU_CHOI")).thenReturn(0L);

        HoSoGianHangDayDuResponse res = hoSoGianHangService.layHoSoGianHangCuaToi(userId);

        assertThat(res).isNotNull();
        assertThat(res.getMaGianHang()).isEqualTo(shopId);
        assertThat(res.getTenGianHang()).isEqualTo("Flex Fashion 1");
        assertThat(res.getTenChuSoHuu()).isEqualTo("Trần Người Bán");
        assertThat(res.getTongSoChungChi()).isEqualTo(1);
        assertThat(res.getSoChungChiChoDuyet()).isEqualTo(1);
        assertThat(res.getDanhSachChungChi()).hasSize(1);
        assertThat(res.getDanhSachChungChi().get(0).getLaTepPdf()).isTrue();
    }

    @Test
    @DisplayName("Lấy hồ sơ gian hàng thất bại khi người dùng chưa có shop")
    void layHoSoGianHangCuaToi_ChuaCoShop_NemNgoaiLe() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hoSoGianHangService.layHoSoGianHangCuaToi(userId))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Bạn chưa có gian hàng nào trên hệ thống");
    }

    @Test
    @DisplayName("Cập nhật thông tin gian hàng thành công")
    void capNhatThongTinGianHang_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(gianHangRepository.existsByTenGianHangIgnoreCaseAndMaGianHangNotAndDaXoaFalse(eq("Flex Fashion New"), eq(shopId)))
                .thenReturn(false);
        when(dangKyGianHangService.taoDuongDanSlugTuTenShop(eq("Flex Fashion New"), eq(shopId)))
                .thenReturn("flex-fashion-new");
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(chungChiGianHangRepository.findByMaGianHangOrderByMaChungChiDesc(shopId))
                .thenReturn(List.of());

        CapNhatThongTinShopRequest req = CapNhatThongTinShopRequest.builder()
                .tenGianHang("Flex Fashion New")
                .diaChiKho("123 Phố Huế, Hai Bà Trưng, Hà Nội")
                .sdtKho("0987654321")
                .moTa("Mô tả mới")
                .build();

        HoSoGianHangDayDuResponse res = hoSoGianHangService.capNhatThongTinGianHang(userId, req, null);

        assertThat(res).isNotNull();
        verify(gianHangRepository, times(1)).save(mockShop);
        assertThat(mockShop.getTenGianHang()).isEqualTo("Flex Fashion New");
        assertThat(mockShop.getDuongDanSlug()).isEqualTo("flex-fashion-new");
    }

    @Test
    @DisplayName("Cập nhật thông tin gian hàng thất bại khi tên shop bị trùng với shop khác")
    void capNhatThongTinGianHang_TrungTenShop_NemNgoaiLe() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(gianHangRepository.existsByTenGianHangIgnoreCaseAndMaGianHangNotAndDaXoaFalse(eq("Shop Da Ton Tai"), eq(shopId)))
                .thenReturn(true);

        CapNhatThongTinShopRequest req = CapNhatThongTinShopRequest.builder()
                .tenGianHang("Shop Da Ton Tai")
                .diaChiKho("123 Phố Huế, Hai Bà Trưng, Hà Nội")
                .sdtKho("0987654321")
                .build();

        assertThatThrownBy(() -> hoSoGianHangService.capNhatThongTinGianHang(userId, req, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("đã có shop khác sử dụng");
    }

    @Test
    @DisplayName("Nộp thêm chứng chỉ mới thành công với trạng thái CHO_DUYET")
    void themChungChi_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(chungChiGianHangRepository.existsBySoGiayToAndMaGianHangNot(eq("VSATTP-2026"), eq(shopId)))
                .thenReturn(false);

        MockMultipartFile filePdf = new MockMultipartFile(
                "fileGiayTo", "attp.pdf", "application/pdf", "%PDF-1.4 sample content".getBytes()
        );

        ThemChungChiRequest req = ThemChungChiRequest.builder()
                .loaiGiayTo("AN_TOAN_THUC_PHAM")
                .soGiayTo("VSATTP-2026")
                .build();

        when(chungChiGianHangRepository.save(any(ChungChiGianHang.class))).thenAnswer(invocation -> {
            ChungChiGianHang c = invocation.getArgument(0);
            c.setMaChungChi(99L);
            return c;
        });

        ChungChiGianHangResponse res = hoSoGianHangService.themChungChi(userId, req, filePdf);

        assertThat(res).isNotNull();
        assertThat(res.getMaChungChi()).isEqualTo(99L);
        assertThat(res.getTrangThaiDuyet()).isEqualTo("CHO_DUYET");
        assertThat(res.getLoaiGiayTo()).isEqualTo("AN_TOAN_THUC_PHAM");
        assertThat(res.getTenLoaiGiayTo()).isEqualTo("Chứng nhận ATTP");
    }

    @Test
    @DisplayName("Nộp thêm chứng chỉ thất bại khi tệp rỗng")
    void themChungChi_FileRong_NemNgoaiLe() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));

        ThemChungChiRequest req = ThemChungChiRequest.builder()
                .loaiGiayTo("AN_TOAN_THUC_PHAM")
                .soGiayTo("VSATTP-2026")
                .build();

        assertThatThrownBy(() -> hoSoGianHangService.themChungChi(userId, req, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Bắt buộc phải tải lên tệp chứng chỉ");
    }

    @Test
    @DisplayName("Xóa chứng chỉ thành công khi ở trạng thái CHO_DUYET")
    void xoaChungChi_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(chungChiGianHangRepository.findByMaChungChiAndMaGianHang(10L, shopId))
                .thenReturn(Optional.of(mockCert));

        hoSoGianHangService.xoaChungChi(userId, 10L);

        verify(chungChiGianHangRepository, times(1)).delete(mockCert);
    }

    @Test
    @DisplayName("Xóa chứng chỉ thất bại khi chứng chỉ đã ở trạng thái DA_DUYET")
    void xoaChungChi_DaDuyet_NemNgoaiLe() {
        mockCert.setTrangThaiDuyet("DA_DUYET");

        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(chungChiGianHangRepository.findByMaChungChiAndMaGianHang(10L, shopId))
                .thenReturn(Optional.of(mockCert));

        assertThatThrownBy(() -> hoSoGianHangService.xoaChungChi(userId, 10L))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Không thể xóa chứng chỉ/giấy phép đã được Quản trị viên phê duyệt");
    }
}
