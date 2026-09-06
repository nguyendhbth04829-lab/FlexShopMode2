package com.example.demo.service;

import com.example.demo.dto.RutTienNguoiBanRequestDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & RÚT TIỀN (DEV 5 - MINH)
 * USER STORY: US-44 - Nghiệp Vụ Rút Tiền Người Bán Về Ngân Hàng
 * =====================================================================
 * Validate chi tiết & Nghiệp vụ chuyên nghiệp:
 *   1. Hạn mức rút tối thiểu: so_tien_rut >= 50,000 VNĐ.
 *   2. Kiểm tra số dư: so_tien_rut <= so_du_kha_dung hiện có.
 *   3. Chống Double-Spending: Trừ ngay số dư khả dụng khi gửi yêu cầu và ghi log.
 *   4. Phê duyệt Admin: Chuyển DA_DUYET và cộng dồn tong_tien_da_rut.
 *   5. Từ chối Admin: Chuyển TU_CHOI và tự động hoàn trả tiền về so_du_kha_dung kèm log.
 *   6. Khóa lạc quan (Optimistic Locking): Bảo vệ chống Race Condition.
 * =====================================================================
 */
@Service
public class RutTienNguoiBanService {

    @Autowired
    private YeuCauRutTienRepository yeuCauRutTienRepository;

    @Autowired
    private ViNguoiBanRepository viNguoiBanRepository;

    @Autowired
    private LichSuGiaoDichViRepository lichSuGiaoDichViRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private KyQuyService kyQuyService;

    public static final BigDecimal HAN_MUC_RUT_TOI_THIEU = new BigDecimal("50000");

