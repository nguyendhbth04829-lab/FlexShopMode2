package com.example.demo.service;

import com.example.demo.dto.DanhGiaSanPhamForm;
import com.example.demo.dto.DonHangDanhGiaItemDTO;
import com.example.demo.dto.ThongKeDanhGiaDTO;
import com.example.demo.dto.ThongKeDanhGiaSellerDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class DanhGiaService {

    private static final String THU_MUC_UPLOAD_ANH = "uploads/danh-gia/";
    private static final Set<String> DINH_DANG_ANH_HOP_LE = Set.of("image/jpeg", "image/png", "image/webp", "image/jpg");
    private static final long DUNG_LUONG_ANH_TOI_DA = 10 * 1024 * 1024; // 10 MB

    @Autowired
    private DanhGiaSanPhamRepository danhGiaSanPhamRepository;

    @Autowired
    private HinhAnhDanhGiaRepository hinhAnhDanhGiaRepository;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    /**
     * Khách hàng gửi đánh giá sản phẩm (1-5 sao + nhận xét + đính kèm ảnh)
     * Tự động tính lại điểm Rating trung bình của sản phẩm & Shop (US-49)
     */
    public DanhGiaSanPham guiDanhGiaSanPham(DanhGiaSanPhamForm form, Long maKhachHang) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu biểu mẫu đánh giá không được để trống.");
        }
        if (maKhachHang == null || maKhachHang <= 0) {
            throw new IllegalArgumentException("Mã định danh khách hàng không hợp lệ.");
        }
        if (form.getMaChiTietDon() == null || form.getMaChiTietDon() <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm trong đơn hàng không hợp lệ.");
        }

        // 1. Validate số sao (1 - 5)
        if (form.getSoSao() == null || form.getSoSao() < 1 || form.getSoSao() > 5) {
            throw new IllegalArgumentException("Số sao đánh giá phải từ 1 đến 5 sao.");
        }

        // 2. Validate nhận xét (Chống rỗng, chống chỉ toàn khoảng trắng)
        if (form.getNoiDung() == null || form.getNoiDung().trim().isBlank()) {
            throw new IllegalArgumentException("Nội dung nhận xét đánh giá không được để trống.");
        }
        String noiDungClean = form.getNoiDung().replaceAll("<[^>]*>", "").trim();
        if (noiDungClean.length() < 5 || noiDungClean.length() > 2000) {
            throw new IllegalArgumentException("Nội dung nhận xét phải từ 5 đến 2.000 ký tự chi tiết.");
        }

        // 3. Kiểm tra chi tiết đơn hàng
        ChiTietDonHang chiTiet = chiTietDonHangRepository.findById(form.getMaChiTietDon())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết sản phẩm đơn hàng ID: " + form.getMaChiTietDon()));

        DonHangShop donHangShop = chiTiet.getDonHangShop();
        if (donHangShop == null) {
            throw new IllegalStateException("Chi tiết sản phẩm không gắn với đơn hàng gian hàng hợp lệ.");
        }

        // 4. KIỂM TRA ĐƠN HÀNG ĐÃ HOÀN TẤT GIAO HÀNG (Acceptance Criteria: Chỉ đơn hoàn tất mới được đánh giá)
        if (!"DA_GIAO".equalsIgnoreCase(donHangShop.getTrangThai())) {
            throw new IllegalStateException("Đơn hàng [" + donHangShop.getMaCodeDonShop() + "] hiện ở trạng thái ["
                    + donHangShop.getTrangThai() + "]. Quý khách chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã giao thành công!");
        }

        // 5. KIỂM TRA QUYỀN SỞ HỮU ĐƠN HÀNG (Chống đánh giá chéo tài khoản khác)
        DonHangTong donHangTong = donHangShop.getDonHangTong();
        if (donHangTong == null || donHangTong.getKhachHang() == null || !donHangTong.getKhachHang().getMaNguoiDung().equals(maKhachHang)) {
            throw new SecurityException("Quý khách không có quyền đánh giá sản phẩm của đơn hàng không thuộc về mình!");
        }

        // 6. CHỐNG ĐÁNH GIÁ TRÙNG LẶP (Mỗi món hàng chỉ đánh giá 1 lần duy nhất)
        if (danhGiaSanPhamRepository.existsByChiTietDonHang_MaChiTietDon(chiTiet.getMaChiTietDon())) {
            throw new IllegalStateException("Sản phẩm này trong đơn hàng đã được quý khách đánh giá trước đó. Mỗi sản phẩm chỉ được đánh giá 1 lần duy nhất!");
        }

        // 7. Xác định sản phẩm gốc từ biến thể
        SanPham sanPham = null;
        if (chiTiet.getMaBienThe() != null) {
            Optional<BienTheSanPham> optBienThe = bienTheSanPhamRepository.findById(chiTiet.getMaBienThe());
            if (optBienThe.isPresent()) {
                sanPham = optBienThe.get().getSanPham();
            }
        }
        if (sanPham == null) {
            // Thử tìm theo mã sản phẩm hoặc gian hàng
            List<SanPham> dsSanPham = sanPhamRepository.findAll();
            sanPham = dsSanPham.stream()
                    .filter(sp -> sp.getGianHang() != null && sp.getGianHang().getMaGianHang().equals(donHangShop.getGianHang().getMaGianHang()))
                    .findFirst()
                    .orElse(dsSanPham.isEmpty() ? null : dsSanPham.get(0));
        }
        if (sanPham == null) {
            throw new IllegalStateException("Không tìm thấy thông tin sản phẩm trên sàn để ghi nhận đánh giá.");
        }

        NguoiDung khachHang = nguoiDungRepository.findById(maKhachHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin khách hàng ID: " + maKhachHang));

        // 8. Tạo và lưu đánh giá sản phẩm
        DanhGiaSanPham danhGia = new DanhGiaSanPham();
        danhGia.setChiTietDonHang(chiTiet);
        danhGia.setSanPham(sanPham);
        danhGia.setGianHang(donHangShop.getGianHang());
        danhGia.setNguoiDung(khachHang);
        danhGia.setSoSao(form.getSoSao());
        danhGia.setNoiDung(noiDungClean);
        danhGia.setAnDanh(Boolean.TRUE.equals(form.getAnDanh()));
        danhGia.setBiAn(false);
        danhGia.setNgayTao(LocalDateTime.now());

        DanhGiaSanPham saved = danhGiaSanPhamRepository.save(danhGia);

        // 9. Xử lý đính kèm ảnh thực tế unboxing (Tối đa 5 ảnh)
        if (form.getDanhSachTepAnh() != null && !form.getDanhSachTepAnh().isEmpty()) {
            List<MultipartFile> dsAnhHopLe = form.getDanhSachTepAnh().stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .toList();

            if (dsAnhHopLe.size() > 5) {
                throw new IllegalArgumentException("Quý khách chỉ có thể đính kèm tối đa 5 hình ảnh thực tế.");
            }

            try {
                Path thuMucPath = Paths.get(THU_MUC_UPLOAD_ANH);
                if (!Files.exists(thuMucPath)) {
                    Files.createDirectories(thuMucPath);
                }

                for (MultipartFile file : dsAnhHopLe) {
                    if (file.getSize() > DUNG_LUONG_ANH_TOI_DA) {
                        throw new IllegalArgumentException("Mỗi ảnh đính kèm không được vượt quá 10MB (" + file.getOriginalFilename() + ").");
                    }
                    String contentType = file.getContentType();
                    if (contentType == null || !DINH_DANG_ANH_HOP_LE.contains(contentType.toLowerCase())) {
                        throw new IllegalArgumentException("Định dạng file không được hỗ trợ (chỉ chấp nhận JPG, PNG, WEBP): " + file.getOriginalFilename());
                    }

                    String tenGoc = file.getOriginalFilename() != null ? file.getOriginalFilename() : "anh_danh_gia.jpg";
                    String duoiFile = tenGoc.contains(".") ? tenGoc.substring(tenGoc.lastIndexOf(".")) : ".jpg";
                    String tenFileLuu = UUID.randomUUID() + "_" + System.currentTimeMillis() + duoiFile;
                    Path duongDanLuu = thuMucPath.resolve(tenFileLuu);

                    Files.copy(file.getInputStream(), duongDanLuu, StandardCopyOption.REPLACE_EXISTING);

                    HinhAnhDanhGia hinhAnh = new HinhAnhDanhGia();
                    hinhAnh.setDanhGiaSanPham(saved);
                    hinhAnh.setLinkAnh("/" + THU_MUC_UPLOAD_ANH + tenFileLuu);
                    hinhAnhDanhGiaRepository.save(hinhAnh);
                    saved.getDanhSachHinhAnh().add(hinhAnh);
                }
            } catch (IOException e) {
                throw new RuntimeException("Lỗi trong quá trình lưu trữ hình ảnh đánh giá: " + e.getMessage(), e);
            }
        }

        // 10. TỰ ĐỘNG TÍNH TOÁN LẠI ĐIỂM RATING TRUNG BÌNH CỦA SẢN PHẨM & SHOP (ACID Core Logic)
        capNhatDiemRatingSanPham(sanPham.getMaSanPham());
        capNhatDiemRatingGianHang(donHangShop.getGianHang().getMaGianHang());

        return saved;
    }

    /**
     * Tự động tính toán lại điểm rating trung bình của Sản Phẩm (Làm tròn 1 chữ số thập phân)
     */
    public void capNhatDiemRatingSanPham(Long maSanPham) {
        if (maSanPham == null) return;
        SanPham sp = sanPhamRepository.findById(maSanPham).orElse(null);
        if (sp == null) return;

        Double diemTb = danhGiaSanPhamRepository.tinhDiemTrungBinhSanPham(maSanPham);
        if (diemTb != null && !diemTb.isNaN()) {
            BigDecimal rounded = BigDecimal.valueOf(diemTb).setScale(1, RoundingMode.HALF_UP);
            sp.setDanhGiaTb(rounded);
        } else {
            sp.setDanhGiaTb(BigDecimal.ZERO);
        }
        sp.setNgayCapNhat(LocalDateTime.now());
        sanPhamRepository.save(sp);
    }

    /**
     * Tự động tính toán lại điểm rating trung bình và tổng đánh giá của Gian Hàng
     */
    public void capNhatDiemRatingGianHang(Long maGianHang) {
        if (maGianHang == null) return;
        GianHang gh = gianHangRepository.findById(maGianHang).orElse(null);
        if (gh == null) return;

        Double diemTb = danhGiaSanPhamRepository.tinhDiemTrungBinhGianHang(maGianHang);
        Long tongDanhGia = danhGiaSanPhamRepository.demTongDanhGiaGianHang(maGianHang);

        if (diemTb != null && !diemTb.isNaN()) {
            BigDecimal rounded = BigDecimal.valueOf(diemTb).setScale(1, RoundingMode.HALF_UP);
            gh.setDiemDanhGiaTb(rounded);
        } else {
            gh.setDiemDanhGiaTb(BigDecimal.ZERO);
        }
        gh.setTongDanhGia(tongDanhGia != null ? tongDanhGia.intValue() : 0);
        gianHangRepository.save(gh);
    }

    /**
     * Thống kê tổng hợp số sao, tỷ lệ phân bố 1-5 sao và số lượng ảnh của Sản phẩm
     */
    @Transactional(readOnly = true)
    public ThongKeDanhGiaDTO layThongKeDanhGiaSanPham(Long maSanPham) {
        if (maSanPham == null) {
            return new ThongKeDanhGiaDTO();
        }

        Double diemTb = danhGiaSanPhamRepository.tinhDiemTrungBinhSanPham(maSanPham);
        long tongSo = danhGiaSanPhamRepository.countBySanPham_MaSanPhamAndBiAnFalse(maSanPham);
        long s5 = danhGiaSanPhamRepository.countBySanPham_MaSanPhamAndSoSaoAndBiAnFalse(maSanPham, 5);
        long s4 = danhGiaSanPhamRepository.countBySanPham_MaSanPhamAndSoSaoAndBiAnFalse(maSanPham, 4);
        long s3 = danhGiaSanPhamRepository.countBySanPham_MaSanPhamAndSoSaoAndBiAnFalse(maSanPham, 3);
        long s2 = danhGiaSanPhamRepository.countBySanPham_MaSanPhamAndSoSaoAndBiAnFalse(maSanPham, 2);
        long s1 = danhGiaSanPhamRepository.countBySanPham_MaSanPhamAndSoSaoAndBiAnFalse(maSanPham, 1);
        long coAnh = danhGiaSanPhamRepository.demDanhGiaCoHinhAnh(maSanPham);

        int tyLe5 = tongSo > 0 ? (int) Math.round((double) s5 * 100 / tongSo) : 0;
        int tyLe4 = tongSo > 0 ? (int) Math.round((double) s4 * 100 / tongSo) : 0;
        int tyLe3 = tongSo > 0 ? (int) Math.round((double) s3 * 100 / tongSo) : 0;
        int tyLe2 = tongSo > 0 ? (int) Math.round((double) s2 * 100 / tongSo) : 0;
        int tyLe1 = tongSo > 0 ? (int) Math.round((double) s1 * 100 / tongSo) : 0;

        return ThongKeDanhGiaDTO.builder()
                .diemTrungBinh(diemTb != null ? diemTb : 5.0)
                .tongSoDanhGia(tongSo)
                .soLuong5Sao(s5)
                .soLuong4Sao(s4)
                .soLuong3Sao(s3)
                .soLuong2Sao(s2)
                .soLuong1Sao(s1)
                .tyLe5Sao(tyLe5)
                .tyLe4Sao(tyLe4)
                .tyLe3Sao(tyLe3)
                .tyLe2Sao(tyLe2)
                .tyLe1Sao(tyLe1)
                .soLuongCoHinhAnh(coAnh)
                .build();
    }

    /**
     * Tra cứu danh sách đánh giá sản phẩm có phân trang và bộ lọc theo sao / ảnh
     */
    @Transactional(readOnly = true)
    public Page<DanhGiaSanPham> layDanhSachDanhGiaSanPham(Long maSanPham, Integer soSao, Boolean coHinhAnh, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return danhGiaSanPhamRepository.timKiemDanhGiaSanPham(maSanPham, soSao, coHinhAnh, pageable);
    }

    /**
     * Lấy lịch sử tất cả các đánh giá của một khách hàng
     */
    @Transactional(readOnly = true)
    public Page<DanhGiaSanPham> layDanhSachDanhGiaCuaToi(Long maKhachHang, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return danhGiaSanPhamRepository.findAllByNguoiDung_MaNguoiDungOrderByNgayTaoDesc(maKhachHang, pageable);
    }

    /**
     * Lấy danh sách sản phẩm trong đơn hàng kèm cờ đã đánh giá để hiển thị giao diện chọn đánh giá
     */
    @Transactional(readOnly = true)
    public List<DonHangDanhGiaItemDTO> layDanhSachSanPhamDonHangDeDanhGia(Long maDonHangShop, Long maKhachHang) {
        DonHangShop donHang = donHangShopRepository.findById(maDonHangShop)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + maDonHangShop));

        if (donHang.getDonHangTong() != null && donHang.getDonHangTong().getKhachHang() != null) {
            if (!donHang.getDonHangTong().getKhachHang().getMaNguoiDung().equals(maKhachHang)) {
                throw new SecurityException("Quý khách không có quyền truy cập đơn hàng của tài khoản khác.");
            }
        }

        List<ChiTietDonHang> dsChiTiet = chiTietDonHangRepository.findAllByDonHangShop_MaDonHangShop(maDonHangShop);
        List<DonHangDanhGiaItemDTO> ketQua = new ArrayList<>();

        for (ChiTietDonHang ct : dsChiTiet) {
            Optional<DanhGiaSanPham> optDg = danhGiaSanPhamRepository.findByChiTietDonHang_MaChiTietDon(ct.getMaChiTietDon());
            
            Long maSp = null;
            String linkAnh = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=120";

            if (ct.getMaBienThe() != null) {
                Optional<BienTheSanPham> optBt = bienTheSanPhamRepository.findById(ct.getMaBienThe());
                if (optBt.isPresent()) {
                    BienTheSanPham bt = optBt.get();
                    if (bt.getLinkAnh() != null && !bt.getLinkAnh().isBlank()) {
                        linkAnh = bt.getLinkAnh();
                    }
                    if (bt.getSanPham() != null) {
                        maSp = bt.getSanPham().getMaSanPham();
                    }
                }
            }

            ketQua.add(DonHangDanhGiaItemDTO.builder()
                    .chiTietDonHang(ct)
                    .maSanPham(maSp)
                    .linkAnhSanPham(linkAnh)
                    .daDanhGia(optDg.isPresent())
                    .danhGia(optDg.orElse(null))
                    .build());
        }

        return ketQua;
    }

    /**
     * US-50: Thống kê tình hình đánh giá & tỷ lệ phản hồi cho Seller Dashboard
     */
    @Transactional(readOnly = true)
    public ThongKeDanhGiaSellerDTO layThongKeDanhGiaChoSeller(Long maGianHang) {
        if (maGianHang == null) {
            return new ThongKeDanhGiaSellerDTO();
        }

        long tongDanhGia = danhGiaSanPhamRepository.countByGianHang_MaGianHangAndBiAnFalse(maGianHang);
        Double diemTb = danhGiaSanPhamRepository.tinhDiemTrungBinhGianHang(maGianHang);
        long daPhanHoi = danhGiaSanPhamRepository.demDanhGiaDaPhanHoi(maGianHang);
        long chuaPhanHoi = danhGiaSanPhamRepository.demDanhGiaChuaPhanHoi(maGianHang);

        int tyLe = tongDanhGia > 0 ? (int) Math.round((double) daPhanHoi * 100 / tongDanhGia) : 0;

        long s5 = danhGiaSanPhamRepository.countByGianHang_MaGianHangAndSoSaoAndBiAnFalse(maGianHang, 5);
        long s4 = danhGiaSanPhamRepository.countByGianHang_MaGianHangAndSoSaoAndBiAnFalse(maGianHang, 4);
        long s3 = danhGiaSanPhamRepository.countByGianHang_MaGianHangAndSoSaoAndBiAnFalse(maGianHang, 3);
        long s2 = danhGiaSanPhamRepository.countByGianHang_MaGianHangAndSoSaoAndBiAnFalse(maGianHang, 2);
        long s1 = danhGiaSanPhamRepository.countByGianHang_MaGianHangAndSoSaoAndBiAnFalse(maGianHang, 1);

        return ThongKeDanhGiaSellerDTO.builder()
                .tongDanhGia(tongDanhGia)
                .diemDanhGiaTb(diemTb != null ? diemTb : 5.0)
                .soLuongDaPhanHoi(daPhanHoi)
                .soLuongChuaPhanHoi(chuaPhanHoi)
                .tyLePhanHoi(tyLe)
                .soLuong5Sao(s5)
                .soLuong4Sao(s4)
                .soLuong3Sao(s3)
                .soLuong2Sao(s2)
                .soLuong1Sao(s1)
                .build();
    }

    /**
     * US-50: Tra cứu danh sách đánh giá của gian hàng có bộ lọc đa tiêu chí và phân trang
     */
    @Transactional(readOnly = true)
    public Page<DanhGiaSanPham> layDanhSachDanhGiaChoSeller(
            Long maGianHang,
            Integer soSao,
            String trangThaiPhanHoi,
            String tuKhoa,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        String tuKhoaClean = (tuKhoa != null && !tuKhoa.trim().isBlank()) ? tuKhoa.trim() : null;
        String tuKhoaPattern = (tuKhoaClean != null) ? "%" + tuKhoaClean + "%" : null;
        return danhGiaSanPhamRepository.timKiemDanhGiaChoSeller(maGianHang, soSao, trangThaiPhanHoi, tuKhoaClean, tuKhoaPattern, pageable);
    }

    /**
     * US-50: Seller phản hồi công khai đánh giá của khách hàng (hoặc chỉnh sửa phản hồi đã gửi)
     */
    public DanhGiaSanPham sellerPhanHoiDanhGia(Long maDanhGia, String noiDungPhanHoi, Long maSeller) {
        if (maDanhGia == null || maDanhGia <= 0) {
            throw new IllegalArgumentException("Mã đánh giá không hợp lệ.");
        }
        if (maSeller == null || maSeller <= 0) {
            throw new IllegalArgumentException("Mã định danh người bán không hợp lệ.");
        }

        // 1. Kiểm tra tồn tại của đánh giá
        DanhGiaSanPham danhGia = danhGiaSanPhamRepository.findById(maDanhGia)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá sản phẩm ID: " + maDanhGia));

        // 2. Chặn phản hồi đánh giá bị ẩn do vi phạm
        if (Boolean.TRUE.equals(danhGia.getBiAn())) {
            throw new IllegalStateException("Không thể phản hồi đánh giá đã bị ẩn do vi phạm chính sách của sàn.");
        }

        // 3. KIỂM TRA BẢO MẬT: Quyền sở hữu gian hàng (Chống Seller can thiệp đánh giá của Shop khác)
        GianHang gianHang = danhGia.getGianHang();
        if (gianHang == null || gianHang.getChuSoHuu() == null || !gianHang.getChuSoHuu().getMaNguoiDung().equals(maSeller)) {
            throw new SecurityException("Quý khách không có quyền phản hồi đánh giá thuộc gian hàng của người khác!");
        }

        // 4. Validate nội dung phản hồi (Chống rỗng, chống chỉ toàn khoảng trắng, chống XSS)
        if (noiDungPhanHoi == null || noiDungPhanHoi.trim().isBlank()) {
            throw new IllegalArgumentException("Nội dung phản hồi của người bán không được để trống.");
        }
        String noiDungClean = noiDungPhanHoi.replaceAll("<[^>]*>", "").trim();
        if (noiDungClean.length() < 5) {
            throw new IllegalArgumentException("Nội dung phản hồi của người bán phải từ 5 đến 1.000 ký tự chi tiết và lịch sự.");
        }
        if (noiDungClean.length() > 1000) {
            throw new IllegalArgumentException("Nội dung phản hồi của người bán không được vượt quá 1.000 ký tự.");
        }

        // 5. Cập nhật nội dung phản hồi và thời gian phản hồi
        danhGia.setPhanHoiCuaShop(noiDungClean);
        danhGia.setNgayShopPhanHoi(LocalDateTime.now());

        return danhGiaSanPhamRepository.save(danhGia);
    }
}
