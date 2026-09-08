package com.example.demo.controller.api;

import com.example.demo.dto.request.TuChoiGianHangRequest;
import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.PhanTrangResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.exception.XuLyNgoaiLeToanCuc;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.KiemDuyetGianHangService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class KiemDuyetGianHangApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private KiemDuyetGianHangService kiemDuyetGianHangService;

    @InjectMocks
    private KiemDuyetGianHangApiController controller;

    private ObjectMapper objectMapper = new ObjectMapper();
    private NguoiDungPrincipal mockAdminPrincipal;

    @BeforeEach
    void setUp() {
        mockAdminPrincipal = NguoiDungPrincipal.builder()
                .maNguoiDung(1L)
                .email("admin@flexshop.vn")
                .hoVaTen("Admin FlexShop")
                .danhSachQuyen(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
                return mockAdminPrincipal;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new XuLyNgoaiLeToanCuc())
                .setCustomArgumentResolvers(principalResolver)
                .build();
    }

    @Test
    @DisplayName("API US09-01: Lấy danh sách gian hàng phân trang và bộ lọc trạng thái")
    void layDanhSachGianHang_ThanhCong() throws Exception {
        GianHangResponse res = GianHangResponse.builder()
                .maGianHang(10L)
                .tenGianHang("Shop Test API")
                .duongDanSlug("shop-test-api")
                .trangThai("CHO_DUYET")
                .ngayTao(LocalDateTime.now())
                .build();

        PhanTrangResponse<GianHangResponse> pageRes = PhanTrangResponse.<GianHangResponse>builder()
                .content(List.of(res))
                .totalElements(1)
                .totalPages(1)
                .number(0)
                .size(10)
                .build();

        when(kiemDuyetGianHangService.danhSachGianHangPhanTrang(any(), any(), anyInt(), anyInt()))
                .thenReturn(pageRes);

        mockMvc.perform(get("/api/v1/admin/kiem-duyet-shop")
                        .param("page", "0")
                        .param("size", "10")
                        .param("trangThai", "CHO_DUYET"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].maGianHang").value(10))
                .andExpect(jsonPath("$.data.content[0].tenGianHang").value("Shop Test API"))
                .andExpect(jsonPath("$.data.content[0].trangThai").value("CHO_DUYET"));
    }

    @Test
    @DisplayName("API US09-02: Lấy thống kê số lượng kiểm duyệt gian hàng")
    void layThongKe_ThanhCong() throws Exception {
        ThongKeGianHangResponse stats = ThongKeGianHangResponse.builder()
                .tongSo(20L)
                .choDuyet(5L)
                .hoatDong(12L)
                .tuChoi(3L)
                .tamKhoa(0L)
                .build();

        when(kiemDuyetGianHangService.layThongKeKiemDuyet()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/kiem-duyet-shop/thong-ke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tongSo").value(20))
                .andExpect(jsonPath("$.data.choDuyet").value(5))
                .andExpect(jsonPath("$.data.hoatDong").value(12))
                .andExpect(jsonPath("$.data.tuChoi").value(3));
    }

    @Test
    @DisplayName("API US09-03: Lấy chi tiết gian hàng theo ID")
    void layChiTietGianHang_ThanhCong() throws Exception {
        GianHangResponse res = GianHangResponse.builder()
                .maGianHang(10L)
                .tenGianHang("Shop Test Chi Tiết")
                .duongDanSlug("shop-test-chi-tiet")
                .trangThai("CHO_DUYET")
                .build();

        when(kiemDuyetGianHangService.layChiTietGianHang(10L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/admin/kiem-duyet-shop/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maGianHang").value(10))
                .andExpect(jsonPath("$.data.tenGianHang").value("Shop Test Chi Tiết"));
    }

    @Test
    @DisplayName("API US09-04: Phê duyệt gian hàng thành công")
    void pheDuyetGianHang_ThanhCong() throws Exception {
        GianHangResponse res = GianHangResponse.builder()
                .maGianHang(10L)
                .tenGianHang("Shop Được Duyệt")
                .trangThai("HOAT_DONG")
                .build();

        when(kiemDuyetGianHangService.pheDuyetGianHang(10L)).thenReturn(res);

        mockMvc.perform(put("/api/v1/admin/kiem-duyet-shop/10/phe-duyet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trangThai").value("HOAT_DONG"))
                .andExpect(jsonPath("$.message", containsString("thành công")));
    }

    @Test
    @DisplayName("API US09-05: Từ chối gian hàng thành công")
    void tuChoiGianHang_ThanhCong() throws Exception {
        TuChoiGianHangRequest req = TuChoiGianHangRequest.builder()
                .lyDoTuChoi("Giấy phép kinh doanh không hợp lệ, vui lòng chụp lại ảnh rõ nét.")
                .build();

        GianHangResponse res = GianHangResponse.builder()
                .maGianHang(10L)
                .tenGianHang("Shop Bị Từ Chối")
                .trangThai("TU_CHOI")
                .lyDoTuChoi(req.getLyDoTuChoi())
                .build();

        when(kiemDuyetGianHangService.tuChoiGianHang(eq(10L), any(TuChoiGianHangRequest.class))).thenReturn(res);

        mockMvc.perform(put("/api/v1/admin/kiem-duyet-shop/10/tu-choi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trangThai").value("TU_CHOI"))
                .andExpect(jsonPath("$.message", containsString("từ chối")));
    }

    @Test
    @DisplayName("API US09-06: Từ chối gian hàng thất bại khi lý do không hợp lệ")
    void tuChoiGianHang_ThatBai_Validation() throws Exception {
        TuChoiGianHangRequest invalidReq = TuChoiGianHangRequest.builder()
                .lyDoTuChoi("") // Trống
                .build();

        mockMvc.perform(put("/api/v1/admin/kiem-duyet-shop/10/tu-choi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }
}