    /**
     * [US-44] Tạo yêu cầu rút tiền người bán với cơ chế trừ tiền ngay lập tức (Chống Double Spending)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public YeuCauRutTien taoYeuCauRutTien(RutTienNguoiBanRequestDTO dto) {
        // 1. Validate DTO đầu vào
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu yêu cầu rút tiền không được để trống!");
        }
        if (dto.getMaGianHang() == null) {
            throw new IllegalArgumentException("Vui lòng chọn gian hàng cần rút tiền!");
        }

        GianHang gianHang = gianHangRepository.findById(dto.getMaGianHang())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy gian hàng với mã ID: " + dto.getMaGianHang()));

        ViNguoiBan vi = kyQuyService.getOrCreateViNguoiBan(gianHang);

        // 2. Validate số tiền rút
        BigDecimal soTienRut = dto.getSoTienRut();
        if (soTienRut == null || soTienRut.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0 VNĐ!");
        }
        if (soTienRut.compareTo(HAN_MUC_RUT_TOI_THIEU) < 0) {
            throw new IllegalArgumentException("Hạn mức rút tiền tối thiểu là " + String.format("%,.0f VNĐ", HAN_MUC_RUT_TOI_THIEU) + "!");
        }
        if (soTienRut.compareTo(vi.getSoDuKhaDung()) > 0) {
            throw new IllegalArgumentException("Số tiền rút (" + String.format("%,.0f đ", soTienRut) +
                    ") vượt quá số dư khả dụng hiện có (" + String.format("%,.0f đ", vi.getSoDuKhaDung()) + ")!");
        }

        // 3. Validate thông tin tài khoản ngân hàng
        if (dto.getTenNganHang() == null || dto.getTenNganHang().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn tên ngân hàng nhận tiền!");
        }
        if (dto.getSoTaiKhoan() == null || dto.getSoTaiKhoan().trim().length() < 6) {
            throw new IllegalArgumentException("Số tài khoản ngân hàng không hợp lệ (tối thiểu 6 chữ số)!");
        }
        if (dto.getTenChuTaiKhoan() == null || dto.getTenChuTaiKhoan().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên chủ tài khoản ngân hàng (viết hoa không dấu)!");
        }

        // 4. US-44: TRỪ NGAY số dư khả dụng trong ví người bán để chống Double Spending
        vi.setSoDuKhaDung(vi.getSoDuKhaDung().subtract(soTienRut));
        vi.setNgayCapNhat(LocalDateTime.now());
        viNguoiBanRepository.save(vi);

        // 5. Lưu bản ghi yêu cầu rút tiền
        YeuCauRutTien yeuCau = new YeuCauRutTien();
        yeuCau.setGianHang(gianHang);
        yeuCau.setSoTienRut(soTienRut);
        yeuCau.setTenNganHang(dto.getTenNganHang().trim());
        yeuCau.setSoTaiKhoan(dto.getSoTaiKhoan().trim());
        yeuCau.setTenChuTaiKhoan(dto.getTenChuTaiKhoan().trim().toUpperCase());
        yeuCau.setTrangThai("CHO_DUYET");
        yeuCau.setNgayTao(LocalDateTime.now());

        YeuCauRutTien savedYeuCau = yeuCauRutTienRepository.save(yeuCau);

        // 6. Ghi Sổ cái biến động số dư ví (lich_su_giao_dich_vi)
        LichSuGiaoDichVi lichSu = new LichSuGiaoDichVi();
        lichSu.setViNguoiBan(vi);
        lichSu.setLoaiGiaoDich("RUT_TIEN");
        lichSu.setSoTien(soTienRut.negate()); // Số âm biểu thị tiền ra khỏi ví
        lichSu.setSoDuSauGiaoDich(vi.getSoDuKhaDung());
        lichSu.setMaThamChieu("YCRT-" + savedYeuCau.getMaYeuCau());
        lichSu.setMoTa("Tạo yêu cầu rút tiền về " + yeuCau.getTenNganHang() + " (STK: " + yeuCau.getSoTaiKhoan() + " - " + yeuCau.getTenChuTaiKhoan() + ")");
        lichSu.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichViRepository.save(lichSu);

        return savedYeuCau;
    }

    /**
     * [US-44] Quản trị viên duyệt yêu cầu rút tiền thành công (Đã chuyển khoản)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public YeuCauRutTien duyetYeuCauRutTien(Long maYeuCau, Long maAdmin) {
        YeuCauRutTien yeuCau = getChiTiet(maYeuCau);

        // Validate trạng thái
        if (!"CHO_DUYET".equals(yeuCau.getTrangThai())) {
            throw new IllegalStateException("Yêu cầu #" + maYeuCau + " không ở trạng thái Chờ Duyệt! (Trạng thái hiện tại: " + yeuCau.getTrangThai() + ")");
        }

        yeuCau.setTrangThai("DA_DUYET");
        yeuCau.setNgayDuyet(LocalDateTime.now());

        if (maAdmin != null) {
            nguoiDungRepository.findById(maAdmin).ifPresent(yeuCau::setAdminDuyet);
        }

        YeuCauRutTien updated = yeuCauRutTienRepository.save(yeuCau);

        // Cập nhật tổng tiền đã rút trong ví
        ViNguoiBan vi = kyQuyService.getOrCreateViNguoiBan(yeuCau.getGianHang());
        BigDecimal daRutHienTai = vi.getTongTienDaRut() != null ? vi.getTongTienDaRut() : BigDecimal.ZERO;
        vi.setTongTienDaRut(daRutHienTai.add(yeuCau.getSoTienRut()));
        vi.setNgayCapNhat(LocalDateTime.now());
        viNguoiBanRepository.save(vi);

        return updated;
    }

    /**
     * [US-44] Quản trị viên từ chối yêu cầu rút tiền -> TỰ ĐỘNG HOÀN TRẢ TIỀN vào số dư khả dụng
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public YeuCauRutTien tuChoiYeuCauRutTien(Long maYeuCau, Long maAdmin, String lyDoTuChoi) {
        YeuCauRutTien yeuCau = getChiTiet(maYeuCau);

        if (!"CHO_DUYET".equals(yeuCau.getTrangThai())) {
            throw new IllegalStateException("Yêu cầu #" + maYeuCau + " không ở trạng thái Chờ Duyệt!");
        }
        if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Bắt buộc phải nhập lý do từ chối rút tiền!");
        }

        yeuCau.setTrangThai("TU_CHOI");
        yeuCau.setLyDoTuChoi(lyDoTuChoi.trim());
        yeuCau.setNgayDuyet(LocalDateTime.now());

        if (maAdmin != null) {
            nguoiDungRepository.findById(maAdmin).ifPresent(yeuCau::setAdminDuyet);
        }

        YeuCauRutTien updated = yeuCauRutTienRepository.save(yeuCau);

        // HOÀN TRẢ TIỀN vào số dư khả dụng của Ví Người Bán
        ViNguoiBan vi = kyQuyService.getOrCreateViNguoiBan(yeuCau.getGianHang());
        vi.setSoDuKhaDung(vi.getSoDuKhaDung().add(yeuCau.getSoTienRut()));
        vi.setNgayCapNhat(LocalDateTime.now());
        viNguoiBanRepository.save(vi);

        // Ghi Sổ cái hoàn tiền
        LichSuGiaoDichVi lichSu = new LichSuGiaoDichVi();
        lichSu.setViNguoiBan(vi);
        lichSu.setLoaiGiaoDich("HOAN_TIEN_RUT_THAT_BAI");
        lichSu.setSoTien(yeuCau.getSoTienRut()); // Số dương biểu thị tiền hoàn lại vào ví
        lichSu.setSoDuSauGiaoDich(vi.getSoDuKhaDung());
        lichSu.setMaThamChieu("YCRT-" + yeuCau.getMaYeuCau());
        lichSu.setMoTa("Hoàn trả tiền do yêu cầu rút tiền bị từ chối: " + lyDoTuChoi.trim());
        lichSu.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichViRepository.save(lichSu);

        return updated;
    }

    /**
     * Tìm kiếm và phân trang danh sách yêu cầu rút tiền
     */
    public Page<YeuCauRutTien> getDanhSachPhanTrang(
            String keyword,
            String trangThai,
            Long maGianHang,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return yeuCauRutTienRepository.timKiemNangCao(
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : null,
                maGianHang,
                pageable
        );
    }

