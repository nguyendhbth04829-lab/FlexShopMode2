package com.example.demo.controller.api;

import com.example.demo.dto.request.CapNhatTrangThaiNguoiDungRequest;
import com.example.demo.dto.request.PhanQuyenNguoiDungRequest;
import com.example.demo.dto.request.TaoNhanVienRequest;
import com.example.demo.dto.response.QuanLyNguoiDungResponse;
import com.example.demo.dto.response.ThongKeNguoiDungResponse;
import com.example.demo.exception.XuLyNgoaiLeToanCuc;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.QuanLyNguoiDungService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class QuanLyNguoiDungApiControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private QuanLyNguoiDungService quanLyNguoiDungService;

    @InjectMocks
    private QuanLyNguoiDungApiController controller;

    private QuanLyNguoiDungResponse sampleUser;
    private NguoiDungPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = NguoiDungPrincipal.builder()
                .maNguoiDung(1L)
                .email("admin@flexshop.vn")
                .hoVaTen("Quản Trị Viên")
                .soDienThoai("0900000001")
                .hoatDong(true)
                .build();

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(NguoiDungPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return mockPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new XuLyNgoaiLeToanCuc())
                .build();

        sampleUser = QuanLyNguoiDungResponse.builder()
                .id(10L)
                .hoVaTen("Nguyễn Văn A")
                .email("nguyenvana@flexshop.vn")
                .soDienThoai("0912345678")
                .trangThai("HOAT_DONG")
                .danhSachVaiTro(Collections.singletonList("KHACH_HANG"))
                .chucVuChinh("KHACH_HANG")
                .ngayTao(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("US-06: GET /api/v1/admin/nguoi-dung - Lấy danh sách thành công")
    void testLayDanhSach_ThanhCong() throws Exception {
        Page<QuanLyNguoiDungResponse> page = new PageImpl<>(Collections.singletonList(sampleUser), PageRequest.of(0, 10), 1);
        when(quanLyNguoiDungService.layDanhSachNguoiDung(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/nguoi-dung")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.content[0].email").value("nguyenvana@flexshop.vn"));
    }

    @Test
    @DisplayName("US-06: GET /api/v1/admin/nguoi-dung/thong-ke - Lấy thống kê KPI thành công")
    void testLayThongKe_ThanhCong() throws Exception {
        ThongKeNguoiDungResponse thongKe = ThongKeNguoiDungResponse.builder()
                .tongSoNguoiDung(100L)
                .soKhachHang(80L)
                .soNguoiBan(10L)
                .soTaiXe(5L)
                .soCskh(3L)
                .soAdmin(2L)
                .soBiKhoa(4L)
                .soHoatDong(96L)
                .build();

        when(quanLyNguoiDungService.layThongKeNguoiDung()).thenReturn(thongKe);

        mockMvc.perform(get("/api/v1/admin/nguoi-dung/thong-ke")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tongSoNguoiDung").value(100))
                .andExpect(jsonPath("$.duLieu.soKhachHang").value(80));
    }

    @Test
    @DisplayName("US-06: GET /api/v1/admin/nguoi-dung/{id} - Lấy chi tiết người dùng thành công")
    void testLayChiTiet_ThanhCong() throws Exception {
        when(quanLyNguoiDungService.layChiTietNguoiDung(10L)).thenReturn(sampleUser);

        mockMvc.perform(get("/api/v1/admin/nguoi-dung/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.id").value(10))
                .andExpect(jsonPath("$.duLieu.email").value("nguyenvana@flexshop.vn"));
    }

    @Test
    @DisplayName("US-06: POST /api/v1/admin/nguoi-dung/tao-nhan-vien - Thành công trả về 201 Created")
    void testTaoNhanVien_ThanhCong() throws Exception {
        TaoNhanVienRequest request = TaoNhanVienRequest.builder()
                .hoVaTen("Nguyễn Văn Shipper")
                .email("shipper01@flexshop.vn")
                .soDienThoai("0912345678")
                .matKhau("MatKhau@123")
                .vaiTro("TAI_XE")
                .build();

        QuanLyNguoiDungResponse response = QuanLyNguoiDungResponse.builder()
                .id(20L)
                .hoVaTen(request.getHoVaTen())
                .email(request.getEmail())
                .soDienThoai(request.getSoDienThoai())
                .trangThai("HOAT_DONG")
                .danhSachVaiTro(Collections.singletonList("TAI_XE"))
                .build();

        when(quanLyNguoiDungService.taoTaiKhoanNhanVien(any(TaoNhanVienRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/nguoi-dung/tao-nhan-vien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.email").value("shipper01@flexshop.vn"));
    }

    @Test
    @DisplayName("US-06: POST /api/v1/admin/nguoi-dung/tao-nhan-vien - Validate sai SĐT trả về 400")
    void testTaoNhanVien_ValidateSaiSdt() throws Exception {
        TaoNhanVienRequest request = TaoNhanVienRequest.builder()
                .hoVaTen("Nguyễn Văn Sai SĐT")
                .email("saisdt@flexshop.vn")
                .soDienThoai("12345") // Sai định dạng
                .matKhau("MatKhau@123")
                .vaiTro("TAI_XE")
                .build();

        mockMvc.perform(post("/api/v1/admin/nguoi-dung/tao-nhan-vien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thanhCong").value(false));
    }

    @Test
    @DisplayName("US-06: PATCH /api/v1/admin/nguoi-dung/{id}/trang-thai - Khóa tài khoản thành công")
    void testDoiTrangThai_ThanhCong() throws Exception {
        CapNhatTrangThaiNguoiDungRequest request = CapNhatTrangThaiNguoiDungRequest.builder()
                .trangThai("BI_KHOA")
                .lyDo("Vi phạm chính sách")
                .build();

        QuanLyNguoiDungResponse response = QuanLyNguoiDungResponse.builder()
                .id(10L)
                .trangThai("BI_KHOA")
                .build();

        when(quanLyNguoiDungService.doiTrangThaiKhoa(eq(10L), any(CapNhatTrangThaiNguoiDungRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/nguoi-dung/10/trang-thai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.trangThai").value("BI_KHOA"));
    }

    @Test
    @DisplayName("US-06: PUT /api/v1/admin/nguoi-dung/{id}/phan-quyen - Phân quyền thành công")
    void testPhanQuyen_ThanhCong() throws Exception {
        PhanQuyenNguoiDungRequest request = PhanQuyenNguoiDungRequest.builder()
                .vaiTro("NGUOI_BAN")
                .build();

        QuanLyNguoiDungResponse response = QuanLyNguoiDungResponse.builder()
                .id(10L)
                .vaiTro("NGUOI_BAN")
                .danhSachVaiTro(Collections.singletonList("NGUOI_BAN"))
                .build();

        when(quanLyNguoiDungService.phanQuyenNguoiDung(eq(10L), any(PhanQuyenNguoiDungRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/nguoi-dung/10/phan-quyen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.vaiTro").value("NGUOI_BAN"));
    }
}
