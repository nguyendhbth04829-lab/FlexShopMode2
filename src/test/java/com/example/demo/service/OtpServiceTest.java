package com.example.demo.service;

import com.example.demo.dto.request.DatLaiMatKhauRequest;
import com.example.demo.dto.request.GuiYeuCauOtpRequest;
import com.example.demo.dto.request.XacThucOtpRequest;
import com.example.demo.dto.response.OtpResponse;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.XacThucOtp;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.XacThucOtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private XacThucOtpRepository xacThucOtpRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OtpService otpService;

    private NguoiDung mockNguoiDung;

    @BeforeEach
    void setUp() {
        mockNguoiDung = NguoiDung.builder()
                .maNguoiDung(1L)
                .email("testuser@flexshop.vn")
                .soDienThoai("0912345678")
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .matKhauMaHoa("$2a$12$OldPasswordHash...")
                .build();
    }

    @Test
    @DisplayName("US-03: Gửi OTP thành công - Mã 6 số ngẫu nhiên & Hiệu lực 300 giây")
    void testGuiOtpKhoiPhuc_ThanhCong() {
        // Given
        GuiYeuCauOtpRequest req = GuiYeuCauOtpRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(xacThucOtpRepository.demSoLanGuiTrongKhoangThoiGian(eq("testuser@flexshop.vn"), eq(OtpService.LOAI_OTP_KHOI_PHUC), any()))
                .thenReturn(0L);

        // When
        OtpResponse response = otpService.guiOtpKhoiPhuc(req);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNguoiNhan()).isEqualTo("testuser@flexshop.vn");
        assertThat(response.getSoGiayHieuLuc()).isEqualTo(300);
        assertThat(response.getSoLanGuiConLai()).isEqualTo(2L); // Còn 3 - 1 = 2
        assertThat(response.getMaOtpDemo()).matches("^\\d{6}$"); // Đúng 6 chữ số ngẫu nhiên

        // Verify entity saved with 300s expiry
        ArgumentCaptor<XacThucOtp> captor = ArgumentCaptor.forClass(XacThucOtp.class);
        verify(xacThucOtpRepository).save(captor.capture());
        XacThucOtp savedOtp = captor.getValue();
        assertThat(savedOtp.getMaXacThuc()).matches("^\\d{6}$");
        assertThat(savedOtp.getDaSuDung()).isFalse();
        assertThat(savedOtp.getSoLanNhapSai()).isEqualTo(0);
        assertThat(savedOtp.getThoiGianHetHan()).isAfter(LocalDateTime.now().plusSeconds(290));

        // Verify email service called
        verify(emailService).guiEmailOtp(eq("testuser@flexshop.vn"), anyString(), eq(300));
    }

    @Test
    @DisplayName("US-03: Rate Limit - Gửi quá 3 lần / 1 giờ bị từ chối 429 TOO_MANY_REQUESTS")
    void testGuiOtpKhoiPhuc_RateLimit_VuotQua3Lan() {
        // Given
        GuiYeuCauOtpRequest req = GuiYeuCauOtpRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(xacThucOtpRepository.demSoLanGuiTrongKhoangThoiGian(eq("testuser@flexshop.vn"), eq(OtpService.LOAI_OTP_KHOI_PHUC), any()))
                .thenReturn(3L); // Đã gửi 3 lần trong 1h

        // When & Then
        assertThatThrownBy(() -> otpService.guiOtpKhoiPhuc(req))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.TOO_MANY_REQUESTS)
                .hasMessageContaining("quá 3 lần trong vòng 1 giờ");

        verify(xacThucOtpRepository, never()).save(any());
        verify(emailService, never()).guiEmailOtp(any(), any(), anyInt());
    }

    @Test
    @DisplayName("US-03: Đang bị khóa 24 giờ do nhập sai quá 5 lần thì không cho gửi OTP mới")
    void testGuiOtpKhoiPhuc_DangBiKhoa24h() {
        // Given
        GuiYeuCauOtpRequest req = GuiYeuCauOtpRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .build();

        XacThucOtp otpKhoa = XacThucOtp.builder()
                .nguoiNhan("testuser@flexshop.vn")
                .khoaDenThoiGian(LocalDateTime.now().plusHours(23))
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(eq("testuser@flexshop.vn"), any()))
                .thenReturn(Optional.of(otpKhoa));

        // When & Then
        assertThatThrownBy(() -> otpService.guiOtpKhoiPhuc(req))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.FORBIDDEN)
                .hasMessageContaining("đang bị tạm khóa trong 24 giờ");
    }

    @Test
    @DisplayName("US-03: Xác thực OTP thành công")
    void testXacThucOtp_ThanhCong() {
        // Given
        XacThucOtpRequest req = XacThucOtpRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .maOtp("123456")
                .build();

        XacThucOtp otpHopLe = XacThucOtp.builder()
                .nguoiNhan("testuser@flexshop.vn")
                .maXacThuc("123456")
                .loaiOtp(OtpService.LOAI_OTP_KHOI_PHUC)
                .thoiGianHetHan(LocalDateTime.now().plusSeconds(250))
                .daSuDung(false)
                .soLanNhapSai(2)
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc("testuser@flexshop.vn", OtpService.LOAI_OTP_KHOI_PHUC))
                .thenReturn(Optional.of(otpHopLe));

        // When
        boolean result = otpService.xacThucOtp(req);

        // Then
        assertThat(result).isTrue();
        assertThat(otpHopLe.getSoLanNhapSai()).isEqualTo(0); // Reset bộ đếm số lần sai
        verify(xacThucOtpRepository).saveAndFlush(otpHopLe);
    }

    @Test
    @DisplayName("US-03: Xác thực thất bại do OTP đã hết hạn sau 300 giây")
    void testXacThucOtp_HetHanSau300Giay() {
        // Given
        XacThucOtpRequest req = XacThucOtpRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .maOtp("123456")
                .build();

        XacThucOtp otpHetHan = XacThucOtp.builder()
                .nguoiNhan("testuser@flexshop.vn")
                .maXacThuc("123456")
                .loaiOtp(OtpService.LOAI_OTP_KHOI_PHUC)
                .thoiGianHetHan(LocalDateTime.now().minusSeconds(10)) // Đã quá hạn
                .daSuDung(false)
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc("testuser@flexshop.vn", OtpService.LOAI_OTP_KHOI_PHUC))
                .thenReturn(Optional.of(otpHetHan));

        // When & Then
        assertThatThrownBy(() -> otpService.xacThucOtp(req))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Mã OTP đã hết hạn sau 300 giây");
    }

    @Test
    @DisplayName("US-03: Nhập sai OTP lần đầu - Tăng số lần sai và báo số lần còn lại")
    void testXacThucOtp_NhapSai_BaoSoLanConLai() {
        // Given
        XacThucOtpRequest req = XacThucOtpRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .maOtp("999999") // Sai mã
                .build();

        XacThucOtp otp = XacThucOtp.builder()
                .nguoiNhan("testuser@flexshop.vn")
                .maXacThuc("123456")
                .loaiOtp(OtpService.LOAI_OTP_KHOI_PHUC)
                .thoiGianHetHan(LocalDateTime.now().plusSeconds(200))
                .daSuDung(false)
                .soLanNhapSai(0)
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc("testuser@flexshop.vn", OtpService.LOAI_OTP_KHOI_PHUC))
                .thenReturn(Optional.of(otp));

        // When & Then
        assertThatThrownBy(() -> otpService.xacThucOtp(req))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Bạn còn 4 lần thử");

        assertThat(otp.getSoLanNhapSai()).isEqualTo(1);
        verify(xacThucOtpRepository).saveAndFlush(otp);
    }

    @Test
    @DisplayName("US-03: Nhập sai OTP quá 5 lần - Khóa tính năng khôi phục trong 24 giờ")
    void testXacThucOtp_NhapSaiQua5Lan_Khoa24h() {
        // Given: Đã sai 4 lần trước đó, lần này là lần thứ 5
        XacThucOtpRequest req = XacThucOtpRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .maOtp("999999") // Sai mã
                .build();

        XacThucOtp otp = XacThucOtp.builder()
                .nguoiNhan("testuser@flexshop.vn")
                .maXacThuc("123456")
                .loaiOtp(OtpService.LOAI_OTP_KHOI_PHUC)
                .thoiGianHetHan(LocalDateTime.now().plusSeconds(200))
                .daSuDung(false)
                .soLanNhapSai(4)
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc("testuser@flexshop.vn", OtpService.LOAI_OTP_KHOI_PHUC))
                .thenReturn(Optional.of(otp));

        // When & Then
        assertThatThrownBy(() -> otpService.xacThucOtp(req))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.FORBIDDEN)
                .hasMessageContaining("tạm khóa trong 24 giờ");

        assertThat(otp.getSoLanNhapSai()).isEqualTo(5);
        assertThat(otp.getKhoaDenThoiGian()).isNotNull();
        assertThat(otp.getKhoaDenThoiGian()).isAfter(LocalDateTime.now().plusHours(23));
        verify(xacThucOtpRepository).saveAndFlush(otp);
    }

    @Test
    @DisplayName("US-03: Đặt lại mật khẩu thành công - Mật khẩu mới được mã hóa và OTP được đánh dấu đã dùng")
    void testDatLaiMatKhau_ThanhCong() {
        // Given
        DatLaiMatKhauRequest req = DatLaiMatKhauRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .maOtp("123456")
                .matKhauMoi("FlexShop@2026")
                .xacNhanMatKhau("FlexShop@2026")
                .build();

        XacThucOtp otpHopLe = XacThucOtp.builder()
                .nguoiNhan("testuser@flexshop.vn")
                .maXacThuc("123456")
                .loaiOtp(OtpService.LOAI_OTP_KHOI_PHUC)
                .thoiGianHetHan(LocalDateTime.now().plusSeconds(250))
                .daSuDung(false)
                .soLanNhapSai(0)
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc("testuser@flexshop.vn", OtpService.LOAI_OTP_KHOI_PHUC))
                .thenReturn(Optional.of(otpHopLe));
        when(passwordEncoder.encode("FlexShop@2026")).thenReturn("$2a$12$NewEncodedPassword...");

        // When
        otpService.datLaiMatKhau(req);

        // Then
        assertThat(mockNguoiDung.getMatKhauMaHoa()).isEqualTo("$2a$12$NewEncodedPassword...");
        assertThat(otpHopLe.getDaSuDung()).isTrue();
        verify(nguoiDungRepository).save(mockNguoiDung);
        verify(xacThucOtpRepository, atLeastOnce()).saveAndFlush(otpHopLe);
    }

    @Test
    @DisplayName("US-03: Đặt lại mật khẩu thất bại khi mật khẩu mới TRÙNG với mật khẩu cũ")
    void testDatLaiMatKhau_TrungMatKhauCu_NemNgoaiLe() {
        // Given
        DatLaiMatKhauRequest req = DatLaiMatKhauRequest.builder()
                .taiKhoan("testuser@flexshop.vn")
                .maOtp("123456")
                .matKhauMoi("OldPassword@123")
                .xacNhanMatKhau("OldPassword@123")
                .build();

        XacThucOtp otpHopLe = XacThucOtp.builder()
                .nguoiNhan("testuser@flexshop.vn")
                .maXacThuc("123456")
                .loaiOtp(OtpService.LOAI_OTP_KHOI_PHUC)
                .thoiGianHetHan(LocalDateTime.now().plusSeconds(250))
                .daSuDung(false)
                .soLanNhapSai(0)
                .build();

        when(nguoiDungRepository.findByIdentifier("testuser@flexshop.vn"))
                .thenReturn(Optional.of(mockNguoiDung));
        when(xacThucOtpRepository.findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc("testuser@flexshop.vn", OtpService.LOAI_OTP_KHOI_PHUC))
                .thenReturn(Optional.of(otpHopLe));
        // Giả lập mật khẩu mới TRÙNG với mật khẩu cũ trong CSDL
        when(passwordEncoder.matches("OldPassword@123", mockNguoiDung.getMatKhauMaHoa())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> otpService.datLaiMatKhau(req))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("không được trùng với mật khẩu hiện tại");

        // Đảm bảo mật khẩu không bị thay đổi và OTP chưa bị đánh dấu là đã sử dụng
        assertThat(otpHopLe.getDaSuDung()).isFalse();
        verify(nguoiDungRepository, never()).save(any());
    }
}
