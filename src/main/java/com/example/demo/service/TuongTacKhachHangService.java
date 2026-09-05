package com.example.demo.service;

import com.example.demo.dto.ThongKeHoiDapSanPhamDTO;
import com.example.demo.dto.ThongKeTuongTacKhachHangDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Dịch vụ xử lý nghiệp vụ Tương tác Khách hàng (US-51 - ENGAGE):
 * 1. Wishlist (Sản phẩm yêu thích)
 * 2. Follow Shop (Theo dõi gian hàng)
 * 3. Hỏi - Đáp cộng đồng (Q&A trên trang sản phẩm)
 */
@Service
@Transactional
public class TuongTacKhachHangService {

    @Autowired
    private SanPhamYeuThichRepository sanPhamYeuThichRepository;

    @Autowired
    private TheoDoiGianHangRepository theoDoiGianHangRepository;

    @Autowired
    private HoiDapSanPhamRepository hoiDapSanPhamRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    // =========================================================================
    // 1. NGHIỆP VỤ SẢN PHẨM YÊU THÍCH (WISHLIST)
    // =========================================================================

    @Transactional(readOnly = true)
    public boolean kiemTraDaYeuThich(Long maNguoiDung, Long maSanPham) {
        if (maNguoiDung == null || maSanPham == null) return false;
        return sanPhamYeuThichRepository.existsByMaNguoiDungAndMaSanPham(maNguoiDung, maSanPham);
    }

    /**
     * Bật/Tắt trạng thái yêu thích sản phẩm (Toggle Wishlist)
     * @return true nếu vừa thêm vào yêu thích, false nếu vừa bỏ yêu thích
     */
    public boolean toggleYeuThich(Long maNguoiDung, Long maSanPham) {
        if (maNguoiDung == null || maNguoiDung <= 0) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ.");
        }
        if (maSanPham == null || maSanPham <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }

