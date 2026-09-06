package com.example.demo.service;

import com.example.demo.config.ChatWebSocketHandler;
import com.example.demo.dto.CauHinhTinNhanTuDongForm;
import com.example.demo.dto.ThongKeTinNhanTuDongDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service quản trị và kích hoạt hệ thống Tin nhắn tự động trả lời (Auto-responder US-60)
 */
@Slf4j
@Service
public class TinNhanTuDongService {

    @Autowired
    private CauHinhTinNhanTuDongRepository cauHinhRepository;

    @Autowired
    private LichSuTinNhanTuDongRepository lichSuRepository;

    @Autowired
    private CuocTroChuyenRepository cuocTroChuyenRepository;

    @Autowired
    private TinNhanRepository tinNhanRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lấy dashboard thống kê số liệu cấu hình tin nhắn tự động của Shop
     */
    @Transactional(readOnly = true)
    public ThongKeTinNhanTuDongDTO layThongKe(Long maGianHang) {
        long tongSoCauHinh = cauHinhRepository.countByGianHang_MaGianHang(maGianHang);
        long soCauHinhDangBat = cauHinhRepository.countByGianHang_MaGianHangAndKichHoatTrue(maGianHang);
        long tongSoTinTuDongDaGui = cauHinhRepository.tongSoTinNhanTuDongDaGui(maGianHang);
        long tongSoVoucherDaTang = lichSuRepository.demSoVoucherDaPhatChoKhach(maGianHang);

        return ThongKeTinNhanTuDongDTO.builder()
                .tongSoCauHinh(tongSoCauHinh)
                .soCauHinhDangBat(soCauHinhDangBat)
                .tongSoTinTuDongDaGui(tongSoTinTuDongDaGui)
                .tongSoVoucherDaTang(tongSoVoucherDaTang)
                .build();
    }

    /**
     * Tìm kiếm và phân trang danh sách cấu hình tin nhắn tự động
     */
    @Transactional(readOnly = true)
    public Page<CauHinhTinNhanTuDong> timKiemVaLoc(
            Long maGianHang,
            String loai,
            String trangThai,
            String tuKhoa,
            Pageable pageable
    ) {
        return cauHinhRepository.timKiemVaLoc(maGianHang, loai, trangThai, tuKhoa, pageable);
    }

