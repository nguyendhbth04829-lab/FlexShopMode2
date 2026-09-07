package com.example.demo.controller.api;

import com.example.demo.dto.request.CapNhatHoSoRequest;
import com.example.demo.dto.request.DoiMatKhauRequest;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeHoSoResponse;
import com.example.demo.exception.XuLyNgoaiLeToanCuc;
import com.example.demo.service.HoSoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HoSoApiControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HoSoService hoSoService;

    @InjectMocks
    private HoSoApiController hoSoApiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hoSoApiController)
                .setControllerAdvice(new XuLyNgoaiLeToanCuc())
                .build();
    }

    @Test
    @DisplayName("US-04: API GET /api/v1/ho-so - Lấy thông tin hồ sơ cá nhân thành công")
    void testLayHoSo_ThanhCong() throws Exception {
        NguoiDungResponse response = NguoiDungResponse.builder()
                .maNguoiDung(1L)
                .hoVaTen("Nguyễn Quản Trị")
                .email("admin@flexshop.vn")
                .soDienThoai("0900000001")
                .build();

        when(hoSoService.layHoSoHienTai()).thenReturn(response);

        mockMvc.perform(get("/api/v1/ho-so")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.hoVaTen").value("Nguyễn Quản Trị"))
                .andExpect(jsonPath("$.duLieu.email").value("admin@flexshop.vn"));
    }

    @Test
    @DisplayName("US-04: API PUT /api/v1/ho-so - Cập nhật thông tin hồ sơ thành công")
    void testCapNhatHoSo_ThanhCong() throws Exception {
        CapNhatHoSoRequest request = CapNhatHoSoRequest.builder()
                .hoVaTen("Nguyễn Cập Nhật")
                .soDienThoai("0988776655")
                .build();

        NguoiDungResponse response = NguoiDungResponse.builder()
                .maNguoiDung(1L)
                .hoVaTen("Nguyễn Cập Nhật")
                .soDienThoai("0988776655")
                .build();

        when(hoSoService.capNhatHoSo(any(CapNhatHoSoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/ho-so")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.hoVaTen").value("Nguyễn Cập Nhật"))
                .andExpect(jsonPath("$.duLieu.soDienThoai").value("0988776655"));
    }

    @Test
    @DisplayName("US-04: API PUT /api/v1/ho-so - Validation thất bại khi SĐT không đúng định dạng VN")
    void testCapNhatHoSo_LoiValidationSoDienThoai() throws Exception {
        CapNhatHoSoRequest request = CapNhatHoSoRequest.builder()
                .hoVaTen("Nguyễn Văn A")
                .soDienThoai("12345") // Không đúng định dạng di động Việt Nam
                .build();

        mockMvc.perform(put("/api/v1/ho-so")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("US-04: API POST /api/v1/ho-so/avatar - Tải lên ảnh đại diện thành công")
    void testTaiLenAvatar_ThanhCong() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});

        NguoiDungResponse response = NguoiDungResponse.builder()
                .maNguoiDung(1L)
                .anhDaiDien("/uploads/avatars/avatar_user_1_test.png")
                .build();

        when(hoSoService.taiLenAnhDaiDien(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/ho-so/avatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.anhDaiDien").value("/uploads/avatars/avatar_user_1_test.png"));
    }

    @Test
    @DisplayName("US-04: API PUT /api/v1/ho-so/doi-mat-khau - Đổi mật khẩu thành công")
    void testDoiMatKhau_ThanhCong() throws Exception {
        DoiMatKhauRequest request = DoiMatKhauRequest.builder()
                .matKhauHienTai("OldPass@123")
                .matKhauMoi("NewPass@456")
                .xacNhanMatKhauMoi("NewPass@456")
                .build();

        NguoiDungResponse response = NguoiDungResponse.builder()
                .maNguoiDung(1L)
                .build();

        when(hoSoService.doiMatKhau(any(DoiMatKhauRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/ho-so/doi-mat-khau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true));
    }

    @Test
    @DisplayName("US-04: API GET /api/v1/ho-so/thong-ke - Lấy thống kê hồ sơ thành công")
    void testLayThongKe_ThanhCong() throws Exception {
        ThongKeHoSoResponse response = ThongKeHoSoResponse.builder()
                .tongSoDiaChi(2L)
                .gioiHanDiaChi(20)
                .soNgayThamGia(10L)
                .trangThaiBaoMat("Bảo mật cao (BCrypt salt 12)")
                .build();

        when(hoSoService.layThongKeHoSo()).thenReturn(response);

        mockMvc.perform(get("/api/v1/ho-so/thong-ke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tongSoDiaChi").value(2))
                .andExpect(jsonPath("$.duLieu.gioiHanDiaChi").value(20));
    }
}
