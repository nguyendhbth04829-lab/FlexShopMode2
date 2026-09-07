package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThietLapHoSoShopResponse;
import com.example.demo.dto.response.ThongKeThietLapShopResponse;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.service.NguoiDungService;
import com.example.demo.service.ThietLapHoSoShopService;
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
class ThietLapHoSoShopWebControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungService nguoiDungService;

    @Mock
    private ThietLapHoSoShopService thietLapHoSoShopService;

    @InjectMocks
    private ThietLapHoSoShopWebController webController;

    private NguoiDungResponse mockUser;

    @BeforeEach
    void setUp() {
        mockUser = NguoiDungResponse.builder()
                .maNguoiDung(2L)
                .email("seller@flexshop.vn")
                .hoVaTen("Nguyễn Văn Bán")
                .soDienThoai("0987654321")
                .danhSachVaiTro(List.of("NGUOI_BAN"))
                .build();

        mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
    }

    @Test
    @DisplayName("Seller có gian hàng truy cập /seller/thiet-lap-shop -> Render view dashboard/thiet-lap-shop")
    void testHienThiThietLapShop_ThanhCong() throws Exception {
        ThietLapHoSoShopResponse mockShop = ThietLapHoSoShopResponse.builder()
                .maGianHang(10L)
                .tenGianHang("Flex Fashion Store")
                .duongDanSlug("flex-fashion-store")
                .trangThai("HOAT_DONG")
                .dangMoCua(true)
                .gioMoCua("08:00")
                .gioDongCua("22:00")
                .phanTramHoanThien(100)
                .build();

        ThongKeThietLapShopResponse mockThongKe = ThongKeThietLapShopResponse.builder()
                .tongSoLanCapNhat(5)
                .phanTramHoanThien(100)
                .trangThaiGianHang("HOAT_DONG")
                .dangMoCua(true)
                .build();

        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(thietLapHoSoShopService.layThietLapShop(2L)).thenReturn(mockShop);
        when(thietLapHoSoShopService.layThongKeThietLap(2L)).thenReturn(mockThongKe);

        mockMvc.perform(get("/seller/thiet-lap-shop"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/thiet-lap-shop"))
                .andExpect(model().attributeExists("shop", "thongKe", "user", "pageTitle", "currentRole"))
                .andExpect(model().attribute("pageTitle", "Thiết Lập Hồ Sơ Gian Hàng - FlexShop Seller"));
    }

    @Test
    @DisplayName("Người dùng chưa có shop truy cập /seller/thiet-lap-shop -> Redirect về /seller/dang-ky")
    void testHienThiThietLapShop_ChuaCoShop() throws Exception {
        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(thietLapHoSoShopService.layThietLapShop(2L))
                .thenThrow(new NgoaiLeUngDung("Chưa có gian hàng", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/seller/thiet-lap-shop"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dang-ky"));
    }
}
