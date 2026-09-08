package com.example.demo.service;

import com.example.demo.dto.request.TuChoiGianHangRequest;
import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.PhanTrangResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.entity.ChungChiGianHang;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.VaiTro;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.ChungChiGianHangRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VaiTroRepository;
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
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KiemDuyetGianHangServiceTest {

    @Mock
    private GianHangRepository gianHangRepository;

    @Mock
    private ChungChiGianHangRepository chungChiGianHangRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private VaiTroRepository vaiTroRepository;

    @InjectMocks
    private KiemDuyetGianHangService kiemDuyetGianHangService;

    private Long shopId;
    private Long userId;
    private NguoiDung mockUser;
    private GianHang mockShop;
    private ChungChiGianHang mockLicense;
    private VaiTro mockRoleKhachHang;
    private VaiTro mockRoleNguoiBan;

    @BeforeEach
    void setUp() {
        shopId = 100L;
        userId = 200L;

        mockRoleKhachHang = VaiTro.builder()
                .maVaiTro(1)
                .tenVaiTro("KHACH_HANG")
                .moTa("Khách hàng")
                .build();

        mockRoleNguoiBan = VaiTro.builder()
                .maVaiTro(2)
                .tenVaiTro("NGUOI_BAN")
                .moTa("Người bán hàng")
                .build();

        mockUser = NguoiDung.builder()
                .maNguoiDung(userId)
                .email("seller@flexshop.vn")
                .hoVaTen("Nguyễn Văn Shop")
                .soDienThoai("0988111222")
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .danhSachVaiTro(new HashSet<>(List.of(mockRoleKhachHang)))
                .build();

        mockLicense = ChungChiGianHang.builder()
                .maChungChi(50L)
                .maGianHang(shopId)
                .loaiGiayTo("GIAY_PHEP_KINH_DOANH")
                .soGiayTo("0109988111")
                .linkAnhGiayTo("/uploads/certificates/gpdk_test.pdf")
                .trangThaiDuyet("CHO_DUYET")
                .ngayTao(LocalDateTime.now())
                .build();

        mockShop = GianHang.builder()
                .maGianHang(shopId)
                .maChuSoHuu(userId)
                .tenGianHang("Thời Trang Flex Shop")
                .duongDanSlug("thoi-trang-flex-shop")
                .moTa("Cửa hàng thời trang")
                .diaChiKho("123 Cầu Giấy, Hà Nội")
                .sdtKho("0988111222")
                .trangThai("CHO_DUYET")
                .daXoa(false)
                .ngayTao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("US09-01: Phê duyệt gian hàng thành công -> Chuyển HOAT_DONG & Cấp vai trò NGUOI_BAN cho chủ shop")
    void pheDuyetGianHang_ThanhCong_ChuyenHoatDongVaCapRoleNguoiBan() {
        // Arrange
        when(gianHangRepository.findById(shopId)).thenReturn(Optional.of(mockShop));
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(shopId)).thenReturn(Optional.of(mockLicense));
        when(vaiTroRepository.findByTenVaiTro("NGUOI_BAN")).thenReturn(Optional.of(mockRoleNguoiBan));
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gianHangRepository.save(any(GianHang.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chungChiGianHangRepository.save(any(ChungChiGianHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        GianHangResponse response = kiemDuyetGianHangService.pheDuyetGianHang(shopId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMaGianHang()).isEqualTo(shopId);
        assertThat(response.getTrangThai()).isEqualTo("HOAT_DONG");
        assertThat(mockShop.getTrangThai()).isEqualTo("HOAT_DONG");
        assertThat(mockLicense.getTrangThaiDuyet()).isEqualTo("DA_DUYET");

        // Kiểm tra User được gán vai trò NGUOI_BAN
        boolean coRoleNguoiBan = mockUser.getDanhSachVaiTro().stream()
                .anyMatch(r -> "NGUOI_BAN".equals(r.getTenVaiTro()));
        assertThat(coRoleNguoiBan).isTrue();

        verify(nguoiDungRepository, times(1)).save(mockUser);
        verify(gianHangRepository, times(1)).save(mockShop);
        verify(chungChiGianHangRepository, times(1)).save(mockLicense);
    }

    @Test
    @DisplayName("US09-02: Phê duyệt thất bại khi gian hàng không tồn tại")
    void pheDuyetGianHang_ThatBai_GianHangKhongTonTai() {
        // Arrange
        when(gianHangRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> kiemDuyetGianHangService.pheDuyetGianHang(999L))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.NOT_FOUND);

        verify(gianHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("US09-03: Phê duyệt thất bại khi gian hàng đã ở trạng thái HOAT_DONG")
    void pheDuyetGianHang_ThatBai_DaHoatDong() {
        // Arrange
        mockShop.setTrangThai("HOAT_DONG");
        when(gianHangRepository.findById(shopId)).thenReturn(Optional.of(mockShop));

        // Act & Assert
        assertThatThrownBy(() -> kiemDuyetGianHangService.pheDuyetGianHang(shopId))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("đang hoạt động");

        verify(gianHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("US09-04: Từ chối gian hàng thành công -> Cập nhật TU_CHOI và lưu lý do từ chối")
    void tuChoiGianHang_ThanhCong_ChuyenTuChoiVaGhiLyDo() {
        // Arrange
        TuChoiGianHangRequest request = TuChoiGianHangRequest.builder()
                .lyDoTuChoi("Giấy phép kinh doanh mờ, mã số thuế không khớp cổng DKKD quốc gia")
                .build();

        when(gianHangRepository.findById(shopId)).thenReturn(Optional.of(mockShop));
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(shopId)).thenReturn(Optional.of(mockLicense));
        when(gianHangRepository.save(any(GianHang.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chungChiGianHangRepository.save(any(ChungChiGianHang.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        GianHangResponse response = kiemDuyetGianHangService.tuChoiGianHang(shopId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTrangThai()).isEqualTo("TU_CHOI");
        assertThat(response.getLyDoTuChoi()).isEqualTo(request.getLyDoTuChoi());
        assertThat(mockShop.getTrangThai()).isEqualTo("TU_CHOI");
        assertThat(mockShop.getLyDoTuChoi()).isEqualTo(request.getLyDoTuChoi());
        assertThat(mockLicense.getTrangThaiDuyet()).isEqualTo("TU_CHOI");

        verify(gianHangRepository, times(1)).save(mockShop);
        verify(chungChiGianHangRepository, times(1)).save(mockLicense);
    }

    @Test
    @DisplayName("US09-05: Từ chối thất bại khi lý do rỗng hoặc dưới 5 ký tự")
    void tuChoiGianHang_ThatBai_LyDoRongHoacQuaNgan() {
        // Arrange
        TuChoiGianHangRequest emptyRequest = TuChoiGianHangRequest.builder()
                .lyDoTuChoi("   ")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> kiemDuyetGianHangService.tuChoiGianHang(shopId, emptyRequest))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("bắt buộc");

        TuChoiGianHangRequest shortRequest = TuChoiGianHangRequest.builder()
                .lyDoTuChoi("Lỗi")
                .build();

        assertThatThrownBy(() -> kiemDuyetGianHangService.tuChoiGianHang(shopId, shortRequest))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("từ 5 đến 255 ký tự");

        verify(gianHangRepository, never()).save(any());
    }

    @Test
    @DisplayName("US09-06: Lấy chi tiết gian hàng thành công")
    void layChiTietGianHang_ThanhCong() {
        // Arrange
        when(gianHangRepository.findById(shopId)).thenReturn(Optional.of(mockShop));
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(shopId)).thenReturn(Optional.of(mockLicense));

        // Act
        GianHangResponse response = kiemDuyetGianHangService.layChiTietGianHang(shopId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMaGianHang()).isEqualTo(shopId);
        assertThat(response.getTenGianHang()).isEqualTo(mockShop.getTenGianHang());
        assertThat(response.getTenChuSoHuu()).isEqualTo(mockUser.getHoVaTen());
        assertThat(response.getEmailChuSoHuu()).isEqualTo(mockUser.getEmail());
        assertThat(response.getSoGiayTo()).isEqualTo("0109988111");
    }

    @Test
    @DisplayName("US09-07: Thống kê số lượng kiểm duyệt chính xác theo trạng thái")
    void layThongKeKiemDuyet_ThanhCong() {
        // Arrange
        when(gianHangRepository.countByDaXoaFalse()).thenReturn(15L);
        when(gianHangRepository.countByTrangThaiAndDaXoaFalse("CHO_DUYET")).thenReturn(5L);
        when(gianHangRepository.countByTrangThaiAndDaXoaFalse("HOAT_DONG")).thenReturn(8L);
        when(gianHangRepository.countByTrangThaiAndDaXoaFalse("TU_CHOI")).thenReturn(2L);
        when(gianHangRepository.countByTrangThaiAndDaXoaFalse("TAM_KHOA")).thenReturn(0L);

        // Act
        ThongKeGianHangResponse stats = kiemDuyetGianHangService.layThongKeKiemDuyet();

        // Assert
        assertThat(stats).isNotNull();
        assertThat(stats.getTongSo()).isEqualTo(15L);
        assertThat(stats.getChoDuyet()).isEqualTo(5L);
        assertThat(stats.getHoatDong()).isEqualTo(8L);
        assertThat(stats.getTuChoi()).isEqualTo(2L);
        assertThat(stats.getTamKhoa()).isEqualTo(0L);
    }

    @Test
    @DisplayName("US09-08: Tìm kiếm và phân trang gian hàng thành công")
    void danhSachGianHangPhanTrang_ThanhCong() {
        // Arrange
        Page<GianHang> page = new PageImpl<>(List.of(mockShop), PageRequest.of(0, 10), 1);
        when(gianHangRepository.timKiemPhanTrang(eq("Thời Trang"), eq("CHO_DUYET"), any())).thenReturn(page);
        when(nguoiDungRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(shopId)).thenReturn(Optional.of(mockLicense));

        // Act
        PhanTrangResponse<GianHangResponse> result = kiemDuyetGianHangService.danhSachGianHangPhanTrang("Thời Trang", "CHO_DUYET", 0, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTenGianHang()).isEqualTo("Thời Trang Flex Shop");
    }
}
