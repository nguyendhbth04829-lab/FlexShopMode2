package com.example.demo.service;

import com.example.demo.dto.CapNhatTrangThaiForm;
import com.example.demo.entity.TaiXeGiaoHang;
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
}
