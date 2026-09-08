package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.service.KiemDuyetGianHangService;
import com.example.demo.service.NguoiDungService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KiemDuyetGianHangWebControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungService nguoiDungService;

    @Mock
    private KiemDuyetGianHangService kiemDuyetGianHangService;

    @InjectMocks
    private KiemDuyetGianHangWebController webController;

    private NguoiDungResponse mockAdmin;
    private ThongKeGianHangResponse mockThongKe;

    @BeforeEach
    void setUp() {
        mockAdmin = NguoiDungResponse.builder()
                .maNguoiDung(1L)
                .email("admin@flexshop.vn")
                .hoVaTen("Quản Trị Viên")
                .danhSachVaiTro(List.of("ADMIN"))
                .build();

        mockThongKe = ThongKeGianHangResponse.builder()
                .tongSo(10L)
                .choDuyet(3L)
                .hoatDong(5L)
                .tuChoi(2L)
                .tamKhoa(0L)
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
    }

    @Test
    @DisplayName("WEB US09-01: Admin truy cập /admin/kiem-duyet-shop -> Trả về giao diện dashboard/kiem-duyet-shop kèm thông tin user và thống kê")
    void trangKiemDuyetShop_DaDangNhap() throws Exception {
        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockAdmin);
        when(kiemDuyetGianHangService.layThongKeKiemDuyet()).thenReturn(mockThongKe);

        mockMvc.perform(get("/admin/kiem-duyet-shop"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/kiem-duyet-shop"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", mockAdmin))
                .andExpect(model().attributeExists("thongKe"))
                .andExpect(model().attribute("thongKe", mockThongKe))
                .andExpect(model().attribute("currentRole", "ADMIN"));
    }
}
