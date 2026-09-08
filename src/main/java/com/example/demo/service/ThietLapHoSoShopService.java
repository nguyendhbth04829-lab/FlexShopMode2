package com.example.demo.service;

import com.example.demo.dto.request.ThietLapHoSoShopRequest;
import com.example.demo.dto.response.LichSuThietLapShopResponse;
import com.example.demo.dto.response.ThietLapHoSoShopResponse;
import com.example.demo.dto.response.ThongKeThietLapShopResponse;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.LichSuThietLapShop;
import com.example.demo.entity.NguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.LichSuThietLapShopRepository;
import com.example.demo.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThietLapHoSoShopService {

    private final GianHangRepository gianHangRepository;
    private final LichSuThietLapShopRepository lichSuThietLapShopRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final DangKyGianHangService dangKyGianHangService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Set<String> MIME_ANH_HOP_LE = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    /**
     * Lấy thông tin thiết lập hồ sơ gian hàng của Seller hiện tại
     */
    @Transactional(readOnly = true)
    public ThietLapHoSoShopResponse layThietLapShop(Long maNguoiDung) {
        GianHang gianHang = layGianHangHopLe(maNguoiDung);
        return chuyenSangResponse(gianHang);
    }

    /**
     * Cập nhật toàn diện thông tin thiết lập gian hàng sau khi được Admin phê duyệt (US-10)
     */
    @Transactional
    public ThietLapHoSoShopResponse capNhatThietLapShop(
            Long maNguoiDung,
            ThietLapHoSoShopRequest yeuCau,
            MultipartFile fileLogo,
            MultipartFile fileBanner
    ) {
        GianHang gianHang = layGianHangHopLe(maNguoiDung);

        // Kiểm tra điều kiện tiên quyết của US-10: Gian hàng phải được Admin phê duyệt (HOAT_DONG)
        if (!"HOAT_DONG".equalsIgnoreCase(gianHang.getTrangThai())) {
            String trangThaiHienTai = gianHang.getTrangThai() != null ? gianHang.getTrangThai() : "CHO_DUYET";
            throw new NgoaiLeUngDung(
                    "Gian hàng hiện đang ở trạng thái '" + trangThaiHienTai +
                    "'. Theo quy định hệ thống, chỉ gian hàng đã được Admin phê duyệt (HOẠT ĐỘNG) mới có thể thiết lập hồ sơ!",
                    HttpStatus.FORBIDDEN
            );
        }

        // Validate logic giờ hoạt động
        try {
            LocalTime gioMo = LocalTime.parse(yeuCau.getGioMoCua().trim());
            LocalTime gioDong = LocalTime.parse(yeuCau.getGioDongCua().trim());
            if (!gioDong.isAfter(gioMo)) {
                throw new NgoaiLeUngDung("Giờ đóng cửa (" + yeuCau.getGioDongCua() + ") phải sau giờ mở cửa (" + yeuCau.getGioMoCua() + ") trong ngày!", HttpStatus.BAD_REQUEST);
            }
        } catch (NgoaiLeUngDung e) {
            throw e;
        } catch (Exception e) {
            throw new NgoaiLeUngDung("Định dạng giờ mở cửa/đóng cửa không hợp lệ (yêu cầu định dạng HH:mm)!", HttpStatus.BAD_REQUEST);
        }

        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung).orElse(null);
        String tenNguoiThucHien = (nguoiDung != null) ? nguoiDung.getHoVaTen() : "Seller #" + maNguoiDung;

        // 1. Kiểm tra trùng tên shop nếu tên thay đổi
        String tenMoi = yeuCau.getTenGianHang().trim();
        if (!gianHang.getTenGianHang().equalsIgnoreCase(tenMoi)) {
            if (gianHangRepository.existsByTenGianHangIgnoreCaseAndMaGianHangNotAndDaXoaFalse(tenMoi, gianHang.getMaGianHang())) {
                throw new NgoaiLeUngDung("Tên gian hàng '" + tenMoi + "' đã được shop khác sử dụng. Vui lòng chọn tên khác!", HttpStatus.CONFLICT, "tenGianHang");
            }
            gianHang.setTenGianHang(tenMoi);
            String slugMoi = dangKyGianHangService.taoDuongDanSlugTuTenShop(tenMoi, gianHang.getMaGianHang());
            gianHang.setDuongDanSlug(slugMoi);
        }

        // 2. Cập nhật thông tin chi tiết
        gianHang.setMoTa(StringUtils.hasText(yeuCau.getMoTa()) ? yeuCau.getMoTa().trim() : null);
        gianHang.setDiaChiKho(yeuCau.getDiaChiKho().trim());
        gianHang.setSdtKho(yeuCau.getSdtKho().trim());
        gianHang.setGioMoCua(yeuCau.getGioMoCua().trim());
        gianHang.setGioDongCua(yeuCau.getGioDongCua().trim());
        if (yeuCau.getDangMoCua() != null) {
            gianHang.setDangMoCua(yeuCau.getDangMoCua());
        }
        gianHang.setGhiChuKho(StringUtils.hasText(yeuCau.getGhiChuKho()) ? yeuCau.getGhiChuKho().trim() : null);
        gianHang.setNguoiLienHeKho(StringUtils.hasText(yeuCau.getNguoiLienHeKho()) ? yeuCau.getNguoiLienHeKho().trim() : (nguoiDung != null ? nguoiDung.getHoVaTen() : null));

        // 3. Xử lý upload ảnh Logo mới nếu có
        if (fileLogo != null && !fileLogo.isEmpty()) {
            String linkLogo = luuTepHinhAnh(fileLogo, maNguoiDung, "shop-logos", "logo");
            gianHang.setLinkLogo(linkLogo);
            ghiLichSuThayDoi(gianHang.getMaGianHang(), "DOI_LOGO", "Cập nhật ảnh Logo đại diện gian hàng mới", tenNguoiThucHien);
        }

        // 4. Xử lý upload ảnh Banner mới nếu có
        if (fileBanner != null && !fileBanner.isEmpty()) {
            String linkBanner = luuTepHinhAnh(fileBanner, maNguoiDung, "shop-banners", "banner");
            gianHang.setLinkBanner(linkBanner);
            ghiLichSuThayDoi(gianHang.getMaGianHang(), "DOI_BANNER", "Cập nhật ảnh Banner trang trí gian hàng mới", tenNguoiThucHien);
        }

        // Lưu thông tin gian hàng
        gianHangRepository.save(gianHang);

        // Ghi nhật ký thiết lập tổng thể
        ghiLichSuThayDoi(
                gianHang.getMaGianHang(),
                "THIET_LAP_TONG_THE",
                "Cập nhật hồ sơ shop (Giờ: " + gianHang.getGioMoCua() + " - " + gianHang.getGioDongCua() +
                ", Trạng thái: " + (Boolean.TRUE.equals(gianHang.getDangMoCua()) ? "Mở cửa nhận đơn" : "Tạm nghỉ") +
                ", Kho: " + gianHang.getDiaChiKho() + ")",
                tenNguoiThucHien
        );

        log.info("Seller {} đã thiết lập thành công hồ sơ gian hàng maGianHang={}", maNguoiDung, gianHang.getMaGianHang());
        return chuyenSangResponse(gianHang);
    }

    /**
     * Chuyển nhanh trạng thái nhận đơn (Đang mở cửa / Tạm nghỉ nhận đơn)
     */
    @Transactional
    public ThietLapHoSoShopResponse chuyenTrangThaiNhanDon(Long maNguoiDung, Boolean dangMoCua) {
        GianHang gianHang = layGianHangHopLe(maNguoiDung);

        if (!"HOAT_DONG".equalsIgnoreCase(gianHang.getTrangThai())) {
            throw new NgoaiLeUngDung("Chỉ gian hàng đã được Admin phê duyệt (HOẠT ĐỘNG) mới có thể chuyển đổi trạng thái nhận đơn!", HttpStatus.FORBIDDEN);
        }

        boolean trangThaiMoi = Boolean.TRUE.equals(dangMoCua);
        gianHang.setDangMoCua(trangThaiMoi);
        gianHangRepository.save(gianHang);

        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung).orElse(null);
        String tenNguoiThucHien = (nguoiDung != null) ? nguoiDung.getHoVaTen() : "Seller #" + maNguoiDung;

        ghiLichSuThayDoi(
                gianHang.getMaGianHang(),
                "BAT_TAT_NHAN_DON",
                trangThaiMoi ? "Mở cửa gian hàng, sẵn sàng tiếp nhận đơn đặt mới" : "Tạm ngưng nhận đơn hàng mới (Chế độ tạm nghỉ)",
                tenNguoiThucHien
        );

        return chuyenSangResponse(gianHang);
    }

    /**
     * Lấy danh sách lịch sử thay đổi thiết lập có tìm kiếm, lọc và phân trang
     */
    @Transactional(readOnly = true)
    public Page<LichSuThietLapShopResponse> layLichSuThayDoi(Long maNguoiDung, String loaiThayDoi, String tuKhoa, Pageable pageable) {
        GianHang gianHang = layGianHangHopLe(maNguoiDung);

        Page<LichSuThietLapShop> trangLichSu = lichSuThietLapShopRepository.timKiemLichSu(
                gianHang.getMaGianHang(),
                StringUtils.hasText(loaiThayDoi) ? loaiThayDoi.trim() : null,
                StringUtils.hasText(tuKhoa) ? tuKhoa.trim() : null,
                pageable
        );

        return trangLichSu.map(this::chuyenSangLichSuResponse);
    }

    /**
     * Lấy thống kê cấu hình gian hàng
     */
    @Transactional(readOnly = true)
    public ThongKeThietLapShopResponse layThongKeThietLap(Long maNguoiDung) {
        GianHang gianHang = layGianHangHopLe(maNguoiDung);
        long tongSoLan = lichSuThietLapShopRepository.countByMaGianHang(gianHang.getMaGianHang());
        int phanTram = tinhPhanTramHoanThien(gianHang);

        return ThongKeThietLapShopResponse.builder()
                .tongSoLanCapNhat(tongSoLan)
                .phanTramHoanThien(phanTram)
                .trangThaiGianHang(gianHang.getTrangThai())
                .dangMoCua(Boolean.TRUE.equals(gianHang.getDangMoCua()))
                .gioHoatDongHienTai((gianHang.getGioMoCua() != null ? gianHang.getGioMoCua() : "08:00") + " - " + (gianHang.getGioDongCua() != null ? gianHang.getGioDongCua() : "22:00"))
                .tenGianHang(gianHang.getTenGianHang())
                .hangGianHang(gianHang.getHangGianHang())
                .build();
    }

    /**
     * Helper kiểm tra và lấy gian hàng của người dùng
     */
    private GianHang layGianHangHopLe(Long maNguoiDung) {
        return gianHangRepository.findFirstByMaChuSoHuuAndDaXoaFalseOrderByMaGianHangDesc(maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Bạn chưa có gian hàng nào trên hệ thống FlexShop. Vui lòng đăng ký mở gian hàng trước!", HttpStatus.NOT_FOUND));
    }

    /**
     * Tính toán % hoàn thiện hồ sơ và các mục checklist còn thiếu
     */
    public int tinhPhanTramHoanThien(GianHang gianHang) {
        int score = 0;
        if (StringUtils.hasText(gianHang.getLinkLogo())) score += 20;
        if (StringUtils.hasText(gianHang.getLinkBanner())) score += 20;
        if (StringUtils.hasText(gianHang.getGioMoCua()) && StringUtils.hasText(gianHang.getGioDongCua())) score += 20;
        if (StringUtils.hasText(gianHang.getDiaChiKho()) && StringUtils.hasText(gianHang.getSdtKho())) score += 20;
        if (StringUtils.hasText(gianHang.getMoTa()) && gianHang.getMoTa().length() >= 10) score += 20;
        return score;
    }

    public List<String> layDanhSachChecklistConThieu(GianHang gianHang) {
        List<String> checklist = new ArrayList<>();
        if (!StringUtils.hasText(gianHang.getLinkLogo())) {
            checklist.add("Tải lên Logo đại diện gian hàng (chuẩn vuông 1:1)");
        }
        if (!StringUtils.hasText(gianHang.getLinkBanner())) {
            checklist.add("Tải lên Banner trang trí gian hàng (chuẩn tỷ lệ rộng 16:5)");
        }
        if (!StringUtils.hasText(gianHang.getGioMoCua()) || !StringUtils.hasText(gianHang.getGioDongCua())) {
            checklist.add("Cài đặt khung giờ mở cửa và đóng cửa nhận đơn");
        }
        if (!StringUtils.hasText(gianHang.getDiaChiKho()) || !StringUtils.hasText(gianHang.getSdtKho())) {
            checklist.add("Cập nhật đầy đủ địa chỉ kho lấy hàng và số điện thoại liên hệ");
        }
        if (!StringUtils.hasText(gianHang.getMoTa()) || gianHang.getMoTa().length() < 10) {
            checklist.add("Bổ sung thông tin giới thiệu / mô tả gian hàng (tối thiểu 10 ký tự)");
        }
        if (!StringUtils.hasText(gianHang.getGhiChuKho())) {
            checklist.add("Thêm ghi chú lấy hàng cho shipper / đơn vị vận chuyển");
        }
        return checklist;
    }

    /**
     * Chuyển đổi GianHang entity sang ThietLapHoSoShopResponse
     */
    private ThietLapHoSoShopResponse chuyenSangResponse(GianHang gianHang) {
        int phanTram = tinhPhanTramHoanThien(gianHang);
        List<String> checklist = layDanhSachChecklistConThieu(gianHang);

        return ThietLapHoSoShopResponse.builder()
                .maGianHang(gianHang.getMaGianHang())
                .maChuSoHuu(gianHang.getMaChuSoHuu())
                .tenGianHang(gianHang.getTenGianHang())
                .duongDanSlug(gianHang.getDuongDanSlug())
                .moTa(gianHang.getMoTa())
                .linkLogo(gianHang.getLinkLogo())
                .linkBanner(gianHang.getLinkBanner())
                .diaChiKho(gianHang.getDiaChiKho())
                .sdtKho(gianHang.getSdtKho())
                .gioMoCua(gianHang.getGioMoCua() != null ? gianHang.getGioMoCua() : "08:00")
                .gioDongCua(gianHang.getGioDongCua() != null ? gianHang.getGioDongCua() : "22:00")
                .dangMoCua(Boolean.TRUE.equals(gianHang.getDangMoCua()))
                .ghiChuKho(gianHang.getGhiChuKho())
                .nguoiLienHeKho(gianHang.getNguoiLienHeKho())
                .trangThai(gianHang.getTrangThai())
                .lyDoTuChoi(gianHang.getLyDoTuChoi())
                .hangGianHang(gianHang.getHangGianHang())
                .diemSaoQuaTa(gianHang.getDiemSaoQuaTa() != null ? gianHang.getDiemSaoQuaTa() : 0)
                .diemDanhGiaTb(gianHang.getDiemDanhGiaTb())
                .tongDanhGia(gianHang.getTongDanhGia() != null ? gianHang.getTongDanhGia() : 0)
                .tongDonHang(gianHang.getTongDonHang() != null ? gianHang.getTongDonHang() : 0)
                .tyLePhanHoiChat(gianHang.getTyLePhanHoiChat())
                .ngayTao(gianHang.getNgayTao())
                .phanTramHoanThien(phanTram)
                .cacBuocConThieu(checklist)
                .build();
    }

    /**
     * Chuyển đổi LichSuThietLapShop entity sang LichSuThietLapShopResponse
     */
    private LichSuThietLapShopResponse chuyenSangLichSuResponse(LichSuThietLapShop lichSu) {
        String tenLoai;
        switch (lichSu.getLoaiThayDoi()) {
            case "THIET_LAP_TONG_THE" -> tenLoai = "Thiết lập tổng thể";
            case "DOI_LOGO" -> tenLoai = "Cập nhật Logo";
            case "DOI_BANNER" -> tenLoai = "Cập nhật Banner";
            case "BAT_TAT_NHAN_DON" -> tenLoai = "Trạng thái nhận đơn";
            case "DOI_GIO_HOAT_DONG" -> tenLoai = "Cập nhật giờ hoạt động";
            case "DOI_DIA_CHI_KHO" -> tenLoai = "Cập nhật địa chỉ kho";
            default -> tenLoai = lichSu.getLoaiThayDoi();
        }

        return LichSuThietLapShopResponse.builder()
                .maLichSu(lichSu.getMaLichSu())
                .maGianHang(lichSu.getMaGianHang())
                .loaiThayDoi(lichSu.getLoaiThayDoi())
                .tenLoaiThayDoi(tenLoai)
                .noiDungThayDoi(lichSu.getNoiDungThayDoi())
                .nguoiThucHien(lichSu.getNguoiThucHien())
                .thoiGian(lichSu.getThoiGian())
                .build();
    }

    /**
     * Ghi nhận lịch sử thay đổi cấu hình shop
     */
    private void ghiLichSuThayDoi(Long maGianHang, String loaiThayDoi, String noiDungThayDoi, String nguoiThucHien) {
        try {
            LichSuThietLapShop logRecord = LichSuThietLapShop.builder()
                    .maGianHang(maGianHang)
                    .loaiThayDoi(loaiThayDoi)
                    .noiDungThayDoi(noiDungThayDoi)
                    .nguoiThucHien(nguoiThucHien)
                    .thoiGian(LocalDateTime.now())
                    .build();
            lichSuThietLapShopRepository.save(logRecord);
        } catch (Exception e) {
            log.warn("Không thể ghi log lịch sử thiết lập shop: {}", e.getMessage());
        }
    }

    /**
     * Lưu trữ tệp hình ảnh an toàn (Logo/Banner) với kiểm tra kích thước, MIME type và path traversal
     */
    private String luuTepHinhAnh(MultipartFile file, Long maNguoiDung, String thuMucCon, String tienTo) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new NgoaiLeUngDung("Kích thước tệp tải lên vượt quá giới hạn 5MB!", HttpStatus.BAD_REQUEST, tienTo.equals("logo") ? "fileLogo" : "fileBanner");
        }

        String contentType = file.getContentType();
        if (contentType == null || !MIME_ANH_HOP_LE.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new NgoaiLeUngDung("Định dạng tệp không hợp lệ! Chỉ chấp nhận ảnh định dạng JPG, JPEG, PNG, WEBP.", HttpStatus.BAD_REQUEST, tienTo.equals("logo") ? "fileLogo" : "fileBanner");
        }

        String tenGoc = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : (tienTo + ".png"));
        String duoiTep = ".png";
        int dotIndex = tenGoc.lastIndexOf(".");
        if (dotIndex > 0) {
            duoiTep = tenGoc.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        try {
            Path thuMucLuu = Paths.get("uploads", thuMucCon).toAbsolutePath().normalize();
            if (!Files.exists(thuMucLuu)) {
                Files.createDirectories(thuMucLuu);
            }

            String tenTepMoi = tienTo + "_" + maNguoiDung + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + duoiTep;
            Path duongDanDich = thuMucLuu.resolve(tenTepMoi);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, duongDanDich, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/" + thuMucCon + "/" + tenTepMoi;

        } catch (IOException e) {
            log.error("Lỗi khi lưu trữ tệp hình ảnh {}: ", thuMucCon, e);
            throw new NgoaiLeUngDung("Không thể lưu trữ tệp ảnh: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
