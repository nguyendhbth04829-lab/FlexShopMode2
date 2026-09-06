package com.example.demo.service;

import com.example.demo.dto.DangKyFlashSaleForm;
import com.example.demo.dto.KhungGioFlashSaleForm;
import com.example.demo.dto.ThongKeFlashSaleDTO;
import com.example.demo.entity.BienTheSanPham;
import com.example.demo.entity.KhungGioFlashSale;
import com.example.demo.entity.SanPhamFlashSale;
import com.example.demo.repository.BienTheSanPhamRepository;
import com.example.demo.repository.KhungGioFlashSaleRepository;
import com.example.demo.repository.SanPhamFlashSaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

/**
 * Service xử lý nghiệp vụ Quản lý Khung Giờ Flash Sale (0h, 12h, 21h),
 * Giá giảm sốc, Giới hạn số lượng và Đếm ngược thời gian (US-53 - PROMOTION).
 */
@Service
public class FlashSaleService {

    private static final Locale LOCALE_VN = new Locale("vi", "VN");

    @Autowired
    private KhungGioFlashSaleRepository khungGioFlashSaleRepository;

    @Autowired
    private SanPhamFlashSaleRepository sanPhamFlashSaleRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Định dạng tiền tệ VND
     */
    public String dinhDangTien(BigDecimal soTien) {
        if (soTien == null) return "0 đ";
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_VN);
        return nf.format(soTien) + " đ";
    }

    // =========================================================================
    // 1. QUẢN LÝ KHUNG GIỜ FLASH SALE (ADMIN / SELLER)
    // =========================================================================

    /**
     * Tạo mới một khung giờ Flash Sale với validate khắt khe
     */
    @Transactional
    public KhungGioFlashSale taoKhungGio(KhungGioFlashSaleForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu khung giờ không được để trống!");
        }

        LocalDateTime start = form.getThoiGianBatDau();
        LocalDateTime end = form.getThoiGianKetThuc();

        if (start == null || end == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống!");
        }

        // Validate 1: Thời gian kết thúc phải sau thời gian bắt đầu tối thiểu 30 phút
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu!");
        }

        long durationMinutes = Duration.between(start, end).toMinutes();
        if (durationMinutes < 30) {
            throw new IllegalArgumentException("Khung giờ Flash Sale phải kéo dài tối thiểu 30 phút!");
        }

        // Validate 2: Kiểm tra chống trùng lặp / chồng chéo thời gian với khung giờ khác
        boolean overlap = khungGioFlashSaleRepository.kiemTraChongCheoThoiGian(start, end, form.getMaFlashSale());
        if (overlap) {
            throw new IllegalArgumentException("Khoảng thời gian này bị trùng lặp hoặc chồng chéo với một khung giờ Flash Sale khác đang hoạt động!");
        }

        KhungGioFlashSale entity = new KhungGioFlashSale();
        if (form.getMaFlashSale() != null) {
            entity = khungGioFlashSaleRepository.findById(form.getMaFlashSale())
                    .orElse(new KhungGioFlashSale());
        }

        entity.setTieuDe(form.getTieuDe().trim());

        // Xử lý tệp ảnh banner tải lên từ máy tính (nếu có)
        if (form.getTepAnhBanner() != null && !form.getTepAnhBanner().isEmpty()) {
            try {
                String duongDanBanner = fileStorageService.luuAnhBannerFlashSale(form.getTepAnhBanner());
                entity.setLinkBanner(duongDanBanner);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Lỗi khi lưu ảnh banner từ máy tính: " + ex.getMessage(), ex);
            }
        } else if (StringUtils.hasText(form.getLinkBanner())) {
            entity.setLinkBanner(form.getLinkBanner().trim());
        } else if (!StringUtils.hasText(entity.getLinkBanner())) {
            entity.setLinkBanner("/images/banner-flashsale-default.jpg");
        }

        entity.setThoiGianBatDau(start);
        entity.setThoiGianKetThuc(end);

        // Xác định trạng thái ban đầu
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            entity.setTrangThai("SAP_DIEN_RA");
        } else if (now.isAfter(end)) {
            entity.setTrangThai("DA_KET_THUC");
        } else {
            entity.setTrangThai("DANG_DIEN_RA");
        }

        return khungGioFlashSaleRepository.save(entity);
    }

    /**
     * Tạo nhanh các khung giờ chuẩn: 0h (00:00 - 02:00), 12h (12:00 - 14:00), 21h (21:00 - 23:59)
     */
    @Transactional
    public KhungGioFlashSale taoKhungGioChuan(String mocGio, LocalDate ngay) {
        if (!StringUtils.hasText(mocGio)) {
            throw new IllegalArgumentException("Vui lòng chọn mốc giờ chuẩn (0H, 12H, 21H)!");
        }
        if (ngay == null) {
            ngay = LocalDate.now();
        }

        String moc = mocGio.trim().toUpperCase();
        KhungGioFlashSaleForm form = new KhungGioFlashSaleForm();
        form.setMocGioNhanh(moc);

        LocalDateTime start;
        LocalDateTime end;
        String banner;
        String tieuDeGoc;

        switch (moc) {
            case "0H":
                start = LocalDateTime.of(ngay, LocalTime.of(0, 0, 0));
                end = LocalDateTime.of(ngay, LocalTime.of(2, 0, 0));
                banner = "/images/banner-flashsale-0h.jpg";
                tieuDeGoc = "⚡ Flash Sale 0h - Nửa Đêm Săn Deal Cháy Phố";
                break;
            case "12H":
                start = LocalDateTime.of(ngay, LocalTime.of(12, 0, 0));
                end = LocalDateTime.of(ngay, LocalTime.of(14, 0, 0));
                banner = "/images/banner-flashsale-12h.jpg";
                tieuDeGoc = "⚡ Flash Sale 12h - Nghỉ Trưa Deal Sốc Chớp Nhoáng";
                break;
            case "21H":
                start = LocalDateTime.of(ngay, LocalTime.of(21, 0, 0));
                end = LocalDateTime.of(ngay, LocalTime.of(23, 59, 59));
                banner = "/images/banner-flashsale-21h.jpg";
                tieuDeGoc = "⚡ Flash Sale 21h - Giờ Vàng Xả Kho Giá Rẻ Vô Địch";
                break;
            default:
                throw new IllegalArgumentException("Mốc giờ không hợp lệ! Vui lòng chọn 0H, 12H hoặc 21H.");
        }

        // Nếu khung giờ hôm nay đã kết thúc hoặc đã có khung giờ hoạt động, tự động tạo cho ngày tiếp theo
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(end) || khungGioFlashSaleRepository.kiemTraChongCheoThoiGian(start, end, null)) {
            ngay = ngay.plusDays(1);
            if (moc.equals("0H")) {
                start = LocalDateTime.of(ngay, LocalTime.of(0, 0, 0));
                end = LocalDateTime.of(ngay, LocalTime.of(2, 0, 0));
            } else if (moc.equals("12H")) {
                start = LocalDateTime.of(ngay, LocalTime.of(12, 0, 0));
                end = LocalDateTime.of(ngay, LocalTime.of(14, 0, 0));
            } else if (moc.equals("21H")) {
                start = LocalDateTime.of(ngay, LocalTime.of(21, 0, 0));
                end = LocalDateTime.of(ngay, LocalTime.of(23, 59, 59));
            }
        }

        form.setTieuDe(tieuDeGoc + " (" + ngay.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + ")");
        form.setThoiGianBatDau(start);
        form.setThoiGianKetThuc(end);
        form.setLinkBanner(banner);

        return taoKhungGio(form);
    }

    /**
     * Xóa khung giờ Flash Sale
     */
    @Transactional
    public void xoaKhungGio(Long maFlashSale) {
        KhungGioFlashSale slot = layChiTietKhungGio(maFlashSale);
        sanPhamFlashSaleRepository.deleteAll(sanPhamFlashSaleRepository.findByKhungGioFlashSale_MaFlashSale(maFlashSale));
        khungGioFlashSaleRepository.delete(slot);
    }

    /**
     * Lấy danh sách khung giờ có phân trang và tìm lọc
     */
    @Transactional(readOnly = true)
    public Page<KhungGioFlashSale> layDanhSachKhungGio(String trangThai, String tuKhoa, int page, int size) {
        if (!StringUtils.hasText(trangThai)) trangThai = "TAT_CA";
        if (StringUtils.hasText(tuKhoa)) tuKhoa = tuKhoa.trim();
        Pageable pageable = PageRequest.of(page, size);
        return khungGioFlashSaleRepository.timKiemKhungGio(trangThai, tuKhoa, pageable);
    }

    /**
     * Lấy chi tiết khung giờ
     */
    @Transactional(readOnly = true)
    public KhungGioFlashSale layChiTietKhungGio(Long maFlashSale) {
        return khungGioFlashSaleRepository.findById(maFlashSale)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khung giờ Flash Sale #" + maFlashSale));
    }

    /**
     * Bật / Tắt tạm khóa khung giờ
     */
    @Transactional
    public void capNhatTrangThaiKhungGio(Long maFlashSale, String trangThaiMoi) {
        KhungGioFlashSale slot = layChiTietKhungGio(maFlashSale);
        slot.setTrangThai(trangThaiMoi);
        khungGioFlashSaleRepository.save(slot);
    }

    /**
     * Lấy khung giờ đang diễn ra hiện tại, nếu không có thì lấy khung giờ sắp diễn ra gần nhất
     */
    @Transactional(readOnly = true)
    public KhungGioFlashSale layKhungGioHienTaiHoacGanNhat() {
        LocalDateTime now = LocalDateTime.now();
        List<KhungGioFlashSale> dangDienRa = khungGioFlashSaleRepository.timKhungGioDangDienRa(now);
        if (!dangDienRa.isEmpty()) {
            return dangDienRa.get(0);
        }

        List<KhungGioFlashSale> sapDienRa = khungGioFlashSaleRepository.timKhungGioSapDienRa(now);
        if (!sapDienRa.isEmpty()) {
            return sapDienRa.get(0);
        }

        // Lấy khung giờ mới nhất
        List<KhungGioFlashSale> all = khungGioFlashSaleRepository.findAll();
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }

    /**
     * Lấy danh sách các khung giờ trong ngày hiện tại để hiển thị trên thanh Tab (0h, 12h, 21h...)
     */
    @Transactional(readOnly = true)
    public List<KhungGioFlashSale> layDanhSachKhungGioTrongNgay(LocalDate ngay) {
        if (ngay == null) ngay = LocalDate.now();
        LocalDateTime startOfDay = ngay.atStartOfDay();
        LocalDateTime endOfDay = ngay.atTime(LocalTime.MAX);
        return khungGioFlashSaleRepository.timKhungGioTrongNgay(startOfDay, endOfDay);
    }

    /**
     * Lấy danh sách tất cả khung giờ hiển thị cho khách hàng (Đang diễn ra, Sắp diễn ra, Đã diễn ra hôm nay)
     */
    @Transactional(readOnly = true)
    public List<KhungGioFlashSale> layDanhSachKhungGioChoKhachHang() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return khungGioFlashSaleRepository.timKhungGioChoKhachHang(startOfToday);
    }

    // =========================================================================
    // 2. ĐĂNG KÝ SẢN PHẨM VÀO FLASH SALE & GIÁ GIẢM SỐC
    // =========================================================================

    /**
     * Đăng ký sản phẩm giảm sốc vào khung giờ với validate khắt khe
     */
    @Transactional
    public SanPhamFlashSale dangKySanPhamFlashSale(DangKyFlashSaleForm form, Long maGianHangSeller, boolean isAdmin) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu đăng ký không được để trống!");
        }

        // 1. Kiểm tra khung giờ tồn tại
        KhungGioFlashSale khungGio = layChiTietKhungGio(form.getMaFlashSale());
        if (khungGio.isDaKetThuc()) {
            throw new IllegalArgumentException("Khung giờ Flash Sale này đã kết thúc, không thể đăng ký thêm sản phẩm!");
        }
        if ("TAM_KHOA".equalsIgnoreCase(khungGio.getTrangThai())) {
            throw new IllegalArgumentException("Khung giờ Flash Sale này hiện đang bị tạm khóa!");
        }

        // 2. Kiểm tra biến thể sản phẩm tồn tại
        BienTheSanPham bienThe = bienTheSanPhamRepository.findById(form.getMaBienThe())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm #" + form.getMaBienThe()));

        if (Boolean.TRUE.equals(bienThe.getDaXoa())) {
            throw new IllegalArgumentException("Biến thể sản phẩm này đã bị xóa khỏi hệ thống!");
        }

        // 3. Bảo mật: Nếu là Seller, kiểm tra sản phẩm phải thuộc gian hàng của mình
        if (!isAdmin && maGianHangSeller != null) {
            Long shopIdOfProduct = (bienThe.getSanPham() != null && bienThe.getSanPham().getGianHang() != null)
                    ? bienThe.getSanPham().getGianHang().getMaGianHang() : null;
            if (shopIdOfProduct == null || !shopIdOfProduct.equals(maGianHangSeller)) {
                throw new IllegalArgumentException("Bạn chỉ được phép đăng ký Flash Sale cho sản phẩm thuộc sở hữu của gian hàng mình!");
            }
        }

        // 4. Validate chống trùng lặp biến thể trong cùng 1 khung giờ
        boolean exists = sanPhamFlashSaleRepository.existsByKhungGioFlashSale_MaFlashSaleAndBienTheSanPham_MaBienThe(
                form.getMaFlashSale(), form.getMaBienThe()
        );
        if (exists) {
            throw new IllegalArgumentException("Biến thể sản phẩm này đã được đăng ký trong khung giờ Flash Sale rồi!");
        }

        // 5. Validate Giá Giảm Sốc: Giá Flash Sale bắt buộc phải nhỏ hơn giá niêm yết hiện tại
        BigDecimal giaGoc = bienThe.getGiaBan();
        if (form.getGiaFlashSale().compareTo(giaGoc) >= 0) {
            throw new IllegalArgumentException("Giá Flash Sale (" + dinhDangTien(form.getGiaFlashSale()) +
                    ") phải nhỏ hơn giá bán niêm yết hiện tại (" + dinhDangTien(giaGoc) + ")!");
        }

        // Giảm tối thiểu 5% để đảm bảo đúng tiêu chí "giảm sốc"
        BigDecimal giamToiThieu = giaGoc.multiply(new BigDecimal("0.95"));
        if (form.getGiaFlashSale().compareTo(giamToiThieu) > 0) {
            throw new IllegalArgumentException("Giá Flash Sale phải giảm tối thiểu 5% so với giá niêm yết hiện tại!");
        }

        // 6. Validate số lượng bán & giới hạn mua mỗi khách
        if (form.getSoLuongGioiHan() == null || form.getSoLuongGioiHan() < 1) {
            throw new IllegalArgumentException("Số lượng suất Flash Sale mở bán phải từ 1 trở lên!");
        }

        if (form.getGioiHanMuaMoiKhach() == null || form.getGioiHanMuaMoiKhach() < 1) {
            throw new IllegalArgumentException("Giới hạn mua mỗi khách phải từ 1 trở lên!");
        }

        if (form.getGioiHanMuaMoiKhach() > form.getSoLuongGioiHan()) {
            throw new IllegalArgumentException("Giới hạn mua mỗi khách không được lớn hơn tổng số lượng mở bán!");
        }

        SanPhamFlashSale sp = new SanPhamFlashSale();
        sp.setKhungGioFlashSale(khungGio);
        sp.setBienTheSanPham(bienThe);
        sp.setGiaFlashSale(form.getGiaFlashSale());
        sp.setSoLuongGioiHan(form.getSoLuongGioiHan());
        sp.setSoLuongDaBan(0);
        sp.setGioiHanMuaMoiKhach(form.getGioiHanMuaMoiKhach());

        return sanPhamFlashSaleRepository.save(sp);
    }

    /**
     * Gỡ bỏ sản phẩm khỏi chương trình Flash Sale
     */
    @Transactional
    public void xoaSanPhamKhoiFlashSale(Long maSanPhamFs, Long maGianHangSeller, boolean isAdmin) {
        SanPhamFlashSale sp = sanPhamFlashSaleRepository.findById(maSanPhamFs)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm Flash Sale #" + maSanPhamFs));

        if (!isAdmin && maGianHangSeller != null) {
            Long shopIdOfProduct = (sp.getBienTheSanPham().getSanPham() != null && sp.getBienTheSanPham().getSanPham().getGianHang() != null)
                    ? sp.getBienTheSanPham().getSanPham().getGianHang().getMaGianHang() : null;
            if (shopIdOfProduct == null || !shopIdOfProduct.equals(maGianHangSeller)) {
                throw new IllegalArgumentException("Bạn không có quyền gỡ sản phẩm của gian hàng khác khỏi Flash Sale!");
            }
        }

        sanPhamFlashSaleRepository.delete(sp);
    }

    /**
     * Lấy danh sách sản phẩm của một khung giờ Flash Sale có phân trang
     */
    @Transactional(readOnly = true)
    public Page<SanPhamFlashSale> layDanhSachSanPhamTheoKhungGio(Long maFlashSale, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sanPhamFlashSaleRepository.findByKhungGioFlashSale_MaFlashSale(maFlashSale, pageable);
    }

    /**
     * Ghi nhận giao dịch mua hàng Flash Sale (Cập nhật số lượng đã bán & kiểm tra giới hạn)
     */
    @Transactional
    public void ghiNhanBanHangFlashSale(Long maSanPhamFs, int soLuongMua, Long maKhachHang) {
        SanPhamFlashSale sp = sanPhamFlashSaleRepository.findById(maSanPhamFs)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm Flash Sale #" + maSanPhamFs));

        if (!sp.getKhungGioFlashSale().isDangDienRa()) {
            throw new IllegalArgumentException("Khung giờ Flash Sale hiện không trong thời gian diễn ra!");
        }

        int daBan = (sp.getSoLuongDaBan() != null) ? sp.getSoLuongDaBan() : 0;
        if (daBan + soLuongMua > sp.getSoLuongGioiHan()) {
            throw new IllegalArgumentException("Số lượng suất Flash Sale còn lại không đủ (Còn lại: " +
                    sp.getSoLuongConLai() + " suất)!");
        }

        int gioiHanKhach = (sp.getGioiHanMuaMoiKhach() != null) ? sp.getGioiHanMuaMoiKhach() : 1;
        if (soLuongMua > gioiHanKhach) {
            throw new IllegalArgumentException("Vượt quá giới hạn mua tối đa (" + gioiHanKhach +
                    " sản phẩm/khách) trong đợt Flash Sale này!");
        }

        sp.setSoLuongDaBan(daBan + soLuongMua);
        sanPhamFlashSaleRepository.save(sp);
    }

    // =========================================================================
    // 3. THỐNG KÊ DOANH SỐ & TIẾN ĐỘ BÁN FLASH SALE
    // =========================================================================

    /**
     * Lấy dữ liệu thống kê tổng quan chương trình Flash Sale
     */
    @Transactional(readOnly = true)
    public ThongKeFlashSaleDTO layThongKeFlashSale() {
        LocalDateTime now = LocalDateTime.now();
        List<KhungGioFlashSale> allSlots = khungGioFlashSaleRepository.findAll();

        long dangDienRa = 0;
        long sapDienRa = 0;
        long daKetThuc = 0;

        for (KhungGioFlashSale slot : allSlots) {
            if (slot.isDangDienRa()) dangDienRa++;
            else if (slot.isSapDienRa()) sapDienRa++;
            else daKetThuc++;
        }

        long tongSanPham = sanPhamFlashSaleRepository.count();
        Long tongSuatBan = sanPhamFlashSaleRepository.tongSuatDaBanToanHeThong();
        BigDecimal doanhThu = sanPhamFlashSaleRepository.tongDoanhThuFlashSaleToanHeThong();

        return ThongKeFlashSaleDTO.builder()
                .tongKhungGio(allSlots.size())
                .tongKhungGioDangDienRa(dangDienRa)
                .tongKhungGioSapDienRa(sapDienRa)
                .tongKhungGioDaKetThuc(daKetThuc)
                .tongSanPhamThamGia(tongSanPham)
                .tongSuatDaBan(tongSuatBan != null ? tongSuatBan : 0L)
                .tongDoanhThuFlashSale(doanhThu != null ? doanhThu : BigDecimal.ZERO)
                .build();
    }
}
