package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - Service Xử Lý Shopee Ads Đấu Thầu Từ Khóa CPC
 * =====================================================================
 */
@Service
public class QuangCaoAdsService {

    @Autowired
    private ChienDichQuangCaoRepository chienDichQuangCaoRepository;

    @Autowired
    private TuKhoaDauThauAdsRepository tuKhoaDauThauAdsRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private ViNguoiBanRepository viNguoiBanRepository;

    @Autowired
    private LichSuGiaoDichViRepository lichSuGiaoDichViRepository;

    /**
     * Lấy danh sách sản phẩm đang hoạt động của Shop để chọn làm quảng cáo
     */
    public List<SanPham> layDanhSachSanPhamCuaShop(Long maGianHang) {
        return sanPhamRepository.findByGianHang_MaGianHangAndDaXoaFalse(maGianHang);
    }

    /**
     * Tạo mới chiến dịch quảng cáo Shopee Ads kèm danh sách từ khóa ban đầu
     */
    @Transactional
    public ChienDichQuangCao taoMoiChienDich(TaoChienDichAdsRequestDTO dto) {
        dto.validate();

        GianHang gianHang = gianHangRepository.findById(dto.getMaGianHang())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gian hàng với mã: " + dto.getMaGianHang()));

        SanPham sanPham = sanPhamRepository.findById(dto.getMaSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với mã: " + dto.getMaSanPham()));

        if (!sanPham.getGianHang().getMaGianHang().equals(gianHang.getMaGianHang())) {
            throw new RuntimeException("Sản phẩm không thuộc quyền sở hữu của gian hàng này!");
        }

        // Kiểm tra số dư ví người bán
        ViNguoiBan vi = viNguoiBanRepository.findByGianHang_MaGianHang(dto.getMaGianHang())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví người bán của gian hàng: " + gianHang.getTenGianHang()));

        if (vi.getSoDuKhaDung() == null || vi.getSoDuKhaDung().compareTo(new BigDecimal("50000.00")) < 0) {
            throw new RuntimeException("Số dư ví khả dụng của Shop hiện tại (" + String.format("%,.0f", vi.getSoDuKhaDung())
                    + " VNĐ) không đủ tối thiểu 50,000 VNĐ để khởi chạy chiến dịch quảng cáo. Vui lòng nạp thêm tiền vào ví.");
        }

        ChienDichQuangCao chienDich = new ChienDichQuangCao();
        chienDich.setGianHang(gianHang);
        chienDich.setSanPham(sanPham);
        chienDich.setTenChienDich(dto.getTenChienDich().trim());
        chienDich.setNganSachNgay(dto.getNganSachNgay());
        chienDich.setTongChiPhiDaDung(BigDecimal.ZERO);
        chienDich.setTrangThai("DANG_CHAY");
        chienDich.setNgayBatDau(LocalDateTime.now());

        ChienDichQuangCao saved = chienDichQuangCaoRepository.save(chienDich);

        // Tách danh sách từ khóa ban đầu (nếu có)
        if (dto.getDanhSachTuKhoaStr() != null && !dto.getDanhSachTuKhoaStr().trim().isEmpty()) {
            String[] tuKhoaArray = dto.getDanhSachTuKhoaStr().split("[,\\n\\r]+");
            Set<String> uniqueKeywords = new HashSet<>();
            for (String tk : tuKhoaArray) {
                String clean = tk.trim();
                if (!clean.isEmpty() && uniqueKeywords.add(clean.toLowerCase())) {
                    TuKhoaDauThauAds tuKhoaAds = new TuKhoaDauThauAds();
                    tuKhoaAds.setChienDichQuangCao(saved);
                    tuKhoaAds.setTuKhoa(clean);
                    tuKhoaAds.setGiaThauMoiClickCpc(dto.getGiaThauMacDinh());
                    tuKhoaAds.setTongLuotClick(0);
                    tuKhoaAds.setDangKichHoat(true);
                    tuKhoaDauThauAdsRepository.save(tuKhoaAds);
                }
            }
        }

        return saved;
    }

    /**
     * Thêm từ khóa đấu thầu mới vào chiến dịch hiện có
     */
    @Transactional
    public TuKhoaDauThauAds themTuKhoaVaoChienDich(ThemTuKhoaAdsRequestDTO dto) {
        dto.validate();

        ChienDichQuangCao cd = chienDichQuangCaoRepository.findById(dto.getMaChienDich())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch với mã: " + dto.getMaChienDich()));

        TuKhoaDauThauAds tuKhoaAds = new TuKhoaDauThauAds();
        tuKhoaAds.setChienDichQuangCao(cd);
        tuKhoaAds.setTuKhoa(dto.getTuKhoa().trim());
        tuKhoaAds.setGiaThauMoiClickCpc(dto.getGiaThauMoiClickCpc());
        tuKhoaAds.setTongLuotClick(0);
        tuKhoaAds.setDangKichHoat(true);

        return tuKhoaDauThauAdsRepository.save(tuKhoaAds);
    }

    /**
     * Bật / Tạm dừng chiến dịch quảng cáo
     */
    @Transactional
    public ChienDichQuangCao thayDoiTrangThaiChienDich(Long maChienDich, String trangThaiMoi) {
        ChienDichQuangCao cd = chienDichQuangCaoRepository.findById(maChienDich)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch với mã: " + maChienDich));

        if ("DANG_CHAY".equalsIgnoreCase(trangThaiMoi)) {
            if (cd.getTongChiPhiDaDung() != null && cd.getNganSachNgay() != null &&
                    cd.getTongChiPhiDaDung().compareTo(cd.getNganSachNgay()) >= 0) {
                throw new RuntimeException("Chiến dịch đã đạt mức ngân sách ngày! Vui lòng nâng ngân sách ngày trước khi tiếp tục chạy.");
            }
            cd.setTrangThai("DANG_CHAY");
        } else if ("TAM_DUNG".equalsIgnoreCase(trangThaiMoi)) {
            cd.setTrangThai("TAM_DUNG");
        } else if ("KET_THUC".equalsIgnoreCase(trangThaiMoi)) {
            cd.setTrangThai("KET_THUC");
        }

        return chienDichQuangCaoRepository.save(cd);
    }

    /**
     * Bật / Tắt trạng thái hoạt động của một từ khóa
     */
    @Transactional
    public TuKhoaDauThauAds batTatTuKhoa(Long maTuKhoa, boolean dangKichHoat) {
        TuKhoaDauThauAds tk = tuKhoaDauThauAdsRepository.findById(maTuKhoa)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy từ khóa đấu thầu với mã: " + maTuKhoa));

        tk.setDangKichHoat(dangKichHoat);
        return tuKhoaDauThauAdsRepository.save(tk);
    }

    /**
     * Cập nhật giá thầu CPC của từ khóa
     */
    @Transactional
    public TuKhoaDauThauAds capNhatGiaThauTuKhoa(Long maTuKhoa, BigDecimal giaThauMoi) {
        if (giaThauMoi == null || giaThauMoi.compareTo(new BigDecimal("500.00")) < 0 || giaThauMoi.compareTo(new BigDecimal("50000.00")) > 0) {
            throw new IllegalArgumentException("Giá thầu mới phải nằm trong khoảng từ 500 VNĐ đến 50,000 VNĐ.");
        }
        TuKhoaDauThauAds tk = tuKhoaDauThauAdsRepository.findById(maTuKhoa)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy từ khóa đấu thầu với mã: " + maTuKhoa));

        tk.setGiaThauMoiClickCpc(giaThauMoi);
        return tuKhoaDauThauAdsRepository.save(tk);
    }

    /**
     * XỬ LÝ LƯỢT CLICK QUẢNG CÁO (COST PER CLICK - CPC):
     * 1. Ghi nhận lượt nhấp (+1 click)
     * 2. Khấu trừ chi phí tương ứng giá thầu vào tổng chi phí chiến dịch
     * 3. Khấu trừ trực tiếp số dư ví khả dụng của Shop
     * 4. Ghi nhận lịch sử giao dịch ví (TRU_TIEN_ADS)
     * 5. Kiểm tra nếu chạm trần ngân sách ngày -> Tự động chuyển HET_NGAN_SACH
     */
    @Transactional
    public Map<String, Object> xuLyClickQuangCao(Long maTuKhoa) {
        TuKhoaDauThauAds tuKhoaAds = tuKhoaDauThauAdsRepository.findById(maTuKhoa)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy từ khóa quảng cáo với mã: " + maTuKhoa));

        ChienDichQuangCao chienDich = tuKhoaAds.getChienDichQuangCao();

        if (!"DANG_CHAY".equalsIgnoreCase(chienDich.getTrangThai())) {
            throw new RuntimeException("Chiến dịch quảng cáo hiện không hoạt động (Trạng thái: " + chienDich.getTenTrangThaiTiengViet() + ").");
        }

        if (tuKhoaAds.getDangKichHoat() == null || !tuKhoaAds.getDangKichHoat()) {
            throw new RuntimeException("Từ khóa đấu thầu này đang bị tạm ngưng.");
        }

        BigDecimal giaCpc = tuKhoaAds.getGiaThauMoiClickCpc();

        // 1. Tăng lượt click từ khóa
        tuKhoaAds.setTongLuotClick((tuKhoaAds.getTongLuotClick() != null ? tuKhoaAds.getTongLuotClick() : 0) + 1);
        tuKhoaDauThauAdsRepository.save(tuKhoaAds);

        // 2. Tăng chi phí đã dùng của chiến dịch
        BigDecimal chiPhiMoi = (chienDich.getTongChiPhiDaDung() != null ? chienDich.getTongChiPhiDaDung() : BigDecimal.ZERO).add(giaCpc);
        chienDich.setTongChiPhiDaDung(chiPhiMoi);

        // Kiểm tra ngân sách ngày
        boolean hetNganSach = false;
        if (chiPhiMoi.compareTo(chienDich.getNganSachNgay()) >= 0) {
            chienDich.setTrangThai("HET_NGAN_SACH");
            hetNganSach = true;
        }
        chienDichQuangCaoRepository.save(chienDich);

        // 3. Khấu trừ số dư Ví người bán của Shop
        GianHang shop = chienDich.getGianHang();
        ViNguoiBan vi = viNguoiBanRepository.findByGianHang_MaGianHang(shop.getMaGianHang())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví người bán của shop: " + shop.getTenGianHang()));

        BigDecimal soDuMoi = vi.getSoDuKhaDung().subtract(giaCpc).max(BigDecimal.ZERO);
        vi.setSoDuKhaDung(soDuMoi);
        vi.setNgayCapNhat(LocalDateTime.now());
        viNguoiBanRepository.save(vi);

        // 4. Ghi nhận biến động số dư trong LichSuGiaoDichVi
        LichSuGiaoDichVi gd = new LichSuGiaoDichVi();
        gd.setViNguoiBan(vi);
        gd.setLoaiGiaoDich("TRU_TIEN_ADS");
        gd.setSoTien(giaCpc.negate()); // Số âm biểu thị khấu trừ
        gd.setSoDuSauGiaoDich(soDuMoi);
        gd.setMaThamChieu("ADS-CLK-CD" + chienDich.getMaChienDich() + "-TK" + tuKhoaAds.getMaTuKhoa());
        gd.setMoTa("Khấu trừ chi phí CPC lượt click từ khóa [" + tuKhoaAds.getTuKhoa() + "] cho chiến dịch " + chienDich.getTenChienDich());
        gd.setNgayTao(LocalDateTime.now());
        lichSuGiaoDichViRepository.save(gd);

        Map<String, Object> result = new HashMap<>();
        result.put("maChienDich", chienDich.getMaChienDich());
        result.put("maTuKhoa", tuKhoaAds.getMaTuKhoa());
        result.put("giaCpc", giaCpc);
        result.put("tongChiPhiDaDung", chiPhiMoi);
        result.put("soDuViConLai", soDuMoi);
        result.put("hetNganSach", hetNganSach);
        result.put("trangThaiChienDich", chienDich.getTrangThai());

        return result;
    }

    /**
     * MÔ PHỎNG TÌM KIẾM SẢN PHẨM KÈM XẾP HẠNG ĐẤU THẦU SHOPEE ADS:
     * - Tìm các từ khóa quảng cáo đang chạy phù hợp với từ khóa tìm kiếm (Sắp xếp theo CPC cao nhất).
     * - Trả về danh sách gồm: Sản phẩm Được Tài Trợ (vị trí đầu) + Sản phẩm Tự Nhiên.
     */
    public List<KetQuaTimKiemAdsDTO> timKiemSanPhamDauThauAds(String tuKhoa) {
        List<KetQuaTimKiemAdsDTO> ketQua = new ArrayList<>();
        Set<Long> sanPhamDaXuatHien = new HashSet<>();

        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return ketQua;
        }

        String searchTrim = tuKhoa.trim();

        // 1. Tìm các sản phẩm chạy quảng cáo phù hợp từ khóa (sắp xếp giá thầu CPC giảm dần)
        List<TuKhoaDauThauAds> dsTuKhoaAds = tuKhoaDauThauAdsRepository.timTuKhoaDauThauKhop(searchTrim);
        for (TuKhoaDauThauAds tk : dsTuKhoaAds) {
            ChienDichQuangCao cd = tk.getChienDichQuangCao();
            SanPham sp = cd.getSanPham();

            if (sp != null && !sanPhamDaXuatHien.contains(sp.getMaSanPham())) {
                sanPhamDaXuatHien.add(sp.getMaSanPham());

                ketQua.add(KetQuaTimKiemAdsDTO.builder()
                        .maSanPham(sp.getMaSanPham())
                        .tenSanPham(sp.getTenSanPham())
                        .giaCoBan(sp.getGiaCoBan())
                        .tenGianHang(sp.getGianHang().getTenGianHang())
                        .maGianHang(sp.getGianHang().getMaGianHang())
                        .maChienDich(cd.getMaChienDich())
                        .maTuKhoa(tk.getMaTuKhoa())
                        .tuKhoaKhop(tk.getTuKhoa())
                        .giaThauCpc(tk.getGiaThauMoiClickCpc())
                        .laQuangCao(true)
                        .build());
            }
        }

        // 2. Tìm các sản phẩm tự nhiên phù hợp theo tên
        List<SanPham> dsSanPhamTuNhien = sanPhamRepository.timKiemSanPhamTuNhien(searchTrim);
        for (SanPham sp : dsSanPhamTuNhien) {
            if (!sanPhamDaXuatHien.contains(sp.getMaSanPham())) {
                sanPhamDaXuatHien.add(sp.getMaSanPham());

                ketQua.add(KetQuaTimKiemAdsDTO.builder()
                        .maSanPham(sp.getMaSanPham())
                        .tenSanPham(sp.getTenSanPham())
                        .giaCoBan(sp.getGiaCoBan())
                        .tenGianHang(sp.getGianHang().getTenGianHang())
                        .maGianHang(sp.getGianHang().getMaGianHang())
                        .laQuangCao(false)
                        .build());
            }
        }

        return ketQua;
    }

