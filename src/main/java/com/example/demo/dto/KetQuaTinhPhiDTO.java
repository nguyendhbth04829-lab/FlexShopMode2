package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * US-32 (lam lai): Ket qua tinh phi van chuyen de hien thi test.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KetQuaTinhPhiDTO {

    private long khoiLuongQuyDoiGram;

    private long khoiLuongTinhCuocGram;

    private BigDecimal phiVanChuyen;

    private String tenDoiTac;

    private String tuyenVanChuyen;
}
