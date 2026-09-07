package com.example.demo.service;

import com.example.demo.entity.ChiTietDonHang;
import com.example.demo.entity.DonHangShop;
import com.example.demo.repository.ChiTietDonHangRepository;
import com.example.demo.repository.DonHangShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * US-31 (làm lại): Seller in phiếu đóng gói / mã vận đơn hàng loạt khổ A6.
 * Quy tắc:
 * - Chỉ in đơn thuộc đúng gian hàng của seller.
 * - Đơn thiếu mã vận đơn thì tự sinh theo format FS-yyyyMMdd-XXXXXX.
 * - Sắp xếp mới nhất trước để dễ bàn giao cho 3PL.
 */
@Service
public class VanDonService {

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    public List<DonHangShop> layDanhSachDonChoIn(Long maGianHang, String tuKhoa) {
        List<DonHangShop> all = donHangShopRepository.findAllByGianHang_MaGianHang(maGianHang);
        // Mới nhất trước
        all.sort((a, b) -> {
            if (a.getNgayTao() == null) return 1;
            if (b.getNgayTao() == null) return -1;
            return b.getNgayTao().compareTo(a.getNgayTao());
        });
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return all;
        }
        String kw = tuKhoa.trim().toLowerCase();
        return all.stream()
                .filter(d -> (d.getMaCodeDonShop() != null && d.getMaCodeDonShop().toLowerCase().contains(kw))
                        || (d.getMaVanDon() != null && d.getMaVanDon().toLowerCase().contains(kw))
                        || (d.getTrangThai() != null && d.getTrangThai().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<DonHangShop> layDonDeIn(List<Long> ids, Long maGianHang) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        // Loại trùng, chỉ giữ đơn đúng shop (chống in nhầm shop khác)
        List<Long> idSach = ids.stream().distinct().collect(Collectors.toList());
        List<DonHangShop> list = donHangShopRepository.findAllById(idSach).stream()
                .filter(d -> d.getGianHang() != null && d.getGianHang().getMaGianHang().equals(maGianHang))
                .collect(Collectors.toList());
        // Tự sinh mã vận đơn chuẩn cho đơn chưa có (US-31)
        for (DonHangShop d : list) {
            if (d.getMaVanDon() == null || d.getMaVanDon().isBlank()) {
                d.setMaVanDon(sinhMaVanDon(d.getMaDonHangShop()));
                donHangShopRepository.save(d);
            }
        }
        return list;
    }

    /** Format: FS-yyyyMMdd-id6so, vd FS-20260908-000088 */
    public String sinhMaVanDon(Long maDonHangShop) {
        String ngay = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        return "FS-" + ngay + "-" + String.format("%06d", maDonHangShop);
    }

    public List<ChiTietDonHang> layChiTiet(Long maDonHangShop) {
        return chiTietDonHangRepository.findAllByDonHangShop_MaDonHangShop(maDonHangShop);
    }
}
