package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Khóa chính tổng hợp (Composite Primary Key) cho bảng san_pham_yeu_thich
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamYeuThichId implements Serializable {

    private Long maNguoiDung;
    private Long maSanPham;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SanPhamYeuThichId that = (SanPhamYeuThichId) o;
        return Objects.equals(maNguoiDung, that.maNguoiDung) &&
               Objects.equals(maSanPham, that.maSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNguoiDung, maSanPham);
    }
}
