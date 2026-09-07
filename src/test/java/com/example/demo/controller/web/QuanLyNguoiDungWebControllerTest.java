package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeNguoiDungResponse;
import com.example.demo.service.NguoiDungService;
import com.example.demo.service.QuanLyNguoiDungService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class QuanLyNguoiDungWebControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungService nguoiDungService;

    @Mock
    private QuanLyNguoiDungService quanLyNguoiDungService;

    @InjectMocks
    private QuanLyNguoiDungWebController webController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
    }

    @Test
    @DisplayName("US-06: Web GET /admin/nguoi-dung - Admin truy cập trang quản lý người dùng thành công")
    void testHienThiTrangQuanLy_ThanhCong() throws Exception {
        NguoiDungResponse mockAdmin = NguoiDungResponse.builder()
                .maNguoiDung(1L)
                .hoVaTen("Quản Trị Viên")
                .email("admin@flexshop.vn")
                .danhSachVaiTro(Collections.singletonList("ADMIN"))
                .build();

        ThongKeNguoiDungResponse mockThongKe = ThongKeNguoiDungResponse.builder()
                .tongSoNguoiDung(50L)
                .soKhachHang(40L)
                .soNguoiBan(5L)
                .soTaiXe(3L)
                .soCskh(1L)
                .soAdmin(1L)
                .soBiKhoa(2L)
                .soHoatDong(48L)
                .build();

        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockAdmin);
        when(quanLyNguoiDungService.layThongKeNguoiDung()).thenReturn(mockThongKe);

        mockMvc.perform(get("/admin/nguoi-dung"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/quan-ly-nguoi-dung"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("thongKe"))
                .andExpect(model().attribute("currentRole", "ADMIN"));
    }
}
