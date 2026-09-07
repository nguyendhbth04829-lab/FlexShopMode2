package com.example.demo.controller.api;

import com.example.demo.dto.request.TaoDiaChiRequest;
import com.example.demo.dto.response.DiaChiResponse;
import com.example.demo.dto.response.ThongKeDiaChiResponse;
import com.example.demo.exception.XuLyNgoaiLeToanCuc;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.DiaChiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DiaChiApiControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DiaChiService diaChiService;

    @InjectMocks
    private DiaChiApiController diaChiApiController;

    private NguoiDungPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = NguoiDungPrincipal.builder()
                .maNguoiDung(3L)
                .email("customer@flexshop.vn")
                .hoVaTen("Lê Khách Hàng")
                .soDienThoai("0987654321")
                .danhSachQuyen(List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
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

        mockMvc = MockMvcBuilders.standaloneSetup(diaChiApiController)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new XuLyNgoaiLeToanCuc())
                .build();
    }

    @Test
    @DisplayName("API GET /api/v1/dia-chi - Lấy danh sách phân trang thành công")
    void testLayDanhSachDiaChi() throws Exception {
        DiaChiResponse resp = DiaChiResponse.builder()
                .maDiaChi(1L)
                .maNguoiDung(3L)
                .tenNguoiNhan("Lê Khách Hàng")
                .soDienThoai("0987654321")
                .tinhThanh("Hà Nội")
                .quanHuyen("Cầu Giấy")
                .xaPhuong("Dịch Vọng")
                .diaChiChiTiet("Số 10 Duy Tân")
                .laMacDinh(true)
                .diaChiDayDu("Số 10 Duy Tân, Dịch Vọng, Cầu Giấy, Hà Nội")
                .ngayTao(LocalDateTime.now())
                .build();

        when(diaChiService.layDanhSachPhanTrang(eq(3L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(resp)));

        mockMvc.perform(get("/api/v1/dia-chi")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.content[0].tenNguoiNhan").value("Lê Khách Hàng"))
                .andExpect(jsonPath("$.duLieu.content[0].laMacDinh").value(true));
    }

    @Test
    @DisplayName("API GET /api/v1/dia-chi/thong-ke - Thống kê giới hạn 20 địa chỉ")
    void testThongKeDiaChi() throws Exception {
        ThongKeDiaChiResponse thongKe = ThongKeDiaChiResponse.builder()
                .tongSoDiaChi(2L)
                .gioiHanToiDa(20)
                .soLuongConLai(18L)
                .coDiaChiMacDinh(true)
                .danhSachTinhThanh(List.of("Hà Nội", "Đà Nẵng"))
                .build();

        when(diaChiService.thongKeDiaChi(3L)).thenReturn(thongKe);

        mockMvc.perform(get("/api/v1/dia-chi/thong-ke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tongSoDiaChi").value(2))
                .andExpect(jsonPath("$.duLieu.gioiHanToiDa").value(20))
                .andExpect(jsonPath("$.duLieu.soLuongConLai").value(18));
    }

    @Test
    @DisplayName("API POST /api/v1/dia-chi - Tạo địa chỉ mới thành công (201 Created)")
    void testTaoDiaChi_ThanhCong() throws Exception {
        TaoDiaChiRequest request = TaoDiaChiRequest.builder()
                .tenNguoiNhan("Lê Khách Hàng")
                .soDienThoai("0987654321")
                .tinhThanh("Hà Nội")
                .quanHuyen("Cầu Giấy")
                .xaPhuong("Dịch Vọng")
                .diaChiChiTiet("Số 10 Duy Tân")
                .laMacDinh(true)
                .build();

        DiaChiResponse resp = DiaChiResponse.builder()
                .maDiaChi(10L)
                .maNguoiDung(3L)
                .tenNguoiNhan("Lê Khách Hàng")
                .soDienThoai("0987654321")
                .laMacDinh(true)
                .build();

        when(diaChiService.taoDiaChi(any(TaoDiaChiRequest.class), eq(3L))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/dia-chi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.maDiaChi").value(10L))
                .andExpect(jsonPath("$.duLieu.laMacDinh").value(true));
    }

    @Test
    @DisplayName("API POST /api/v1/dia-chi - Validate thất bại khi số điện thoại sai định dạng VN")
    void testTaoDiaChi_SoDienThoaiSaiDinhDang_BaoLoi400() throws Exception {
        TaoDiaChiRequest request = TaoDiaChiRequest.builder()
                .tenNguoiNhan("Lê Khách Hàng")
                .soDienThoai("123456") // Không đúng regex SĐT VN
                .tinhThanh("Hà Nội")
                .quanHuyen("Cầu Giấy")
                .xaPhuong("Dịch Vọng")
                .diaChiChiTiet("Số 10 Duy Tân")
                .build();

        mockMvc.perform(post("/api/v1/dia-chi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thanhCong").value(false));

        verify(diaChiService, never()).taoDiaChi(any(), any());
    }

    @Test
    @DisplayName("API PATCH /api/v1/dia-chi/{id}/dat-mac-dinh - Thiết lập mặc định thành công")
    void testDatLamMacDinh_ThanhCong() throws Exception {
        DiaChiResponse resp = DiaChiResponse.builder()
                .maDiaChi(15L)
                .laMacDinh(true)
                .build();

        when(diaChiService.datLamMacDinh(15L, 3L)).thenReturn(resp);

        mockMvc.perform(patch("/api/v1/dia-chi/15/dat-mac-dinh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.laMacDinh").value(true));
    }

    @Test
    @DisplayName("API DELETE /api/v1/dia-chi/{id} - Xóa địa chỉ thành công")
    void testXoaDiaChi_ThanhCong() throws Exception {
        doNothing().when(diaChiService).xoaDiaChi(15L, 3L);

        mockMvc.perform(delete("/api/v1/dia-chi/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true));

        verify(diaChiService, times(1)).xoaDiaChi(15L, 3L);
    }
}