    /**
     * Lấy cấu hình theo ID và Gian hàng
     */
    @Transactional(readOnly = true)
    public CauHinhTinNhanTuDong layTheoId(Long maCauHinh, Long maGianHang) {
        return cauHinhRepository.findByMaCauHinhAndGianHang_MaGianHang(maCauHinh, maGianHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cấu hình tin nhắn tự động #" + maCauHinh));
    }

    /**
     * Tạo mới hoặc cập nhật cấu hình tin nhắn tự động với validation nghiệp vụ chặt chẽ
     */
    @Transactional
    public CauHinhTinNhanTuDong luuCauHinh(Long maGianHang, CauHinhTinNhanTuDongForm form) {
        GianHang gianHang = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gian hàng #" + maGianHang));

        // Validate nghiệp vụ cho từng loại kịch bản
        LocalTime gioBatDau = null;
        LocalTime gioKetThuc = null;

        if ("NGOAI_GIO_LAM_VIEC".equalsIgnoreCase(form.getLoaiTinNhanTuDong())) {
            if (form.getGioBatDau() == null || form.getGioBatDau().trim().isEmpty() ||
                    form.getGioKetThuc() == null || form.getGioKetThuc().trim().isEmpty()) {
                throw new IllegalArgumentException("Kịch bản ngoài giờ làm việc bắt buộc phải chọn Giờ bắt đầu và Giờ kết thúc!");
            }
            try {
                gioBatDau = LocalTime.parse(form.getGioBatDau().trim());
                gioKetThuc = LocalTime.parse(form.getGioKetThuc().trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Định dạng giờ không hợp lệ (yêu cầu định dạng HH:mm)!");
            }
        } else if ("TU_KHOA".equalsIgnoreCase(form.getLoaiTinNhanTuDong())) {
            if (form.getTuKhoaKichHoat() == null || form.getTuKhoaKichHoat().trim().isEmpty()) {
                throw new IllegalArgumentException("Kịch bản từ khóa bắt buộc phải nhập ít nhất 1 từ khóa kích hoạt!");
            }
        }

        // Validate voucher nếu có đính kèm
        MaGiamGia voucher = null;
        if (form.getMaVoucher() != null) {
            voucher = maGiamGiaRepository.findById(form.getMaVoucher())
                    .orElseThrow(() -> new IllegalArgumentException("Mã voucher #" + form.getMaVoucher() + " không tồn tại!"));

            if (voucher.getGianHang() != null && !voucher.getGianHang().getMaGianHang().equals(maGianHang)) {
                throw new IllegalArgumentException("Mã voucher này không thuộc quyền quản lý của gian hàng bạn!");
            }
            if (Boolean.TRUE.equals(voucher.getDaXoa()) || Boolean.FALSE.equals(voucher.getDangHoatDong())) {
                throw new IllegalArgumentException("Mã voucher đã hết hạn hoặc không còn hoạt động!");
            }
        }

        CauHinhTinNhanTuDong entity;
        if (form.getMaCauHinh() != null) {
            entity = layTheoId(form.getMaCauHinh(), maGianHang);
            entity.setNgayCapNhat(LocalDateTime.now());
        } else {
            entity = new CauHinhTinNhanTuDong();
            entity.setGianHang(gianHang);
            entity.setNgayTao(LocalDateTime.now());
            entity.setSoLanDaGui(0);
        }

        entity.setTieuDe(form.getTieuDe().trim());
        entity.setLoaiTinNhanTuDong(form.getLoaiTinNhanTuDong().trim());
        entity.setNoiDungTinNhan(form.getNoiDungTinNhan().trim());
        entity.setMaGiamGia(voucher);
        entity.setGioBatDau(gioBatDau);
        entity.setGioKetThuc(gioKetThuc);
        entity.setTuKhoaKichHoat(form.getTuKhoaKichHoat() != null ? form.getTuKhoaKichHoat().trim() : null);
        entity.setKichHoat(form.getKichHoat() != null ? form.getKichHoat() : true);
        entity.setDoTreGiay(form.getDoTreGiay() != null ? form.getDoTreGiay() : 1);
        entity.setGioiHanGuiMoiKhachNgay(form.getGioiHanGuiMoiKhachNgay() != null ? form.getGioiHanGuiMoiKhachNgay() : 1);

        return cauHinhRepository.save(entity);
    }

    /**
     * Đổi trạng thái Bật / Tắt nhanh (AJAX)
     */
    @Transactional
    public boolean doiTrangThai(Long maCauHinh, Long maGianHang) {
        CauHinhTinNhanTuDong entity = layTheoId(maCauHinh, maGianHang);
        entity.setKichHoat(!entity.getKichHoat());
        entity.setNgayCapNhat(LocalDateTime.now());
        cauHinhRepository.save(entity);
        return entity.getKichHoat();
    }

    /**
     * Xóa cấu hình tin nhắn tự động
     */
    @Transactional
    public void xoaCauHinh(Long maCauHinh, Long maGianHang) {
        CauHinhTinNhanTuDong entity = layTheoId(maCauHinh, maGianHang);
        cauHinhRepository.delete(entity);
    }

    /**
     * Lấy lịch sử tin nhắn tự động đã gửi của Shop
     */
    @Transactional(readOnly = true)
    public Page<LichSuTinNhanTuDong> layLichSu(Long maGianHang, Pageable pageable) {
        return lichSuRepository.findByGianHang_MaGianHangOrderByThoiGianGuiDesc(maGianHang, pageable);
    }

    // =========================================================================
    // CORE ENGINE: PHÂN TÍCH VÀ KÍCH HOẠT PHẢN HỒI TỰ ĐỘNG (AUTO-RESPONDER)
    // =========================================================================

    /**
     * Kích hoạt tự động trả lời khi nhận tin nhắn từ Khách hàng
     */
    @Transactional
    public Optional<TinNhan> xuLyTuDongPhanHoi(CuocTroChuyen ctc, TinNhan tinNhanKhach) {
        if (ctc == null || tinNhanKhach == null) {
            return Optional.empty();
        }

        // Chỉ phản hồi tự động nếu người gửi là KHACH_HANG
        if (!"KHACH_HANG".equalsIgnoreCase(tinNhanKhach.getLoaiNguoiGui())) {
            return Optional.empty();
        }

        Long maGianHang = ctc.getGianHang().getMaGianHang();
        Long maKhachHang = ctc.getKhachHang().getMaNguoiDung();
        List<CauHinhTinNhanTuDong> dsCauHinh = cauHinhRepository.findByGianHang_MaGianHangAndKichHoatTrueOrderByMaCauHinhDesc(maGianHang);

        if (dsCauHinh == null || dsCauHinh.isEmpty()) {
            return Optional.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        LocalDateTime dauNgay = LocalDate.now().atStartOfDay();
        LocalDateTime cuoiNgay = LocalDate.now().atTime(LocalTime.MAX);

        CauHinhTinNhanTuDong cauHinhPhuHop = null;

        // Ưu tiên 1: So khớp kịch bản TU_KHOA
        String noiDungKhach = tinNhanKhach.getNoiDung() != null ? tinNhanKhach.getNoiDung().toLowerCase() : "";
        for (CauHinhTinNhanTuDong ch : dsCauHinh) {
            if ("TU_KHOA".equalsIgnoreCase(ch.getLoaiTinNhanTuDong()) && ch.getTuKhoaKichHoat() != null) {
                String[] keywords = ch.getTuKhoaKichHoat().split("[,;]");
                for (String kw : keywords) {
                    if (!kw.trim().isEmpty() && noiDungKhach.contains(kw.trim().toLowerCase())) {
                        if (kiemTraChuaVuotQuaGioiHan(ch, maKhachHang, dauNgay, cuoiNgay)) {
                            cauHinhPhuHop = ch;
                            break;
                        }
                    }
                }
            }
            if (cauHinhPhuHop != null) break;
        }

        // Ưu tiên 2: So khớp kịch bản NGOAI_GIO_LAM_VIEC
        if (cauHinhPhuHop == null) {
            for (CauHinhTinNhanTuDong ch : dsCauHinh) {
                if ("NGOAI_GIO_LAM_VIEC".equalsIgnoreCase(ch.getLoaiTinNhanTuDong())) {
                    if (laNgoaiGioLamViec(currentTime, ch.getGioBatDau(), ch.getGioKetThuc())) {
                        if (kiemTraChuaVuotQuaGioiHan(ch, maKhachHang, dauNgay, cuoiNgay)) {
                            cauHinhPhuHop = ch;
                            break;
                        }
                    }
                }
            }
        }

        // Ưu tiên 3: So khớp kịch bản CHAO_MUNG (khách mới nhắn tin lần đầu)
        if (cauHinhPhuHop == null) {
            long soLanDaNhanChaoMungHomNay = lichSuRepository.demSoLanNhanTinChaoMungHomNay(maGianHang, maKhachHang, dauNgay, cuoiNgay);
            if (soLanDaNhanChaoMungHomNay == 0) {
                for (CauHinhTinNhanTuDong ch : dsCauHinh) {
                    if ("CHAO_MUNG".equalsIgnoreCase(ch.getLoaiTinNhanTuDong())) {
                        if (kiemTraChuaVuotQuaGioiHan(ch, maKhachHang, dauNgay, cuoiNgay)) {
                            cauHinhPhuHop = ch;
                            break;
                        }
                    }
                }
            }
        }

        // Ưu tiên 4: So khớp kịch bản VANG_MAT_TAM_THOI (nếu Shop kích hoạt chế độ này)
        if (cauHinhPhuHop == null) {
            for (CauHinhTinNhanTuDong ch : dsCauHinh) {
                if ("VANG_MAT_TAM_THOI".equalsIgnoreCase(ch.getLoaiTinNhanTuDong())) {
                    if (kiemTraChuaVuotQuaGioiHan(ch, maKhachHang, dauNgay, cuoiNgay)) {
                        cauHinhPhuHop = ch;
                        break;
                    }
                }
            }
        }

        if (cauHinhPhuHop == null) {
            return Optional.empty();
        }

        // Thực hiện tự động gửi tin nhắn phản hồi
        TinNhan tinNhanTuDong = thucHienGuiTinNhanTuDong(ctc, cauHinhPhuHop);
        return Optional.of(tinNhanTuDong);
    }

    private boolean kiemTraChuaVuotQuaGioiHan(CauHinhTinNhanTuDong ch, Long maKhachHang, LocalDateTime dauNgay, LocalDateTime cuoiNgay) {
        int gioiHan = ch.getGioiHanGuiMoiKhachNgay() != null ? ch.getGioiHanGuiMoiKhachNgay() : 1;
        long soLanDaGuiHomNay = lichSuRepository.countByCauHinh_MaCauHinhAndKhachHang_MaNguoiDungAndThoiGianGuiBetween(
                ch.getMaCauHinh(), maKhachHang, dauNgay, cuoiNgay);
        return soLanDaGuiHomNay < gioiHan;
    }

    private boolean laNgoaiGioLamViec(LocalTime current, LocalTime batDau, LocalTime ketThuc) {
        if (batDau == null || ketThuc == null) return false;
        if (batDau.isBefore(ketThuc)) {
            // Ví dụ: 12:00 đến 13:30 (nghỉ trưa)
            return !current.isBefore(batDau) && current.isBefore(ketThuc);
        } else {
            // Ví dụ: 18:00 đến 08:00 hôm sau (vắt qua đêm)
            return !current.isBefore(batDau) || current.isBefore(ketThuc);
        }
    }

    private TinNhan thucHienGuiTinNhanTuDong(CuocTroChuyen ctc, CauHinhTinNhanTuDong cauHinh) {
        GianHang gianHang = ctc.getGianHang();
        Long maChuShop = gianHang.getChuSoHuu() != null ? gianHang.getChuSoHuu().getMaNguoiDung() : 0L;

        TinNhan tinNhan = new TinNhan();
        tinNhan.setCuocTroChuyen(ctc);
        tinNhan.setLoaiNguoiGui("SHOP");
        tinNhan.setMaNguoiGui(maChuShop);
        tinNhan.setDaXem(false);
        tinNhan.setNgayTao(LocalDateTime.now().plusSeconds(cauHinh.getDoTreGiay() != null ? cauHinh.getDoTreGiay() : 1));

        MaGiamGia voucher = cauHinh.getMaGiamGia();
        if (voucher != null) {
            tinNhan.setLoaiTinNhan("VOUCHER");
            tinNhan.setNoiDung(cauHinh.getNoiDungTinNhan() + "\n\n🎁 [MÃ VOUCHER ĐÍNH KÈM: " + voucher.getMaCodeVoucher() + "]");

            Map<String, Object> voucherMap = new HashMap<>();
            voucherMap.put("maVoucher", voucher.getMaVoucher());
            voucherMap.put("maCodeVoucher", voucher.getMaCodeVoucher());
            voucherMap.put("tenVoucher", voucher.getTenVoucher());
            voucherMap.put("loaiVoucher", voucher.getLoaiVoucher());
            voucherMap.put("giaTriGiam", voucher.getGiaTriGiam());
            voucherMap.put("giaTriDonToiThieu", voucher.getGiaTriDonToiThieu());
            voucherMap.put("ngayKetThuc", voucher.getNgayKetThuc() != null ? voucher.getNgayKetThuc().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Không giới hạn");
            try {
                tinNhan.setDuLieuDinhKemJson(objectMapper.writeValueAsString(voucherMap));
            } catch (Exception e) {
                log.error("Lỗi parse JSON voucher auto-responder: {}", e.getMessage());
            }
        } else {
            tinNhan.setLoaiTinNhan("VAN_BAN");
            tinNhan.setNoiDung(cauHinh.getNoiDungTinNhan());
        }

        TinNhan saved = tinNhanRepository.save(tinNhan);

        // Cập nhật cuộc trò chuyện
        ctc.setTinNhanCuoiCung(saved.getNoiDung());
        ctc.setThoiGianTinCuoi(saved.getNgayTao());
        ctc.setSoTinChuaDocKhach((ctc.getSoTinChuaDocKhach() == null ? 0 : ctc.getSoTinChuaDocKhach()) + 1);
        cuocTroChuyenRepository.save(ctc);

        // Tăng số lần đã gửi của cấu hình
        cauHinh.setSoLanDaGui(cauHinh.getSoLanDaGui() + 1);
        cauHinhRepository.save(cauHinh);

        // Lưu vào nhật ký lịch sử gửi
        LichSuTinNhanTuDong lichSu = LichSuTinNhanTuDong.builder()
                .cauHinh(cauHinh)
                .cuocTroChuyen(ctc)
                .khachHang(ctc.getKhachHang())
                .gianHang(gianHang)
                .noiDungDaGui(saved.getNoiDung())
                .voucherDaTang(voucher)
                .thoiGianGui(LocalDateTime.now())
                .build();
        lichSuRepository.save(lichSu);

        // Phát sóng WebSocket đến cả 2 bên (Khách và Shop)
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "TIN_NHAN_MOI");
            payload.put("maCuocTroChuyen", ctc.getMaCuocTroChuyen());
            payload.put("tinNhan", saved);
            chatWebSocketHandler.broadcast(ctc.getMaCuocTroChuyen(), payload);
        } catch (Exception e) {
            log.error("Lỗi gửi WebSocket Auto-responder: {}", e.getMessage());
        }

        log.info("🤖 [Auto-responder US-60] Đã tự động gửi kịch bản '{}' cho khách hàng #{}",
                cauHinh.getTieuDe(), ctc.getKhachHang().getMaNguoiDung());

        return saved;
    }
}
