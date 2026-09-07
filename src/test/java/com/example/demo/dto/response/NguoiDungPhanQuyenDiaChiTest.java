package com.example.demo.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NguoiDungPhanQuyenDiaChiTest {

    @Test
    @DisplayName("Vai trò KHACH_HANG -> Có quyền truy cập sổ địa chỉ")
    void testKhachHang_CoQuyenDiaChi() {
        NguoiDungResponse user = NguoiDungResponse.builder()
                .danhSachVaiTro(List.of("KHACH_HANG"))
                .build();
        assertThat(user.isCoQuyenDiaChi()).isTrue();
    }

    @Test
    @DisplayName("Vai trò ROLE_KHACH_HANG -> Có quyền truy cập sổ địa chỉ")
    void testRoleKhachHang_CoQuyenDiaChi() {
        NguoiDungResponse user = NguoiDungResponse.builder()
                .danhSachVaiTro(List.of("ROLE_KHACH_HANG"))
                .build();
        assertThat(user.isCoQuyenDiaChi()).isTrue();
    }

    @Test
    @DisplayName("Vai trò NGUOI_BAN -> Có quyền truy cập sổ địa chỉ")
    void testNguoiBan_CoQuyenDiaChi() {
        NguoiDungResponse user = NguoiDungResponse.builder()
                .danhSachVaiTro(List.of("NGUOI_BAN"))
                .build();
        assertThat(user.isCoQuyenDiaChi()).isTrue();
    }

    @Test
    @DisplayName("Vai trò ROLE_NGUOI_BAN -> Có quyền truy cập sổ địa chỉ")
    void testRoleNguoiBan_CoQuyenDiaChi() {
        NguoiDungResponse user = NguoiDungResponse.builder()
                .danhSachVaiTro(List.of("ROLE_NGUOI_BAN"))
                .build();
        assertThat(user.isCoQuyenDiaChi()).isTrue();
    }

    @Test
    @DisplayName("Vai trò ADMIN -> KHÔNG có quyền truy cập sổ địa chỉ")
    void testAdmin_KhongCoQuyenDiaChi() {
        NguoiDungResponse user = NguoiDungResponse.builder()
                .danhSachVaiTro(List.of("ADMIN", "ROLE_ADMIN"))
                .build();
        assertThat(user.isCoQuyenDiaChi()).isFalse();
    }

    @Test
    @DisplayName("Vai trò TAI_XE / SHIPPER -> KHÔNG có quyền truy cập sổ địa chỉ")
    void testTaiXe_KhongCoQuyenDiaChi() {
        NguoiDungResponse user = NguoiDungResponse.builder()
                .danhSachVaiTro(List.of("TAI_XE", "SHIPPER"))
                .build();
        assertThat(user.isCoQuyenDiaChi()).isFalse();
    }

    @Test
    @DisplayName("Vai trò CSKH -> KHÔNG có quyền truy cập sổ địa chỉ")
    void testCskh_KhongCoQuyenDiaChi() {
        NguoiDungResponse user = NguoiDungResponse.builder()
                .danhSachVaiTro(List.of("CSKH"))
                .build();
        assertThat(user.isCoQuyenDiaChi()).isFalse();
    }

    @Test
    @DisplayName("Danh sách vai trò rỗng hoặc null -> KHÔNG có quyền truy cập sổ địa chỉ")
    void testNullHoacRong_KhongCoQuyenDiaChi() {
        NguoiDungResponse userNull = NguoiDungResponse.builder()
                .danhSachVaiTro(null)
                .build();
        assertThat(userNull.isCoQuyenDiaChi()).isFalse();

        NguoiDungResponse userRong = NguoiDungResponse.builder()
                .danhSachVaiTro(Collections.emptyList())
                .build();
        assertThat(userRong.isCoQuyenDiaChi()).isFalse();
    }
}
