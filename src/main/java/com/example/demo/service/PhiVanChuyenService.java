package com.example.demo.service;

import com.example.demo.dto.KetQuaTinhPhiDTO;
import com.example.demo.dto.TinhPhiVanChuyenForm;
import com.example.demo.entity.BangGiaVanChuyen;
import com.example.demo.repository.BangGiaVanChuyenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * US-32 (lam lai): Tinh phi van chuyen theo kich thuoc va can nang quy doi.
 * - KhoiLuongQuyDoi(gram) = D * R * C / 5 (vi D*R*C/5000 ra kg, x1000 ra gram)
 * - KhoiLuongTinhCuoc = MAX(CanNangThuc, QuyDoi)
 * - Tra bang_gia_van_chuyen theo tuyen (NOI_THANH | TINH | BAN_SO_DIA),
 *   vuot chuan cu moi 500g cong them cuoc_phi_vuot_moi_500g.
 */
@Service
public class PhiVanChuyenService {

    @Autowired
    private BangGiaVanChuyenRepository bangGiaVanChuyenRepository;

    public long tinhKhoiLuongQuyDoiGram(int daiCm, int rongCm, int caoCm) {
        if (daiCm <= 0 || rongCm <= 0 || caoCm <= 0) {
            throw new IllegalArgumentException("Kich thuoc phai > 0");
        }
        return (long) Math.ceil(daiCm * rongCm * caoCm / 5.0);
    }

    public KetQuaTinhPhiDTO tinhPhi(TinhPhiVanChuyenForm form) {
        long quyDoi = tinhKhoiLuongQuyDoiGram(
                form.getChieuDaiCm(), form.getChieuRongCm(), form.getChieuCaoCm());
        long tinhCuoc = Math.max(form.getCanNangGram(), quyDoi);

        BangGiaVanChuyen bangGia = chonBangGia(form.getMaDoiTac(), form.getTuyenVanChuyen());

        BigDecimal phi = tinhCuocTheoBangGia(tinhCuoc, bangGia);

        return new KetQuaTinhPhiDTO(
                quyDoi,
                tinhCuoc,
                phi,
                bangGia.getDoiTac() != null ? bangGia.getDoiTac().getTenDoiTac() : null,
                bangGia.getTuyenVanChuyen());
    }

    private BangGiaVanChuyen chonBangGia(Integer maDoiTac, String tuyen) {
        if (maDoiTac != null) {
            return bangGiaVanChuyenRepository
                    .findFirstByDoiTac_MaDoiTacAndTuyenVanChuyen(maDoiTac, tuyen)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Khong co bang gia cho doi tac " + maDoiTac + " tuyen " + tuyen));
        }
        List<BangGiaVanChuyen> list = bangGiaVanChuyenRepository.findByTuyenVanChuyen(tuyen);
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("Khong co bang gia cho tuyen " + tuyen + ". Hay chay SQL US32.");
        }
        return list.stream()
                .min(Comparator.comparing(BangGiaVanChuyen::getCuocPhiChuan))
                .orElse(list.get(0));
    }

    private BigDecimal tinhCuocTheoBangGia(long tinhCuocGram, BangGiaVanChuyen bangGia) {
        BigDecimal chuan = bangGia.getCuocPhiChuan();
        if (tinhCuocGram <= bangGia.getKhoiLuongChuanGram()) {
            return chuan;
        }
        long vuot = tinhCuocGram - bangGia.getKhoiLuongChuanGram();
        long soBlock = (long) Math.ceil(vuot / 500.0);
        return chuan.add(bangGia.getCuocPhiVuotMoi500g().multiply(BigDecimal.valueOf(soBlock)));
    }

    public List<BangGiaVanChuyen> layTatCaBangGia() {
        return bangGiaVanChuyenRepository.findAll();
    }
}
