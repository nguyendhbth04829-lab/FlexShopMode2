package com.example.demo.controller.web;

import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.service.DangKyGianHangService;
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
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DangKyGianHangWebControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungService nguoiDungService;

    @Mock
    private DangKyGianHangService dangKyGianHangService;

    @InjectMocks
    private DangKyGianHangWebController webController;

    private NguoiDungResponse mockUser;

    @BeforeEach
    void setUp() {
        mockUser = NguoiDungResponse.builder()
                .maNguoiDung(12L)
                .email("customer_test@flexshop.vn")
                .hoVaTen("Nguyễn Thị Test")
                .soDienThoai("0981234567")
                .danhSachVaiTro(List.of("KHACH_HANG"))
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
    }

    @Test
    @DisplayName("US-08: Người dùng chưa có shop truy cập /seller/dang-ky -> Hiển thị form đăng ký dang-ky-shop")
    void testHienThiTrangDangKy_ChuaCoShop() throws Exception {
        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(dangKyGianHangService.layThongTinGianHangCuaToi(12L)).thenReturn(null);

        mockMvc.perform(get("/seller/dang-ky"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/dang-ky-shop"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeDoesNotExist("gianHang"));
    }

    @Test
    @DisplayName("US-08: Người dùng đã có shop HOAT_DONG -> Chuyển hướng redirect sang /seller/dashboard")
    void testHienThiTrangDangKy_DaCoShopHoatDong_Redirect() throws Exception {
        GianHangResponse activeShop = GianHangResponse.builder()
                .maGianHang(99L)
                .tenGianHang("Shop Đã Hoạt Động")
                .trangThai("HOAT_DONG")
                .build();

        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(dangKyGianHangService.layThongTinGianHangCuaToi(12L)).thenReturn(activeShop);

        mockMvc.perform(get("/seller/dang-ky"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"));
    }

    @Test
    @DisplayName("US-08: Người dùng có đơn CHO_DUYET -> Hiển thị trang dang-ky-shop kèm thông tin gian hàng")
    void testHienThiTrangDangKy_DangChoDuyet() throws Exception {
        GianHangResponse pendingShop = GianHangResponse.builder()
                .maGianHang(101L)
                .tenGianHang("Shop Đang Chờ Duyệt")
                .trangThai("CHO_DUYET")
                .build();

        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(dangKyGianHangService.layThongTinGianHangCuaToi(12L)).thenReturn(pendingShop);

        mockMvc.perform(get("/seller/dang-ky"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/dang-ky-shop"))
                .andExpect(model().attributeExists("gianHang"))
                .andExpect(model().attribute("gianHang", pendingShop));
    }
}
