package com.example.demo.service;

import com.example.demo.dto.ThongKeViXuDTO;
import com.example.demo.entity.DonHangTong;
import com.example.demo.entity.LichSuGiaoDichXu;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.ViXuNguoiDung;
import com.example.demo.repository.DonHangTongRepository;
import com.example.demo.repository.LichSuGiaoDichXuRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.ViXuNguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service nghiệp vụ Hệ Thống Điểm Thưởng / Xu (Coin Reward System - US-55)
 * Hỗ trợ: Tích xu 1% mua hàng, Điểm danh nhận 1.000 xu, Giảm trừ trực tiếp vào đơn hàng, Hoàn xu
 */
@Service
public class ViXuService {

    public static final long XU_DIEM_DANH_HANG_NGAY = 1000L;
    public static final double TY_LE_TICH_XU_DON_HANG = 0.01; // 1% giá trị tiền hàng

    @Autowired
    private ViXuNguoiDungRepository viXuNguoiDungRepository;

    @Autowired
    private LichSuGiaoDichXuRepository lichSuGiaoDichXuRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    // =========================================================================
    // 1. QUẢN LÝ VÍ XU & ĐIỂM DANH HÀNG NGÀY
    // =========================================================================

    /**
     * Lấy thông tin ví xu của người dùng, nếu chưa có sẽ tự động khởi tạo
     */
    @Transactional
    public ViXuNguoiDung layHoacTaoViXu(Long maNguoiDung) {
        if (maNguoiDung == null) {
            throw new IllegalArgumentException("Mã người dùng không được để trống!");
        }

        return viXuNguoiDungRepository.findByMaNguoiDung(maNguoiDung)
                .orElseGet(() -> {
                    NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng #" + maNguoiDung));

                    ViXuNguoiDung viMoi = new ViXuNguoiDung();
                    viMoi.setMaNguoiDung(maNguoiDung);
                    viMoi.setNguoiDung(nguoiDung);
                    viMoi.setSoXuHienTai(0L);
                    viMoi.setTongXuDaTichLuy(0L);
                    viMoi.setNgayCapNhat(LocalDateTime.now());
                    viMoi.setPhienBanLock(1);
                    return viXuNguoiDungRepository.save(viMoi);
                });
    }

    /**
     * Điểm danh nhận thưởng xu hàng ngày (+1.000 Xu)
     * Validate: Chặn điểm danh lần thứ 2 trong cùng 1 ngày
     */
    @Transactional
    public Long diemDanhNhanXu(Long maNguoiDung) {
        LocalDateTime dauNgay = LocalDate.now().atStartOfDay();
        LocalDateTime cuoiNgay = LocalDate.now().atTime(LocalTime.MAX);

        boolean daDiemDanh = lichSuGiaoDichXuRepository.daDiemDanhHomNay(maNguoiDung, dauNgay, cuoiNgay);
        if (daDiemDanh) {
            throw new IllegalStateException("Hôm nay bạn đã điểm danh nhận xu rồi! Hãy quay lại vào ngày mai nhé.");
        }

        ViXuNguoiDung viXu = layHoacTaoViXu(maNguoiDung);
        viXu.setSoXuHienTai(viXu.getSoXuHienTai() + XU_DIEM_DANH_HANG_NGAY);
        viXu.setTongXuDaTichLuy(viXu.getTongXuDaTichLuy() + XU_DIEM_DANH_HANG_NGAY);
        viXu.setNgayCapNhat(LocalDateTime.now());
        viXuNguoiDungRepository.save(viXu);

        // Ghi vết lịch sử giao dịch xu
        LichSuGiaoDichXu ls = new LichSuGiaoDichXu();
        ls.setNguoiDung(viXu.getNguoiDung());
        ls.setSoXuThayDoi(XU_DIEM_DANH_HANG_NGAY);
        ls.setLoaiGiaoDich("DIEM_DANH_HANG_NGAY");
        ls.setMaThamChieu("CHECKIN-" + LocalDate.now());
        ls.setMoTa("Điểm danh hàng ngày nhận thưởng FlexShop Xu");
        ls.setSoXuSauGiaoDich(viXu.getSoXuHienTai());
        ls.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichXuRepository.save(ls);

        return XU_DIEM_DANH_HANG_NGAY;
    }

