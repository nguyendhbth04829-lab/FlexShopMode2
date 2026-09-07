package com.example.demo.service;

import com.example.demo.dto.request.CapNhatTrangThaiNguoiDungRequest;
import com.example.demo.dto.request.PhanQuyenNguoiDungRequest;
import com.example.demo.dto.request.TaoNhanVienRequest;
import com.example.demo.dto.response.QuanLyNguoiDungResponse;
import com.example.demo.dto.response.ThongKeNguoiDungResponse;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.VaiTro;
import com.example.demo.exception.NgoaiLeUngDung;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuanLyNguoiDungServiceTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private VaiTroRepository vaiTroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private QuanLyNguoiDungService quanLyNguoiDungService;

    private NguoiDung userKhachHang;
    private NguoiDung userAdmin;
    private VaiTro vaiTroKhachHang;
    private VaiTro vaiTroAdmin;
    private VaiTro vaiTroTaiXe;
    private VaiTro vaiTroCskh;

    @BeforeEach
    void setUp() {
        vaiTroKhachHang = VaiTro.builder().maVaiTro(1).tenVaiTro("KHACH_HANG").moTa("Khách hàng").build();
        vaiTroAdmin = VaiTro.builder().maVaiTro(2).tenVaiTro("ADMIN").moTa("Quản trị").build();
        vaiTroTaiXe = VaiTro.builder().maVaiTro(3).tenVaiTro("TAI_XE").moTa("Tài xế").build();
        vaiTroCskh = VaiTro.builder().maVaiTro(4).tenVaiTro("CSKH").moTa("CSKH").build();

        Set<VaiTro> rolesKhachHang = new HashSet<>(Collections.singletonList(vaiTroKhachHang));
        userKhachHang = NguoiDung.builder()
                .maNguoiDung(10L)
                .hoVaTen("Khách Hàng A")
                .email("khachhang@example.com")
                .soDienThoai("0912345678")
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .danhSachVaiTro(rolesKhachHang)
                .ngayTao(LocalDateTime.now())
                .build();

        Set<VaiTro> rolesAdmin = new HashSet<>(Collections.singletonList(vaiTroAdmin));
        userAdmin = NguoiDung.builder()
                .maNguoiDung(1L)
                .hoVaTen("Quản Trị Viên")
                .email("admin@flexshop.vn")
                .soDienThoai("0900000001")
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .danhSachVaiTro(rolesAdmin)
                .ngayTao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("US-06: Lấy danh sách người dùng phân trang & tìm kiếm thành công")
    void testLayDanhSachNguoiDung_ThanhCong() {
        Pageable pageable = PageRequest.of(0, 10);
        List<NguoiDung> danhSach = Collections.singletonList(userKhachHang);
        Page<NguoiDung> pageNguoiDung = new PageImpl<>(danhSach, pageable, 1);

        when(nguoiDungRepository.timKiemPhanTrang("khach", "KHACH_HANG", "HOAT_DONG", pageable))
                .thenReturn(pageNguoiDung);

        Page<QuanLyNguoiDungResponse> result = quanLyNguoiDungService.layDanhSachNguoiDung("khach", "KHACH_HANG", "HOAT_DONG", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("khachhang@example.com");
    }

    @Test
    @DisplayName("US-06: Lấy thống kê người dùng KPI thành công")
    void testLayThongKeNguoiDung_ThanhCong() {
        when(nguoiDungRepository.countByDaXoaFalse()).thenReturn(100L);
        when(nguoiDungRepository.demSoLuongTheoVaiTro("KHACH_HANG")).thenReturn(80L);
        when(nguoiDungRepository.demSoLuongTheoVaiTro("NGUOI_BAN")).thenReturn(10L);
        when(nguoiDungRepository.demSoLuongTheoVaiTro("TAI_XE")).thenReturn(5L);
        when(nguoiDungRepository.demSoLuongTheoVaiTro("CSKH")).thenReturn(3L);
        when(nguoiDungRepository.demSoLuongTheoVaiTro("ADMIN")).thenReturn(2L);
        when(nguoiDungRepository.countByTrangThaiAndDaXoaFalse("BI_KHOA")).thenReturn(4L);
        when(nguoiDungRepository.countByTrangThaiAndDaXoaFalse("HOAT_DONG")).thenReturn(96L);
        when(nguoiDungRepository.demSoNguoiDungMoiTu(any())).thenReturn(7L);

        ThongKeNguoiDungResponse thongKe = quanLyNguoiDungService.layThongKeNguoiDung();

        assertThat(thongKe).isNotNull();
        assertThat(thongKe.getTongSoNguoiDung()).isEqualTo(100L);
        assertThat(thongKe.getSoKhachHang()).isEqualTo(80L);
        assertThat(thongKe.getSoBiKhoa()).isEqualTo(4L);
        assertThat(thongKe.getSoHoatDong()).isEqualTo(96L);
    }

    @Test
    @DisplayName("US-06: Lấy chi tiết người dùng thành công")
    void testLayChiTietNguoiDung_ThanhCong() {
        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(userKhachHang));

        QuanLyNguoiDungResponse response = quanLyNguoiDungService.layChiTietNguoiDung(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getEmail()).isEqualTo("khachhang@example.com");
    }

    @Test
    @DisplayName("US-06: Lấy chi tiết người dùng - Ném 404 khi không tìm thấy")
    void testLayChiTietNguoiDung_NotFound() {
        when(nguoiDungRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quanLyNguoiDungService.layChiTietNguoiDung(999L))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("US-06: Cấp tài khoản nhân viên Shipper (TAI_XE) thành công")
    void testTaoTaiKhoanNhanVien_Shipper_ThanhCong() {
        TaoNhanVienRequest request = TaoNhanVienRequest.builder()
                .hoVaTen("Nguyễn Văn Shipper")
                .email("shipper@flexshop.vn")
                .soDienThoai("0933445566")
                .matKhau("MatKhau@123")
                .vaiTro("TAI_XE")
                .build();

        when(nguoiDungRepository.existsByEmailIgnoreCase("shipper@flexshop.vn")).thenReturn(false);
        when(nguoiDungRepository.existsBySoDienThoai("0933445566")).thenReturn(false);
        when(vaiTroRepository.findByTenVaiTro("TAI_XE")).thenReturn(Optional.of(vaiTroTaiXe));
        when(passwordEncoder.encode("MatKhau@123")).thenReturn("encodedPassword");

        NguoiDung savedUser = NguoiDung.builder()
                .maNguoiDung(20L)
                .hoVaTen(request.getHoVaTen())
                .email(request.getEmail())
                .soDienThoai(request.getSoDienThoai())
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .danhSachVaiTro(new HashSet<>(Collections.singletonList(vaiTroTaiXe)))
                .build();

        when(nguoiDungRepository.save(any(NguoiDung.class))).thenReturn(savedUser);

        QuanLyNguoiDungResponse response = quanLyNguoiDungService.taoTaiKhoanNhanVien(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getEmail()).isEqualTo("shipper@flexshop.vn");
        assertThat(response.getDanhSachVaiTro()).contains("TAI_XE");
    }

    @Test
    @DisplayName("US-06: Cấp tài khoản nhân viên CSKH thành công")
    void testTaoTaiKhoanNhanVien_CSKH_ThanhCong() {
        TaoNhanVienRequest request = TaoNhanVienRequest.builder()
                .hoVaTen("Trần Thị CSKH")
                .email("cskh@flexshop.vn")
                .soDienThoai("0944556677")
                .matKhau("MatKhau@123")
                .vaiTro("CSKH")
                .build();

        when(nguoiDungRepository.existsByEmailIgnoreCase("cskh@flexshop.vn")).thenReturn(false);
        when(nguoiDungRepository.existsBySoDienThoai("0944556677")).thenReturn(false);
        when(vaiTroRepository.findByTenVaiTro("CSKH")).thenReturn(Optional.of(vaiTroCskh));
        when(passwordEncoder.encode("MatKhau@123")).thenReturn("encodedPassword");

        NguoiDung savedUser = NguoiDung.builder()
                .maNguoiDung(21L)
                .hoVaTen(request.getHoVaTen())
                .email(request.getEmail())
                .soDienThoai(request.getSoDienThoai())
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .danhSachVaiTro(new HashSet<>(Collections.singletonList(vaiTroCskh)))
                .build();

        when(nguoiDungRepository.save(any(NguoiDung.class))).thenReturn(savedUser);

        QuanLyNguoiDungResponse response = quanLyNguoiDungService.taoTaiKhoanNhanVien(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(21L);
        assertThat(response.getDanhSachVaiTro()).contains("CSKH");
    }

    @Test
    @DisplayName("US-06: Cấp tài khoản nhân viên - Trùng Email ném 409 Conflict")
    void testTaoTaiKhoanNhanVien_TrungEmail() {
        TaoNhanVienRequest request = TaoNhanVienRequest.builder()
                .hoVaTen("Trùng Email")
                .email("khachhang@example.com")
                .soDienThoai("0933445566")
                .matKhau("MatKhau@123")
                .vaiTro("TAI_XE")
                .build();

        when(nguoiDungRepository.existsByEmailIgnoreCase("khachhang@example.com")).thenReturn(true);

        assertThatThrownBy(() -> quanLyNguoiDungService.taoTaiKhoanNhanVien(request, 1L))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.CONFLICT)
                .hasMessageContaining("đã tồn tại trong hệ thống");
    }

    @Test
    @DisplayName("US-06: Cấp tài khoản nhân viên - Trùng Số điện thoại ném 409 Conflict")
    void testTaoTaiKhoanNhanVien_TrungSoDienThoai() {
        TaoNhanVienRequest request = TaoNhanVienRequest.builder()
                .hoVaTen("Trùng Phone")
                .email("moi@flexshop.vn")
                .soDienThoai("0912345678")
                .matKhau("MatKhau@123")
                .vaiTro("TAI_XE")
                .build();

        when(nguoiDungRepository.existsByEmailIgnoreCase("moi@flexshop.vn")).thenReturn(false);
        when(nguoiDungRepository.existsBySoDienThoai("0912345678")).thenReturn(true);

        assertThatThrownBy(() -> quanLyNguoiDungService.taoTaiKhoanNhanVien(request, 1L))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.CONFLICT)
                .hasMessageContaining("Số điện thoại");
    }

    @Test
    @DisplayName("US-06: Khóa tài khoản người dùng thành công")
    void testDoiTrangThaiKhoa_KhoaUser_ThanhCong() {
        CapNhatTrangThaiNguoiDungRequest request = CapNhatTrangThaiNguoiDungRequest.builder()
                .trangThai("BI_KHOA")
                .lyDo("Vi phạm chính sách sàn")
                .build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(userKhachHang));
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuanLyNguoiDungResponse response = quanLyNguoiDungService.doiTrangThaiKhoa(10L, request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getTrangThai()).isEqualTo("BI_KHOA");
        assertThat(response.isBiKhoa()).isTrue();
    }

    @Test
    @DisplayName("US-06: Mở khóa tài khoản người dùng thành công")
    void testDoiTrangThaiKhoa_MoKhoaUser_ThanhCong() {
        userKhachHang.setTrangThai("BI_KHOA");

        CapNhatTrangThaiNguoiDungRequest request = CapNhatTrangThaiNguoiDungRequest.builder()
                .trangThai("HOAT_DONG")
                .lyDo("Hết thời gian đình chỉ")
                .build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(userKhachHang));
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuanLyNguoiDungResponse response = quanLyNguoiDungService.doiTrangThaiKhoa(10L, request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getTrangThai()).isEqualTo("HOAT_DONG");
        assertThat(response.isHoatDong()).isTrue();
    }

    @Test
    @DisplayName("US-06: Chặn Admin tự khóa tài khoản của chính mình")
    void testDoiTrangThaiKhoa_ChanTuKhoaChinhMinh() {
        CapNhatTrangThaiNguoiDungRequest request = CapNhatTrangThaiNguoiDungRequest.builder()
                .trangThai("BI_KHOA")
                .lyDo("Thử khóa")
                .build();

        assertThatThrownBy(() -> quanLyNguoiDungService.doiTrangThaiKhoa(1L, request, 1L))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("không thể tự khóa tài khoản");
    }

    @Test
    @DisplayName("US-06: Chặn khóa tài khoản Quản trị viên tối cao admin@flexshop.vn")
    void testDoiTrangThaiKhoa_ChanKhoaSuperAdmin() {
        CapNhatTrangThaiNguoiDungRequest request = CapNhatTrangThaiNguoiDungRequest.builder()
                .trangThai("BI_KHOA")
                .lyDo("Thử khóa super admin")
                .build();

        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(userAdmin));

        assertThatThrownBy(() -> quanLyNguoiDungService.doiTrangThaiKhoa(1L, request, 2L))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Quản trị viên tối cao");
    }

    @Test
    @DisplayName("US-06: Cập nhật phân quyền sang 1 vai trò duy nhất cho người dùng thành công")
    void testPhanQuyenNguoiDung_ThanhCong() {
        PhanQuyenNguoiDungRequest request = PhanQuyenNguoiDungRequest.builder()
                .vaiTro("NGUOI_BAN")
                .build();

        VaiTro roleNguoiBan = VaiTro.builder().maVaiTro(5).tenVaiTro("NGUOI_BAN").build();

        when(nguoiDungRepository.findById(10L)).thenReturn(Optional.of(userKhachHang));
        when(vaiTroRepository.findByTenVaiTro("NGUOI_BAN")).thenReturn(Optional.of(roleNguoiBan));
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuanLyNguoiDungResponse response = quanLyNguoiDungService.phanQuyenNguoiDung(10L, request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getVaiTro()).isEqualTo("NGUOI_BAN");
        assertThat(response.getDanhSachVaiTro()).containsExactly("NGUOI_BAN");
    }

    @Test
    @DisplayName("US-06: Phân quyền - Chặn Admin tự tước bỏ quyền ADMIN của chính mình")
    void testPhanQuyenNguoiDung_ChanTuTuocQuyenAdmin() {
        PhanQuyenNguoiDungRequest request = PhanQuyenNguoiDungRequest.builder()
                .danhSachVaiTro(Collections.singletonList("KHACH_HANG"))
                .build();

        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(userAdmin));

        assertThatThrownBy(() -> quanLyNguoiDungService.phanQuyenNguoiDung(1L, request, 1L))
                .isInstanceOf(NgoaiLeUngDung.class)
                .hasFieldOrPropertyWithValue("maTrangThai", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("không thể tự tước bỏ quyền Quản trị viên");
    }
}
