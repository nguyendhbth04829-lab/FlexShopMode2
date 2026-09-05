package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Khóa chính tổng hợp (Composite Primary Key) cho bảng theo_doi_gian_hang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheoDoiGianHangId implements Serializable {

    private Long maNguoiDung;
    private Long maGianHang;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TheoDoiGianHangId that = (TheoDoiGianHangId) o;
        return Objects.equals(maNguoiDung, that.maNguoiDung) &&
               Objects.equals(maGianHang, that.maGianHang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNguoiDung, maGianHang);
    }
}
