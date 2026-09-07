package com.example.demo.controller.api;

import com.example.demo.dto.request.ThietLapHoSoShopRequest;
import com.example.demo.dto.response.LichSuThietLapShopResponse;
import com.example.demo.dto.response.ThietLapHoSoShopResponse;
import com.example.demo.dto.response.ThongKeThietLapShopResponse;
import com.example.demo.exception.XuLyNgoaiLeToanCuc;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.ThietLapHoSoShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ThietLapHoSoShopApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ThietLapHoSoShopService thietLapHoSoShopService;

    @InjectMocks
    private ThietLapHoSoShopApiController controller;

    private NguoiDungPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = NguoiDungPrincipal.builder()
                .maNguoiDung(2L)
                .email("seller@flexshop.vn")
                .hoVaTen("Nguyễn Văn Bán")
                .soDienThoai("0987654321")
                .danhSachQuyen(List.of(new SimpleGrantedAuthority("ROLE_NGUOI_BAN")))
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
    }

    @Test
    @DisplayName("GET /api/v1/seller/thiet-lap-shop trả về 200 OK")
    void layThietLapShop_200() throws Exception {
        ThietLapHoSoShopResponse mockRes = ThietLapHoSoShopResponse.builder()
                .maGianHang(10L)
                .tenGianHang("Flex Fashion Store")
                .gioMoCua("08:00")
                .gioDongCua("22:00")
                .dangMoCua(true)
                .diaChiKho("123 Phố Huế, Hà Nội")
                .sdtKho("0987654321")
                .phanTramHoanThien(100)
                .build();

        when(thietLapHoSoShopService.layThietLapShop(2L)).thenReturn(mockRes);

        mockMvc.perform(get("/api/v1/seller/thiet-lap-shop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tenGianHang").value("Flex Fashion Store"))
                .andExpect(jsonPath("$.duLieu.gioMoCua").value("08:00"))
                .andExpect(jsonPath("$.duLieu.phanTramHoanThien").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/seller/thiet-lap-shop với Multipart tải lên thành công")
    void capNhatThietLapShop_200() throws Exception {
        ThietLapHoSoShopResponse mockRes = ThietLapHoSoShopResponse.builder()
                .maGianHang(10L)
                .tenGianHang("Flex Fashion Updated")
                .gioMoCua("09:00")
                .gioDongCua("21:00")
                .dangMoCua(true)
                .diaChiKho("456 Cầu Giấy, Hà Nội")
                .sdtKho("0912345678")
                .build();

        when(thietLapHoSoShopService.capNhatThietLapShop(eq(2L), any(ThietLapHoSoShopRequest.class), any(), any()))
                .thenReturn(mockRes);

        MockMultipartFile fileLogo = new MockMultipartFile("fileLogo", "logo.png", "image/png", "test logo".getBytes());

        mockMvc.perform(multipart("/api/v1/seller/thiet-lap-shop")
                        .file(fileLogo)
                        .param("tenGianHang", "Flex Fashion Updated")
                        .param("diaChiKho", "456 Cầu Giấy, Hà Nội")
                        .param("sdtKho", "0912345678")
                        .param("gioMoCua", "09:00")
                        .param("gioDongCua", "21:00")
                        .param("dangMoCua", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tenGianHang").value("Flex Fashion Updated"));
    }

    @Test
    @DisplayName("PATCH /api/v1/seller/thiet-lap-shop/trang-thai-mo-cua chuyển trạng thái nhận đơn thành công")
    void chuyenTrangThaiNhanDon_200() throws Exception {
        ThietLapHoSoShopResponse mockRes = ThietLapHoSoShopResponse.builder()
                .maGianHang(10L)
                .dangMoCua(false)
                .build();

        when(thietLapHoSoShopService.chuyenTrangThaiNhanDon(eq(2L), eq(false))).thenReturn(mockRes);

        mockMvc.perform(patch("/api/v1/seller/thiet-lap-shop/trang-thai-mo-cua")
                        .param("dangMoCua", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.dangMoCua").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/seller/thiet-lap-shop/lich-su trả về phân trang 200 OK")
    void layLichSuThayDoi_200() throws Exception {
        LichSuThietLapShopResponse logItem = LichSuThietLapShopResponse.builder()
                .maLichSu(1L)
                .loaiThayDoi("THIET_LAP_TONG_THE")
                .tenLoaiThayDoi("Thiết lập tổng thể")
                .noiDungThayDoi("Cập nhật giờ hoạt động")
                .nguoiThucHien("Nguyễn Văn Bán")
                .thoiGian(LocalDateTime.now())
                .build();

        Page<LichSuThietLapShopResponse> pageRes = new PageImpl<>(List.of(logItem), PageRequest.of(0, 10), 1);
        when(thietLapHoSoShopService.layLichSuThayDoi(eq(2L), any(), any(), any(Pageable.class)))
                .thenReturn(pageRes);

        mockMvc.perform(get("/api/v1/seller/thiet-lap-shop/lich-su")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.content[0].loaiThayDoi").value("THIET_LAP_TONG_THE"));
    }

    @Test
    @DisplayName("GET /api/v1/seller/thiet-lap-shop/thong-ke trả về thống kê 200 OK")
    void layThongKe_200() throws Exception {
        ThongKeThietLapShopResponse stats = ThongKeThietLapShopResponse.builder()
                .tongSoLanCapNhat(8)
                .phanTramHoanThien(100)
                .trangThaiGianHang("HOAT_DONG")
                .dangMoCua(true)
                .build();

        when(thietLapHoSoShopService.layThongKeThietLap(2L)).thenReturn(stats);

        mockMvc.perform(get("/api/v1/seller/thiet-lap-shop/thong-ke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tongSoLanCapNhat").value(8))
                .andExpect(jsonPath("$.duLieu.phanTramHoanThien").value(100));
    }
}
