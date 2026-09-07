package com.example.demo.service;

import com.example.demo.config.LivestreamWebSocketHandler;
import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service nghiệp vụ Livestream bán hàng & Ghim sản phẩm giảm giá sốc (US-61)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LivestreamService {

    private final PhongLivestreamRepository phongLivestreamRepository;
    private final SanPhamLivestreamRepository sanPhamLivestreamRepository;
    private final BinhLuanLivestreamRepository binhLuanLivestreamRepository;
    private final SanPhamRepository sanPhamRepository;
    private final GianHangRepository gianHangRepository;
    private final LivestreamWebSocketHandler webSocketHandler;

    /**
     * Tìm kiếm phòng livestream dành cho Seller có bộ lọc và phân trang
     */
    public Page<PhongLivestream> timKiemSeller(Long maGianHang, String trangThai, String tuKhoa, Pageable pageable) {
        return phongLivestreamRepository.timKiemSeller(maGianHang, trangThai, tuKhoa, pageable);
    }

    /**
     * Khách hàng khám phá các phòng đang phát trực tiếp hoặc sắp diễn ra
     */
    public Page<PhongLivestream> timKiemKhamPha(String tuKhoa, Pageable pageable) {
        return phongLivestreamRepository.timKiemKhamPha(tuKhoa, pageable);
    }

    /**
     * Lấy thông tin chi tiết phòng livestream
     */
    public PhongLivestream layPhongLive(Long maLive) {
        return phongLivestreamRepository.findById(maLive)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng Livestream với mã: " + maLive));
    }

    /**
     * Thống kê tổng quan hoạt động Livestream của Seller
     */
    public ThongKeLivestreamDTO layThongKeSeller(Long maGianHang) {
        long tongSoPhien = phongLivestreamRepository.countByGianHang_MaGianHang(maGianHang);
        long dangLive = phongLivestreamRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "DANG_LIVE");
        long sapDienRa = phongLivestreamRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "SAP_DIEN_RA");
        long daKetThuc = phongLivestreamRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "DA_KET_THUC");
        long tongLuotXem = phongLivestreamRepository.tongLuotXemCuaShop(maGianHang);

        // Lấy danh sách các phòng của shop để tính tổng tương tác
        List<PhongLivestream> danhSachPhong = phongLivestreamRepository.findByGianHang_MaGianHangOrderByThoiGianBatDauDesc(maGianHang, Pageable.unpaged()).getContent();
        long tongTim = danhSachPhong.stream().mapToLong(p -> p.getSoLuotThich() != null ? p.getSoLuotThich() : 0).sum();

        long tongBinhLuan = 0;
        long tongDonHangLive = 0;
        BigDecimal tongDoanhThuLive = BigDecimal.ZERO;

        for (PhongLivestream phong : danhSachPhong) {
            tongBinhLuan += binhLuanLivestreamRepository.countByPhongLivestream_MaLive(phong.getMaLive());
            List<SanPhamLivestream> spLives = sanPhamLivestreamRepository.findByPhongLivestream_MaLiveOrderByThuTuHienThiAsc(phong.getMaLive());
            for (SanPhamLivestream sp : spLives) {
                int daBan = sp.getSoLuongDaBan() != null ? sp.getSoLuongDaBan() : 0;
                tongDonHangLive += daBan;
                if (sp.getGiaDocQuyenLive() != null) {
                    tongDoanhThuLive = tongDoanhThuLive.add(sp.getGiaDocQuyenLive().multiply(BigDecimal.valueOf(daBan)));
                }
            }
        }

        return ThongKeLivestreamDTO.builder()
                .tongSoPhienLive(tongSoPhien)
                .soPhienDangLive(dangLive)
                .soPhienSapDienRa(sapDienRa)
                .soPhienDaKetThuc(daKetThuc)
                .tongLuotXem(tongLuotXem)
                .tongLuotThich(tongTim)
                .tongBinhLuan(tongBinhLuan)
                .tongDonHangLive(tongDonHangLive)
                .tongDoanhThuLive(tongDoanhThuLive)
                .build();
    }

    /**
     * Tạo mới phiên phát Livestream (Seller)
     */
    @Transactional
    public PhongLivestream taoPhongLive(TaoPhongLiveForm form, NguoiDung seller) {
        GianHang gianHang = gianHangRepository.findByChuSoHuu_MaNguoiDung(seller.getMaNguoiDung())
                .orElseThrow(() -> new IllegalStateException("Người dùng chưa sở hữu gian hàng trên hệ thống"));

        String linkStream = (form.getLinkStreamRtmp() != null && !form.getLinkStreamRtmp().isBlank())
                ? form.getLinkStreamRtmp()
                : "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

        String linkAnhBia = (form.getLinkAnhBia() != null && !form.getLinkAnhBia().isBlank())
                ? form.getLinkAnhBia()
                : "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600";

        PhongLivestream phongLive = PhongLivestream.builder()
                .gianHang(gianHang)
                .tieuDe(form.getTieuDe())
                .linkStreamRtmp(linkStream)
                .linkAnhBia(linkAnhBia)
                .moTa(form.getMoTa())
                .trangThai("DANG_LIVE")
                .thoiGianBatDau(form.getThoiGianBatDau() != null ? form.getThoiGianBatDau() : LocalDateTime.now())
                .tongLuotXem(1)
                .soNguoiXemHienTai(1)
                .soLuotThich(0)
                .build();

        phongLive = phongLivestreamRepository.save(phongLive);

        // Thêm các sản phẩm được chọn vào phòng livestream
        if (form.getDanhSachMaSanPham() != null && !form.getDanhSachMaSanPham().isEmpty()) {
            int thuTu = 1;
            for (Long maSp : form.getDanhSachMaSanPham()) {
                Optional<SanPham> optSp = sanPhamRepository.findById(maSp);
                if (optSp.isPresent()) {
                    SanPham sp = optSp.get();
                    BigDecimal giaLive = (form.getMapGiaDocQuyen() != null && form.getMapGiaDocQuyen().containsKey(maSp))
                            ? form.getMapGiaDocQuyen().get(maSp)
                            : sp.getGiaCoBan().multiply(BigDecimal.valueOf(0.8)); // Mặc định giảm 20% trên live

                    Integer gioiHan = (form.getMapSoLuongGioiHan() != null && form.getMapSoLuongGioiHan().containsKey(maSp))
                            ? form.getMapSoLuongGioiHan().get(maSp)
                            : 50;

                    boolean laGhim = (thuTu == 1); // Ghim sản phẩm đầu tiên

                    SanPhamLivestream spLive = SanPhamLivestream.builder()
                            .phongLivestream(phongLive)
                            .sanPham(sp)
                            .giaDocQuyenLive(giaLive)
                            .thuTuHienThi(thuTu)
                            .laSanPhamDangGhim(laGhim)
                            .soLuongGioiHan(gioiHan)
                            .soLuongDaBan(0)
                            .build();

                    sanPhamLivestreamRepository.save(spLive);
                    thuTu++;
                }
            }
        }

        // Tạo bình luận chào mừng từ hệ thống
        BinhLuanLivestream blHeThong = BinhLuanLivestream.builder()
                .phongLivestream(phongLive)
                .nguoiDung(seller)
                .hoTenNguoiDung("Hệ thống FlexShop Live")
                .noiDung("Chào mừng bạn đến với phiên phát trực tiếp của " + gianHang.getTenGianHang() + "! Hãy thả tim và săn deal độc quyền nhé!")
                .laTinHeThong(true)
                .laNguoiBan(false)
                .build();
        binhLuanLivestreamRepository.save(blHeThong);

        log.info("Đã tạo phiên Livestream mới: maLive={}, tieuDe={}", phongLive.getMaLive(), phongLive.getTieuDe());
        return phongLive;
    }

    /**
     * Cập nhật thông tin phòng live
     */
    @Transactional
    public PhongLivestream capNhatPhongLive(Long maLive, TaoPhongLiveForm form) {
        PhongLivestream phong = layPhongLive(maLive);
        phong.setTieuDe(form.getTieuDe());
        if (form.getMoTa() != null) phong.setMoTa(form.getMoTa());
        if (form.getLinkAnhBia() != null && !form.getLinkAnhBia().isBlank()) phong.setLinkAnhBia(form.getLinkAnhBia());
        if (form.getLinkStreamRtmp() != null && !form.getLinkStreamRtmp().isBlank()) phong.setLinkStreamRtmp(form.getLinkStreamRtmp());
        return phongLivestreamRepository.save(phong);
    }

    /**
     * Thay đổi trạng thái phòng (DANG_LIVE, DA_KET_THUC, SAP_DIEN_RA)
     */
    @Transactional
    public PhongLivestream chuyenTrangThai(Long maLive, String trangThaiMoi) {
        PhongLivestream phong = layPhongLive(maLive);
        phong.setTrangThai(trangThaiMoi);
        if ("DANG_LIVE".equalsIgnoreCase(trangThaiMoi) && phong.getThoiGianBatDau() == null) {
            phong.setThoiGianBatDau(LocalDateTime.now());
        } else if ("DA_KET_THUC".equalsIgnoreCase(trangThaiMoi)) {
            phong.setThoiGianKetThuc(LocalDateTime.now());
            // Bỏ ghim tất cả sản phẩm khi kết thúc live
            sanPhamLivestreamRepository.boGhimTatCaTrongPhong(maLive);
        }
        phong = phongLivestreamRepository.save(phong);

        // Phát sóng WebSocket thông báo trạng thái tới người xem
        webSocketHandler.broadcast(maLive, ThongDiepLiveWebSocket.builder()
                .loai("TRANG_THAI_LIVE")
                .maLive(maLive)
                .duLieu(Map.of("trangThai", trangThaiMoi, "thongBao", "Buổi livestream " + phong.getTrangThaiHienThi().toLowerCase()))
                .build());

        return phong;
    }

    /**
     * Lấy danh sách sản phẩm trong phòng livestream
     */
    public List<SanPhamLivestream> layDanhSachSanPhamLive(Long maLive) {
        return sanPhamLivestreamRepository.findByPhongLivestream_MaLiveOrderByThuTuHienThiAsc(maLive);
    }

    /**
     * Chuyển danh sách sản phẩm sang DTO có tính toán giảm giá & hình ảnh
     */
    public List<SanPhamGhimDTO> layDanhSachSanPhamLiveDTO(Long maLive) {
        List<SanPhamLivestream> list = layDanhSachSanPhamLive(maLive);
        return list.stream().map(this::chuyenSangDTO).collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm đang được ghim trong phòng live
     */
    public Optional<SanPhamLivestream> laySanPhamDangGhim(Long maLive) {
        return sanPhamLivestreamRepository.findFirstByPhongLivestream_MaLiveAndLaSanPhamDangGhimTrue(maLive);
    }

    public Optional<SanPhamGhimDTO> laySanPhamDangGhimDTO(Long maLive) {
        return laySanPhamDangGhim(maLive).map(this::chuyenSangDTO);
    }

    /**
     * GHIM SẢN PHẨM GIẢM GIÁ SỐC LÊN MÀN HÌNH (Chức năng cốt lõi US-61)
     * - Bỏ ghim tất cả sản phẩm khác trong phòng live
     * - Ghim sản phẩm được chọn
     * - Phát sóng WebSocket thời gian thực tới tất cả khán giả ngay lập tức!
     * - Tự động thêm thông báo vào kênh chat live
     */
    @Transactional
    public SanPhamGhimDTO ghimSanPham(Long maLive, Long maSanPham) {
        // 1. Gỡ ghim toàn bộ sản phẩm trước đó trong phòng
        sanPhamLivestreamRepository.boGhimTatCaTrongPhong(maLive);

        // 2. Lấy lại thông tin phòng live và sản phẩm live cần ghim (fresh từ DB)
        PhongLivestream phong = layPhongLive(maLive);
        SanPhamLivestream spLive = sanPhamLivestreamRepository.findByPhongLivestream_MaLiveAndSanPham_MaSanPham(maLive, maSanPham)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại trong phiên livestream này"));

        spLive.setLaSanPhamDangGhim(true);
        spLive = sanPhamLivestreamRepository.saveAndFlush(spLive);

        SanPhamGhimDTO dto = chuyenSangDTO(spLive);

        // 3. Phát sóng ngay qua WebSocket tới TOÀN BỘ khán giả đang xem live
        webSocketHandler.broadcast(maLive, ThongDiepLiveWebSocket.builder()
                .loai("GHIM_SAN_PHAM")
                .maLive(maLive)
                .duLieu(dto)
                .build());

        // 4. Đẩy thêm một tin nhắn hệ thống vào khung chat thông báo Shop vừa ghim deal sốc
        NguoiDung shopUser = null;
        try {
            if (phong.getGianHang() != null && phong.getGianHang().getChuSoHuu() != null) {
                shopUser = phong.getGianHang().getChuSoHuu();
            }
        } catch (Exception ignored) {}

        String tenSp = spLive.getSanPham().getTenSanPham();
        String giaStr = String.format("%,dđ", spLive.getGiaDocQuyenLive().longValue());
        int giamGia = spLive.getPhanTramGiamGia();

        String noiDungTinNhan = "🔥 SHOP ĐÃ GHIM DEAL SỐC: " + tenSp + " - GIẢM " + giamGia + "% chỉ còn " + giaStr + "! Bấm [MUA NGAY]!";
        guiBinhLuan(maLive, shopUser, noiDungTinNhan, true);

        log.info("Shop đã ghim sản phẩm thành công: maLive={}, maSanPham={}, tenSanPham={}", maLive, maSanPham, tenSp);
        return dto;
    }

    /**
     * Gỡ ghim sản phẩm trên màn hình livestream
     */
    @Transactional
    public void boGhimSanPham(Long maLive, Long maSanPham) {
        sanPhamLivestreamRepository.findByPhongLivestream_MaLiveAndSanPham_MaSanPham(maLive, maSanPham)
                .ifPresent(sp -> {
                    sp.setLaSanPhamDangGhim(false);
                    sanPhamLivestreamRepository.saveAndFlush(sp);
                });

        // Báo qua WebSocket gỡ thẻ ghim trên màn hình khán giả
        webSocketHandler.broadcast(maLive, ThongDiepLiveWebSocket.builder()
                .loai("BO_GHIM_SAN_PHAM")
                .maLive(maLive)
                .duLieu(Map.of("maSanPham", maSanPham))
                .build());

        log.info("Shop đã bỏ ghim sản phẩm: maLive={}, maSanPham={}", maLive, maSanPham);
    }

    /**
     * Thêm sản phẩm kèm giá sốc vào phòng live
     */
    @Transactional
    public SanPhamLivestream themSanPhamVaoLive(ThemSanPhamLiveForm form) {
        PhongLivestream phong = layPhongLive(form.getMaLive());
        SanPham sanPham = sanPhamRepository.findById(form.getMaSanPham())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm mã " + form.getMaSanPham()));

        // Kiểm tra xem đã có trong live chưa
        Optional<SanPhamLivestream> tonTai = sanPhamLivestreamRepository
                .findByPhongLivestream_MaLiveAndSanPham_MaSanPham(form.getMaLive(), form.getMaSanPham());

        SanPhamLivestream spLive;
        if (tonTai.isPresent()) {
            spLive = tonTai.get();
            spLive.setGiaDocQuyenLive(form.getGiaDocQuyenLive());
            if (form.getSoLuongGioiHan() != null) spLive.setSoLuongGioiHan(form.getSoLuongGioiHan());
        } else {
            long count = sanPhamLivestreamRepository.countByPhongLivestream_MaLive(form.getMaLive());
            spLive = SanPhamLivestream.builder()
                    .phongLivestream(phong)
                    .sanPham(sanPham)
                    .giaDocQuyenLive(form.getGiaDocQuyenLive())
                    .soLuongGioiHan(form.getSoLuongGioiHan() != null ? form.getSoLuongGioiHan() : 50)
                    .thuTuHienThi((int) count + 1)
                    .laSanPhamDangGhim(false)
                    .soLuongDaBan(0)
                    .build();
        }

        spLive = sanPhamLivestreamRepository.save(spLive);
        return spLive;
    }

    /**
     * Xóa sản phẩm khỏi phòng live
     */
    @Transactional
    public void xoaSanPhamKhoiLive(Long maLive, Long maSanPham) {
        sanPhamLivestreamRepository.findByPhongLivestream_MaLiveAndSanPham_MaSanPham(maLive, maSanPham)
                .ifPresent(sp -> {
                    if (Boolean.TRUE.equals(sp.getLaSanPhamDangGhim())) {
                        boGhimSanPham(maLive, maSanPham);
                    }
                    sanPhamLivestreamRepository.delete(sp);
                });
    }

    /**
     * Lấy 50 bình luận gần nhất của phòng live
     */
    public List<BinhLuanLiveDTO> layDanhSachBinhLuan(Long maLive) {
        List<BinhLuanLivestream> list = binhLuanLivestreamRepository.findTop50ByPhongLivestream_MaLiveOrderByThoiGianGuiAsc(maLive);
        return list.stream().map(this::chuyenSangBinhLuanDTO).collect(Collectors.toList());
    }

    /**
     * Gửi bình luận tương tác trong phòng live & phát sóng WebSocket thời gian thực
     */
    @Transactional
    public BinhLuanLiveDTO guiBinhLuan(Long maLive, NguoiDung nguoiDung, String noiDung, boolean laNguoiBan) {
        if (noiDung == null || noiDung.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung bình luận không được để trống");
        }

        PhongLivestream phong = layPhongLive(maLive);
        String hoTen = nguoiDung != null ? nguoiDung.getHoVaTen() : "Khách ẩn danh";

        BinhLuanLivestream bl = BinhLuanLivestream.builder()
                .phongLivestream(phong)
                .nguoiDung(nguoiDung)
                .hoTenNguoiDung(hoTen)
                .noiDung(noiDung.trim())
                .thoiGianGui(LocalDateTime.now())
                .laTinHeThong(false)
                .laNguoiBan(laNguoiBan)
                .build();

        bl = binhLuanLivestreamRepository.save(bl);

        BinhLuanLiveDTO dto = chuyenSangBinhLuanDTO(bl);

        // Phát sóng bình luận ngay lập tức tới mọi người xem
        webSocketHandler.broadcast(maLive, ThongDiepLiveWebSocket.builder()
                .loai("BINH_LUAN_MOI")
                .maLive(maLive)
                .duLieu(dto)
                .build());

        return dto;
    }

    /**
     * Khán giả bấm THẢ TIM tăng tương tác & phát sóng hiệu ứng tim bay
     */
    @Transactional
    public int thaTim(Long maLive) {
        PhongLivestream phong = layPhongLive(maLive);
        int current = phong.getSoLuotThich() != null ? phong.getSoLuotThich() : 0;
        int updated = current + 1;
        phong.setSoLuotThich(updated);
        phongLivestreamRepository.save(phong);

        // Phát sóng hiệu ứng tim bay cho cả phòng
        webSocketHandler.broadcast(maLive, ThongDiepLiveWebSocket.builder()
                .loai("THA_TIM")
                .maLive(maLive)
                .duLieu(Map.of("tongSoTim", updated))
                .build());

        return updated;
    }

    /**
     * ĐẶT HÀNG NHANH TRÊN LIVESTREAM (Flash Sale Live)
     * - Trừ số lượng suất mua live còn lại
     * - Tăng số lượng đã bán
     * - Phát sóng thông báo "Khách hàng ... vừa mua thành công!" cho tất cả người xem thấy
     */
    @Transactional
    public String datHangNhanhLive(DatHangLiveForm form, NguoiDung khachHang) {
        PhongLivestream phong = layPhongLive(form.getMaLive());
        SanPhamLivestream spLive = sanPhamLivestreamRepository
                .findByPhongLivestream_MaLiveAndSanPham_MaSanPham(form.getMaLive(), form.getMaSanPham())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong buổi Livestream này"));

        int conLai = spLive.getSoLuongConLai();
        if (conLai < form.getSoLuong()) {
            throw new IllegalStateException("Rất tiếc, suất mua ưu đãi độc quyền trên Live chỉ còn " + conLai + " sản phẩm!");
        }

        // Cập nhật số lượng đã bán
        spLive.setSoLuongDaBan(spLive.getSoLuongDaBan() + form.getSoLuong());
        sanPhamLivestreamRepository.save(spLive);

        String tenNguoiNhan = form.getHoTenNguoiNhan();
        String tenSp = spLive.getSanPham().getTenSanPham();
        String giaStr = String.format("%,dđ", spLive.getGiaDocQuyenLive().longValue());

        // Phát sóng thông báo chốt đơn tới toàn bộ màn hình người xem
        webSocketHandler.broadcast(form.getMaLive(), ThongDiepLiveWebSocket.builder()
                .loai("THONG_BAO_MUA_HANG")
                .maLive(form.getMaLive())
                .duLieu(Map.of(
                        "hoTen", cheTen(tenNguoiNhan),
                        "tenSanPham", tenSp,
                        "soLuong", form.getSoLuong(),
                        "giaLive", giaStr,
                        "conLai", spLive.getSoLuongConLai()
                ))
                .build());

        // Ghi thêm một bình luận hệ thống chúc mừng
        BinhLuanLivestream bl = BinhLuanLivestream.builder()
                .phongLivestream(phong)
                .nguoiDung(khachHang)
                .hoTenNguoiDung("Hệ thống FlexShop Live")
                .noiDung("🎉 Chúc mừng " + cheTen(tenNguoiNhan) + " vừa chốt thành công " + form.getSoLuong() + "x [" + tenSp + "] với giá ưu đãi " + giaStr + "!")
                .laTinHeThong(true)
                .laNguoiBan(false)
                .build();
        binhLuanLivestreamRepository.save(bl);

        log.info("Khách đặt hàng nhanh Live thành công: maLive={}, maSanPham={}, soLuong={}", form.getMaLive(), form.getMaSanPham(), form.getSoLuong());
        return "Đặt hàng thành công với giá độc quyền FlexShop Live!";
    }

    /**
     * Che bớt tên khách hàng để bảo mật quyền riêng tư (VD: Nguyễn V*** A)
     */
    private String cheTen(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) return "Khách hàng giấu tên";
        String[] parts = hoTen.trim().split("\\s+");
        if (parts.length <= 1) {
            return hoTen.charAt(0) + "***";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0]).append(" ");
        for (int i = 1; i < parts.length - 1; i++) {
            sb.append(parts[i].charAt(0)).append("*** ");
        }
        sb.append(parts[parts.length - 1]);
        return sb.toString();
    }

    private SanPhamGhimDTO chuyenSangDTO(SanPhamLivestream spLive) {
        SanPham sp = spLive.getSanPham();
        String linkAnh = layLinkAnhSanPham(sp.getMaSanPham(), sp.getTenSanPham());

        return SanPhamGhimDTO.builder()
                .maSpLive(spLive.getMaSpLive())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .linkAnh(linkAnh)
                .giaGoc(sp.getGiaCoBan())
                .giaDocQuyenLive(spLive.getGiaDocQuyenLive())
                .phanTramGiamGia(spLive.getPhanTramGiamGia())
                .soLuongGioiHan(spLive.getSoLuongGioiHan())
                .soLuongDaBan(spLive.getSoLuongDaBan())
                .soLuongConLai(spLive.getSoLuongConLai())
                .laSanPhamDangGhim(spLive.getLaSanPhamDangGhim())
                .thuTuHienThi(spLive.getThuTuHienThi())
                .build();
    }

    private BinhLuanLiveDTO chuyenSangBinhLuanDTO(BinhLuanLivestream bl) {
        return BinhLuanLiveDTO.builder()
                .maBinhLuan(bl.getMaBinhLuan())
                .maLive(bl.getPhongLivestream().getMaLive())
                .maNguoiDung(bl.getNguoiDung() != null ? bl.getNguoiDung().getMaNguoiDung() : null)
                .hoTenNguoiDung(bl.getHoTenNguoiDung())
                .noiDung(bl.getNoiDung())
                .thoiGianGui(bl.getThoiGianGui())
                .laTinHeThong(bl.getLaTinHeThong())
                .laNguoiBan(bl.getLaNguoiBan())
                .build();
    }

    private String layLinkAnhSanPham(Long maSanPham, String tenSanPham) {
        if (tenSanPham != null) {
            String lower = tenSanPham.toLowerCase();
            if (lower.contains("sony") || lower.contains("tai nghe") || lower.contains("wh-1000xm5")) {
                return "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400";
            } else if (lower.contains("chuột") || lower.contains("logitech") || lower.contains("mx master")) {
                return "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=400";
            } else if (lower.contains("bàn phím") || lower.contains("keychron")) {
                return "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400";
            } else if (lower.contains("anker") || lower.contains("sạc")) {
                return "https://images.unsplash.com/photo-1622445268462-328bf16ae76a?w=400";
            }
        }
        return "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400";
    }
}
