package com.example.demo.service;

import com.example.demo.dto.request.DangKyRequest;
import com.example.demo.dto.request.DangNhapRequest;
import com.example.demo.dto.request.LamMoiTokenRequest;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.XacThucResponse;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class XacThucServiceTest {

    @Autowired
    private XacThucService xacThucService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private com.example.demo.repository.NguoiDungRepository nguoiDungRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("US-01: Đăng ký tài khoản thành công với email hợp lệ, mật khẩu mạnh và gán mặc định role KHACH_HANG")
    void testDangKyThanhCong() {
        DangKyRequest request = DangKyRequest.builder()
                .email("test.vietnam.user@flexshop.vn")
                .soDienThoai("0933111222")
                .matKhau("SecurePass@2026")
                .hoVaTen("Người Dùng Việt Nam")
                .build();

        NguoiDungResponse response = xacThucService.dangKy(request);

        assertNotNull(response);
        assertNotNull(response.getMaNguoiDung());
        assertEquals("test.vietnam.user@flexshop.vn", response.getEmail());
        assertEquals("0933111222", response.getSoDienThoai());
        assertEquals("Người Dùng Việt Nam", response.getHoVaTen());
        assertTrue(response.getDanhSachVaiTro().contains("KHACH_HANG"), "Mặc định phải có vai trò KHACH_HANG");
        assertEquals("/customer/dashboard", response.getDuongDanDashboard());
    }

    @Test
    @DisplayName("US-01: Chặn đăng ký trùng Email không phân biệt chữ hoa, chữ thường (Case-insensitive)")
    void testDangKyTrungEmailKhongPhanBietHoaThuong() {
        // Đăng ký lần 1 bằng chữ thường
        DangKyRequest request1 = DangKyRequest.builder()
                .email("trung.email@flexshop.vn")
                .soDienThoai("0988001122")
                .matKhau("FlexShop@2026")
                .hoVaTen("User Mot")
                .build();
        xacThucService.dangKy(request1);

        // Đăng ký lần 2 bằng chữ hoa chữ thường lẫn lộn
        DangKyRequest request2 = DangKyRequest.builder()
                .email("TRUNG.EMAIL@FLEXSHOP.VN")
                .soDienThoai("0988001133")
                .matKhau("FlexShop@2026")
                .hoVaTen("User Hai")
                .build();

        NgoaiLeUngDung exception = assertThrows(NgoaiLeUngDung.class, () -> xacThucService.dangKy(request2));
        assertTrue(exception.getMessage().contains("đã được đăng ký"), "Phải báo lỗi trùng email");
    }

    @Test
    @DisplayName("US-02: Đăng nhập thành công, sinh JWT Access Token (15m) & Refresh Token (7d) và điều hướng đúng Dashboard")
    void testDangNhapThanhCong() {
        DangNhapRequest loginRequest = DangNhapRequest.builder()
                .taiKhoan("admin@flexshop.vn")
                .matKhau("12345678")
                .build();

        XacThucResponse authResponse = xacThucService.dangNhap(loginRequest);

        assertNotNull(authResponse);
        assertNotNull(authResponse.getAccessToken(), "Access Token không được rỗng");
        assertNotNull(authResponse.getRefreshToken(), "Refresh Token không được rỗng");
        assertEquals("Bearer", authResponse.getLoaiToken());
        assertEquals(900L, authResponse.getThoiGianHetHan(), "Access Token phải hết hạn sau 15 phút (900 giây)");

        assertTrue(jwtTokenProvider.validateToken(authResponse.getAccessToken()));
        assertTrue(jwtTokenProvider.validateToken(authResponse.getRefreshToken()));

        assertEquals("/admin/dashboard", authResponse.getThongTinNguoiDung().getDuongDanDashboard());
    }

    @Test
    @DisplayName("US-02: Hỗ trợ đăng nhập bằng Số điện thoại")
    void testDangNhapBangSoDienThoai() {
        DangNhapRequest loginRequest = DangNhapRequest.builder()
                .taiKhoan("0900000002")
                .matKhau("12345678")
                .build();

        XacThucResponse authResponse = xacThucService.dangNhap(loginRequest);

        assertNotNull(authResponse);
        assertEquals("seller@flexshop.vn", authResponse.getThongTinNguoiDung().getEmail());
        assertEquals("/seller/dashboard", authResponse.getThongTinNguoiDung().getDuongDanDashboard());
    }

    @Test
    @DisplayName("US-02: Từ chối đăng nhập khi sai mật khẩu")
    void testDangNhapSaiMatKhau() {
        DangNhapRequest loginRequest = DangNhapRequest.builder()
                .taiKhoan("admin@flexshop.vn")
                .matKhau("MatKhauSai@123")
                .build();

        assertThrows(BadCredentialsException.class, () -> xacThucService.dangNhap(loginRequest));
    }

    @Test
    @DisplayName("US-02: Gia hạn Access Token mới bằng Refresh Token")
    void testGiaHanTokenThanhCong() {
        DangNhapRequest loginRequest = DangNhapRequest.builder()
                .taiKhoan("customer@flexshop.vn")
                .matKhau("12345678")
                .build();
        XacThucResponse loginResponse = xacThucService.dangNhap(loginRequest);

        LamMoiTokenRequest refreshReq = LamMoiTokenRequest.builder()
                .refreshToken(loginResponse.getRefreshToken())
                .build();

        XacThucResponse refreshedResponse = xacThucService.lamMoiToken(refreshReq);

        assertNotNull(refreshedResponse);
        assertNotNull(refreshedResponse.getAccessToken());
        assertTrue(jwtTokenProvider.validateToken(refreshedResponse.getAccessToken()));
    }

    @Test
    @DisplayName("US-02: Kiểm tra chính xác đường dẫn Dashboard cho cả 5 Role")
    void testXacDinhDuongDanDashboardCho5VaiTro() {
        assertEquals("/admin/dashboard", xacThucService.xacDinhDuongDanDashboard(List.of("ADMIN")));
        assertEquals("/seller/dashboard", xacThucService.xacDinhDuongDanDashboard(List.of("NGUOI_BAN", "KHACH_HANG")));
        assertEquals("/customer/dashboard", xacThucService.xacDinhDuongDanDashboard(List.of("KHACH_HANG")));
        assertEquals("/shipper/dashboard", xacThucService.xacDinhDuongDanDashboard(List.of("TAI_XE")));
        assertEquals("/shipper/dashboard", xacThucService.xacDinhDuongDanDashboard(List.of("SHIPPER")));
        assertEquals("/cskh/dashboard", xacThucService.xacDinhDuongDanDashboard(List.of("CSKH")));
    }

    @Test
    @DisplayName("US-02: Chặn đăng nhập tài khoản bị khóa, hiển thị rõ lý do khóa và bỏ dòng chữ (BI_KHOA)")
    void testDangNhapTaiKhoanBiKhoa() {
        com.example.demo.entity.NguoiDung userKhoa = com.example.demo.entity.NguoiDung.builder()
                .email("test_locked_user@flexshop.vn")
                .soDienThoai("0977889900")
                .matKhauMaHoa(passwordEncoder.encode("12345678"))
                .hoVaTen("Người Dùng Bị Khóa")
                .trangThai("BI_KHOA")
                .lyDoKhoa("Vi phạm chính sách đăng tải thông tin")
                .daXoa(false)
                .build();
        nguoiDungRepository.save(userKhoa);

        DangNhapRequest loginRequest = DangNhapRequest.builder()
                .taiKhoan("test_locked_user@flexshop.vn")
                .matKhau("12345678")
                .build();

        NgoaiLeUngDung exception = assertThrows(NgoaiLeUngDung.class, () -> xacThucService.dangNhap(loginRequest));
        assertTrue(exception.getMessage().contains("Tài khoản của bạn đã bị khóa. Lý do: Vi phạm chính sách đăng tải thông tin."));
        assertFalse(exception.getMessage().contains("(BI_KHOA)"));
    }
}
