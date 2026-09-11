package com.example.demo.service;

import com.example.demo.dto.request.CapNhatDiaChiRequest;
import com.example.demo.dto.request.TaoDiaChiRequest;
import com.example.demo.dto.response.DiaChiResponse;
import com.example.demo.dto.response.ThongKeDiaChiResponse;
import com.example.demo.entity.DiaChiNguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.DiaChiNguoiDungRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaChiServiceTest {

    @Mock
    private DiaChiNguoiDungRepository diaChiRepository;

    @InjectMocks
    private DiaChiService diaChiService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 3L;
    }

    @Test
    @DisplayName("US-05: Thêm địa chỉ đầu tiên -> Tự động thiết lập làm địa chỉ mặc định")
    void testTaoDiaChi_DauTien_TuDongThanhMacDinh() {
        // Given
        TaoDiaChiRequest request = TaoDiaChiRequest.builder()
                .tenNguoiNhan("Lê Khách Hàng")
                .soDienThoai("0987654321")
                .tinhThanh("Hà Nội")
                .quanHuyen("Cầu Giấy")
                .xaPhuong("Dịch Vọng")
                .diaChiChiTiet("Số 10 Duy Tân")
                .laMacDinh(false) // Người dùng không tick mặc định
                .build();

        when(diaChiRepository.countByMaNguoiDungAndDaXoaFalse(userId)).thenReturn(0L);
        when(diaChiRepository.save(any(DiaChiNguoiDung.class))).thenAnswer(invocation -> {
            DiaChiNguoiDung entity = invocation.getArgument(0);
            entity.setMaDiaChi(100L);
            return entity;
        });

        // When
        DiaChiResponse response = diaChiService.taoDiaChi(request, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMaDiaChi()).isEqualTo(100L);
        assertThat(response.getLaMacDinh()).isTrue(); // Tự động set mặc định
        verify(diaChiRepository, never()).huyMacDinhCacDiaChiKhac(anyLong(), anyLong());
    }

    @Test
    @DisplayName("US-05: Thêm địa chỉ mới với la_mac_dinh = true -> Tự động chuyển các địa chỉ khác về false trong DB")
    void testTaoDiaChi_MacDinh_DongBoCacDiaChiKhac() {
        // Given: User đã có 2 địa chỉ
        TaoDiaChiRequest request = TaoDiaChiRequest.builder()
                .tenNguoiNhan("Lê Khách Hàng 2")
                .soDienThoai("0912345678")
                .tinhThanh("TP. Hồ Chí Minh")
                .quanHuyen("Quận 1")
                .xaPhuong("Bến Nghé")
                .diaChiChiTiet("Bitexco Tower")
                .laMacDinh(true)
                .build();

        when(diaChiRepository.countByMaNguoiDungAndDaXoaFalse(userId)).thenReturn(2L);
        when(diaChiRepository.save(any(DiaChiNguoiDung.class))).thenAnswer(invocation -> {
            DiaChiNguoiDung entity = invocation.getArgument(0);
            entity.setMaDiaChi(200L);
            return entity;
        });

        // When
        DiaChiResponse response = diaChiService.taoDiaChi(request, userId);

        // Then
        assertThat(response.getLaMacDinh()).isTrue();
        // Kiểm tra đã gọi hủy mặc định các địa chỉ khác
        verify(diaChiRepository, times(1)).huyMacDinhCacDiaChiKhac(eq(userId), eq(200L));
    }

    @Test
    @DisplayName("US-05: Chặn thêm mới khi đã đạt giới hạn tối đa 20 địa chỉ")
    void testTaoDiaChi_VuotQuaGioiHan20_NemNgoaiLe() {
        // Given: Đã có 20 địa chỉ
        when(diaChiRepository.countByMaNguoiDungAndDaXoaFalse(userId)).thenReturn(20L);

        TaoDiaChiRequest request = TaoDiaChiRequest.builder()
                .tenNguoiNhan("Địa chỉ thứ 21")
                .soDienThoai("0987654321")
                .tinhThanh("Hà Nội")
                .quanHuyen("Cầu Giấy")
                .xaPhuong("Dịch Vọng")
                .diaChiChiTiet("Số 21")
                .build();

        // When & Then
        assertThatThrownBy(() -> diaChiService.taoDiaChi(request, userId))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Bạn đã đạt giới hạn tối đa 20 địa chỉ");

        verify(diaChiRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-05: Đặt làm mặc định -> Chuyển tất cả địa chỉ khác về la_mac_dinh = 0")
    void testDatLamMacDinh_ThanhCong() {
        Long maDiaChi = 15L;
        DiaChiNguoiDung diaChiHienTai = DiaChiNguoiDung.builder()
                .maDiaChi(maDiaChi)
                .nguoiDung(null)
                .tenNguoiNhan("Người nhận")
                .soDienThoai("0987654321")
                .laMacDinh(false)
                .daXoa(false)
                .build();

        when(diaChiRepository.findByMaDiaChiAndMaNguoiDungAndDaXoaFalse(maDiaChi, userId))
                .thenReturn(Optional.of(diaChiHienTai));
        when(diaChiRepository.save(any(DiaChiNguoiDung.class))).thenReturn(diaChiHienTai);

        // When
        DiaChiResponse response = diaChiService.datLamMacDinh(maDiaChi, userId);

        // Then
        assertThat(response.getLaMacDinh()).isTrue();
        verify(diaChiRepository, times(1)).huyMacDinhCacDiaChiKhac(userId, maDiaChi);
    }

    @Test
    @DisplayName("US-05: Đặt làm mặc định khi địa chỉ đã là mặc định -> Không gọi hủy lặp lại")
    void testDatLamMacDinh_DaLaMacDinhRoi() {
        Long maDiaChi = 15L;
        DiaChiNguoiDung diaChiHienTai = DiaChiNguoiDung.builder()
                .maDiaChi(maDiaChi)
                .nguoiDung(null)
                .laMacDinh(true)
                .daXoa(false)
                .build();

        when(diaChiRepository.findByMaDiaChiAndMaNguoiDungAndDaXoaFalse(maDiaChi, userId))
                .thenReturn(Optional.of(diaChiHienTai));

        // When
        DiaChiResponse response = diaChiService.datLamMacDinh(maDiaChi, userId);

        // Then
        assertThat(response.getLaMacDinh()).isTrue();
        verify(diaChiRepository, never()).huyMacDinhCacDiaChiKhac(anyLong(), anyLong());
    }

    @Test
    @DisplayName("US-05: Xóa địa chỉ mặc định -> Tự động gán địa chỉ còn lại gần nhất làm mặc định")
    void testXoaDiaChi_MacDinh_TuDongGanDiaChiKhac() {
        Long maDiaChiXoa = 10L;
        DiaChiNguoiDung diaChiXoa = DiaChiNguoiDung.builder()
                .maDiaChi(maDiaChiXoa)
                .nguoiDung(null)
                .laMacDinh(true)
                .daXoa(false)
                .build();

        DiaChiNguoiDung diaChiThayThe = DiaChiNguoiDung.builder()
                .maDiaChi(11L)
                .nguoiDung(null)
                .laMacDinh(false)
                .daXoa(false)
                .build();

        when(diaChiRepository.findByMaDiaChiAndMaNguoiDungAndDaXoaFalse(maDiaChiXoa, userId))
                .thenReturn(Optional.of(diaChiXoa));
        when(diaChiRepository.findTopByMaNguoiDungAndDaXoaFalseOrderByNgayTaoDesc(userId))
                .thenReturn(Optional.of(diaChiThayThe));

        // When
        diaChiService.xoaDiaChi(maDiaChiXoa, userId);

        // Then
        assertThat(diaChiXoa.getDaXoa()).isTrue();
        assertThat(diaChiXoa.getLaMacDinh()).isFalse();
        assertThat(diaChiThayThe.getLaMacDinh()).isTrue(); // Đã được gán thay thế
        verify(diaChiRepository, atLeastOnce()).save(diaChiThayThe);
    }

    @Test
    @DisplayName("US-05: Cập nhật địa chỉ người khác -> Báo lỗi 404 Không tìm thấy")
    void testCapNhatDiaChi_KhongDungQuyen_NemNgoaiLe() {
        Long maDiaChi = 999L;
        when(diaChiRepository.findByMaDiaChiAndMaNguoiDungAndDaXoaFalse(maDiaChi, userId))
                .thenReturn(Optional.empty());

        CapNhatDiaChiRequest req = CapNhatDiaChiRequest.builder()
                .tenNguoiNhan("Hack")
                .soDienThoai("0987654321")
                .tinhThanh("HN")
                .quanHuyen("CG")
                .xaPhuong("DV")
                .diaChiChiTiet("123")
                .build();

        assertThatThrownBy(() -> diaChiService.capNhatDiaChi(maDiaChi, req, userId))
                .isInstanceOf(NgoaiLeUngDung.class)
                .satisfies(e -> {
                    NgoaiLeUngDung ex = (NgoaiLeUngDung) e;
                    assertThat(ex.getMaTrangThai()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("US-05: Thống kê địa chỉ người dùng (số lượng, giới hạn, còn lại)")
    void testThongKeDiaChi() {
        when(diaChiRepository.countByMaNguoiDungAndDaXoaFalse(userId)).thenReturn(5L);
        when(diaChiRepository.findByMaNguoiDungAndLaMacDinhTrueAndDaXoaFalse(userId))
                .thenReturn(Optional.of(DiaChiNguoiDung.builder().maDiaChi(1L).laMacDinh(true).build()));
        when(diaChiRepository.layDanhSachTinhThanhCuaNguoiDung(userId))
                .thenReturn(List.of("Hà Nội", "Đà Nẵng", "TP. Hồ Chí Minh"));

        ThongKeDiaChiResponse thongKe = diaChiService.thongKeDiaChi(userId);

        assertThat(thongKe.getTongSoDiaChi()).isEqualTo(5L);
        assertThat(thongKe.getGioiHanToiDa()).isEqualTo(20);
        assertThat(thongKe.getSoLuongConLai()).isEqualTo(15L);
        assertThat(thongKe.getCoDiaChiMacDinh()).isTrue();
        assertThat(thongKe.getDanhSachTinhThanh()).hasSize(3);
    }
}