    // =========================================================================
    // 2. SỬ DỤNG XU ĐỂ GIẢM TRỪ VÀO ĐƠN HÀNG (CHECKOUT)
    // =========================================================================

    /**
     * Tính số xu tối đa khách hàng có thể áp dụng cho đơn hàng hiện tại
     * Quy tắc: Không vượt quá số dư xu, không vượt quá 50% giá trị thanh toán đơn hàng
     */
    @Transactional(readOnly = true)
    public Long tinhSoXuToiDaChoPhep(Long maNguoiDung, DonHangTong donHang) {
        ViXuNguoiDung viXu = viXuNguoiDungRepository.findByMaNguoiDung(maNguoiDung).orElse(null);
        if (viXu == null || viXu.getSoXuHienTai() <= 0) {
            return 0L;
        }

        // Tính số tiền chưa tính giảm từ xu
        BigDecimal tienHienTai = donHang.getTongThanhToanCuoi();
        if (donHang.getSoTienGiamTuXu() != null) {
            tienHienTai = tienHienTai.add(donHang.getSoTienGiamTuXu());
        }

        if (tienHienTai.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }

        // Giới hạn tối đa 50% giá trị đơn hàng
        BigDecimal toiDa50PhanTram = tienHienTai.multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.FLOOR);
        long gioiHanDonHang = toiDa50PhanTram.longValue();

