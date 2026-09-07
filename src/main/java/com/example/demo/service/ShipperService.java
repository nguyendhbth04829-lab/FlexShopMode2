package com.example.demo.service;

import com.example.demo.dto.CapNhatTrangThaiForm;
import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.LichSuHanhTrinhDon;
import com.example.demo.entity.NhiemVuGiaoHang;
import com.example.demo.entity.TaiXeGiaoHang;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.LichSuHanhTrinhDonRepository;
import com.example.demo.repository.NhiemVuGiaoHangRepository;
import com.example.demo.repository.TaiXeGiaoHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * US-34: Shipper bat/tat trang thai lam viec (mobile app).
 * Quy tac: chi duoc Online khi trang_thai = DANG_HOAT_DONG.
 * GPS: vi do [-90,90], kinh do [-180,180].
 */
@Service
public class ShipperService {

    @Autowired
    private TaiXeGiaoHangRepository taiXeRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private NhiemVuGiaoHangRepository nhiemVuRepository;

    @Autowired
    private LichSuHanhTrinhDonRepository hanhTrinhRepository;

    public List<TaiXeGiaoHang> layTatCaShipper() {
        return taiXeRepository.findAll();
    }

    /** Tam: chua co login nen lay shipper dau tien de test tren mobile. */
    public TaiXeGiaoHang layShipperHienTai() {
        return taiXeRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Chua co tai khoan shipper. Hay chay SQL US34."));
    }

    public boolean duocNhanNhiemVu(TaiXeGiaoHang tx) {
        return Boolean.TRUE.equals(tx.getDangTrucTuyen())
                && "DANG_HOAT_DONG".equals(tx.getTrangThai());
    }

    @Transactional
    public TaiXeGiaoHang doiTrangThai(Long maTaiXe, CapNhatTrangThaiForm form) {
        TaiXeGiaoHang tx = taiXeRepository.findById(maTaiXe)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay shipper " + maTaiXe));
        kiemTraGps(form.getViDoHienTai(), form.getKinhDoHienTai());
        if (Boolean.TRUE.equals(form.getDangTrucTuyen())
                && !"DANG_HOAT_DONG".equals(tx.getTrangThai())) {
            throw new IllegalStateException("Tai khoan dang " + tx.getTrangThai()
                    + ", khong duoc phep Online. Lien he dieu phoi.");
        }
        tx.setDangTrucTuyen(form.getDangTrucTuyen());
        tx.setViDoHienTai(form.getViDoHienTai());
        tx.setKinhDoHienTai(form.getKinhDoHienTai());
        return taiXeRepository.save(tx);
    }

    private void kiemTraGps(BigDecimal viDo, BigDecimal kinhDo) {
        if (viDo == null || viDo.compareTo(new BigDecimal("-90")) < 0
                || viDo.compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException("Vi do GPS phai trong [-90, 90].");
        }
        if (kinhDo == null || kinhDo.compareTo(new BigDecimal("-180")) < 0
                || kinhDo.compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("Kinh do GPS phai trong [-180, 180].");
        }
    }

    // ================= US-35: Xem cuoc + nhan don =================

    /** Don san sang lay: DA_XAC_NHAN va chua co shipper nao nhan. */
    public List<DonHangShop> layDonChoNhan() {
        return layDonChoNhan(0, Integer.MAX_VALUE).getContent();
    }

    /** Don cho nhan co phan trang (US-35: 10 cuoc/trang). */
    public org.springframework.data.domain.Page<DonHangShop> layDonChoNhan(int page, int size) {
        List<DonHangShop> loc = donHangShopRepository.findAll().stream()
                .filter(d -> "DA_XAC_NHAN".equals(d.getTrangThai()))
                .filter(d -> !nhiemVuRepository.existsByDonHangShop_MaDonHangShop(d.getMaDonHangShop()))
                .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao()))
                .collect(java.util.stream.Collectors.toList());
        int tong = loc.size();
        int tu = Math.min(page * size, tong);
        int den = Math.min(tu + size, tong);
        return new org.springframework.data.domain.PageImpl<>(loc.subList(tu, den),
                org.springframework.data.domain.PageRequest.of(page, size), tong);
    }

    public List<NhiemVuGiaoHang> layCuocCuaToi(Long maTaiXe) {
        return nhiemVuRepository.findAllByTaiXe_MaTaiXeOrderByNgayTaoDesc(maTaiXe);
    }

    @Transactional
    public NhiemVuGiaoHang nhanCuoc(Long maTaiXe, Long maDonHangShop) {
        TaiXeGiaoHang tx = taiXeRepository.findById(maTaiXe)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay shipper " + maTaiXe));
        if (!duocNhanNhiemVu(tx)) {
            throw new IllegalStateException("Ban dang Offline hoac tai khoan khong DANG_HOAT_DONG nen khong duoc nhan don.");
        }
        DonHangShop don = donHangShopRepository.findById(maDonHangShop)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don " + maDonHangShop));
        if (!"DA_XAC_NHAN".equals(don.getTrangThai())) {
            throw new IllegalStateException("Don " + don.getMaCodeDonShop() + " dang " + don.getTrangThai() + ", khong the nhan.");
        }
        if (nhiemVuRepository.existsByDonHangShop_MaDonHangShop(maDonHangShop)) {
            throw new IllegalStateException("Don nay da co shipper khac nhan truoc.");
        }
        NhiemVuGiaoHang nv = new NhiemVuGiaoHang();
        nv.setDonHangShop(don);
        nv.setTaiXe(tx);
        nv.setLoaiNhiemVu("GIAO_HANG");
        nv.setTrangThai("DA_PHAN_CONG");
        nv.setTienCodCanThu(tinhCod(don));
        nv.setDaThuCod(false);
        nv.setSoLanGiao(1);
        nv = nhiemVuRepository.save(nv);
        // Auto-ghi hanh trinh US-33
        LichSuHanhTrinhDon moc = new LichSuHanhTrinhDon();
        moc.setDonHangShop(don);
        moc.setHub(null);
        moc.setTieuDeMoc("Shipper nhan don - cho lay hang");
        moc.setViTriHienTai(tx.getBienSoXe());
        moc.setThoiGian(java.time.LocalDateTime.now());
        hanhTrinhRepository.save(moc);
        return nv;
    }

    private BigDecimal tinhCod(DonHangShop don) {
        if (don.getDonHangTong() != null
                && "COD".equalsIgnoreCase(don.getDonHangTong().getPhuongThucThanhToan())) {
            return don.getTongTienShopNhan() != null ? don.getTongTienShopNhan() : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }
}
