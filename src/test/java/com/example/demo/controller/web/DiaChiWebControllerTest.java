package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThongKeDiaChiResponse;
import com.example.demo.service.DiaChiService;
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
class DiaChiWebControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungService nguoiDungService;

    @Mock
    private DiaChiService diaChiService;

    @InjectMocks
    private DiaChiWebController diaChiWebController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(diaChiWebController).build();
    }

    @Test
    @DisplayName("Web GET /customer/dia-chi - KhÃƒÂ¡ch hÃƒÂ ng truy cÃ¡ÂºÂ­p sÃ¡Â»â€¢ Ã„â€˜Ã¡Â»â€¹a chÃ¡Â»â€° thÃƒÂ nh cÃƒÂ´ng")
    void testTrangSoDiaChi_KhachHang() throws Exception {
        NguoiDungResponse mockUser = NguoiDungResponse.builder()
                .maNguoiDung(3L)
                .hoVaTen("LÃƒÂª KhÃƒÂ¡ch HÃƒÂ ng")
                .email("customer@flexshop.vn")
                .danhSachVaiTro(List.of("KHACH_HANG"))
                .build();

        ThongKeDiaChiResponse mockThongKe = ThongKeDiaChiResponse.builder()
                .tongSoDiaChi(2L)
                .gioiHanToiDa(20)
                .coDiaChiMacDinh(true)
                .build();

        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(diaChiService.thongKeDiaChi(3L)).thenReturn(mockThongKe);

        mockMvc.perform(get("/customer/dia-chi"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/so-dia-chi"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("thongKe"))
                .andExpect(model().attribute("currentRole", "KHACH_HANG"));
    }

    @Test
    @DisplayName("Web GET /dia-chi - NgÃ†Â°Ã¡Â»Âi bÃƒÂ¡n truy cÃ¡ÂºÂ­p sÃ¡Â»â€¢ Ã„â€˜Ã¡Â»â€¹a chÃ¡Â»â€° thÃƒÂ nh cÃƒÂ´ng")
    void testTrangSoDiaChi_NguoiBan() throws Exception {
        NguoiDungResponse mockUser = NguoiDungResponse.builder()
                .maNguoiDung(2L)
                .hoVaTen("TrÃ¡ÂºÂ§n NgÃ†Â°Ã¡Â»Âi BÃƒÂ¡n")
                .email("seller@flexshop.vn")
                .danhSachVaiTro(List.of("NGUOI_BAN"))
                .build();

        ThongKeDiaChiResponse mockThongKe = ThongKeDiaChiResponse.builder()
                .tongSoDiaChi(1L)
                .gioiHanToiDa(20)
                .coDiaChiMacDinh(true)
                .build();

        when(nguoiDungService.layNguoiDungHienTai()).thenReturn(mockUser);
        when(diaChiService.thongKeDiaChi(2L)).thenReturn(mockThongKe);

        mockMvc.perform(get("/dia-chi"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/so-dia-chi"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("thongKe"))
                .andExpect(model().attribute("currentRole", "NGUOI_BAN"));
    }
}