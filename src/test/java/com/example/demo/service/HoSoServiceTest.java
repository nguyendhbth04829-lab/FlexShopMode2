package com.example.demo.service;

import com.example.demo.dto.request.CapNhatHoSoRequest;
import com.example.demo.dto.request.DoiMatKhauRequest;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeHoSoResponse;
import com.example.demo.entity.NguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.DiaChiNguoiDungRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.security.NguoiDungPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoSoServiceTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private DiaChiNguoiDungRepository diaChiNguoiDungRepository;

    @Mock
    private XacThucService xacThucService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private HoSoService hoSoService;

    private NguoiDung nguoiDung;
    private NguoiDungPrincipal principal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hoSoService, "thuMucUpload", "uploads/avatars");

        nguoiDung = NguoiDung.builder()
                .maNguoiDung(10L)
                .email("test.user@flexshop.vn")
                .hoVaTen("Nguyễn Văn Test")
                .soDienThoai("0912345678")
                .matKhauMaHoa("$2a$12$oldHashedPasswordExample123456789")
                .anhDaiDien("/images/default-avatar.png")
                .trangThai("HOAT_DONG")
                .ngayTao(LocalDateTime.now().minusDays(5))
                .ngayCapNhat(LocalDateTime.now())
                .danhSachVaiTro(Collections.emptySet())
                .build();

        principal = NguoiDungPrincipal.builder()
                .maNguoiDung(10L)
                .email("test.user@flexshop.vn")
                .hoVaTen("Nguyễn Văn Test")
                .soDienThoai("0912345678")
                .danhSachQuyen(Collections.emptyList())
                .hoatDong(true)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("US-04: Lấy thông tin hồ sơ hiện tại thành công")
    void testLayHoSoHienTai_ThanhCong() {
        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(xacThucService.chuyenSangNguoiDungResponse(nguoiDung)).thenReturn(
                NguoiDungResponse.builder().maNguoiDung(10L).hoVaTen("Nguyễn Văn Test").email("test.user@flexshop.vn").build()
        );

        NguoiDungResponse response = hoSoService.layHoSoHienTai();

        assertThat(response).isNotNull();
        assertThat(response.getMaNguoiDung()).isEqualTo(10L);
        assertThat(response.getHoVaTen()).isEqualTo("Nguyễn Văn Test");
    }

    @Test
    @DisplayName("US-04: Cập nhật thông tin hồ sơ (Họ tên, SĐT) thành công")
    void testCapNhatHoSo_ThanhCong() {
        CapNhatHoSoRequest request = CapNhatHoSoRequest.builder()
                .hoVaTen("Nguyễn Văn Cập Nhật")
                .soDienThoai("0988776655")
                .build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(nguoiDungRepository.existsBySoDienThoaiAndMaNguoiDungNot("0988776655", 10L)).thenReturn(false);
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(xacThucService.chuyenSangNguoiDungResponse(any(NguoiDung.class))).thenReturn(
                NguoiDungResponse.builder().maNguoiDung(10L).hoVaTen("Nguyễn Văn Cập Nhật").soDienThoai("0988776655").build()
        );

        NguoiDungResponse response = hoSoService.capNhatHoSo(request);

        assertThat(response.getHoVaTen()).isEqualTo("Nguyễn Văn Cập Nhật");
        assertThat(response.getSoDienThoai()).isEqualTo("0988776655");
        verify(nguoiDungRepository).save(nguoiDung);
    }

    @Test
    @DisplayName("US-04: Cập nhật hồ sơ thất bại do trùng Số điện thoại của tài khoản khác (409 Conflict)")
    void testCapNhatHoSo_TrungSoDienThoai() {
        CapNhatHoSoRequest request = CapNhatHoSoRequest.builder()
                .hoVaTen("Nguyễn Văn Test")
                .soDienThoai("0999999999")
                .build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(nguoiDungRepository.existsBySoDienThoaiAndMaNguoiDungNot("0999999999", 10L)).thenReturn(true);

        assertThatThrownBy(() -> hoSoService.capNhatHoSo(request))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("đã được đăng ký bởi một tài khoản khác")
                .satisfies(ex -> assertThat(((NgoaiLeUngDung) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verify(nguoiDungRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-04: Đổi mật khẩu thành công khi thông tin hợp lệ")
    void testDoiMatKhau_ThanhCong() {
        DoiMatKhauRequest request = DoiMatKhauRequest.builder()
                .matKhauHienTai("OldPass@123")
                .matKhauMoi("NewPass@456")
                .xacNhanMatKhauMoi("NewPass@456")
                .build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(passwordEncoder.matches("OldPass@123", nguoiDung.getMatKhauMaHoa())).thenReturn(true);
        when(passwordEncoder.matches("NewPass@456", nguoiDung.getMatKhauMaHoa())).thenReturn(false);
        when(passwordEncoder.encode("NewPass@456")).thenReturn("$2a$12$newHashedPasswordEncoded123");
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(xacThucService.chuyenSangNguoiDungResponse(any(NguoiDung.class))).thenReturn(
                NguoiDungResponse.builder().maNguoiDung(10L).build()
        );

        NguoiDungResponse response = hoSoService.doiMatKhau(request);

        assertThat(response).isNotNull();
        assertThat(nguoiDung.getMatKhauMaHoa()).isEqualTo("$2a$12$newHashedPasswordEncoded123");
        verify(nguoiDungRepository).save(nguoiDung);
    }

    @Test
    @DisplayName("US-04: Đổi mật khẩu thất bại do mật khẩu hiện tại không đúng")
    void testDoiMatKhau_SaiMatKhauHienTai() {
        DoiMatKhauRequest request = DoiMatKhauRequest.builder()
                .matKhauHienTai("WrongPass@123")
                .matKhauMoi("NewPass@456")
                .xacNhanMatKhauMoi("NewPass@456")
                .build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(passwordEncoder.matches("WrongPass@123", nguoiDung.getMatKhauMaHoa())).thenReturn(false);

        assertThatThrownBy(() -> hoSoService.doiMatKhau(request))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Mật khẩu hiện tại không chính xác");

        verify(nguoiDungRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-04: Đổi mật khẩu thất bại do Mật khẩu mới TRÙNG với Mật khẩu cũ")
    void testDoiMatKhau_TrungMatKhauCu() {
        DoiMatKhauRequest request = DoiMatKhauRequest.builder()
                .matKhauHienTai("OldPass@123")
                .matKhauMoi("OldPass@123")
                .xacNhanMatKhauMoi("OldPass@123")
                .build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(passwordEncoder.matches("OldPass@123", nguoiDung.getMatKhauMaHoa())).thenReturn(true);

        assertThatThrownBy(() -> hoSoService.doiMatKhau(request))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Mật khẩu mới không được trùng với mật khẩu cũ");

        verify(nguoiDungRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-04: Đổi mật khẩu thất bại do xác nhận mật khẩu mới không khớp")
    void testDoiMatKhau_XacNhanKhongKhop() {
        DoiMatKhauRequest request = DoiMatKhauRequest.builder()
                .matKhauHienTai("OldPass@123")
                .matKhauMoi("NewPass@456")
                .xacNhanMatKhauMoi("DifferentPass@789")
                .build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(passwordEncoder.matches("OldPass@123", nguoiDung.getMatKhauMaHoa())).thenReturn(true);
        when(passwordEncoder.matches("NewPass@456", nguoiDung.getMatKhauMaHoa())).thenReturn(false);

        assertThatThrownBy(() -> hoSoService.doiMatKhau(request))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("không khớp với mật khẩu mới");

        verify(nguoiDungRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-04: Upload Avatar thất bại khi tệp rỗng")
    void testTaiLenAnhDaiDien_TepRong() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> hoSoService.taiLenAnhDaiDien(emptyFile))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Vui lòng chọn tệp ảnh đại diện");
    }

    @Test
    @DisplayName("US-04: Upload Avatar thất bại khi định dạng không hợp lệ")
    void testTaiLenAnhDaiDien_SaiDinhDang() {
        MockMultipartFile textFile = new MockMultipartFile("file", "test.txt", "text/plain", "abc".getBytes());

        assertThatThrownBy(() -> hoSoService.taiLenAnhDaiDien(textFile))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasMessageContaining("Định dạng ảnh không hợp lệ");
    }

    @Test
    @DisplayName("US-04: Upload Avatar thành công")
    void testTaiLenAnhDaiDien_ThanhCong() {
        MockMultipartFile validImage = new MockMultipartFile(
                "file", "my_avatar.png", "image/png", new byte[]{ (byte) 0x89, 0x50, 0x4E, 0x47 }
        );

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(inv -> inv.getArgument(0));
        when(xacThucService.chuyenSangNguoiDungResponse(any(NguoiDung.class))).thenAnswer(inv -> {
            NguoiDung u = inv.getArgument(0);
            return NguoiDungResponse.builder().maNguoiDung(u.getMaNguoiDung()).anhDaiDien(u.getAnhDaiDien()).build();
        });

        NguoiDungResponse response = hoSoService.taiLenAnhDaiDien(validImage);

        assertThat(response).isNotNull();
        assertThat(response.getAnhDaiDien()).startsWith("/uploads/avatars/avatar_user_10_");
        verify(nguoiDungRepository).save(nguoiDung);
    }

    @Test
    @DisplayName("US-04: Lấy thống kê hồ sơ cá nhân thành công")
    void testLayThongKeHoSo_ThanhCong() {
        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(nguoiDung));
        when(xacThucService.chuyenSangNguoiDungResponse(nguoiDung)).thenReturn(
                NguoiDungResponse.builder().maNguoiDung(10L).build()
        );
        when(diaChiNguoiDungRepository.countByMaNguoiDungAndDaXoaFalse(10L)).thenReturn(3L);

        ThongKeHoSoResponse response = hoSoService.layThongKeHoSo();

        assertThat(response).isNotNull();
        assertThat(response.getTongSoDiaChi()).isEqualTo(3L);
        assertThat(response.getGioiHanDiaChi()).isEqualTo(20);
        assertThat(response.getTrangThaiBaoMat()).contains("BCrypt salt 12");
    }
}