    /**
     * Thống kê hiệu suất chiến dịch quảng cáo của Shop
     */
    public AdsThongKeDTO layThongKeAdsCuaShop(Long maGianHang) {
        GianHang shop = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gian hàng ID: " + maGianHang));

        ViNguoiBan vi = viNguoiBanRepository.findByGianHang_MaGianHang(maGianHang).orElse(new ViNguoiBan());

        long tongChienDich = chienDichQuangCaoRepository.countByGianHang_MaGianHang(maGianHang);
        long dangChay = chienDichQuangCaoRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "DANG_CHAY");
        long tamDung = chienDichQuangCaoRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "TAM_DUNG");
        long hetNganSach = chienDichQuangCaoRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "HET_NGAN_SACH");

        BigDecimal tongNganSach = chienDichQuangCaoRepository.tinhTongNganSachNgayCuaShop(maGianHang);
        BigDecimal tongChiPhi = chienDichQuangCaoRepository.tinhTongChiPhiDaDungCuaShop(maGianHang);

        List<ChienDichQuangCao> allCd = chienDichQuangCaoRepository.findByGianHang_MaGianHangOrderByNgayBatDauDesc(maGianHang);
        int tongClicks = allCd.stream().mapToInt(ChienDichQuangCao::getTongLuotClick).sum();

        BigDecimal cpcTb = BigDecimal.ZERO;
        if (tongClicks > 0 && tongChiPhi.compareTo(BigDecimal.ZERO) > 0) {
            cpcTb = tongChiPhi.divide(new BigDecimal(tongClicks), 0, RoundingMode.HALF_UP);
        }

        return AdsThongKeDTO.builder()
                .maGianHang(maGianHang)
                .tenGianHang(shop.getTenGianHang())
                .soDuViKhaDung(vi.getSoDuKhaDung() != null ? vi.getSoDuKhaDung() : BigDecimal.ZERO)
                .tongChienDich(tongChienDich)
                .chienDichDangChay(dangChay)
                .chienDichTamDung(tamDung)
                .chienDichHetNganSach(hetNganSach)
                .tongNganSachNgay(tongNganSach)
                .tongChiPhiDaDung(tongChiPhi)
                .tongLuotClick(tongClicks)
                .cpcTrungBinh(cpcTb)
                .build();
    }

    /**
     * Phân trang và tìm kiếm danh sách chiến dịch
     */
    public Page<ChienDichQuangCao> getDanhSachChienDichPhanTrang(String keyword, String trangThai, Long maGianHang, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayBatDau").descending());
        return chienDichQuangCaoRepository.timKiemNangCao(keyword, trangThai, maGianHang, pageable);
    }

    /**
     * Xem chi tiết chiến dịch
     */
    public ChienDichQuangCao getChiTietChienDich(Long maChienDich) {
        return chienDichQuangCaoRepository.findById(maChienDich)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch quảng cáo với mã: " + maChienDich));
    }
}
