package com.example.demo.controller.api;

import com.example.demo.dto.request.DangKyGianHangRequest;
import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.exception.XuLyNgoaiLeToanCuc;
import com.example.demo.security.NguoiDungPrincipal;
import com.example.demo.service.DangKyGianHangService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DangKyGianHangApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DangKyGianHangService dangKyGianHangService;

    @InjectMocks
    private DangKyGianHangApiController controller;

    private NguoiDungPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockPrincipal = NguoiDungPrincipal.builder()
                .maNguoiDung(5L)
                .email("seller_applicant@flexshop.vn")
                .hoVaTen("Vũ Đăng Ký")
                .soDienThoai("0988776655")
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

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new XuLyNgoaiLeToanCuc())
                .build();
    }

    @Test
    @DisplayName("US-08: API POST /api/v1/seller/dang-ky-shop nộp hồ sơ thành công -> Trả về 201 Created")
    void testDangKyShop_ThanhCong_201() throws Exception {
        MockMultipartFile fileGiayPhep = new MockMultipartFile(
                "fileGiayPhep",
                "giay_phep.pdf",
                "application/pdf",
                "%PDF-1.4 Mock License Content".getBytes()
        );

        GianHangResponse response = GianHangResponse.builder()
                .maGianHang(1L)
                .tenGianHang("Thời Trang Flex Shop")
                .duongDanSlug("thoi-trang-flex-shop")
                .trangThai("CHO_DUYET")
                .soGiayTo("0109988776")
                .trangThaiGiayTo("CHO_DUYET")
                .build();

        when(dangKyGianHangService.dangKyGianHang(eq(5L), any(DangKyGianHangRequest.class), any(), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/seller/dang-ky-shop")
                        .file(fileGiayPhep)
                        .param("tenGianHang", "Thời Trang Flex Shop")
                        .param("sdtKho", "0988776655")
                        .param("diaChiKho", "Số 10 Tràng Thi, Hoàn Kiếm, Hà Nội")
                        .param("soGiayTo", "0109988776")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tenGianHang").value("Thời Trang Flex Shop"))
                .andExpect(jsonPath("$.duLieu.trangThai").value("CHO_DUYET"));
    }

    @Test
    @DisplayName("US-08: API GET /api/v1/seller/dang-ky-shop/trang-thai -> Trả về 200 OK")
    void testLayTrangThai_ThanhCong_200() throws Exception {
        GianHangResponse response = GianHangResponse.builder()
                .maGianHang(1L)
                .tenGianHang("Thời Trang Flex Shop")
                .trangThai("CHO_DUYET")
                .build();

        when(dangKyGianHangService.layThongTinGianHangCuaToi(5L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/seller/dang-ky-shop/trang-thai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.trangThai").value("CHO_DUYET"));
    }

    @Test
    @DisplayName("US-08: API GET /api/v1/seller/dang-ky-shop/kiem-tra-slug -> Trả về gợi ý slug chuẩn")
    void testKiemTraSlug_ThanhCong() throws Exception {
        when(dangKyGianHangService.kiemTraTenGianHangKhaDung(eq("Mỹ Phẩm Trắng Da"), any())).thenReturn(true);
        when(dangKyGianHangService.taoDuongDanSlugTuTenShop(eq("Mỹ Phẩm Trắng Da"), any())).thenReturn("my-pham-trang-da");

        mockMvc.perform(get("/api/v1/seller/dang-ky-shop/kiem-tra-slug")
                        .param("tenGianHang", "Mỹ Phẩm Trắng Da"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tenKhaDung").value(true))
                .andExpect(jsonPath("$.duLieu.slugGoiY").value("my-pham-trang-da"));
    }

    @Test
    @DisplayName("US-08: API GET /api/v1/seller/dang-ky-shop/thong-ke -> Trả về 200 OK")
    void testLayThongKe_ThanhCong() throws Exception {
        ThongKeGianHangResponse thongKe = ThongKeGianHangResponse.builder()
                .tongSo(15L)
                .choDuyet(5L)
                .hoatDong(8L)
                .tuChoi(2L)
                .tamKhoa(0L)
                .build();

        when(dangKyGianHangService.layThongKeGianHang()).thenReturn(thongKe);

        mockMvc.perform(get("/api/v1/seller/dang-ky-shop/thong-ke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanhCong").value(true))
                .andExpect(jsonPath("$.duLieu.tongSo").value(15))
                .andExpect(jsonPath("$.duLieu.choDuyet").value(5));
    }
}
