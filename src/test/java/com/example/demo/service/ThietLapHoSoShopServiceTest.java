package com.example.demo.service;

import com.example.demo.dto.request.ThietLapHoSoShopRequest;
import com.example.demo.dto.response.LichSuThietLapShopResponse;
import com.example.demo.dto.response.ThietLapHoSoShopResponse;
import com.example.demo.dto.response.ThongKeThietLapShopResponse;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.LichSuThietLapShop;
import com.example.demo.entity.NguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.LichSuThietLapShopRepository;
import com.example.demo.repository.NguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class ThietLapHoSoShopServiceTest {

    @Mock
    private GianHangRepository gianHangRepository;

    @Mock
    private LichSuThietLapShopRepository lichSuThietLapShopRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private DangKyGianHangService dangKyGianHangService;

    @InjectMocks
    private ThietLapHoSoShopService thietLapHoSoShopService;

    private Long userId;
    private Long shopId;
    private GianHang mockShop;
    private NguoiDung mockUser;

    @BeforeEach
    void setUp() {
        userId = 2L;
        shopId = 10L;

        mockUser = NguoiDung.builder()
                .maNguoiDung(userId)
                .hoVaTen("Nguyễn Văn Bán")
                .email("seller@flexshop.vn")
                .soDienThoai("0987654321")
                .build();

        mockShop = GianHang.builder()
                .maGianHang(shopId)
                .maChuSoHuu(userId)
                .tenGianHang("Flex Fashion Store")
                .duongDanSlug("flex-fashion-store")
                .moTa("Mô tả shop thời trang chất lượng cao")
                .linkLogo("/uploads/shop-logos/logo_test.png")
                .linkBanner("/uploads/shop-banners/banner_test.png")
                .diaChiKho("123 Phố Huế, Hoàn Kiếm, Hà Nội")
                .sdtKho("0987654321")
                .gioMoCua("08:00")
                .gioDongCua("22:00")
                .dangMoCua(true)
                .ghiChuKho("Giao hàng tại cổng phụ")
                .nguoiLienHeKho("Nguyễn Văn Bán")
                .trangThai("HOAT_DONG")
                .hangGianHang("TIEM_NANG")
                .diemSaoQuaTa(0)
                .diemDanhGiaTb(BigDecimal.valueOf(5.0))
                .tongDanhGia(10)
                .tongDonHang(50)
                .tyLePhanHoiChat(BigDecimal.valueOf(100.00))
                .daXoa(false)
                .ngayTao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Lấy thông tin thiết lập Shop thành công khi có gian hàng")
    void layThietLapShop_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));

        ThietLapHoSoShopResponse res = thietLapHoSoShopService.layThietLapShop(userId);

        assertThat(res).isNotNull();
        assertThat(res.getMaGianHang()).isEqualTo(shopId);
        assertThat(res.getTenGianHang()).isEqualTo("Flex Fashion Store");
        assertThat(res.getGioMoCua()).isEqualTo("08:00");
        assertThat(res.getGioDongCua()).isEqualTo("22:00");
        assertThat(res.getDangMoCua()).isTrue();
        assertThat(res.getPhanTramHoanThien()).isEqualTo(100);
    }

    @Test
    @DisplayName("Lấy thông tin thiết lập Shop thất bại khi người dùng chưa có gian hàng")
    void layThietLapShop_ThatBai_ChuaCoGianHang() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> thietLapHoSoShopService.layThietLapShop(userId))
                .isInstanceOf(NgoaiLeUngDung.class)
                .satisfies(ex -> {
                    NgoaiLeUngDung err = (NgoaiLeUngDung) ex;
                    assertThat(err.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Cập nhật thiết lập thành công khi Shop đã được Admin phê duyệt (HOAT_DONG)")
    void capNhatThietLapShop_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.save(any(GianHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ThietLapHoSoShopRequest req = ThietLapHoSoShopRequest.builder()
                .tenGianHang("Flex Fashion Store")
                .moTa("Mô tả mới được cập nhật")
                .diaChiKho("456 Cầu Giấy, Hà Nội")
                .sdtKho("0912345678")
                .gioMoCua("09:00")
                .gioDongCua("21:30")
                .dangMoCua(true)
                .ghiChuKho("Bấm chuông kho 2")
                .nguoiLienHeKho("Trần Văn Kho")
                .build();

        ThietLapHoSoShopResponse res = thietLapHoSoShopService.capNhatThietLapShop(userId, req, null, null);

        assertThat(res).isNotNull();
        assertThat(res.getDiaChiKho()).isEqualTo("456 Cầu Giấy, Hà Nội");
        assertThat(res.getGioMoCua()).isEqualTo("09:00");
        assertThat(res.getGioDongCua()).isEqualTo("21:30");
        verify(lichSuThietLapShopRepository, atLeastOnce()).save(any(LichSuThietLapShop.class));
    }

    @Test
    @DisplayName("Từ chối cập nhật thiết lập khi Shop chưa được Admin phê duyệt (CHO_DUYET)")
    void capNhatThietLapShop_ThatBai_KhiShopChuaDuyet() {
        mockShop.setTrangThai("CHO_DUYET");
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));

        ThietLapHoSoShopRequest req = ThietLapHoSoShopRequest.builder()
                .tenGianHang("Flex Fashion Store")
                .diaChiKho("123 Phố Huế, Hà Nội")
                .sdtKho("0987654321")
                .gioMoCua("08:00")
                .gioDongCua("22:00")
                .dangMoCua(true)
                .build();

        assertThatThrownBy(() -> thietLapHoSoShopService.capNhatThietLapShop(userId, req, null, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .satisfies(ex -> {
                    NgoaiLeUngDung err = (NgoaiLeUngDung) ex;
                    assertThat(err.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(err.getMessage()).containsIgnoringCase("chỉ gian hàng đã được Admin phê duyệt");
                });
    }

    @Test
    @DisplayName("Từ chối cập nhật khi giờ đóng cửa trước giờ mở cửa")
    void capNhatThietLapShop_ThatBai_GioDongTruocGioMo() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));

        ThietLapHoSoShopRequest req = ThietLapHoSoShopRequest.builder()
                .tenGianHang("Flex Fashion Store")
                .diaChiKho("123 Phố Huế, Hà Nội")
                .sdtKho("0987654321")
                .gioMoCua("22:00")
                .gioDongCua("08:00")
                .dangMoCua(true)
                .build();

        assertThatThrownBy(() -> thietLapHoSoShopService.capNhatThietLapShop(userId, req, null, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .satisfies(ex -> {
                    NgoaiLeUngDung err = (NgoaiLeUngDung) ex;
                    assertThat(err.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(err.getMessage()).contains("Giờ đóng cửa (08:00) phải sau giờ mở cửa (22:00)");
                });
    }

    @Test
    @DisplayName("Từ chối cập nhật khi tên shop bị trùng với shop khác")
    void capNhatThietLapShop_ThatBai_TrungTenShop() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.existsByTenGianHangIgnoreCaseAndMaGianHangNotAndDaXoaFalse(eq("Shop Trung Ten"), eq(shopId)))
                .thenReturn(true);

        ThietLapHoSoShopRequest req = ThietLapHoSoShopRequest.builder()
                .tenGianHang("Shop Trung Ten")
                .diaChiKho("123 Phố Huế, Hà Nội")
                .sdtKho("0987654321")
                .gioMoCua("08:00")
                .gioDongCua("22:00")
                .dangMoCua(true)
                .build();

        assertThatThrownBy(() -> thietLapHoSoShopService.capNhatThietLapShop(userId, req, null, null))
                .isInstanceOf(NgoaiLeUngDung.class)
                .satisfies(ex -> {
                    NgoaiLeUngDung err = (NgoaiLeUngDung) ex;
                    assertThat(err.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(err.getMessage()).contains("đã được shop khác sử dụng");
                });
    }

    @Test
    @DisplayName("Chuyển trạng thái nhận đơn (Đang mở cửa / Tạm nghỉ) thành công")
    void chuyenTrangThaiNhanDon_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(gianHangRepository.save(any(GianHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ThietLapHoSoShopResponse res = thietLapHoSoShopService.chuyenTrangThaiNhanDon(userId, false);

        assertThat(res).isNotNull();
        assertThat(res.getDangMoCua()).isFalse();
        verify(lichSuThietLapShopRepository).save(any(LichSuThietLapShop.class));
    }

    @Test
    @DisplayName("Lấy lịch sử thay đổi thiết lập có phân trang thành công")
    void layLichSuThayDoi_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));

        LichSuThietLapShop mockLog = LichSuThietLapShop.builder()
                .maLichSu(1L)
                .maGianHang(shopId)
                .loaiThayDoi("THIET_LAP_TONG_THE")
                .noiDungThayDoi("Cập nhật giờ mở cửa")
                .nguoiThucHien("Nguyễn Văn Bán")
                .thoiGian(LocalDateTime.now())
                .build();

        Page<LichSuThietLapShop> mockPage = new PageImpl<>(List.of(mockLog), PageRequest.of(0, 10), 1);
        when(lichSuThietLapShopRepository.timKiemLichSu(eq(shopId), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<LichSuThietLapShopResponse> res = thietLapHoSoShopService.layLichSuThayDoi(userId, null, null, PageRequest.of(0, 10));

        assertThat(res).isNotNull();
        assertThat(res.getTotalElements()).isEqualTo(1);
        assertThat(res.getContent().get(0).getTenLoaiThayDoi()).isEqualTo("Thiết lập tổng thể");
    }

    @Test
    @DisplayName("Lấy thống kê cấu hình gian hàng thành công")
    void layThongKeThietLap_ThanhCong() {
        when(gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(userId))
                .thenReturn(Optional.of(mockShop));
        when(lichSuThietLapShopRepository.countByMaGianHang(shopId)).thenReturn(5L);

        ThongKeThietLapShopResponse res = thietLapHoSoShopService.layThongKeThietLap(userId);

        assertThat(res).isNotNull();
        assertThat(res.getTongSoLanCapNhat()).isEqualTo(5L);
        assertThat(res.getPhanTramHoanThien()).isEqualTo(100);
        assertThat(res.getDangMoCua()).isTrue();
    }
}