        if (sanPhamYeuThichRepository.existsByMaNguoiDungAndMaSanPham(maNguoiDung, maSanPham)) {
            xoaKhoiYeuThich(maNguoiDung, maSanPham);
            return false;
        } else {
            themVaoYeuThich(maNguoiDung, maSanPham);
            return true;
        }
    }

    /**
     * Thêm sản phẩm vào danh sách yêu thích
     */
    public SanPhamYeuThich themVaoYeuThich(Long maNguoiDung, Long maSanPham) {
        if (maNguoiDung == null || maNguoiDung <= 0) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ.");
        }
        if (maSanPham == null || maSanPham <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }

        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + maNguoiDung));

        SanPham sanPham = sanPhamRepository.findById(maSanPham)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + maSanPham));

        if (Boolean.TRUE.equals(sanPham.getDaXoa()) || Boolean.TRUE.equals(sanPham.getBiKhoa())) {
            throw new IllegalStateException("Không thể lưu sản phẩm đã bị xóa hoặc tạm khóa vào yêu thích.");
        }

        // Chống thêm trùng lặp
        return sanPhamYeuThichRepository.findByMaNguoiDungAndMaSanPham(maNguoiDung, maSanPham)
                .orElseGet(() -> {
                    SanPhamYeuThich yt = new SanPhamYeuThich(maNguoiDung, maSanPham);
                    yt.setNguoiDung(nguoiDung);
                    yt.setSanPham(sanPham);
                    yt.setNgayTao(LocalDateTime.now());
                    return sanPhamYeuThichRepository.save(yt);
                });
    }

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     */
    public void xoaKhoiYeuThich(Long maNguoiDung, Long maSanPham) {
        if (maNguoiDung == null || maSanPham == null) return;
        sanPhamYeuThichRepository.xoaSanPhamYeuThich(maNguoiDung, maSanPham);
    }

    /**
     * Tra cứu danh sách sản phẩm yêu thích của khách hàng có phân trang và bộ lọc
     */
    @Transactional(readOnly = true)
    public Page<SanPhamYeuThich> layDanhSachWishlist(Long maNguoiDung, Long maDanhMuc, String tuKhoa, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        String tuKhoaClean = (tuKhoa != null && !tuKhoa.trim().isBlank()) ? tuKhoa.trim() : null;
        String tuKhoaPattern = (tuKhoaClean != null) ? "%" + tuKhoaClean + "%" : null;
        return sanPhamYeuThichRepository.timKiemWishlistChoKhachHang(maNguoiDung, maDanhMuc, tuKhoaClean, tuKhoaPattern, pageable);
    }

    @Transactional(readOnly = true)
    public long demLuotYeuThichSanPham(Long maSanPham) {
        if (maSanPham == null) return 0;
        return sanPhamYeuThichRepository.countByMaSanPham(maSanPham);
    }

    // =========================================================================
    // 2. NGHIỆP VỤ THEO DÕI GIAN HÀNG (FOLLOW SHOP)
    // =========================================================================

    @Transactional(readOnly = true)
    public boolean kiemTraDaTheoDoi(Long maNguoiDung, Long maGianHang) {
        if (maNguoiDung == null || maGianHang == null) return false;
        return theoDoiGianHangRepository.existsByMaNguoiDungAndMaGianHang(maNguoiDung, maGianHang);
    }

    /**
     * Bật/Tắt trạng thái theo dõi gian hàng (Toggle Follow Shop)
     * @return true nếu vừa theo dõi, false nếu vừa hủy theo dõi
     */
    public boolean toggleTheoDoiGianHang(Long maNguoiDung, Long maGianHang) {
        if (maNguoiDung == null || maNguoiDung <= 0) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ.");
        }
        if (maGianHang == null || maGianHang <= 0) {
            throw new IllegalArgumentException("Mã gian hàng không hợp lệ.");
        }

        if (theoDoiGianHangRepository.existsByMaNguoiDungAndMaGianHang(maNguoiDung, maGianHang)) {
            huyTheoDoiGianHang(maNguoiDung, maGianHang);
            return false;
        } else {
            theoDoiGianHang(maNguoiDung, maGianHang);
            return true;
        }
    }

    /**
     * Theo dõi gian hàng mới
     */
    public TheoDoiGianHang theoDoiGianHang(Long maNguoiDung, Long maGianHang) {
        if (maNguoiDung == null || maNguoiDung <= 0) {
            throw new IllegalArgumentException("Mã người dùng không hợp lệ.");
        }
        if (maGianHang == null || maGianHang <= 0) {
            throw new IllegalArgumentException("Mã gian hàng không hợp lệ.");
        }

        NguoiDung nguoiDung = nguoiDungRepository.findById(maNguoiDung)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + maNguoiDung));

        GianHang gianHang = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gian hàng ID: " + maGianHang));

        // BẢO MẬT: Chặn chủ shop tự theo dõi gian hàng của chính mình
        if (gianHang.getChuSoHuu() != null && gianHang.getChuSoHuu().getMaNguoiDung().equals(maNguoiDung)) {
            throw new IllegalArgumentException("Quý khách không thể tự theo dõi gian hàng của chính mình!");
        }

        if (Boolean.TRUE.equals(gianHang.getDaXoa())) {
            throw new IllegalStateException("Không thể theo dõi gian hàng đã ngừng hoạt động.");
        }

        return theoDoiGianHangRepository.findByMaNguoiDungAndMaGianHang(maNguoiDung, maGianHang)
                .orElseGet(() -> {
                    TheoDoiGianHang td = new TheoDoiGianHang(maNguoiDung, maGianHang);
                    td.setNguoiDung(nguoiDung);
                    td.setGianHang(gianHang);
                    td.setNgayTao(LocalDateTime.now());
                    return theoDoiGianHangRepository.save(td);
                });
    }

    /**
     * Hủy theo dõi gian hàng
     */
    public void huyTheoDoiGianHang(Long maNguoiDung, Long maGianHang) {
        if (maNguoiDung == null || maGianHang == null) return;
        theoDoiGianHangRepository.huyTheoDoiGianHang(maNguoiDung, maGianHang);
    }

    /**
     * Tra cứu danh sách gian hàng đang theo dõi của khách hàng
     */
    @Transactional(readOnly = true)
    public Page<TheoDoiGianHang> layDanhSachGianHangTheoDoi(Long maNguoiDung, String tuKhoa, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        String tuKhoaClean = (tuKhoa != null && !tuKhoa.trim().isBlank()) ? tuKhoa.trim() : null;
        String tuKhoaPattern = (tuKhoaClean != null) ? "%" + tuKhoaClean + "%" : null;
        return theoDoiGianHangRepository.timKiemGianHangTheoDoi(maNguoiDung, tuKhoaClean, tuKhoaPattern, pageable);
    }

    @Transactional(readOnly = true)
    public long demLuotTheoDoiGianHang(Long maGianHang) {
        if (maGianHang == null) return 0;
        return theoDoiGianHangRepository.countByMaGianHang(maGianHang);
    }

    // =========================================================================
    // 3. NGHIỆP VỤ HỎI - ĐÁP CỘNG ĐỒNG (Q&A TRÊN TRANG SẢN PHẨM)
    // =========================================================================

    /**
     * Khách hàng đặt câu hỏi về sản phẩm
     */
    public HoiDapSanPham datCauHoi(Long maSanPham, Long maNguoiHoi, String cauHoi) {
        if (maSanPham == null || maSanPham <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
        if (maNguoiHoi == null || maNguoiHoi <= 0) {
            throw new IllegalArgumentException("Mã người hỏi không hợp lệ.");
        }

        SanPham sanPham = sanPhamRepository.findById(maSanPham)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + maSanPham));

        NguoiDung nguoiHoi = nguoiDungRepository.findById(maNguoiHoi)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng ID: " + maNguoiHoi));

        if (cauHoi == null || cauHoi.trim().isBlank()) {
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống.");
        }

        // Lọc mã độc HTML/XSS
        String cauHoiClean = cauHoi.replaceAll("<[^>]*>", "").trim();
        if (cauHoiClean.length() < 10) {
            throw new IllegalArgumentException("Câu hỏi phải có ít nhất 10 ký tự chi tiết và rõ ràng.");
        }
        if (cauHoiClean.length() > 1000) {
            throw new IllegalArgumentException("Câu hỏi không được vượt quá 1.000 ký tự.");
        }

        HoiDapSanPham hd = new HoiDapSanPham();
        hd.setSanPham(sanPham);
        hd.setNguoiHoi(nguoiHoi);
        hd.setCauHoi(cauHoiClean);
        hd.setNgayHoi(LocalDateTime.now());
        return hoiDapSanPhamRepository.save(hd);
    }

    /**
     * Trả lời câu hỏi Hỏi-Đáp (Người bán hoặc Quản trị viên/Khách hàng phản hồi)
     */
    public HoiDapSanPham traLoiCauHoi(Long maHoiDap, Long maNguoiTraLoi, String cauTraLoi) {
        if (maHoiDap == null || maHoiDap <= 0) {
            throw new IllegalArgumentException("Mã câu hỏi không hợp lệ.");
        }
        if (maNguoiTraLoi == null || maNguoiTraLoi <= 0) {
            throw new IllegalArgumentException("Mã người trả lời không hợp lệ.");
        }

        HoiDapSanPham hd = hoiDapSanPhamRepository.findById(maHoiDap)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + maHoiDap));

        NguoiDung nguoiTraLoi = nguoiDungRepository.findById(maNguoiTraLoi)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người trả lời ID: " + maNguoiTraLoi));

        if (cauTraLoi == null || cauTraLoi.trim().isBlank()) {
            throw new IllegalArgumentException("Nội dung câu trả lời không được để trống.");
        }

        // Lọc mã độc HTML/XSS
        String cauTraLoiClean = cauTraLoi.replaceAll("<[^>]*>", "").trim();
        if (cauTraLoiClean.length() < 5) {
            throw new IllegalArgumentException("Câu trả lời phải có ít nhất 5 ký tự chi tiết và lịch sự.");
        }
        if (cauTraLoiClean.length() > 1000) {
            throw new IllegalArgumentException("Câu trả lời không được vượt quá 1.000 ký tự.");
        }

        hd.setCauTraLoi(cauTraLoiClean);
        hd.setNguoiTraLoi(nguoiTraLoi);
        hd.setNgayTraLoi(LocalDateTime.now());

        return hoiDapSanPhamRepository.save(hd);
    }

    /**
     * Tra cứu danh sách hỏi đáp của sản phẩm có phân trang và bộ lọc
     */
    @Transactional(readOnly = true)
    public Page<HoiDapSanPham> layDanhSachHoiDapChoSanPham(
            Long maSanPham,
            String trangThai,
            String tuKhoa,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        String tuKhoaClean = (tuKhoa != null && !tuKhoa.trim().isBlank()) ? tuKhoa.trim() : null;
        String tuKhoaPattern = (tuKhoaClean != null) ? "%" + tuKhoaClean + "%" : null;
        return hoiDapSanPhamRepository.timKiemHoiDapChoSanPham(maSanPham, trangThai, tuKhoaClean, tuKhoaPattern, pageable);
    }

    /**
     * Lấy các chỉ số thống kê Q&A của sản phẩm
     */
    @Transactional(readOnly = true)
    public ThongKeHoiDapSanPhamDTO layThongKeHoiDap(Long maSanPham) {
        long tong = hoiDapSanPhamRepository.countBySanPham_MaSanPham(maSanPham);
        long daTraLoi = hoiDapSanPhamRepository.demCauHoiDaTraLoi(maSanPham);
        long choTraLoi = hoiDapSanPhamRepository.demCauHoiChoTraLoi(maSanPham);
        int tyLe = (tong > 0) ? (int) Math.round(((double) daTraLoi / tong) * 100) : 0;

        return ThongKeHoiDapSanPhamDTO.builder()
                .tongSoCauHoi(tong)
                .soCauHoiDaTraLoi(daTraLoi)
                .soCauHoiChoTraLoi(choTraLoi)
                .tyLeDaTraLoi(tyLe)
                .build();
    }

    /**
     * Lấy tổng hợp chỉ số tương tác của khách hàng
     */
    @Transactional(readOnly = true)
    public ThongKeTuongTacKhachHangDTO layThongKeTuongTacKhachHang(Long maNguoiDung) {
        long tongYeuThich = sanPhamYeuThichRepository.countByMaNguoiDung(maNguoiDung);
        long tongTheoDoi = theoDoiGianHangRepository.countByMaNguoiDung(maNguoiDung);
        long tongCauHoi = hoiDapSanPhamRepository.countByNguoiHoi_MaNguoiDung(maNguoiDung);

        return ThongKeTuongTacKhachHangDTO.builder()
                .tongSoSanPhamYeuThich(tongYeuThich)
                .tongSoShopTheoDoi(tongTheoDoi)
                .tongSoCauHoiDaDat(tongCauHoi)
                .build();
    }
}
