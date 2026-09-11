package com.example.demo.service;

import com.example.demo.dto.ChiTietPhieuKhoForm;
import com.example.demo.dto.PhieuNhapXuatKhoForm;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class KhoHangService {

    @Autowired private KhoHangRepository khoHangRepository;
    @Autowired private PhieuNhapXuatKhoRepository phieuKhoRepository;
    @Autowired private ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    @Autowired private TonKhoChiTietRepository tonKhoRepository;
    @Autowired private BienTheSanPhamRepository bienTheRepository;

    @Transactional
    public PhieuNhapXuatKho taoPhieuNhapXuatKho(PhieuNhapXuatKhoForm form, Long nguoiLapPhieu) {
        // 1. Validate tổng số lượng chi tiết phiếu > 0
        int tongSoLuong = form.getChiTietList().stream()
                .mapToInt(ChiTietPhieuKhoForm::getSoLuong)
                .sum();
        if (tongSoLuong <= 0) {
            throw new RuntimeException("Tổng số lượng trong chi tiết phiếu kho phải lớn hơn 0");
        }

        KhoHang kho = khoHangRepository.findById(form.getMaKho())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho hàng"));

        // 2. Tạo Phiếu
        PhieuNhapXuatKho phieu = new PhieuNhapXuatKho();
        phieu.setKhoHang(kho);
        phieu.setLoaiPhieu(form.getLoaiPhieu()); // "NHAP_KHO" hoặc "XUAT_KHO"
        phieu.setMaChungTuLienQuan(form.getMaChungTuLienQuan());
        phieu.setGhiChu(form.getGhiChu());
        phieu.setNguoiLapPhieu(nguoiLapPhieu);

        PhieuNhapXuatKho phieuDaLuu = phieuKhoRepository.save(phieu);

        // 3. Xử lý Chi Tiết và Cập nhật Tồn kho (với Optimistic Locking)
        for (ChiTietPhieuKhoForm ctForm : form.getChiTietList()) {
            BienTheSanPham bienThe = bienTheRepository.findById(ctForm.getMaBienThe())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể SKU: " + ctForm.getMaBienThe()));

            // Lưu chi tiết phiếu
            ChiTietPhieuKho ct = new ChiTietPhieuKho();
            ct.setPhieuKho(phieuDaLuu);
            ct.setBienThe(bienThe);
            ct.setSoLuong(ctForm.getSoLuong());
            if (ctForm.getDonGiaVon() != null) {
                ct.setDonGiaVon(ctForm.getDonGiaVon());
            }
            chiTietPhieuKhoRepository.save(ct);

            // Cập nhật tồn kho (Tự động tăng phien_ban_lock nhờ @Version bên Entity)
            Optional<TonKhoChiTiet> tonKhoOpt = tonKhoRepository.findByKhoHang_MaKhoAndBienThe_MaBienThe(kho.getMaKho(), bienThe.getMaBienThe());
            TonKhoChiTiet tonKho;

            if (tonKhoOpt.isPresent()) {
                tonKho = tonKhoOpt.get();
            } else {
                if (form.getLoaiPhieu().equals("XUAT_KHO")) {
                    throw new RuntimeException("Sản phẩm chưa từng nhập kho, không thể xuất.");
                }
                tonKho = new TonKhoChiTiet();
                tonKho.setKhoHang(kho);
                tonKho.setBienThe(bienThe);
            }

            if (form.getLoaiPhieu().equals("NHAP_KHO")) {
                tonKho.setSoLuongTon(tonKho.getSoLuongTon() + ctForm.getSoLuong());
            } else if (form.getLoaiPhieu().equals("XUAT_KHO")) {
                // Validate US-15: Tồn kho >= 0
                if (tonKho.getSoLuongKhaDung() < ctForm.getSoLuong()) {
                    throw new RuntimeException("Số lượng khả dụng không đủ để xuất cho biến thể: " + bienThe.getMaSku());
                }
                tonKho.setSoLuongTon(tonKho.getSoLuongTon() - ctForm.getSoLuong());
            }

            // In ra cảnh báo nếu tồn kho <= 5 (US-15)
            if (tonKho.getSoLuongTon() <= 5) {
                System.out.println("CẢNH BÁO: Tồn kho an toàn đã chạm mức thấp (<=5) cho mã SKU: " + bienThe.getMaSku());
            }

            tonKhoRepository.save(tonKho);
        }

        return phieuDaLuu;
    }

    public java.util.List<KhoHang> layDanhSachKho(Long maGianHang) {
        return khoHangRepository.findByMaGianHangAndDaXoaFalse(maGianHang);
    }

    @Transactional
    public KhoHang themKho(com.example.demo.dto.KhoHangForm form, Long maGianHang) {
        KhoHang kho = new KhoHang();
        kho.setMaGianHang(maGianHang);
        kho.setTenKho(form.getTenKho());
        kho.setDiaChi(form.getDiaChi());
        kho.setTinhThanh(form.getTinhThanh());
        kho.setSdtLienHe(form.getSdtLienHe());
        kho.setLaKhoChinh(form.getLaKhoChinh() != null ? form.getLaKhoChinh() : false);
        kho.setDangHoatDong(form.getDangHoatDong() != null ? form.getDangHoatDong() : true);
        return khoHangRepository.save(kho);
    }
}
