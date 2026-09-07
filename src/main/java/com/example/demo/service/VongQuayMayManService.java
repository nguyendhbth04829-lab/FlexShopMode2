package com.example.demo.service;

import com.example.demo.dto.KetQuaQuayThuongDTO;
import com.example.demo.dto.ThongKeVongQuayDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service nghiệp vụ Vòng Quay May Mắn (Shopee Lucky Wheel - US-66)
 * Xử lý: Quay thưởng xác suất trung thực, Trả thưởng tức thì vào Ví Xu / Voucher,
 * Giới hạn 1 lượt quay miễn phí mỗi ngày, Đổi xu lấy thêm lượt quay, Phân trang và Thống kê.
 */
@Slf4j
@Service
public class VongQuayMayManService {

    public static final long GIA_XU_DOI_LUOT_QUAY = 1000L; // 1.000 Xu = 1 lượt quay thêm
    public static final int SO_LUOT_QUAY_MIEN_PHI_MOI_NGAY = 1;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Lưu trữ số lượt quay mua thêm trong ngày (Thread-safe Cache)
    private final Map<String, Integer> luotQuayMuaThemMap = new ConcurrentHashMap<>();

    @Autowired
    private VongQuayMayManRepository vongQuayMayManRepository;

    @Autowired
    private PhanThuongVongQuayRepository phanThuongVongQuayRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private ViXuNguoiDungRepository viXuNguoiDungRepository;

    @Autowired
    private LichSuGiaoDichXuRepository lichSuGiaoDichXuRepository;

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    @Autowired
    private ViXuService viXuService;

    // =========================================================================
    // 1. LẤY CẤU HÌNH PHẦN THƯỞNG & THÔNG TIN LƯỢT QUAY
    // =========================================================================

    /**
     * Lấy danh sách 8 ô phần thưởng vòng quay đang hoạt động
     */
    public List<PhanThuongVongQuay> layDanhSachPhanThuong() {
        List<PhanThuongVongQuay> list = phanThuongVongQuayRepository.findByDangHoatDongTrueOrderByThuTuOAsc();
        if (list.isEmpty()) {
            log.warn("Chưa có cấu hình phần thưởng vòng quay trong CSDL. Vui lòng nạp script SQL!");
        }
        return list;
    }

    /**
     * Tính số lượt quay còn lại hôm nay của người dùng
     */
    public int tinhSoLuotQuayConLai(Long maNguoiDung) {
        if (maNguoiDung == null) return 0;

        LocalDateTime dauNgay = LocalDate.now().atStartOfDay();
        LocalDateTime cuoiNgay = LocalDate.now().atTime(LocalTime.MAX);

        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung).orElse(null);
        if (nguoiDung == null) return 0;

        long daQuayHomNay = vongQuayMayManRepository.countByNguoiDungAndNgayQuayBetween(nguoiDung, dauNgay, cuoiNgay);
        int luotMuaThem = laySoLuotMuaThemHomNay(maNguoiDung);

