package com.example.demo.controller.web;

import com.example.demo.dto.response.ChungChiGianHangResponse;
import com.example.demo.dto.response.HoSoGianHangDayDuResponse;
import com.example.demo.dto.response.NguoiDungResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class HoSoGianHangTemplateRenderTest {

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void testRenderTemplate() {
        Context context = new Context();
        NguoiDungResponse user = NguoiDungResponse.builder()
                .maNguoiDung(47L)
                .email("ruy53465@gmail.com")
                .hoVaTen("Khánh Dung")
                .soDienThoai("0987654321")
                .danhSachVaiTro(List.of("NGUOI_BAN"))
                .build();

        ChungChiGianHangResponse cert = ChungChiGianHangResponse.builder()
                .maChungChi(1L)
                .maGianHang(3L)
                .loaiGiayTo("GIAY_PHEP_KINH_DOANH")
                .tenLoaiGiayTo("Giấy phép kinh doanh")
                .soGiayTo("12345678")
                .linkAnhGiayTo("/uploads/giay-phep-kd/gpkd_47_1788776855546_1c4179.png")
                .trangThaiDuyet("DA_DUYET")
                .tenTrangThaiDuyet("Đã duyệt")
                .ngayTao(LocalDateTime.now())
                .laTepPdf(false)
                .build();

        HoSoGianHangDayDuResponse hoSo = HoSoGianHangDayDuResponse.builder()
                .maGianHang(3L)
                .maChuSoHuu(47L)
                .tenChuSoHuu("Khánh Dung")
                .emailChuSoHuu("ruy53465@gmail.com")
                .sdtChuSoHuu("0987654321")
                .tenGianHang("Flex Fashon")
                .duongDanSlug("flex-fashon")
                .moTa("Bán thời trang")
                .linkLogo("/uploads/shop-logos/logo.png")
                .linkBanner(null)
                .diaChiKho("Hà Nội")
                .sdtKho("0987654321")
                .trangThai("HOAT_DONG")
                .tenTrangThai("Đang hoạt động")
                .hangGianHang("TIEM_NANG")
                .tenHangGianHang("Shop Tiềm Năng")
                .diemSaoQuaTa(0)
                .diemDanhGiaTb(BigDecimal.valueOf(5.0))
                .tongDanhGia(0)
                .tongDonHang(0)
                .tyLePhanHoiChat(BigDecimal.valueOf(100.0))
                .ngayTao(LocalDateTime.now())
                .tongSoChungChi(1)
                .soChungChiDaDuyet(1)
                .soChungChiChoDuyet(0)
                .soChungChiTuChoi(0)
                .danhSachChungChi(List.of(cert))
                .build();

        context.setVariable("user", user);
        context.setVariable("hoSo", hoSo);
        context.setVariable("pageTitle", "Hồ Sơ Gian Hàng & Giấy Phép");

        String rendered = templateEngine.process("dashboard/ho-so-shop", context);
        System.out.println("RENDERED HTML LENGTH: " + rendered.length());
        System.out.println("RENDERED FIRST 500 CHARS:\n" + rendered.substring(0, Math.min(500, rendered.length())));
        System.out.println("RENDERED LAST 500 CHARS:\n" + rendered.substring(Math.max(0, rendered.length() - 500)));
    }
}
