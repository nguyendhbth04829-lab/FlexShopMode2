package com.example.demo.service;

import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.LichSuHanhTrinhDon;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.LichSuHanhTrinhDonRepository;
import com.example.demo.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * US-41: Khach hang bam "Da nhan duoc hang" de hoan tat don.
 * Don DANG_GIAO tu shipper (POD) -> DA_GIAO; khach xac nhan -> HOAN_THANH,
 * mo khoa danh gia (danh gia mo tu DA_GIAO - xem DanhGiaController).
 */
@Service
public class KhachHangDonHangService {

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private LichSuHanhTrinhDonRepository hanhTrinhRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    /** Tam: chua co login nen lay khach hang test de demo. */
    public Long layMaKhachHangHienTai() {
        return nguoiDungRepository.findByEmail("khachhang@flexshop.vn")
                .map(n -> n.getMaNguoiDung())
                .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst()
                        .map(n -> n.getMaNguoiDung()).orElse(1L));
    }

    public List<DonHangShop> layDonCuaKhachHang(Long maKhachHang) {
        return donHangShopRepository.findAllByKhachHangId(maKhachHang);
    }

    @Transactional
    public DonHangShop xacNhanDaNhan(Long maKhachHang, Long maDonHangShop) {
        DonHangShop don = donHangShopRepository.findById(maDonHangShop)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng " + maDonHangShop));
        if (don.getDonHangTong() == null || don.getDonHangTong().getKhachHang() == null
                || !don.getDonHangTong().getKhachHang().getMaNguoiDung().equals(maKhachHang)) {
            throw new IllegalStateException("Đơn hàng này không phải của bạn.");
        }
        if (!"DA_GIAO".equals(don.getTrangThai())) {
            throw new IllegalStateException("Đơn đang " + don.getTrangThai() + ", chưa thể xác nhận đã nhận.");
        }
        don.setTrangThai("HOAN_THANH");
        don = donHangShopRepository.save(don);
        LichSuHanhTrinhDon moc = new LichSuHanhTrinhDon();
        moc.setDonHangShop(don);
        moc.setHub(null);
        moc.setTieuDeMoc("Khách đã nhận hàng - hoàn tất");
        moc.setViTriHienTai(null);
        moc.setThoiGian(java.time.LocalDateTime.now());
        hanhTrinhRepository.save(moc);
        return don;
    }

    public List<DonHangShop> layDonChoXacNhan(Long maKhachHang) {
        return layDonCuaKhachHang(maKhachHang).stream()
                .filter(d -> "DA_GIAO".equals(d.getTrangThai()))
                .collect(Collectors.toList());
    }
}
