package com.example.demo.service;

import com.example.demo.dto.GhiHanhTrinhForm;
import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.LichSuHanhTrinhDon;
import com.example.demo.entity.TramTrungChuyenHub;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.LichSuHanhTrinhDonRepository;
import com.example.demo.repository.TramTrungChuyenHubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * US-33: Dinh tuyen kien hang qua Buu cuc / Tram trung chuyen (Sorting Hub).
 * Ghi nhan lich su kien qua cac Hub (Da roi Hub, Dang nhap Hub...).
 */
@Service
public class HanhTrinhService {

    @Autowired
    private LichSuHanhTrinhDonRepository hanhTrinhRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private TramTrungChuyenHubRepository hubRepository;

    public List<LichSuHanhTrinhDon> layTimeline(Long maDonHangShop) {
        return hanhTrinhRepository.findAllByDonHangShop_MaDonHangShopOrderByThoiGianDesc(maDonHangShop);
    }

    public List<TramTrungChuyenHub> layTatCaHub() {
        return hubRepository.findAll();
    }

    @Transactional
    public LichSuHanhTrinhDon ghiMoc(GhiHanhTrinhForm form) {
        DonHangShop don = donHangShopRepository.findById(form.getMaDonHangShop())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn shop " + form.getMaDonHangShop()));
        TramTrungChuyenHub hub = null;
        if (form.getMaHub() != null) {
            hub = hubRepository.findById(form.getMaHub())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Hub " + form.getMaHub()));
        }
        LichSuHanhTrinhDon moc = new LichSuHanhTrinhDon();
        moc.setDonHangShop(don);
        moc.setHub(hub);
        moc.setTieuDeMoc(form.getTieuDeMoc().trim());
        moc.setViTriHienTai(form.getViTriHienTai());
        moc.setThoiGian(LocalDateTime.now());
        return hanhTrinhRepository.save(moc);
    }
}
