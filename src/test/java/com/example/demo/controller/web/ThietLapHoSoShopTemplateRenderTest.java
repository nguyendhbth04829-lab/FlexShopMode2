package com.example.demo.controller.web;

import com.example.demo.dto.response.NguoiDungResponse;
import com.example.demo.dto.response.ThietLapHoSoShopResponse;
import com.example.demo.dto.response.ThongKeThietLapShopResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ThietLapHoSoShopTemplateRenderTest {

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void testRenderThietLapShopTemplate_HoatDong() {
        Context context = new Context();
        NguoiDungResponse user = NguoiDungResponse.builder()
                .maNguoiDung(2L)
                .email("seller@flexshop.vn")
                .hoVaTen("Nguyễn Văn Bán")
                .soDienThoai("0987654321")
                .anhDaiDien("https://cdn-icons-png.flaticon.com/512/3135/3135715.png")
                .danhSachVaiTro(List.of("NGUOI_BAN"))
                .build();

        ThietLapHoSoShopResponse shop = ThietLapHoSoShopResponse.builder()
                .maGianHang(10L)
                .maChuSoHuu(2L)
                .tenGianHang("Flex Fashion Store")
                .duongDanSlug("flex-fashion-store")
                .moTa("Shop thời trang cao cấp")
                .linkLogo("/uploads/shop-logos/logo_test.png")
                .linkBanner("/uploads/shop-banners/banner_test.png")
                .diaChiKho("123 Phố Huế, Hoàn Kiếm, Hà Nội")
                .sdtKho("0987654321")
                .gioMoCua("08:00")
                .gioDongCua("22:00")
                .dangMoCua(true)
                .ghiChuKho("Bấm chuông kho 2")
                .nguoiLienHeKho("Trần Văn Kho")
                .trangThai("HOAT_DONG")
                .hangGianHang("TIEM_NANG")
                .diemSaoQuaTa(0)
                .diemDanhGiaTb(BigDecimal.valueOf(5.0))
                .tongDanhGia(15)
                .tongDonHang(88)
                .tyLePhanHoiChat(BigDecimal.valueOf(100.00))
                .ngayTao(LocalDateTime.now())
                .phanTramHoanThien(100)
                .cacBuocConThieu(List.of())
                .build();

        ThongKeThietLapShopResponse thongKe = ThongKeThietLapShopResponse.builder()
                .tongSoLanCapNhat(5)
                .phanTramHoanThien(100)
                .trangThaiGianHang("HOAT_DONG")
                .dangMoCua(true)
                .gioHoatDongHienTai("08:00 - 22:00")
                .tenGianHang("Flex Fashion Store")
                .hangGianHang("TIEM_NANG")
                .build();

        context.setVariable("user", user);
        context.setVariable("shop", shop);
        context.setVariable("thongKe", thongKe);
        context.setVariable("pageTitle", "Thiết Lập Hồ Sơ Gian Hàng - FlexShop Seller");
        context.setVariable("currentRole", "NGUOI_BAN");

        String renderedHtml = templateEngine.process("dashboard/thiet-lap-shop", context);

        assertThat(renderedHtml).isNotBlank();
        assertThat(renderedHtml).contains("Flex Fashion Store");
        assertThat(renderedHtml).contains("08:00");
        assertThat(renderedHtml).contains("22:00");
        assertThat(renderedHtml).contains("123 Phố Huế, Hoàn Kiếm, Hà Nội");
    }

    @Test
    void testRenderThietLapShopTemplate_ChoDuyet() {
        Context context = new Context();
        NguoiDungResponse user = NguoiDungResponse.builder()
                .maNguoiDung(2L)
                .email("seller@flexshop.vn")
                .hoVaTen("Nguyễn Văn Bán")
                .soDienThoai("0987654321")
                .danhSachVaiTro(List.of("NGUOI_BAN"))
                .build();

        ThietLapHoSoShopResponse shop = ThietLapHoSoShopResponse.builder()
                .maGianHang(11L)
                .maChuSoHuu(2L)
                .tenGianHang("Shop Cho Duyet")
                .duongDanSlug("shop-cho-duyet")
                .moTa("Mô tả")
                .linkLogo(null)
                .linkBanner(null)
                .diaChiKho("Hà Nội")
                .sdtKho("0987654321")
                .gioMoCua("08:00")
                .gioDongCua("22:00")
                .dangMoCua(false)
                .trangThai("CHO_DUYET")
                .lyDoTuChoi(null)
                .hangGianHang("TIEM_NANG")
                .phanTramHoanThien(40)
                .cacBuocConThieu(List.of("Tải lên Logo đại diện gian hàng", "Tải lên Banner trang trí gian hàng"))
                .build();

        context.setVariable("user", user);
        context.setVariable("shop", shop);
        context.setVariable("thongKe", null);
        context.setVariable("pageTitle", "Thiết Lập Hồ Sơ Gian Hàng - FlexShop Seller");
        context.setVariable("currentRole", "NGUOI_BAN");

        String renderedHtml = templateEngine.process("dashboard/thiet-lap-shop", context);

        assertThat(renderedHtml).isNotBlank();
        assertThat(renderedHtml).contains("Gian Hàng Chưa Được Quản Trị Viên Kích Hoạt!");
        assertThat(renderedHtml).contains("CHO_DUYET");
    }
}
