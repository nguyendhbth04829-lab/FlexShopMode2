package com.example.demo.service;

import com.example.demo.dto.ComboKhuyenMaiForm;
import com.example.demo.dto.DealSocMuaKemDTO;
import com.example.demo.dto.ThemSanPhamComboForm;
import com.example.demo.dto.ThongKeComboDTO;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.ComboKhuyenMai;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.SanPhamComboKhuyenMai;
import com.example.demo.repository.BienTheSanPhamRepository;
import com.example.demo.repository.ComboKhuyenMaiRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.SanPhamComboKhuyenMaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service nghiệp vụ Quản lý Combo Khuyến Mãi & Mua Kèm Deal Sốc (US-54 - PROMOTION)
 */
@Service
public class ComboKhuyenMaiService {

    @Autowired
    private ComboKhuyenMaiRepository comboKhuyenMaiRepository;

    @Autowired
    private SanPhamComboKhuyenMaiRepository sanPhamComboKhuyenMaiRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    // =========================================================================
    // 1. QUẢN LÝ CHƯƠNG TRÌNH COMBO (DÀNH CHO SELLER)
    // =========================================================================

    /**
     * Tạo mới chương trình Combo & Mua Kèm Deal Sốc với validate chuyên nghiệp
     */
    @Transactional
    public ComboKhuyenMai taoCombo(ComboKhuyenMaiForm form, Long maGianHang) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu chương trình khuyến mãi không được để trống!");
        }

        GianHang gianHang = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gian hàng #" + maGianHang));

        LocalDateTime start = form.getThoiGianBatDau();
        LocalDateTime end = form.getThoiGianKetThuc();

        if (start == null || end == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống!");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu!");
        }

        long durationMinutes = Duration.between(start, end).toMinutes();
        if (durationMinutes < 30) {
            throw new IllegalArgumentException("Chương trình khuyến mãi phải kéo dài tối thiểu 30 phút!");
        }

        if (form.getGiaTriGiam() == null || form.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Mức giảm giá ưu đãi phải lớn hơn 0!");
        }

        ComboKhuyenMai combo = new ComboKhuyenMai();
        combo.setGianHang(gianHang);
        combo.setTenCombo(form.getTenCombo().trim());
        combo.setLoaiCombo(form.getLoaiCombo() != null ? form.getLoaiCombo().trim() : "DEAL_SOC_MUA_KEM");
        combo.setSoLuongToiThieu(form.getSoLuongToiThieu() != null ? form.getSoLuongToiThieu() : 1);
        combo.setGiaTriGiam(form.getGiaTriGiam());
        combo.setNgayBatDau(start);
        combo.setNgayKetThuc(end);
        combo.setDangHoatDong(form.getDangHoatDong() != null ? form.getDangHoatDong() : true);

        return comboKhuyenMaiRepository.save(combo);
    }

    /**
     * Cập nhật thông tin chương trình Combo
     */
    @Transactional
    public ComboKhuyenMai capNhatCombo(Long maCombo, ComboKhuyenMaiForm form, Long maGianHang) {
        ComboKhuyenMai combo = layChiTietCombo(maCombo, maGianHang);

        LocalDateTime start = form.getThoiGianBatDau();
        LocalDateTime end = form.getThoiGianKetThuc();

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu!");
        }

        combo.setTenCombo(form.getTenCombo().trim());
        combo.setLoaiCombo(form.getLoaiCombo());
        combo.setSoLuongToiThieu(form.getSoLuongToiThieu());
        combo.setGiaTriGiam(form.getGiaTriGiam());
        combo.setNgayBatDau(start);
        combo.setNgayKetThuc(end);
        combo.setDangHoatDong(form.getDangHoatDong());

        return comboKhuyenMaiRepository.save(combo);
    }

    /**
     * Gán sản phẩm vào Combo (Sản phẩm chính A hoặc Phụ kiện B giảm giá 50%)
     */
    @Transactional
    public SanPhamComboKhuyenMai themSanPhamVaoCombo(ThemSanPhamComboForm form, Long maGianHang) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu gán sản phẩm không được để trống!");
        }

        ComboKhuyenMai combo = layChiTietCombo(form.getMaCombo(), maGianHang);

        BienTheSanPham bienThe = bienTheSanPhamRepository.findById(form.getMaBienThe())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm #" + form.getMaBienThe()));

        // Bảo mật nghiệp vụ: Sản phẩm bắt buộc phải thuộc quyền sở hữu của Gian Hàng
        if (bienThe.getSanPham() == null || bienThe.getSanPham().getGianHang() == null
                || !bienThe.getSanPham().getGianHang().getMaGianHang().equals(maGianHang)) {
            throw new IllegalArgumentException("Bạn chỉ được phép cấu hình ưu đãi cho sản phẩm thuộc gian hàng của mình!");
        }

        // Chặn trùng lặp sản phẩm trong cùng 1 combo
        boolean exists = sanPhamComboKhuyenMaiRepository.existsByComboKhuyenMaiMaComboAndBienTheSanPhamMaBienThe(
                combo.getMaCombo(), bienThe.getMaBienThe()
        );
        if (exists) {
            throw new IllegalArgumentException("Sản phẩm '" + bienThe.getTenBienThe() + "' đã có mặt trong chương trình này rồi!");
        }

        SanPhamComboKhuyenMai item = new SanPhamComboKhuyenMai();
        item.setComboKhuyenMai(combo);
        item.setBienTheSanPham(bienThe);
        String vaiTro = (form.getVaiTro() != null && !form.getVaiTro().isBlank()) ? form.getVaiTro().trim() : "MUA_KEM_DEAL_SOC";
        item.setVaiTro(vaiTro);

        BigDecimal giaGoc = bienThe.getGiaBan() != null ? bienThe.getGiaBan() : BigDecimal.ZERO;

        if ("MUA_KEM_DEAL_SOC".equalsIgnoreCase(vaiTro)) {
            BigDecimal phanTram = form.getPhanTramGiam();
            BigDecimal giaUuDai = form.getGiaUuDai();

            // Nếu người dùng nhập giá ưu đãi trực tiếp
            if (giaUuDai != null && giaUuDai.compareTo(BigDecimal.ZERO) > 0) {
                if (giaUuDai.compareTo(giaGoc) >= 0) {
                    throw new IllegalArgumentException("Giá ưu đãi mua kèm (" + dinhDangTien(giaUuDai) +
                            ") phải nhỏ hơn giá niêm yết hiện tại (" + dinhDangTien(giaGoc) + ")!");
                }
                item.setGiaUuDai(giaUuDai);
                // Tính ngược lại % giảm
                BigDecimal diff = giaGoc.subtract(giaUuDai);
                BigDecimal pct = diff.multiply(new BigDecimal(100)).divide(giaGoc, 2, RoundingMode.HALF_UP);
                item.setPhanTramGiam(pct);
            } else {
                // Áp dụng theo % giảm (mặc định 50%)
                if (phanTram == null || phanTram.compareTo(BigDecimal.ZERO) <= 0 || phanTram.compareTo(new BigDecimal(100)) >= 0) {
                    phanTram = combo.getGiaTriGiam() != null ? combo.getGiaTriGiam() : new BigDecimal("50.00");
                }
                item.setPhanTramGiam(phanTram);
                BigDecimal heSo = new BigDecimal(100).subtract(phanTram).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
                BigDecimal calculated = giaGoc.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
                item.setGiaUuDai(calculated);
            }

            item.setGioiHanMuaKemMoiDon(form.getGioiHanMuaKemMoiDon() != null ? form.getGioiHanMuaKemMoiDon() : 1);
            item.setSoLuongToiDa(form.getSoLuongToiDa() != null ? form.getSoLuongToiDa() : 100);
            item.setSoLuongDaBan(0);
        } else {
            // Sản phẩm chính: Bán theo giá niêm yết chuẩn
            item.setPhanTramGiam(BigDecimal.ZERO);
            item.setGiaUuDai(giaGoc);
            item.setGioiHanMuaKemMoiDon(1);
            item.setSoLuongToiDa(9999);
            item.setSoLuongDaBan(0);
        }

        return sanPhamComboKhuyenMaiRepository.save(item);
    }

    /**
     * Gỡ bỏ sản phẩm khỏi combo
     */
    @Transactional
    public void xoaSanPhamKhoiCombo(Long maSanPhamCombo, Long maGianHang) {
        SanPhamComboKhuyenMai sp = sanPhamComboKhuyenMaiRepository.findById(maSanPhamCombo)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong combo #" + maSanPhamCombo));

        if (sp.getComboKhuyenMai() == null || sp.getComboKhuyenMai().getGianHang() == null
                || !sp.getComboKhuyenMai().getGianHang().getMaGianHang().equals(maGianHang)) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên sản phẩm của gian hàng khác!");
        }

        sanPhamComboKhuyenMaiRepository.delete(sp);
    }

    /**
     * Bật / Tắt trạng thái chương trình
     */
    @Transactional
    public void doiTrangThaiCombo(Long maCombo, Long maGianHang) {
        ComboKhuyenMai combo = layChiTietCombo(maCombo, maGianHang);
        combo.setDangHoatDong(!Boolean.TRUE.equals(combo.getDangHoatDong()));
        comboKhuyenMaiRepository.save(combo);
    }

    /**
     * Xóa hoàn toàn chương trình combo
     */
    @Transactional
    public void xoaCombo(Long maCombo, Long maGianHang) {
        ComboKhuyenMai combo = layChiTietCombo(maCombo, maGianHang);
        comboKhuyenMaiRepository.delete(combo);
    }

    /**
     * Lấy danh sách Combo phân trang, lọc và tìm kiếm cho Seller
     */
    @Transactional(readOnly = true)
    public Page<ComboKhuyenMai> layDanhSachCombo(Long maGianHang, String loaiCombo, String trangThai, String tuKhoa, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        String loai = (loaiCombo != null && !loaiCombo.isBlank()) ? loaiCombo : "TAT_CA";
        String status = (trangThai != null && !trangThai.isBlank()) ? trangThai : "TAT_CA";
        return comboKhuyenMaiRepository.timKiemCombo(maGianHang, loai, status, tuKhoa, LocalDateTime.now(), pageable);
    }

    /**
     * Lấy chi tiết Combo và kiểm tra quyền sở hữu
     */
    @Transactional(readOnly = true)
    public ComboKhuyenMai layChiTietCombo(Long maCombo, Long maGianHang) {
        ComboKhuyenMai combo = comboKhuyenMaiRepository.findById(maCombo)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi #" + maCombo));

        if (maGianHang != null && (combo.getGianHang() == null || !combo.getGianHang().getMaGianHang().equals(maGianHang))) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập chương trình khuyến mãi của gian hàng khác!");
        }

        return combo;
    }

    /**
     * Lấy thống kê KPI chương trình cho Dashboard Seller
     */
    @Transactional(readOnly = true)
    public ThongKeComboDTO layThongKeCombo(Long maGianHang) {
        LocalDateTime now = LocalDateTime.now();
        long tongSoCombo = comboKhuyenMaiRepository.countByGianHangMaGianHang(maGianHang);
        long dangChay = comboKhuyenMaiRepository.countDangChay(maGianHang, now);
        long sapChay = comboKhuyenMaiRepository.countSapChay(maGianHang, now);
        long tongSpThamGia = sanPhamComboKhuyenMaiRepository.countSanPhamThamGiaTheoShop(maGianHang);
        long tongDaBan = sanPhamComboKhuyenMaiRepository.tinhTongSoLuongDaBanTheoShop(maGianHang);
        BigDecimal doanhThu = sanPhamComboKhuyenMaiRepository.tinhTongDoanhThuComboTheoShop(maGianHang);

        return ThongKeComboDTO.builder()
                .tongSoCombo(tongSoCombo)
                .soComboDangChay(dangChay)
                .soComboSapChay(sapChay)
                .tongSanPhamThamGia(tongSpThamGia)
                .tongLuotBanUuDai(tongDaBan)
                .tongDoanhThuCombo(doanhThu)
                .build();
    }

    // =========================================================================
    // 2. TÍNH TOÁN & HIỂN THỊ DÀNH CHO NGƯỜI MUA (SHOPPING & CHECKOUT)
    // =========================================================================

    /**
     * Lấy danh sách phụ kiện B giảm giá 50% mua kèm sản phẩm chính A
     */
    @Transactional(readOnly = true)
    public List<DealSocMuaKemDTO> layDanhSachDealSocChoKhachHang(Long maBienTheChinh) {
        List<SanPhamComboKhuyenMai> dsPhuKien = sanPhamComboKhuyenMaiRepository.timPhuKienMuaKemTheoSanPhamChinh(
                maBienTheChinh, LocalDateTime.now()
        );

        return dsPhuKien.stream().map(sp -> {
            BienTheSanPham bt = sp.getBienTheSanPham();
            String tenSp = (bt.getSanPham() != null) ? bt.getSanPham().getTenSanPham() : "";
            int conLai = (sp.getSoLuongToiDa() != null ? sp.getSoLuongToiDa() : 0) - (sp.getSoLuongDaBan() != null ? sp.getSoLuongDaBan() : 0);

            return DealSocMuaKemDTO.builder()
                    .maCombo(sp.getComboKhuyenMai().getMaCombo())
                    .tenCombo(sp.getComboKhuyenMai().getTenCombo())
                    .maSanPhamCombo(sp.getMaSanPhamCombo())
                    .maBienThe(bt.getMaBienThe())
                    .tenSanPham(tenSp)
                    .tenBienThe(bt.getTenBienThe())
                    .linkAnh(bt.getLinkAnh() != null ? bt.getLinkAnh() : "/images/product-placeholder.jpg")
                    .giaGoc(sp.getGiaGoc())
                    .giaUuDai(sp.getGiaUuDai())
                    .phanTramGiam(sp.getPhanTramGiam())
                    .tietKiem(sp.getTietKiem())
                    .gioiHanMuaKemMoiDon(sp.getGioiHanMuaKemMoiDon())
                    .conLai(Math.max(0, conLai))
                    .isChayHang(sp.isChayHang())
                    .build();
        }).toList();
    }

    /**
     * Lấy danh sách tất cả các combo đang hoạt động để hiển thị trên trang khám phá
     */
    @Transactional(readOnly = true)
    public List<ComboKhuyenMai> layDanhSachComboDangChay() {
        return comboKhuyenMaiRepository.timComboDangChay(LocalDateTime.now());
    }

    public String dinhDangTien(BigDecimal tien) {
        if (tien == null) return "0 đ";
        DecimalFormat df = new DecimalFormat("###,###,### đ");
        return df.format(tien);
    }
}