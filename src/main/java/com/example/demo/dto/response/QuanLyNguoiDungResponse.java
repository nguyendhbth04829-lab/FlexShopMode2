package com.example.demo.dto.response;

import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.VaiTro;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO hiển thị thông tin người dùng trong trang quản trị Admin - US-06
 * Mỗi người dùng chỉ hiển thị và gán 1 vai trò duy nhất
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuanLyNguoiDungResponse {

    private Long maNguoiDung;
    private String email;
    private String soDienThoai;
    private String hoVaTen;
    private String anhDaiDien;
    private String trangThai;
    private String lyDoKhoa;
    private String vaiTro;
    private List<String> danhSachVaiTro;
    private String chucVuChinh;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    public Long getId() {
        return maNguoiDung;
    }

    public static class QuanLyNguoiDungResponseBuilder {
        public QuanLyNguoiDungResponseBuilder id(Long id) {
            this.maNguoiDung = id;
            return this;
        }
    }

    public boolean isBiKhoa() {
        return "BI_KHOA".equalsIgnoreCase(trangThai) || "KHOA".equalsIgnoreCase(trangThai);
    }

    public boolean isHoatDong() {
        return "HOAT_DONG".equalsIgnoreCase(trangThai);
    }

    public String getVaiTro() {
        if (vaiTro != null && !vaiTro.isEmpty()) return vaiTro;
        if (danhSachVaiTro != null && !danhSachVaiTro.isEmpty()) return danhSachVaiTro.get(0);
        return "KHACH_HANG";
    }

    public static QuanLyNguoiDungResponse tuEntity(NguoiDung entity) {
        if (entity == null) return null;

        List<String> rawRoles = entity.getDanhSachVaiTro() != null
                ? entity.getDanhSachVaiTro().stream().map(VaiTro::getTenVaiTro).collect(Collectors.toList())
                : Collections.emptyList();

        String vaiTroDuyNhat = xacDinhVaiTroDuyNhat(rawRoles);
        List<String> singleRoleList = Collections.singletonList(vaiTroDuyNhat);
        String chucVu = xacDinhChucVuChinh(vaiTroDuyNhat);

        return QuanLyNguoiDungResponse.builder()
                .maNguoiDung(entity.getMaNguoiDung())
                .email(entity.getEmail())
                .soDienThoai(entity.getSoDienThoai())
                .hoVaTen(entity.getHoVaTen())
                .anhDaiDien(entity.getAnhDaiDien() != null ? entity.getAnhDaiDien() : "https://ui-avatars.com/api/?name=User&background=4f46e5&color=fff")
                .trangThai(entity.getTrangThai() != null ? entity.getTrangThai() : "HOAT_DONG")
                .lyDoKhoa(entity.getLyDoKhoa())
                .vaiTro(vaiTroDuyNhat)
                .danhSachVaiTro(singleRoleList)
                .chucVuChinh(chucVu)
                .ngayTao(entity.getNgayTao())
                .ngayCapNhat(entity.getNgayCapNhat())
                .build();
    }

    public static String xacDinhVaiTroDuyNhat(List<String> roles) {
        if (roles == null || roles.isEmpty()) return "KHACH_HANG";
        if (roles.contains("ADMIN") || roles.contains("ROLE_ADMIN")) return "ADMIN";
        if (roles.contains("CSKH") || roles.contains("ROLE_CSKH")) return "CSKH";
        if (roles.contains("TAI_XE") || roles.contains("ROLE_TAI_XE") || roles.contains("SHIPPER") || roles.contains("ROLE_SHIPPER")) return "TAI_XE";
        if (roles.contains("NGUOI_BAN") || roles.contains("ROLE_NGUOI_BAN")) return "NGUOI_BAN";
        return "KHACH_HANG";
    }

    private static String xacDinhChucVuChinh(String role) {
        if ("ADMIN".equals(role)) return "Quản Trị Viên";
        if ("CSKH".equals(role)) return "Chăm Sóc Khách Hàng";
        if ("TAI_XE".equals(role) || "SHIPPER".equals(role)) return "Tài Xế Giao Hàng";
        if ("NGUOI_BAN".equals(role)) return "Người Bán Hàng";
        return "Khách Hàng";
    }
}