package com.example.demo.service;

import com.example.demo.dto.request.DangKyGianHangRequest;
import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DangKyGianHangServiceTest {

    @Mock
    private GianHangRepository gianHangRepository;

    @Mock
    private ChungChiGianHangRepository chungChiGianHangRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @InjectMocks
    private DangKyGianHangService dangKyGianHangService;

    private Long userId;
    private NguoiDung mockUser;
    private DangKyGianHangRequest mockRequest;
    private MockMultipartFile mockPdfFile;

    @BeforeEach
    void setUp() {
        userId = 10L;

        mockUser = NguoiDung.builder()
                .maNguoiDung(userId)
                .email("user@flexshop.vn")
                .hoVaTen("Nguyễn Văn Khách")
                .soDienThoai("0981122334")
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .build();

        mockRequest = DangKyGianHangRequest.builder()
                .tenGianHang("Thời Trang Flex Shop")
                .duongDanSlug("")
                .sdtKho("0981122334")
                .diaChiKho("123 Đường Cầu Giấy, Hà Nội")
                .moTa("Shop thời trang nam nữ cao cấp")
                .soGiayTo("0108877665")
                .loaiGiayTo("GIAY_PHEP_KINH_DOANH")
                .build();

        mockPdfFile = new MockMultipartFile(
                "fileGiayPhep",
                "giay_phep_kd.pdf",
                "application/pdf",
                "%PDF-1.4 Mock PDF Data For Test".getBytes()
        );
    }

    @Test
    @DisplayName("US-08: Đăng ký mở shop thành công -> Chuyển về trạng thái CHO_DUYET & sinh slug tự động")
    void testDangKyGianHang_ThanhCong() {
        // Given
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId)).thenReturn(Optional.empty());
        when(gianHangRepository.existsByTenGianHangIgnoreCaseAndDaXoaFalse("Thời Trang Flex Shop")).thenReturn(false);
        when(gianHangRepository.existsByDuongDanSlugIgnoreCaseAndDaXoaFalse(anyString())).thenReturn(false);

        when(gianHangRepository.save(any(GianHang.class))).thenAnswer(invocation -> {
            GianHang g = invocation.getArgument(0);
            g.setMaGianHang(100L);
            return g;
        });

        when(chungChiGianHangRepository.save(any(ChungChiGianHang.class))).thenAnswer(invocation -> {
            ChungChiGianHang cc = invocation.getArgument(0);
            cc.setMaChungChi(200L);
            return cc;
        });

        // When
        GianHangResponse response = dangKyGianHangService.dangKyGianHang(userId, mockRequest, mockPdfFile, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTenGianHang()).isEqualTo("Thời Trang Flex Shop");
        assertThat(response.getTrangThai()).isEqualTo("CHO_DUYET");
        assertThat(response.getDuongDanSlug()).isEqualTo("thoi-trang-flex-shop");
        assertThat(response.getSoGiayTo()).isEqualTo("0108877665");
        assertThat(response.getTrangThaiGiayTo()).isEqualTo("CHO_DUYET");
        assertThat(response.getLinkAnhGiayTo()).contains("/uploads/giay-phep-kd/");

        verify(gianHangRepository, times(1)).save(any(GianHang.class));
        verify(chungChiGianHangRepository, times(1)).save(any(ChungChiGianHang.class));
    }

    @Test
    @DisplayName("US-08: Đăng ký trùng tên shop -> Báo lỗi 409 CONFLICT")
    void testDangKyGianHang_TrungTenShop_BaoLoiConflict() {
        // Given
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId)).thenReturn(Optional.empty());
        when(gianHangRepository.existsByTenGianHangIgnoreCaseAndDaXoaFalse("Thời Trang Flex Shop")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> dangKyGianHangService.dangKyGianHang(userId, mockRequest, mockPdfFile, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("đã được sử dụng")
                .satisfies(e -> assertThat(((NgoaiLeUngDung) e).getMaTrangThai()).isEqualTo(HttpStatus.CONFLICT));

        verify(gianHangRepository, never()).save(any(GianHang.class));
    }

    @Test
    @DisplayName("US-08: Người dùng đã có gian hàng hoạt động -> Báo lỗi 409 CONFLICT")
    void testDangKyGianHang_DaCoShopHoatDong_BaoLoi() {
        // Given
        GianHang existingShop = GianHang.builder()
                .maGianHang(50L)
                .maChuSoHuu(userId)
                .tenGianHang("Shop Cũ")
                .trangThai("HOAT_DONG")
                .build();

        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId)).thenReturn(Optional.of(existingShop));

        // When & Then
        assertThatThrownBy(() -> dangKyGianHangService.dangKyGianHang(userId, mockRequest, mockPdfFile, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("đang hoạt động")
                .satisfies(e -> assertThat(((NgoaiLeUngDung) e).getMaTrangThai()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("US-08: Người dùng đã nộp đơn và đang chờ duyệt -> Báo lỗi 400 BAD REQUEST")
    void testDangKyGianHang_DaCoShopChoDuyet_BaoLoi() {
        // Given
        GianHang pendingShop = GianHang.builder()
                .maGianHang(51L)
                .maChuSoHuu(userId)
                .tenGianHang("Shop Đang Duyệt")
                .trangThai("CHO_DUYET")
                .build();

        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId)).thenReturn(Optional.of(pendingShop));

        // When & Then
        assertThatThrownBy(() -> dangKyGianHangService.dangKyGianHang(userId, mockRequest, mockPdfFile, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("đang chờ Quản trị viên duyệt")
                .satisfies(e -> assertThat(((NgoaiLeUngDung) e).getMaTrangThai()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("US-08: Không tải lên tệp Giấy phép kinh doanh -> Báo lỗi 400 BAD REQUEST")
    void testDangKyGianHang_ThieuGiayPhep_BaoLoi() {
        // Given
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId)).thenReturn(Optional.empty());
        when(gianHangRepository.existsByTenGianHangIgnoreCaseAndDaXoaFalse(anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> dangKyGianHangService.dangKyGianHang(userId, mockRequest, null, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Bắt buộc tải lên tệp Giấy phép kinh doanh")
                .satisfies(e -> assertThat(((NgoaiLeUngDung) e).getMaTrangThai()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("US-08: Tệp vượt quá 5MB -> Báo lỗi 400 BAD REQUEST")
    void testDangKyGianHang_TepVuotQua5MB_BaoLoi() {
        // Given
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6MB > 5MB
        MockMultipartFile largeFile = new MockMultipartFile("fileGiayPhep", "large.pdf", "application/pdf", largeBytes);

        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId)).thenReturn(Optional.empty());
        when(gianHangRepository.existsByTenGianHangIgnoreCaseAndDaXoaFalse(anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> dangKyGianHangService.dangKyGianHang(userId, mockRequest, largeFile, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("vượt quá giới hạn cho phép (tối đa 5MB)")
                .satisfies(e -> assertThat(((NgoaiLeUngDung) e).getMaTrangThai()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("US-08: Tệp không đúng định dạng PDF/JPEG/PNG -> Báo lỗi 400 BAD REQUEST")
    void testDangKyGianHang_DinhDangSai_BaoLoi() {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile("fileGiayPhep", "virus.exe", "application/x-msdownload", "bad code".getBytes());

        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId)).thenReturn(Optional.empty());
        when(gianHangRepository.existsByTenGianHangIgnoreCaseAndDaXoaFalse(anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> dangKyGianHangService.dangKyGianHang(userId, mockRequest, invalidFile, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Phần mở rộng tệp giấy phép kinh doanh không hợp lệ")
                .satisfies(e -> assertThat(((NgoaiLeUngDung) e).getMaTrangThai()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("US-08: Tự động sinh Slug tiếng Việt chuẩn SEO, không ký tự đặc biệt")
    void testTaoDuongDanSlugTuTenShop() {
        when(gianHangRepository.existsByDuongDanSlugIgnoreCaseAndDaXoaFalse("shop-thoi-trang-dien-tu")).thenReturn(false);

        String slug = dangKyGianHangService.taoDuongDanSlugTuTenShop("Shop Thời Trang & Điện Tử!", null);

        assertThat(slug).isEqualTo("shop-thoi-trang-dien-tu");
        assertThat(slug).matches("^[a-z0-9-]+$");
    }

    @Test
    @DisplayName("US-08: Tự động thêm hậu tố nếu Slug đã tồn tại trong CSDL")
    void testTaoDuongDanSlugTuTenShop_TrungSlug_ThemHauTo() {
        when(gianHangRepository.existsByDuongDanSlugIgnoreCaseAndDaXoaFalse("my-shop")).thenReturn(true);
        when(gianHangRepository.existsByDuongDanSlugIgnoreCaseAndDaXoaFalse("my-shop-1")).thenReturn(false);

        String slug = dangKyGianHangService.taoDuongDanSlugTuTenShop("My Shop", null);

        assertThat(slug).isEqualTo("my-shop-1");
    }

    @Test
    @DisplayName("US-08: Thống kê số lượng gian hàng theo các trạng thái")
    void testLayThongKeGianHang() {
        when(gianHangRepository.countByDaXoaFalse()).thenReturn(10L);
        when(gianHangRepository.countByTrangThaiAndDaXoaFalse("CHO_DUYET")).thenReturn(3L);
        when(gianHangRepository.countByTrangThaiAndDaXoaFalse("HOAT_DONG")).thenReturn(5L);
        when(gianHangRepository.countByTrangThaiAndDaXoaFalse("TU_CHOI")).thenReturn(1L);
        when(gianHangRepository.countByTrangThaiAndDaXoaFalse("TAM_KHOA")).thenReturn(1L);

        ThongKeGianHangResponse response = dangKyGianHangService.layThongKeGianHang();

        assertThat(response.getTongSo()).isEqualTo(10L);
        assertThat(response.getChoDuyet()).isEqualTo(3L);
        assertThat(response.getHoatDong()).isEqualTo(5L);
        assertThat(response.getTuChoi()).isEqualTo(1L);
        assertThat(response.getTamKhoa()).isEqualTo(1L);
    }
}
