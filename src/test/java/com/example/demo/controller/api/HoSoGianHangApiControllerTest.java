package com.example.demo.controller.api;

import com.example.demo.dto.request.CapNhatThongTinShopRequest;
import com.example.demo.dto.request.ThemChungChiRequest;
import com.example.demo.dto.response.ChungChiGianHangResponse;
import com.example.demo.dto.response.HoSoGianHangDayDuResponse;
import com.example.demo.exception.XuLyNgoaiLeToanCuc;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.HoSoGianHangService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HoSoGianHangApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HoSoGianHangService hoSoGianHangService;

    @InjectMocks
    private HoSoGianHangApiController controller;

    private NguoiDungPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = NguoiDungPrincipal.builder()
                .maNguoiDung(2L)
                .email("seller@flexshop.vn")
                .hoVaTen("Trần Người Bán")
                .soDienThoai("0912345678")
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
    @DisplayName("API GET /api/v1/seller/ho-so-shop trả về 200 OK và thông tin hồ sơ gian hàng")
    void testLayHoSoShop_ThanhCong() throws Exception {
        HoSoGianHangDayDuResponse response = HoSoGianHangDayDuResponse.builder()
                .maGianHang(4L)
                .tenGianHang("Flex Fashion 1")
                .duongDanSlug("flex-fashion-1")
                .trangThai("HOAT_DONG")
                .tongSoChungChi(2)
                .soChungChiDaDuyet(1)
                .soChungChiChoDuyet(1)
                .build();

        when(hoSoGianHangService.layHoSoGianHangCuaToi(2L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/seller/ho-so-shop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.maGianHang").value(4))
                .andExpect(jsonPath("$.duLieu.tenGianHang").value("Flex Fashion 1"))
                .andExpect(jsonPath("$.duLieu.tongSoChungChi").value(2));
    }

    @Test
    @DisplayName("API POST /api/v1/seller/ho-so-shop/thong-tin cập nhật thông tin shop thành công")
    void testCapNhatThongTin_ThanhCong() throws Exception {
        HoSoGianHangDayDuResponse response = HoSoGianHangDayDuResponse.builder()
                .maGianHang(4L)
                .tenGianHang("Flex Fashion Updated")
                .diaChiKho("123 Phố Huế, Hai Bà Trưng, Hà Nội")
                .sdtKho("0912345678")
                .build();

        when(hoSoGianHangService.capNhatThongTinGianHang(eq(2L), any(CapNhatThongTinShopRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/seller/ho-so-shop/thong-tin")
                        .param("tenGianHang", "Flex Fashion Updated")
                        .param("sdtKho", "0912345678")
                        .param("diaChiKho", "123 Phố Huế, Hai Bà Trưng, Hà Nội")
                        .param("moTa", "Mô tả cập nhật")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tenGianHang").value("Flex Fashion Updated"));
    }

    @Test
    @DisplayName("API POST /api/v1/seller/ho-so-shop/chung-chi nộp chứng chỉ mới thành công trả về 201 Created")
    void testThemChungChi_ThanhCong() throws Exception {
        MockMultipartFile fileGiayTo = new MockMultipartFile(
                "fileGiayTo", "attp.pdf", "application/pdf", "%PDF-1.4 Mock Document".getBytes()
        );

        ChungChiGianHangResponse response = ChungChiGianHangResponse.builder()
                .maChungChi(15L)
                .maGianHang(4L)
                .loaiGiayTo("AN_TOAN_THUC_PHAM")
                .soGiayTo("VSATTP-2026-09")
                .trangThaiDuyet("CHO_DUYET")
                .ngayTao(LocalDateTime.now())
                .build();

        when(hoSoGianHangService.themChungChi(eq(2L), any(ThemChungChiRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/seller/ho-so-shop/chung-chi")
                        .file(fileGiayTo)
                        .param("loaiGiayTo", "AN_TOAN_THUC_PHAM")
                        .param("soGiayTo", "VSATTP-2026-09")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.maChungChi").value(15))
                .andExpect(jsonPath("$.duLieu.trangThaiDuyet").value("CHO_DUYET"));
    }

    @Test
    @DisplayName("API DELETE /api/v1/seller/ho-so-shop/chung-chi/{id} xóa chứng chỉ thành công")
    void testXoaChungChi_ThanhCong() throws Exception {
        doNothing().when(hoSoGianHangService).xoaChungChi(2L, 10L);

        mockMvc.perform(delete("/api/v1/seller/ho-so-shop/chung-chi/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.thongBao").value("Đã xóa chứng chỉ thành công!"));
    }
}
