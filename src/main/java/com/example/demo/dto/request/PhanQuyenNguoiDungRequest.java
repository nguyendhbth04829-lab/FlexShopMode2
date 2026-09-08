package com.example.demo.dto.request;

import lombok.*;

import java.util.List;

/**
 * DTO yêu cầu cập nhật phân quyền vai trò người dùng - US-06
 * Mỗi người dùng chỉ sở hữu duy nhất 1 vai trò chính trong hệ thống
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhanQuyenNguoiDungRequest {

    private String vaiTro;
    private List<String> danhSachVaiTro;

    /**
     * Lấy vai trò duy nhất được chọn từ request
     */
    public String layVaiTroDuyNhat() {
        if (vaiTro != null && !vaiTro.trim().isEmpty()) {
            return vaiTro.trim();
        }
        if (danhSachVaiTro != null && !danhSachVaiTro.isEmpty()) {
            return danhSachVaiTro.get(0).trim();
        }
        return null;
    }
}