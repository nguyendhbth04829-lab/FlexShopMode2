package com.example.demo.service;

import com.example.demo.dto.DoiChieuBaBenDTO;
import com.example.demo.dto.PhanLoaiTicketForm;
import com.example.demo.dto.ThongKeDashboardCskhDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class CskhService {

    private static final Set<String> HOP_LE_LOAI_KHIEU_NAI = Set.of("HONG_VO", "GIAO_SAI", "HANG_GIA", "THIEU_HANG", "HET_HAN", "KHAC");
    private static final Set<String> HOP_LE_MUC_DO = Set.of("THAP", "TRUNG_BINH", "CAO", "KHAN_CAP");
    private static final Set<String> HOP_LE_TRANG_THAI = Set.of("MO_MOI", "DANG_XU_LY", "CHO_SHOP_PHAN_HOI", "CHAP_NHAN_HOAN_TIEN", "TU_CHOI_KHIEU_NAI", "DONG_PHIEU");

    @Autowired
    private PhieuKhieuNaiRepository phieuKhieuNaiRepository;

    @Autowired
    private BangChungKhieuNaiRepository bangChungKhieuNaiRepository;

    @Autowired
    private GhiChuNoiBoKhieuNaiRepository ghiChuNoiBoKhieuNaiRepository;

    @Autowired
    private NhiemVuGiaoHangRepository nhiemVuGiaoHangRepository;

    @Autowired
    private TaiXeGiaoHangRepository taiXeGiaoHangRepository;

    @Autowired
    private LichSuTrangThaiDonRepository lichSuTrangThaiDonRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    /**
     * Thống kê KPI thời gian thực cho CSKH Dashboard (US-46)
     */
    @Transactional(readOnly = true)
    public ThongKeDashboardCskhDTO layThongKeDashboard() {
        List<String> trangThaiDong = Arrays.asList("DA_HUY", "DONG_PHIEU");

        long tongSo = phieuKhieuNaiRepository.count();
        long chuaTiepNhan = phieuKhieuNaiRepository.countByCskhXuLyIsNullAndTrangThaiNotIn(trangThaiDong);
        long uuTienCao = phieuKhieuNaiRepository.countByMucDoUuTienInAndTrangThaiNotIn(
                Arrays.asList("CAO", "KHAN_CAP"), trangThaiDong);
        long khieuNaiVanChuyen = phieuKhieuNaiRepository.countByLoaiKhieuNaiInAndTrangThaiNotIn(
                Arrays.asList("HONG_VO", "GIAO_SAI", "THIEU_HANG"), trangThaiDong);
        long choShop = phieuKhieuNaiRepository.countByTrangThai("CHO_SHOP_PHAN_HOI");
        long dangXuLy = phieuKhieuNaiRepository.countByTrangThai("DANG_XU_LY");

        return ThongKeDashboardCskhDTO.builder()
                .tongSoTicket(tongSo)
                .ticketChuaTiepNhan(chuaTiepNhan)
                .ticketUuTienCao(uuTienCao)
                .khieuNaiVanChuyenPod(khieuNaiVanChuyen)
                .ticketChoShopPhanHoi(choShop)
                .ticketDangXuLy(dangXuLy)
                .build();
    }

    /**
     * Tìm kiếm và lọc nâng cao ticket hỗ trợ trên toàn sàn cho CSKH
     */
    @Transactional(readOnly = true)
    public Page<PhieuKhieuNai> layDanhSachTicketCskh(
            String tuKhoa,
            String trangThai,
            String loaiKhieuNai,
            String mucDoUuTien,
            String locPhuTrach,
            Long maCskhHienTai,
            String tuNgayStr,
            String denNgayStr,
            int page,
            int size
    ) {
        LocalDateTime tuNgay = null;
        LocalDateTime denNgay = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (tuNgayStr != null && !tuNgayStr.isBlank()) {
            tuNgay = LocalDate.parse(tuNgayStr, formatter).atStartOfDay();
        }
        if (denNgayStr != null && !denNgayStr.isBlank()) {
            denNgay = LocalDate.parse(denNgayStr, formatter).atTime(23, 59, 59);
        }

        Long locMaCskh = null;
        boolean chiChuaTiepNhan = false;

        if ("CHUA_TIEP_NHAN".equalsIgnoreCase(locPhuTrach)) {
            chiChuaTiepNhan = true;
        } else if ("CUA_TOI".equalsIgnoreCase(locPhuTrach) && maCskhHienTai != null) {
            locMaCskh = maCskhHienTai;
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return phieuKhieuNaiRepository.timKiemTicketCskh(
                tuKhoa, trangThai, loaiKhieuNai, mucDoUuTien, locMaCskh, chiChuaTiepNhan, tuNgay, denNgay, pageable);
    }

    /**
     * CSKH tiếp nhận ticket xử lý nhanh với validation kiểm tra nghiêm ngặt
     */
    public PhieuKhieuNai tiepNhanTicket(Long maPhieu, Long maNhanVienCskh) {
        if (maPhieu == null || maPhieu <= 0) {
            throw new IllegalArgumentException("Mã ticket khiếu nại không hợp lệ: " + maPhieu);
        }
        if (maNhanVienCskh == null || maNhanVienCskh <= 0) {
            throw new IllegalArgumentException("Mã nhân viên CSKH không hợp lệ: " + maNhanVienCskh);
        }

        PhieuKhieuNai phieu = phieuKhieuNaiRepository.findById(maPhieu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu khiếu nại ID: " + maPhieu));

        // Kiểm tra trạng thái đóng
        if (Set.of("DA_HUY", "DONG_PHIEU").contains(phieu.getTrangThai())) {
            throw new IllegalStateException("Ticket [" + phieu.getMaCodePhieu() + "] đã ở trạng thái kết thúc (" + phieu.getTrangThaiDisplay() + "), không thể tiếp nhận lại!");
        }

        NguoiDung cskh = nguoiDungRepository.findById(maNhanVienCskh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên CSKH ID: " + maNhanVienCskh));

        if (Boolean.TRUE.equals(cskh.getDaXoa()) || (cskh.getTrangThai() != null && !"HOAT_DONG".equalsIgnoreCase(cskh.getTrangThai()))) {
            throw new SecurityException("Tài khoản nhân viên CSKH này đang bị khóa hoặc đã ngừng hoạt động.");
        }

        String nguoiCu = phieu.getCskhXuLy() != null ? phieu.getCskhXuLy().getHoVaTen() : "Chưa có";
        phieu.setCskhXuLy(cskh);
        if ("MO_MOI".equals(phieu.getTrangThai())) {
            phieu.setTrangThai("DANG_XU_LY");
        }
        PhieuKhieuNai saved = phieuKhieuNaiRepository.save(phieu);

        // Thêm ghi chú nội bộ tự động
        GhiChuNoiBoKhieuNai ghiChu = new GhiChuNoiBoKhieuNai();
        ghiChu.setPhieuKhieuNai(saved);
        ghiChu.setNhanVien(cskh);
        ghiChu.setNoiDung("[Tiếp nhận Ticket]: Nhân viên CSKH " + cskh.getHoVaTen() + " đã tiếp nhận thụ lý ticket (Người phụ trách trước đó: " + nguoiCu + ").");
        ghiChu.setNgayTao(LocalDateTime.now());
        ghiChuNoiBoKhieuNaiRepository.save(ghiChu);

        // Lưu vào lịch sử trạng thái đơn
        LichSuTrangThaiDon ls = new LichSuTrangThaiDon();
        ls.setDonHangShop(saved.getDonHangShop());
        ls.setTrangThaiCu(saved.getDonHangShop().getTrangThai());
        ls.setTrangThaiMoi(saved.getDonHangShop().getTrangThai());
        ls.setNguoiThucHien("CSKH: " + cskh.getHoVaTen());
        ls.setGhiChu("Tiếp nhận điều tra khiếu nại " + saved.getMaCodePhieu());
        ls.setThoiGian(LocalDateTime.now());
        lichSuTrangThaiDonRepository.save(ls);

        return saved;
    }

    /**
     * Phân loại ticket: Gán loại lỗi, mức độ ưu tiên, cập nhật trạng thái và nhân viên (Airtight validation)
     */
    public PhieuKhieuNai phanLoaiTicket(Long maPhieu, PhanLoaiTicketForm form, Long maCskhThucHien) {
        if (maPhieu == null || maPhieu <= 0) {
            throw new IllegalArgumentException("Mã ticket khiếu nại không hợp lệ.");
        }
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu biểu mẫu phân loại không được để trống.");
        }
        if (maCskhThucHien == null || maCskhThucHien <= 0) {
            throw new IllegalArgumentException("Mã định danh CSKH thực hiện không hợp lệ.");
        }

        // Validate enum sets
        if (!HOP_LE_LOAI_KHIEU_NAI.contains(form.getLoaiKhieuNai())) {
            throw new IllegalArgumentException("Nhóm phân loại khiếu nại không hợp lệ: " + form.getLoaiKhieuNai());
        }
        if (!HOP_LE_MUC_DO.contains(form.getMucDoUuTien())) {
            throw new IllegalArgumentException("Mức độ ưu tiên xử lý không hợp lệ: " + form.getMucDoUuTien());
        }
        if (!HOP_LE_TRANG_THAI.contains(form.getTrangThai())) {
            throw new IllegalArgumentException("Trạng thái xử lý không hợp lệ: " + form.getTrangThai());
        }

        PhieuKhieuNai phieu = phieuKhieuNaiRepository.findById(maPhieu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu khiếu nại ID: " + maPhieu));

        // State machine rule: Ticket đã đóng DONG_PHIEU không được tùy tiện chuyển trạng thái
        if ("DONG_PHIEU".equals(phieu.getTrangThai()) && !"DONG_PHIEU".equals(form.getTrangThai())) {
            throw new IllegalStateException("Ticket [" + phieu.getMaCodePhieu() + "] đã hoàn tất đóng hồ sơ. Cần quyền Quản trị viên (Admin) để mở lại!");
        }

        phieu.setLoaiKhieuNai(form.getLoaiKhieuNai());
        phieu.setMucDoUuTien(form.getMucDoUuTien());
        phieu.setTrangThai(form.getTrangThai());

        // Nếu có gán nhân viên phụ trách mới
        if (form.getMaCskhPhuTrach() != null && form.getMaCskhPhuTrach() > 0) {
            NguoiDung cskhMoi = nguoiDungRepository.findById(form.getMaCskhPhuTrach())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên CSKH ID: " + form.getMaCskhPhuTrach()));
            if (Boolean.TRUE.equals(cskhMoi.getDaXoa()) || (cskhMoi.getTrangThai() != null && !"HOAT_DONG".equalsIgnoreCase(cskhMoi.getTrangThai()))) {
                throw new SecurityException("Nhân viên CSKH được gán hiện đang không khả dụng hoặc bị tạm khóa.");
            }
            phieu.setCskhXuLy(cskhMoi);
        }

        PhieuKhieuNai saved = phieuKhieuNaiRepository.save(phieu);

        // Thêm ghi chú điều tra nội bộ nếu có
        if (form.getGhiChuXuLy() != null && !form.getGhiChuXuLy().trim().isBlank()) {
            String ghiChuClean = form.getGhiChuXuLy().replaceAll("<[^>]*>", "").trim();
            NguoiDung nguoiGhi = nguoiDungRepository.findById(maCskhThucHien).orElse(saved.getCskhXuLy());
            GhiChuNoiBoKhieuNai ghiChu = new GhiChuNoiBoKhieuNai();
            ghiChu.setPhieuKhieuNai(saved);
            ghiChu.setNhanVien(nguoiGhi);
            ghiChu.setNoiDung("[Phân loại Ticket]: " + ghiChuClean);
            ghiChu.setNgayTao(LocalDateTime.now());
            ghiChuNoiBoKhieuNaiRepository.save(ghiChu);
        }

        return saved;
    }

    /**
     * Tra cứu thông tin đối chiếu 3 bên (Khách hàng - Shop - Shipper POD) kèm dòng thời gian
     */
    @Transactional(readOnly = true)
    public DoiChieuBaBenDTO layDuLieuDoiChieuBaBen(Long maPhieu) {
        if (maPhieu == null || maPhieu <= 0) {
            throw new IllegalArgumentException("Mã ticket tra cứu không hợp lệ.");
        }

        PhieuKhieuNai phieu = phieuKhieuNaiRepository.findById(maPhieu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu khiếu nại ID: " + maPhieu));

        DonHangShop donHangShop = phieu.getDonHangShop();

        // 1. Tìm nhiệm vụ giao hàng gần nhất có ảnh POD
        Optional<NhiemVuGiaoHang> optNhiemVu = nhiemVuGiaoHangRepository
                .findFirstByDonHangShop_MaDonHangShopAndLinkAnhBangChungPodIsNotNullOrderByNgayTaoDesc(donHangShop.getMaDonHangShop());

        NhiemVuGiaoHang nhiemVuGiaoHang = optNhiemVu.orElse(null);
        TaiXeGiaoHang taiXe = null;

        if (nhiemVuGiaoHang != null) {
            taiXe = nhiemVuGiaoHang.getTaiXe();
        } else {
            // Thử tìm bất kỳ nhiệm vụ nào của đơn hàng này
            List<NhiemVuGiaoHang> dsNhiemVu = nhiemVuGiaoHangRepository.timNhiemVuTheoDonHangSapXepMoiNhat(donHangShop.getMaDonHangShop());
            if (!dsNhiemVu.isEmpty()) {
                nhiemVuGiaoHang = dsNhiemVu.get(0);
                taiXe = nhiemVuGiaoHang.getTaiXe();
            }
        }

        // 2. Lịch sử trạng thái đơn hàng (Timeline)
        List<LichSuTrangThaiDon> danhSachLichSu = lichSuTrangThaiDonRepository
                .findAllByDonHangShop_MaDonHangShopOrderByThoiGianAsc(donHangShop.getMaDonHangShop());

        // 3. Danh sách ghi chú điều tra nội bộ
        List<GhiChuNoiBoKhieuNai> danhSachGhiChu = ghiChuNoiBoKhieuNaiRepository
                .findAllByPhieuKhieuNai_MaPhieuOrderByNgayTaoDesc(maPhieu);

        // 4. Bằng chứng khách hàng đã tải lên
        List<BangChungKhieuNai> danhSachBangChung = bangChungKhieuNaiRepository
                .findAllByPhieuKhieuNai_MaPhieu(maPhieu);

        return DoiChieuBaBenDTO.builder()
                .phieuKhieuNai(phieu)
                .donHangShop(donHangShop)
                .nhiemVuGiaoHang(nhiemVuGiaoHang)
                .taiXe(taiXe)
                .danhSachLichSuTrangThai(danhSachLichSu)
                .danhSachGhiChuNoiBo(danhSachGhiChu)
                .danhSachBangChungKhachHang(danhSachBangChung)
                .build();
    }

    /**
     * Thêm ghi chú điều tra nội bộ cho phiếu khiếu nại (CSKH & Admin) - Khắc khe chống rỗng / chống XSS
     */
    public GhiChuNoiBoKhieuNai themGhiChuNoiBo(Long maPhieu, Long maNhanVien, String noiDung) {
        if (maPhieu == null || maPhieu <= 0) {
            throw new IllegalArgumentException("Mã ticket khiếu nại không hợp lệ.");
        }
        if (maNhanVien == null || maNhanVien <= 0) {
            throw new IllegalArgumentException("Mã nhân viên thực hiện không hợp lệ.");
        }
        if (noiDung == null || noiDung.trim().isBlank()) {
            throw new IllegalArgumentException("Nội dung ghi chú điều tra nội bộ không được để trống.");
        }

        String noiDungClean = noiDung.replaceAll("<[^>]*>", "").trim();
        if (noiDungClean.length() < 5 || noiDungClean.length() > 2000) {
            throw new IllegalArgumentException("Nội dung ghi chú điều tra phải từ 5 đến 2.000 ký tự hợp lệ.");
        }

        PhieuKhieuNai phieu = phieuKhieuNaiRepository.findById(maPhieu)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu khiếu nại ID: " + maPhieu));

        NguoiDung nhanVien = nguoiDungRepository.findById(maNhanVien)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên CSKH ID: " + maNhanVien));

        if (Boolean.TRUE.equals(nhanVien.getDaXoa()) || (nhanVien.getTrangThai() != null && !"HOAT_DONG".equalsIgnoreCase(nhanVien.getTrangThai()))) {
            throw new SecurityException("Tài khoản nhân viên CSKH này không hoạt động hoặc đã bị vô hiệu hóa.");
        }

        GhiChuNoiBoKhieuNai ghiChu = new GhiChuNoiBoKhieuNai();
        ghiChu.setPhieuKhieuNai(phieu);
        ghiChu.setNhanVien(nhanVien);
        ghiChu.setNoiDung(noiDungClean);
        ghiChu.setNgayTao(LocalDateTime.now());

        return ghiChuNoiBoKhieuNaiRepository.save(ghiChu);
    }

    /**
     * Lấy danh sách nhân viên CSKH để gán phụ trách
     */
    @Transactional(readOnly = true)
    public List<NguoiDung> layDanhSachNhanVienCskh() {
        return nguoiDungRepository.findAll();
    }
}
