package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDungResponse {
    private Long maNguoiDung;
    private String email;
    private String soDienThoai;
    private String hoVaTen;
    private String anhDaiDien;
    private String trangThai;
    private List<String> danhSachVaiTro;
    private String duongDanDashboard;
    private LocalDateTime ngayTao;

    // Getter tương thích cho JavaScript / JSON
    public Long getId() {
        return maNguoiDung;
    }

    public List<String> getRoles() {
        return danhSachVaiTro;
    }

    public String getTargetDashboardUrl() {
        return duongDanDashboard;
    }

    /**
     * Chỉ Người Bán (NGUOI_BAN) và Khách Hàng (KHACH_HANG) mới có quyền sử dụng Sổ Địa Chỉ
     */
    public boolean isCoQuyenDiaChi() {
        if (danhSachVaiTro == null) {
            return false;
        }
        return danhSachVaiTro.stream().anyMatch(role ->
                role.equalsIgnoreCase("KHACH_HANG") || role.equalsIgnoreCase("ROLE_KHACH_HANG") ||
                role.equalsIgnoreCase("NGUOI_BAN") || role.equalsIgnoreCase("ROLE_NGUOI_BAN")
        );
    }
}
