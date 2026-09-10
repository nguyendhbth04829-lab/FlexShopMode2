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

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private com.example.demo.repository.YeuCauChuyenHoanRepository chuyenHoanRepository;

    public List<TaiXeGiaoHang> layTatCaShipper() {
        return taiXeRepository.findAll();
    }

    /** Tam: chua co login nen lay shipper dau tien de test tren mobile. */
    public TaiXeGiaoHang layShipperHienTai() {
        return taiXeRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Chưa có tài khoản shipper. Hãy chạy SQL US34."));
    }

    public boolean duocNhanNhiemVu(TaiXeGiaoHang tx) {
        return Boolean.TRUE.equals(tx.getDangTrucTuyen())
                && "DANG_HOAT_DONG".equals(tx.getTrangThai());
    }

    /** So cuoc dang lam do (DA_PHAN_CONG / DANG_GIAO) - de canh bao khi tat Online. */
    public long demCuocDangLam(Long maTaiXe) {
        return nhiemVuRepository.countByTaiXe_MaTaiXeAndTrangThaiIn(
                maTaiXe, java.util.List.of("DA_PHAN_CONG", "DANG_GIAO"));
    }

    @Transactional
    public TaiXeGiaoHang doiTrangThai(Long maTaiXe, CapNhatTrangThaiForm form) {
        TaiXeGiaoHang tx = taiXeRepository.findById(maTaiXe)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy shipper " + maTaiXe));
        kiemTraGps(form.getViDoHienTai(), form.getKinhDoHienTai());
        if (Boolean.TRUE.equals(form.getDangTrucTuyen())
                && !"DANG_HOAT_DONG".equals(tx.getTrangThai())) {
            throw new IllegalStateException("Tài khoản đang " + tx.getTrangThai()
                    + ", không được phép Online. Liên hệ điều phối.");
        }
        tx.setDangTrucTuyen(form.getDangTrucTuyen());
        tx.setViDoHienTai(form.getViDoHienTai());
        tx.setKinhDoHienTai(form.getKinhDoHienTai());
        return taiXeRepository.save(tx);
    }

    private void kiemTraGps(BigDecimal viDo, BigDecimal kinhDo) {
        if (viDo == null || viDo.compareTo(new BigDecimal("-90")) < 0
                || viDo.compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException("Vĩ độ GPS phải trong [-90, 90].");
        }
        if (kinhDo == null || kinhDo.compareTo(new BigDecimal("-180")) < 0
                || kinhDo.compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("Kinh độ GPS phải trong [-180, 180].");
        }
    }

    // ================= US-35: Xem cuoc + nhan don =================

    /** Don san sang lay: DA_XAC_NHAN va chua co shipper nao nhan. */
    public List<DonHangShop> layDonChoNhan() {
        return layDonChoNhan(0, Integer.MAX_VALUE).getContent();
    }

    /** Don cho nhan co phan trang (US-35: 10 cuoc/trang). */
    public org.springframework.data.domain.Page<DonHangShop> layDonChoNhan(int page, int size) {
        return layDonChoNhan(page, size, false);
    }

    /** Don cho nhan co phan trang + sap xep theo thoi gian (cuNhat=true: cu nhat truoc). */
    public org.springframework.data.domain.Page<DonHangShop> layDonChoNhan(int page, int size, boolean cuNhat) {
        java.util.Comparator<DonHangShop> ss = java.util.Comparator.comparing(DonHangShop::getNgayTao);
        if (!cuNhat) ss = ss.reversed();
        List<DonHangShop> loc = donHangShopRepository.findAll().stream()
                .filter(d -> "DA_XAC_NHAN".equals(d.getTrangThai()))
                .filter(d -> !nhiemVuRepository.existsByDonHangShop_MaDonHangShop(d.getMaDonHangShop()))
                .sorted(ss)
                .collect(java.util.stream.Collectors.toList());
        return catTrang(loc, page, size);
    }

    private <T> org.springframework.data.domain.Page<T> catTrang(List<T> loc, int page, int size) {
        int tong = loc.size();
        int tu = Math.min(Math.max(page, 0) * size, tong);
        int den = Math.min(tu + size, tong);
        return new org.springframework.data.domain.PageImpl<>(loc.subList(tu, den),
                org.springframework.data.domain.PageRequest.of(Math.max(page, 0), size), tong);
    }

    public List<NhiemVuGiaoHang> layCuocCuaToi(Long maTaiXe) {
        return nhiemVuRepository.findAllByTaiXe_MaTaiXeOrderByNgayTaoDesc(maTaiXe);
    }

    /** Cuoc cua toi co phan trang + sap xep theo thoi gian. */
    public org.springframework.data.domain.Page<NhiemVuGiaoHang> layCuocCuaToi(Long maTaiXe, int page, int size, boolean cuNhat) {
        List<NhiemVuGiaoHang> loc = new java.util.ArrayList<>(layCuocCuaToi(maTaiXe));
        loc.sort((a, b) -> cuNhat
                ? a.getNgayTao().compareTo(b.getNgayTao())
                : b.getNgayTao().compareTo(a.getNgayTao()));
        return catTrang(loc, page, size);
    }

    @Transactional
    public NhiemVuGiaoHang nhanCuoc(Long maTaiXe, Long maDonHangShop) {
        TaiXeGiaoHang tx = taiXeRepository.findById(maTaiXe)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy shipper " + maTaiXe));
        if (!duocNhanNhiemVu(tx)) {
            throw new IllegalStateException("Bạn đang Offline hoặc tài khoản không DANG_HOAT_DONG nên không được nhận đơn.");
        }
        DonHangShop don = donHangShopRepository.findById(maDonHangShop)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn " + maDonHangShop));
        if (!"DA_XAC_NHAN".equals(don.getTrangThai())) {
            throw new IllegalStateException("Đơn " + don.getMaCodeDonShop() + " đang " + don.getTrangThai() + ", không thể nhận.");
        }
        if (nhiemVuRepository.existsByDonHangShop_MaDonHangShop(maDonHangShop)) {
            throw new IllegalStateException("Đơn này đã có shipper khác nhận trước.");
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
        moc.setTieuDeMoc("Shipper nhận đơn - chờ lấy hàng");
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

    // ================= US-36: Xac nhan da lay hang =================

    @Transactional
    public NhiemVuGiaoHang xacNhanLayHang(Long maTaiXe, Long maNhiemVu) {
        NhiemVuGiaoHang nv = nhiemVuRepository.findById(maNhiemVu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ " + maNhiemVu));
        if (!nv.getTaiXe().getMaTaiXe().equals(maTaiXe)) {
            throw new IllegalStateException("Cuốc này không phải của bạn.");
        }
        if (!"DA_PHAN_CONG".equals(nv.getTrangThai())) {
            throw new IllegalStateException("Cuốc đang " + nv.getTrangThaiDisplay() + ", không thể xác nhận lấy hàng.");
        }
        DonHangShop don = nv.getDonHangShop();
        if (!"DA_XAC_NHAN".equals(don.getTrangThai())) {
            throw new IllegalStateException("Đơn " + don.getMaCodeDonShop() + " đang " + don.getTrangThai() + ", không thể lấy hàng.");
        }
        nv.setTrangThai("DANG_GIAO");
        nv.setThoiGianLayHang(java.time.LocalDateTime.now());
        nv = nhiemVuRepository.save(nv);
        don.setTrangThai("DANG_GIAO");
        donHangShopRepository.save(don);
        // Auto-ghi hanh trinh US-33
        LichSuHanhTrinhDon moc = new LichSuHanhTrinhDon();
        moc.setDonHangShop(don);
        moc.setHub(null);
        moc.setTieuDeMoc("Đã lấy hàng từ Shop - đang giao");
        moc.setViTriHienTai(don.getGianHang() != null ? don.getGianHang().getDiaChiKho() : null);
        moc.setThoiGian(java.time.LocalDateTime.now());
        hanhTrinhRepository.save(moc);
        return nv;
    }

    // ================= US-37: Xac nhan giao thanh cong (POD + COD) =================

    @Transactional
    public NhiemVuGiaoHang xacNhanGiaoThanhCong(Long maTaiXe, Long maNhiemVu,
                                                org.springframework.web.multipart.MultipartFile anhPod,
                                                java.math.BigDecimal viDo, java.math.BigDecimal kinhDo) {
        NhiemVuGiaoHang nv = nhiemVuRepository.findById(maNhiemVu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ " + maNhiemVu));
        if (!nv.getTaiXe().getMaTaiXe().equals(maTaiXe)) {
            throw new IllegalStateException("Cuốc này không phải của bạn.");
        }
        if (!"DANG_GIAO".equals(nv.getTrangThai())) {
            throw new IllegalStateException("Cuốc đang " + nv.getTrangThaiDisplay() + ", không thể xác nhận giao thành công.");
        }
        kiemTraGps(viDo, kinhDo);
        String linkAnh;
        try {
            linkAnh = fileStorageService.luuAnhPod(anhPod);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Lưu ảnh POD thất bại: " + e.getMessage());
        }
        TaiXeGiaoHang tx = nv.getTaiXe();
        nv.setLinkAnhBangChungPod(linkAnh);
        nv.setViDoGiaoHang(viDo);
        nv.setKinhDoGiaoHang(kinhDo);
        nv.setThoiGianGiaoThanhCong(java.time.LocalDateTime.now());
        nv.setTrangThai("THANH_CONG");
        // Tien COD: neu don COD thi bat buoc da_thu_cod=1 + cong don so_du_cod_dang_giu
        BigDecimal cod = nv.getTienCodCanThu() != null ? nv.getTienCodCanThu() : BigDecimal.ZERO;
        if (cod.compareTo(BigDecimal.ZERO) > 0) {
            nv.setDaThuCod(true);
            BigDecimal du = tx.getSoDuCodDangGiu() != null ? tx.getSoDuCodDangGiu() : BigDecimal.ZERO;
            tx.setSoDuCodDangGiu(du.add(cod));
            taiXeRepository.save(tx);
        }
        nv = nhiemVuRepository.save(nv);
        DonHangShop don = nv.getDonHangShop();
        don.setTrangThai("DA_GIAO");
        donHangShopRepository.save(don);
        // Auto-ghi hanh trinh US-33
        LichSuHanhTrinhDon moc = new LichSuHanhTrinhDon();
        moc.setDonHangShop(don);
        moc.setHub(null);
        moc.setTieuDeMoc("Giao hàng thành công");
        moc.setViTriHienTai(viDo + ", " + kinhDo);
        moc.setThoiGian(java.time.LocalDateTime.now());
        hanhTrinhRepository.save(moc);
        return nv;
    }

    // ================= US-38: Bao giao that bai + hen giao lai =================

    @Transactional
    public NhiemVuGiaoHang baoThatBai(Long maTaiXe, Long maNhiemVu,
                                      com.example.demo.dto.BaoThatBaiForm form) {
        NhiemVuGiaoHang nv = nhiemVuRepository.findById(maNhiemVu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ " + maNhiemVu));
        if (!nv.getTaiXe().getMaTaiXe().equals(maTaiXe)) {
            throw new IllegalStateException("Cuốc này không phải của bạn.");
        }
        if (!"DANG_GIAO".equals(nv.getTrangThai())) {
            throw new IllegalStateException("Cuốc đang " + nv.getTrangThaiDisplay() + ", không thể báo thất bại.");
        }
        int lan = nv.getSoLanGiao() != null ? nv.getSoLanGiao() : 1;
        if (lan >= 3) {
            // US-40: that bai lan 3 -> tu dong kich hoat chuyen hoan thay vi chan
            taoChuyenHoan(maTaiXe, maNhiemVu);
            return nhiemVuRepository.findById(maNhiemVu).orElse(nv);
        }
        if ("HEN_LAI".equals(form.getLyDoThatBai())) {
            if (form.getThoiGianHenGiaoLai() == null) {
                throw new IllegalArgumentException("Hẹn giao lại thì bắt buộc chọn thời gian hẹn.");
            }
            if (!form.getThoiGianHenGiaoLai().isAfter(java.time.LocalDateTime.now())) {
                throw new IllegalArgumentException("Thời gian hẹn phải trong tương lai.");
            }
            nv.setThoiGianHenGiaoLai(form.getThoiGianHenGiaoLai());
        } else {
            nv.setThoiGianHenGiaoLai(null);
        }
        nv.setLyDoThatBai(form.getLyDoThatBai());
        nv.setSoLanGiao(lan + 1);
        nv = nhiemVuRepository.save(nv);
        // Auto-ghi hanh trinh US-33 (don van DANG_GIAO, cho giao lai)
        DonHangShop don = nv.getDonHangShop();
        LichSuHanhTrinhDon moc = new LichSuHanhTrinhDon();
        moc.setDonHangShop(don);
        moc.setHub(null);
        moc.setTieuDeMoc("Giao thất bại lần " + lan + ": " + form.getLyDoThatBai());
        moc.setViTriHienTai(form.getThoiGianHenGiaoLai() != null
                && "HEN_LAI".equals(form.getLyDoThatBai())
                ? "Hẹn giao lại: " + form.getThoiGianHenGiaoLai() : null);
        moc.setThoiGian(java.time.LocalDateTime.now());
        hanhTrinhRepository.save(moc);
        return nv;
    }

    // ================= US-40: Chuyen hoan khi that bai 3 lan =================

    @Transactional
    public com.example.demo.entity.YeuCauChuyenHoan taoChuyenHoan(Long maTaiXe, Long maNhiemVu) {
        NhiemVuGiaoHang nv = nhiemVuRepository.findById(maNhiemVu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ " + maNhiemVu));
        if (!nv.getTaiXe().getMaTaiXe().equals(maTaiXe)) {
            throw new IllegalStateException("Cuốc này không phải của bạn.");
        }
        DonHangShop don = nv.getDonHangShop();
        if (chuyenHoanRepository.existsByDonHangShop_MaDonHangShop(don.getMaDonHangShop())) {
            return chuyenHoanRepository.findByDonHangShop_MaDonHangShop(don.getMaDonHangShop()).orElseThrow();
        }
        int lan = nv.getSoLanGiao() != null ? nv.getSoLanGiao() : 1;
        String lyDo = "Giao thất bại " + Math.min(lan, 3) + "/3 lần"
                + (nv.getLyDoThatBai() != null ? " (" + nv.getLyDoThatBai() + ")" : "");
        com.example.demo.entity.YeuCauChuyenHoan yc = new com.example.demo.entity.YeuCauChuyenHoan();
        yc.setDonHangShop(don);
        yc.setLyDoChuyenHoan(lyDo);
        yc.setMaVanDonTraHang("TH-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + String.format("%06d", don.getMaDonHangShop()));
        yc.setTrangThai("DANG_CHUYEN_HOAN");
        yc = chuyenHoanRepository.save(yc);
        nv.setTrangThai("CHUYEN_HOAN");
        nhiemVuRepository.save(nv);
        don.setTrangThai("GIAO_THAT_BAI");
        donHangShopRepository.save(don);
        // Auto-ghi hanh trinh US-33
        LichSuHanhTrinhDon moc = new LichSuHanhTrinhDon();
        moc.setDonHangShop(don);
        moc.setHub(null);
        moc.setTieuDeMoc("Chuyển hoàn về Shop");
        moc.setViTriHienTai(yc.getMaVanDonTraHang());
        moc.setThoiGian(java.time.LocalDateTime.now());
        hanhTrinhRepository.save(moc);
        return yc;
    }

    public java.util.Optional<com.example.demo.entity.YeuCauChuyenHoan> layChuyenHoan(Long maDonHangShop) {
        return chuyenHoanRepository.findByDonHangShop_MaDonHangShop(maDonHangShop);
    }

    // ================= US-39: Lich su + thong ke COD THEO NGAY =================

    public com.example.demo.dto.ThongKeShipperDTO thongKeShipper(Long maTaiXe, java.time.LocalDate ngay) {
        if (ngay == null) ngay = java.time.LocalDate.now();
        final java.time.LocalDate locNgay = ngay;
        List<NhiemVuGiaoHang> list = layCuocCuaToi(maTaiXe).stream()
                .filter(n -> n.getNgayTao() != null && n.getNgayTao().toLocalDate().equals(locNgay))
                .collect(java.util.stream.Collectors.toList());
        long thanhCong = list.stream().filter(n -> "THANH_CONG".equals(n.getTrangThai())).count();
        long dangGiao = list.stream().filter(n -> "DANG_GIAO".equals(n.getTrangThai())).count();
        long choLay = list.stream().filter(n -> "DA_PHAN_CONG".equals(n.getTrangThai())).count();
        long luotFail = list.stream().filter(n -> n.getLyDoThatBai() != null && !n.getLyDoThatBai().isBlank()).count();
        BigDecimal daThu = list.stream()
                .filter(n -> Boolean.TRUE.equals(n.getDaThuCod()) && n.getTienCodCanThu() != null)
                .map(NhiemVuGiaoHang::getTienCodCanThu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        TaiXeGiaoHang tx = taiXeRepository.findById(maTaiXe)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy shipper " + maTaiXe));
        BigDecimal du = tx.getSoDuCodDangGiu() != null ? tx.getSoDuCodDangGiu() : BigDecimal.ZERO;
        return new com.example.demo.dto.ThongKeShipperDTO(
                list.size(), thanhCong, dangGiao, choLay, luotFail, du, daThu,
                thanhCong, daThu);
    }

    /** Lich su cuoc trong 1 ngay (US-39). */
    public List<NhiemVuGiaoHang> layCuocTrongNgay(Long maTaiXe, java.time.LocalDate ngay) {
        if (ngay == null) ngay = java.time.LocalDate.now();
        final java.time.LocalDate locNgay = ngay;
        return layCuocCuaToi(maTaiXe).stream()
                .filter(n -> n.getNgayTao() != null && n.getNgayTao().toLocalDate().equals(locNgay))
                .collect(java.util.stream.Collectors.toList());
    }
}
