package com.example.demo.controller;

import com.example.demo.dto.request.DangKyRequest;
import com.example.demo.dto.request.DangNhapRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class XacThucApiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("US-01: API /api/v1/auth/register - Chặn đăng ký khi mật khẩu không đạt chuẩn (thiếu số, ký tự đặc biệt)")
    void testDangKyMatKhauYeuThatBai() throws Exception {
        DangKyRequest request = DangKyRequest.builder()
                .email("test.weak@flexshop.vn")
                .soDienThoai("0912345678")
                .matKhau("simplepass") // Không có hoa, số, ký tự đặc biệt
                .hoVaTen("Test Weak")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thanhCong", is(false)))
                .andExpect(jsonPath("$.thongBao", containsString("không hợp lệ")));
    }

    @Test
    @DisplayName("US-01: API /api/v1/auth/register - Đăng ký thành công với dữ liệu hợp lệ")
    void testDangKyApiThanhCong() throws Exception {
        DangKyRequest request = DangKyRequest.builder()
                .email("api.reg.vn@flexshop.vn")
                .soDienThoai("0944001122")
                .matKhau("StrongPass@2026")
                .hoVaTen("Nguyễn Văn API")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.thanhCong", is(true)))
                .andExpect(jsonPath("$.duLieu.email", is("api.reg.vn@flexshop.vn")))
                .andExpect(jsonPath("$.duLieu.danhSachVaiTro", hasItem("KHACH_HANG")))
                .andExpect(jsonPath("$.duLieu.duongDanDashboard", is("/customer/dashboard")));
    }

    @Test
    @DisplayName("US-02: API /api/v1/auth/login - Đăng nhập nhận Access Token 15m, Refresh Token 7d và targetDashboardUrl")
    void testDangNhapApiThanhCong() throws Exception {
        DangNhapRequest request = DangNhapRequest.builder()
                .taiKhoan("admin@flexshop.vn")
                .matKhau("12345678")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong", is(true)))
                .andExpect(jsonPath("$.duLieu.accessToken", notNullValue()))
                .andExpect(jsonPath("$.duLieu.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.duLieu.thoiGianHetHan", is(900)))
                .andExpect(jsonPath("$.duLieu.thongTinNguoiDung.duongDanDashboard", is("/admin/dashboard")));
    }
}