        int tongLuotDuocQuay = SO_LUOT_QUAY_MIEN_PHI_MOI_NGAY + luotMuaThem;
        int conLai = tongLuotDuocQuay - (int) daQuayHomNay;
        return Math.max(0, conLai);
    }

    private int laySoLuotMuaThemHomNay(Long maNguoiDung) {
        LocalDateTime dauNgay = LocalDate.now().atStartOfDay();
        LocalDateTime cuoiNgay = LocalDate.now().atTime(LocalTime.MAX);
        return (int) lichSuGiaoDichXuRepository.demSoLuotDoiXuHomNay(maNguoiDung, dauNgay, cuoiNgay);
    }

    private void tangSoLuotMuaThemHomNay(Long maNguoiDung) {
        // Đã được ghi vết tự động và bền vững qua bảng lich_su_giao_dich_xu
    }

    // =========================================================================
    // 2. THỰC HIỆN QUAY THƯỞNG (CORE LOGIC)
    // =========================================================================

    /**
     * Xử lý lượt quay thưởng của khách hàng (Transaction an toàn, trả thưởng tức thì)
     */
    @Transactional
    public KetQuaQuayThuongDTO thucHienQuayThuong(Long maNguoiDung, String diaChiIp) {
        if (maNguoiDung == null) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ!");
        }

        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản người dùng #" + maNguoiDung));

        // 1. Kiểm tra số lượt quay còn lại
        int soLuotConLai = tinhSoLuotQuayConLai(maNguoiDung);
        if (soLuotConLai <= 0) {
            throw new IllegalStateException("Bạn đã sử dụng hết lượt quay hôm nay! Hãy đổi 1.000 Xu để nhận thêm lượt quay hoặc quay lại vào ngày mai nhé.");
        }

        // 2. Lấy danh sách 8 ô phần thưởng hợp lệ
        List<PhanThuongVongQuay> dsPhanThuong = phanThuongVongQuayRepository.findByDangHoatDongTrueOrderByThuTuOAsc();
        if (dsPhanThuong.isEmpty()) {
            throw new IllegalStateException("Hệ thống vòng quay hiện đang bảo trì phần thưởng. Vui lòng thử lại sau!");
        }

        // 3. Thuật toán chọn phần thưởng theo xác suất (Weighted Random Selection)
        PhanThuongVongQuay phanThuongTrungs = chonPhanThuongTheoXacSuat(dsPhanThuong);

        // 4. Cập nhật số lượng trúng thưởng của ô đó
        phanThuongTrungs.setSoLuongDaTrung(phanThuongTrungs.getSoLuongDaTrung() + 1);
        phanThuongVongQuayRepository.save(phanThuongTrungs);

        // 5. Tính góc dừng bánh xe (360 độ / 8 ô = 45 độ mỗi ô)
        // Ô số k (1..8) nằm ở góc: [(k-1)*45, k*45] độ
        // Ta thêm số vòng quay cố định (5 vòng = 1800 độ) cộng với góc giữa ô để tạo hiệu ứng quay mượt mà
        int thuTuO = phanThuongTrungs.getThuTuO();
        int gocTrungTamO = (thuTuO - 1) * 45 + 22; // Tâm ô thưởng
        int tongGocQuay = 1800 + gocTrungTamO;

        // 6. Xử lý trả thưởng tức thì
        String moTaKetQua;
        String trangThaiTraThuong = "Đã hoàn thành";
        Long soXuNhan = 0L;
        String maCodeVoucher = null;
        String tenVoucher = null;

        if ("XU".equalsIgnoreCase(phanThuongTrungs.getLoaiPhanThuong())) {
            soXuNhan = phanThuongTrungs.getGiaTriXu();
            congXuVaoViNguoiDung(nguoiDung, soXuNhan, phanThuongTrungs.getTenPhanThuong());
            trangThaiTraThuong = "Đã cộng " + String.format("%,d", soXuNhan) + " Xu vào ví";
            moTaKetQua = "Chúc mừng bạn nhận được " + String.format("%,d", soXuNhan) + " Xu FlexShop!";
        } else if ("VOUCHER".equalsIgnoreCase(phanThuongTrungs.getLoaiPhanThuong())) {
            if (phanThuongTrungs.getMaVoucher() != null) {
                MaGiamGia voucher = maGiamGiaRepository.findById(phanThuongTrungs.getMaVoucher()).orElse(null);
                if (voucher != null) {
                    maCodeVoucher = voucher.getMaCodeVoucher();
                    tenVoucher = voucher.getTenVoucher();
                }
            }
            if (maCodeVoucher == null) {
                maCodeVoucher = "FLEXMAYMAN";
                tenVoucher = phanThuongTrungs.getTenPhanThuong();
            }
            trangThaiTraThuong = "Đã lưu Voucher " + maCodeVoucher;
            moTaKetQua = "Chúc mừng bạn trúng Voucher: " + phanThuongTrungs.getTenPhanThuong() + " (Mã: " + maCodeVoucher + ")!";
        } else {
            // Chúc may mắn lần sau
            trangThaiTraThuong = "Chúc may mắn";
            moTaKetQua = "Cảm ơn bạn đã tham gia! Chúc bạn may mắn lần sau.";
        }

        // 7. Ghi vết lịch sử lượt quay vào bảng vong_quay_may_man
        VongQuayMayMan luotQuay = new VongQuayMayMan();
        luotQuay.setNguoiDung(nguoiDung);
        luotQuay.setPhanThuong(phanThuongTrungs.getTenPhanThuong());
        luotQuay.setLoaiPhanThuong(phanThuongTrungs.getLoaiPhanThuong());
        luotQuay.setSoXuNhan(soXuNhan);
        luotQuay.setMaVoucher(phanThuongTrungs.getMaVoucher());
        luotQuay.setMaCodeVoucher(maCodeVoucher);
        luotQuay.setTrangThaiTraThuong(trangThaiTraThuong);
        luotQuay.setGocQuayDo(tongGocQuay);
        luotQuay.setMoTaKetQua(moTaKetQua);
        luotQuay.setNgayQuay(LocalDateTime.now());
        VongQuayMayMan daLuu = vongQuayMayManRepository.save(luotQuay);

        log.info("Lượt quay #{} thành công cho khách hàng #{} ({}) -> Trúng: {}",
                daLuu.getMaLuotQuay(), nguoiDung.getMaNguoiDung(), nguoiDung.getHoVaTen(), phanThuongTrungs.getTenPhanThuong());

        // Lấy số dư ví xu mới nhất
        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(maNguoiDung);
        int soLuotConLaiMoi = tinhSoLuotQuayConLai(maNguoiDung);

        return KetQuaQuayThuongDTO.builder()
                .thanhCong(true)
                .thongBao(moTaKetQua)
                .maLuotQuay(daLuu.getMaLuotQuay())
                .maPhanThuong(phanThuongTrungs.getMaPhanThuong())
                .tenPhanThuong(phanThuongTrungs.getTenPhanThuong())
                .loaiPhanThuong(phanThuongTrungs.getLoaiPhanThuong())
                .giaTriXu(soXuNhan)
                .maCodeVoucher(maCodeVoucher)
                .tenVoucher(tenVoucher)
                .thuTuO(thuTuO)
                .gocDung(tongGocQuay)
                .soXuHienTai(viXu.getSoXuHienTai())
                .soLuotQuayConLai(soLuotConLaiMoi)
                .moTa(moTaKetQua)
                .build();
    }

    /**
     * Thuật toán chọn phần thưởng theo tỷ lệ xác suất (Weighted Random)
     */
    private PhanThuongVongQuay chonPhanThuongTheoXacSuat(List<PhanThuongVongQuay> danhSach) {
        double tongTyLe = danhSach.stream()
                .mapToDouble(p -> p.getTyLeTrung() != null ? p.getTyLeTrung() : 0.0)
                .sum();

        if (tongTyLe <= 0) {
            return danhSach.get(0);
        }

        double rand = Math.random() * tongTyLe;
        double tichLuy = 0.0;

        for (PhanThuongVongQuay pt : danhSach) {
            tichLuy += (pt.getTyLeTrung() != null ? pt.getTyLeTrung() : 0.0);
            if (rand <= tichLuy && pt.conPhanThuong()) {
                return pt;
            }
        }

        // Mặc định trả về phần thưởng đầu tiên nếu không khớp
        return danhSach.get(0);
    }

    /**
     * Cộng xu trực tiếp vào Ví Xu và ghi vết lịch sử giao dịch xu
     */
    private void congXuVaoViNguoiDung(NguoiDung nguoiDung, Long soXu, String tenPhanThuong) {
        if (soXu == null || soXu <= 0) return;

        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(nguoiDung.getMaNguoiDung());
        viXu.setSoXuHienTai(viXu.getSoXuHienTai() + soXu);
        viXu.setTongXuDaTichLuy(viXu.getTongXuDaTichLuy() + soXu);
        viXu.setNgayCapNhat(LocalDateTime.now());
        viXuNguoiDungRepository.save(viXu);

        LichSuGiaoDichXu ls = new LichSuGiaoDichXu();
        ls.setNguoiDung(nguoiDung);
        ls.setSoXuThayDoi(soXu);
        ls.setLoaiGiaoDich("VONG_QUAY_MAY_MAN");
        ls.setMaThamChieu("LUCKY-SPIN-" + System.currentTimeMillis());
        ls.setMoTa("Trúng thưởng Vòng Quay May Mắn FlexShop: " + tenPhanThuong);
        ls.setSoXuSauGiaoDich(viXu.getSoXuHienTai());
        ls.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichXuRepository.save(ls);
    }

    // =========================================================================
    // 3. ĐỔI XU LẤY THÊM LƯỢT QUAY
    // =========================================================================

    /**
     * Dùng 1.000 Xu để đổi lấy +1 lượt quay thêm
     */
    @Transactional
    public Long doiXuLayLuotQuay(Long maNguoiDung) {
        if (maNguoiDung == null) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ!");
        }

        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản người dùng #" + maNguoiDung));

        ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(maNguoiDung);
        if (viXu.getSoXuHienTai() < GIA_XU_DOI_LUOT_QUAY) {
            throw new IllegalStateException("Số dư ví Xu không đủ 1.000 Xu để đổi thêm lượt quay! Hiện bạn chỉ có " +
                    String.format("%,d", viXu.getSoXuHienTai()) + " Xu.");
        }

        // Trừ 1.000 Xu
        viXu.setSoXuHienTai(viXu.getSoXuHienTai() - GIA_XU_DOI_LUOT_QUAY);
        viXu.setNgayCapNhat(LocalDateTime.now());
        viXuNguoiDungRepository.save(viXu);

        // Ghi vết giao dịch trừ xu
        LichSuGiaoDichXu ls = new LichSuGiaoDichXu();
        ls.setNguoiDung(nguoiDung);
        ls.setSoXuThayDoi(-GIA_XU_DOI_LUOT_QUAY);
        ls.setLoaiGiaoDich("TRU_XU_DOI_LUOT_QUAY");
        ls.setMaThamChieu("SPIN-BUY-" + System.currentTimeMillis());
        ls.setMoTa("Đổi 1.000 Xu lấy +1 lượt quay Vòng Quay May Mắn");
        ls.setSoXuSauGiaoDich(viXu.getSoXuHienTai());
        ls.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichXuRepository.save(ls);

        // Tăng lượt quay mua thêm
        tangSoLuotMuaThemHomNay(maNguoiDung);

        log.info("Khách hàng #{} đã đổi 1.000 Xu lấy +1 lượt quay thành công. Số dư còn: {}",
                maNguoiDung, viXu.getSoXuHienTai());

        return viXu.getSoXuHienTai();
    }

    // =========================================================================
    // 4. LỊCH SỬ QUAY THƯỞNG CÁ NHÂN & TOÀN SÀN (PHÂN TRANG & TÌM LỌC)
    // =========================================================================

    /**
     * Lấy lịch sử quay cá nhân của người dùng có phân trang và lọc theo loại phần thưởng
     */
    public Page<VongQuayMayMan> layLichSuQuayCaNhan(Long maNguoiDung, String loaiPhanThuong, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung).orElse(null);
        if (nguoiDung == null) {
            return Page.empty();
        }

        if (loaiPhanThuong != null && !loaiPhanThuong.isBlank() && !"TAT_CA".equalsIgnoreCase(loaiPhanThuong)) {
            return vongQuayMayManRepository.findByNguoiDungAndLoaiPhanThuongOrderByNgayQuayDesc(
                    nguoiDung, loaiPhanThuong.trim().toUpperCase(), pageable);
        }

        return vongQuayMayManRepository.findByNguoiDungOrderByNgayQuayDesc(nguoiDung, pageable);
    }

    /**
     * Lấy danh sách 10 người trúng thưởng mới nhất cho Live Ticker
     */
    public List<VongQuayMayMan> layDanhSachVinhDanhMoiNhat() {
        return vongQuayMayManRepository.findTop10ByOrderByNgayQuayDesc();
    }

    /**
     * Lấy thống kê tổng quan Vòng quay
     */
    public ThongKeVongQuayDTO layThongKeVongQuay(Long maNguoiDung) {
        LocalDateTime dauNgay = LocalDate.now().atStartOfDay();
        LocalDateTime cuoiNgay = LocalDate.now().atTime(LocalTime.MAX);

        Long tongLuotQuayToanSan = vongQuayMayManRepository.count();
        Long tongXuDaPhat = vongQuayMayManRepository.sumTongXuDaPhat();
        Long tongVoucherDaTrao = vongQuayMayManRepository.countTongVoucherDaTrao();
        Long tongLuotQuayHomNay = vongQuayMayManRepository.countLuotQuayHomNayToanSan(dauNgay, cuoiNgay);

        Long soLuotQuayCuaToi = 0L;
        Long soXuHienTai = 0L;
        int soLuotConLaiHomNay = 0;
        boolean daDungMienPhi = false;

        if (maNguoiDung != null) {
            NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung).orElse(null);
            if (nguoiDung != null) {
                long daQuayHomNay = vongQuayMayManRepository.countByNguoiDungAndNgayQuayBetween(nguoiDung, dauNgay, cuoiNgay);
                daDungMienPhi = (daQuayHomNay >= SO_LUOT_QUAY_MIEN_PHI_MOI_NGAY);
                soLuotConLaiHomNay = tinhSoLuotQuayConLai(maNguoiDung);

                ViXuNguoiDung viXu = viXuService.layHoacTaoViXu(maNguoiDung);
                soXuHienTai = viXu.getSoXuHienTai();
                soLuotQuayCuaToi = (long) vongQuayMayManRepository.findByNguoiDungOrderByNgayQuayDesc(nguoiDung, PageRequest.of(0, 1)).getTotalElements();
            }
        }

        return ThongKeVongQuayDTO.builder()
                .tongLuotQuayToanSan(tongLuotQuayToanSan != null ? tongLuotQuayToanSan : 0L)
                .tongXuDaPhat(tongXuDaPhat != null ? tongXuDaPhat : 0L)
                .tongVoucherDaTrao(tongVoucherDaTrao != null ? tongVoucherDaTrao : 0L)
                .tongLuotQuayHomNay(tongLuotQuayHomNay != null ? tongLuotQuayHomNay : 0L)
                .soLuotQuayCuaToi(soLuotQuayCuaToi)
                .soXuHienTai(soXuHienTai)
                .soLuotQuayConLaiHomNay(soLuotConLaiHomNay)
                .daDungLuotMienPhiHomNay(daDungMienPhi)
                .build();
    }

    /**
     * Tìm kiếm và lọc lịch sử toàn sàn cho Quản trị viên
     */
    public Page<VongQuayMayMan> layLichSuQuayToanSan(String tuKhoa, String loaiPhanThuong, String tuNgayStr, String denNgayStr, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));

        LocalDateTime tuNgay = null;
        LocalDateTime denNgay = null;

        try {
            if (tuNgayStr != null && !tuNgayStr.isBlank()) {
                tuNgay = LocalDate.parse(tuNgayStr).atStartOfDay();
            }
            if (denNgayStr != null && !denNgayStr.isBlank()) {
                denNgay = LocalDate.parse(denNgayStr).atTime(LocalTime.MAX);
            }
        } catch (Exception ignored) {}

        return vongQuayMayManRepository.timKiemVaLocToanSan(
                tuKhoa != null && !tuKhoa.isBlank() ? tuKhoa.trim() : null,
                loaiPhanThuong != null && !loaiPhanThuong.isBlank() ? loaiPhanThuong.trim() : null,
                tuNgay, denNgay, pageable);
    }
}