    /**
     * Lấy chi tiết yêu cầu rút tiền
     */
    public YeuCauRutTien getChiTiet(Long maYeuCau) {
        if (maYeuCau == null || maYeuCau <= 0) {
            throw new IllegalArgumentException("Mã yêu cầu không hợp lệ!");
        }
        return yeuCauRutTienRepository.findById(maYeuCau)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy yêu cầu rút tiền với mã: #" + maYeuCau));
    }

    /**
     * Thống kê số liệu rút tiền đồng bộ thời gian thực từ Database
     */
    public Map<String, Object> getThongKeRutTienDongBo() {
        BigDecimal tongChoDuyet = yeuCauRutTienRepository.tinhTongTienTheoTrangThai("CHO_DUYET");
        BigDecimal tongDaDuyet = yeuCauRutTienRepository.tinhTongTienTheoTrangThai("DA_DUYET");
        BigDecimal tongTuChoi = yeuCauRutTienRepository.tinhTongTienTheoTrangThai("TU_CHOI");
        long countChoDuyet = yeuCauRutTienRepository.demSoLuongTheoTrangThai("CHO_DUYET");
        long countDaDuyet = yeuCauRutTienRepository.demSoLuongTheoTrangThai("DA_DUYET");
        long countTuChoi = yeuCauRutTienRepository.demSoLuongTheoTrangThai("TU_CHOI");
        long tongYeuCau = yeuCauRutTienRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("tongYeuCau", tongYeuCau);
        stats.put("tongChoDuyet", tongChoDuyet != null ? tongChoDuyet : BigDecimal.ZERO);
        stats.put("tongDaDuyet", tongDaDuyet != null ? tongDaDuyet : BigDecimal.ZERO);
        stats.put("tongTuChoi", tongTuChoi != null ? tongTuChoi : BigDecimal.ZERO);
        stats.put("countChoDuyet", countChoDuyet);
        stats.put("countDaDuyet", countDaDuyet);
        stats.put("countTuChoi", countTuChoi);
        return stats;
    }
}