        return Math.min(viXu.getSoXuHienTai(), Math.max(0L, gioiHanDonHang));
    }

    /**
     * Áp dụng Xu để giảm trừ trực tiếp vào giá thanh toán đơn hàng
     */
    @Transactional
    public DonHangTong apDungXuVaoDonHang(Long maNguoiDung, String maCodeDonTong, Long soXuMuonDung) {
        if (soXuMuonDung == null || soXuMuonDung < 100) {
            throw new IllegalArgumentException("Số xu sử dụng tối thiểu phải từ 100 xu trở lên!");
        }

        DonHangTong donHang = donHangTongRepository.findByMaCodeDonTong(maCodeDonTong)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + maCodeDonTong));

        if (!donHang.getKhachHang().getMaNguoiDung().equals(maNguoiDung)) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên đơn hàng của người khác!");
        }

        if ("DA_HUY".equalsIgnoreCase(donHang.getTrangThaiDonHang()) || "DA_GIAO".equalsIgnoreCase(donHang.getTrangThaiDonHang())) {
            throw new IllegalArgumentException("Đơn hàng ở trạng thái này không thể áp dụng xu!");
        }

        // Khôi phục xu cũ nếu trước đó đơn hàng đã từng áp dụng
        if (donHang.getSoXuDaDung() != null && donHang.getSoXuDaDung() > 0) {
            huyApDungXu(maNguoiDung, maCodeDonTong);
            // Tải lại đối tượng đơn hàng sau khi khôi phục
            donHang = donHangTongRepository.findByMaCodeDonTong(maCodeDonTong).orElseThrow();
        }

        ViXuNguoiDung viXu = layHoacTaoViXu(maNguoiDung);
        if (soXuMuonDung > viXu.getSoXuHienTai()) {
            throw new IllegalArgumentException("Số xu sử dụng (" + soXuMuonDung + ") vượt quá số dư khả dụng trong ví (" + viXu.getSoXuHienTai() + " xu)!");
        }

        BigDecimal tienTruocGiamXu = donHang.getTongThanhToanCuoi();
        BigDecimal toiDa50 = tienTruocGiamXu.multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.FLOOR);
        if (soXuMuonDung > toiDa50.longValue()) {
            throw new IllegalArgumentException("Chính sách ưu đãi chỉ cho phép dùng xu giảm tối đa 50% đơn hàng (" + toiDa50.longValue() + " xu)!");
        }

        // Trừ xu trong ví
        viXu.setSoXuHienTai(viXu.getSoXuHienTai() - soXuMuonDung);
        viXu.setNgayCapNhat(LocalDateTime.now());
        viXuNguoiDungRepository.save(viXu);

        // Giảm trừ vào đơn hàng (1 Xu = 1 VNĐ)
        BigDecimal soTienGiamTuXu = BigDecimal.valueOf(soXuMuonDung);
        donHang.setSoXuDaDung(soXuMuonDung);
        donHang.setSoTienGiamTuXu(soTienGiamTuXu);
        donHang.setTongThanhToanCuoi(tienTruocGiamXu.subtract(soTienGiamTuXu));
        donHang = donHangTongRepository.save(donHang);

        // Ghi vết lịch sử biến động xu
        LichSuGiaoDichXu ls = new LichSuGiaoDichXu();
        ls.setNguoiDung(viXu.getNguoiDung());
        ls.setSoXuThayDoi(-soXuMuonDung);
        ls.setLoaiGiaoDich("TRU_XU_DON_HANG");
        ls.setMaThamChieu(maCodeDonTong);
        ls.setMoTa("Dùng " + soXuMuonDung + " xu giảm trực tiếp vào đơn hàng #" + maCodeDonTong);
        ls.setSoXuSauGiaoDich(viXu.getSoXuHienTai());
        ls.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichXuRepository.save(ls);

        return donHang;
    }

    /**
     * Hủy sử dụng xu trên đơn hàng và hoàn trả lại ví
     */
    @Transactional
    public DonHangTong huyApDungXu(Long maNguoiDung, String maCodeDonTong) {
        DonHangTong donHang = donHangTongRepository.findByMaCodeDonTong(maCodeDonTong)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + maCodeDonTong));

        if (donHang.getSoXuDaDung() == null || donHang.getSoXuDaDung() <= 0) {
            return donHang; // Không có xu để hoàn
        }

        Long soXuHoan = donHang.getSoXuDaDung();
        BigDecimal soTienHoan = donHang.getSoTienGiamTuXu() != null ? donHang.getSoTienGiamTuXu() : BigDecimal.valueOf(soXuHoan);

        // Hoàn xu về ví
        ViXuNguoiDung viXu = layHoacTaoViXu(maNguoiDung);
        viXu.setSoXuHienTai(viXu.getSoXuHienTai() + soXuHoan);
        viXu.setNgayCapNhat(LocalDateTime.now());
        viXuNguoiDungRepository.save(viXu);

        // Cập nhật lại đơn hàng
        donHang.setTongThanhToanCuoi(donHang.getTongThanhToanCuoi().add(soTienHoan));
        donHang.setSoXuDaDung(0L);
        donHang.setSoTienGiamTuXu(BigDecimal.ZERO);
        donHang = donHangTongRepository.save(donHang);

        // Ghi vết lịch sử
        LichSuGiaoDichXu ls = new LichSuGiaoDichXu();
        ls.setNguoiDung(viXu.getNguoiDung());
        ls.setSoXuThayDoi(soXuHoan);
        ls.setLoaiGiaoDich("HOAN_XU_HUY_DON");
        ls.setMaThamChieu(maCodeDonTong);
        ls.setMoTa("Hoàn lại " + soXuHoan + " xu khi hủy áp dụng xu trên đơn hàng #" + maCodeDonTong);
        ls.setSoXuSauGiaoDich(viXu.getSoXuHienTai());
        ls.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichXuRepository.save(ls);

        return donHang;
    }

    // =========================================================================
    // 3. TÍCH XU KHI MUA HÀNG THÀNH CÔNG & HOÀN XU KHI HỦY ĐƠN
    // =========================================================================

    /**
     * Tự động tích lũy 1% Xu khi đơn hàng hoàn thành
     */
    @Transactional
    public Long tichXuKhiHoanThanhDon(String maCodeDonTong) {
        DonHangTong donHang = donHangTongRepository.findByMaCodeDonTong(maCodeDonTong)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + maCodeDonTong));

        BigDecimal tienHang = donHang.getTongTienHang();
        if (tienHang == null || tienHang.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }

        long soXuTich = (long) Math.floor(tienHang.doubleValue() * TY_LE_TICH_XU_DON_HANG);
        if (soXuTich <= 0) return 0L;

        Long maNguoiDung = donHang.getKhachHang().getMaNguoiDung();
        ViXuNguoiDung viXu = layHoacTaoViXu(maNguoiDung);
        viXu.setSoXuHienTai(viXu.getSoXuHienTai() + soXuTich);
        viXu.setTongXuDaTichLuy(viXu.getTongXuDaTichLuy() + soXuTich);
        viXu.setNgayCapNhat(LocalDateTime.now());
        viXuNguoiDungRepository.save(viXu);

        LichSuGiaoDichXu ls = new LichSuGiaoDichXu();
        ls.setNguoiDung(viXu.getNguoiDung());
        ls.setSoXuThayDoi(soXuTich);
        ls.setLoaiGiaoDich("TICH_XU_DON_HANG");
        ls.setMaThamChieu(maCodeDonTong);
        ls.setMoTa("Tích 1% xu từ đơn hàng hoàn thành #" + maCodeDonTong);
        ls.setSoXuSauGiaoDich(viXu.getSoXuHienTai());
        ls.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichXuRepository.save(ls);

        return soXuTich;
    }

    // =========================================================================
    // 4. THỐNG KÊ & TRUY VẤN LỊCH SỬ GIAO DỊCH PHÂN TRANG
    // =========================================================================

    /**
     * Thống kê toàn diện thông tin Ví Xu cho Dashboard khách hàng
     */
    @Transactional(readOnly = true)
    public ThongKeViXuDTO layThongKeViXu(Long maNguoiDung) {
        ViXuNguoiDung viXu = viXuNguoiDungRepository.findByMaNguoiDung(maNguoiDung).orElse(null);
        long soXuHienTai = viXu != null ? viXu.getSoXuHienTai() : 0L;
        long tongTichLuy = viXu != null ? viXu.getTongXuDaTichLuy() : 0L;

        LocalDateTime dauNgay = LocalDate.now().atStartOfDay();
        LocalDateTime cuoiNgay = LocalDate.now().atTime(LocalTime.MAX);
        boolean daDiemDanh = lichSuGiaoDichXuRepository.daDiemDanhHomNay(maNguoiDung, dauNgay, cuoiNgay);

        LocalDateTime dauThang = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Long daTieuThang = lichSuGiaoDichXuRepository.tinhTongXuDaTieu(maNguoiDung, dauThang);
        Long daTichThang = lichSuGiaoDichXuRepository.tinhTongXuDaTichTrongThang(maNguoiDung, dauThang);

        return ThongKeViXuDTO.builder()
                .soXuHienTai(soXuHienTai)
                .giaTriQuyDoiVnd(soXuHienTai)
                .tongXuDaTichLuy(tongTichLuy)
                .tongXuDaTieuTrongThang(daTieuThang != null ? daTieuThang : 0L)
                .tongXuTichTrongThang(daTichThang != null ? daTichThang : 0L)
                .daDiemDanhHomNay(daDiemDanh)
                .xuNhanKhiDiemDanh(XU_DIEM_DANH_HANG_NGAY)
                .build();
    }

    /**
     * Lấy danh sách lịch sử biến động xu phân trang và lọc đa năng
     */
    @Transactional(readOnly = true)
    public Page<LichSuGiaoDichXu> layLichSuGiaoDichPhanTrang(Long maNguoiDung, String loaiGiaoDich, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        String loai = (loaiGiaoDich != null && !loaiGiaoDich.isBlank()) ? loaiGiaoDich.trim() : "TAT_CA";
        return lichSuGiaoDichXuRepository.timKiemLichSuGiaoDich(maNguoiDung, loai, pageable);
    }
}
