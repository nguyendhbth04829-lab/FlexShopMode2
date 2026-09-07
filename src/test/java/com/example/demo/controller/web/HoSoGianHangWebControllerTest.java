package com.example.demo.controller.web;

import com.example.demo.dto.response.HoSoGianHangDayDuResponse;
import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.service.HoSoGianHangService;
import com.example.demo.service.NguoiDungService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HoSoGianHangWebControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungService nguoiDungService;

    @Mock
    private HoSoGianHangService hoSoGianHangService;

    @InjectMocks
    private HoSoGianHangWebController webController;

    private NguoiDungResponse mockUser;

    @BeforeEach
    void setUp() {
        mockUser = NguoiDungResponse.builder()
                .maNguoiDung(2L)
                .email("seller@flexshop.vn")
                .hoVaTen("Trần Người Bán")
                .soDienThoai("0912345678")
                .danhSachVaiTro(List.of("NGUOI_BAN"))
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
    }

    @Test
    @DisplayName("Seller có gian hàng truy cập /seller/ho-so-shop -> Render view dashboard/ho-so-shop")
    void testHienThiHoSoShop_ThanhCong() throws Exception {
        HoSoGianHangDayDuResponse mockHoSo = HoSoGianHangDayDuResponse.builder()
                .maGianHang(4L)
                .tenGianHang("Flex Fashion 1")
                .duongDanSlug("flex-fashion-1")
                .trangThai("HOAT_DONG")
                .build();

        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(hoSoGianHangService.layHoSoGianHangCuaToi(2L)).thenReturn(mockHoSo);

        mockMvc.perform(get("/seller/ho-so-shop"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/ho-so-shop"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("hoSo"))
                .andExpect(model().attribute("pageTitle", "Hồ Sơ Gian Hàng & Chứng Chỉ Pháp Lý - FlexShop"));
    }

    @Test
    @DisplayName("User chưa có gian hàng truy cập /seller/ho-so-shop -> Chuyển hướng sang /seller/dang-ky")
    void testHienThiHoSoShop_ChuaCoShop_ChuyenHuong() throws Exception {
        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(hoSoGianHangService.layHoSoGianHangCuaToi(2L))
                .thenThrow(new NgoaiLeUngDung("Bạn chưa có gian hàng nào trên hệ thống", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/seller/ho-so-shop"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dang-ky"));
    }
}
