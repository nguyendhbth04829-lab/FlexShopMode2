package com.example.demo.controller;

import com.example.demo.dto.DanhGiaSanPhamForm;
import com.example.demo.dto.DonHangDanhGiaItemDTO;
import com.example.demo.dto.ThongKeDanhGiaDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.DanhGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller xử lý nghiệp vụ Đánh giá sản phẩm (US-49 - ENGAGE)
 * Dành cho Khách hàng đánh giá sản phẩm sau khi đơn hàng hoàn tất
 * Tự động đồng bộ điểm Rating trung bình của Sản phẩm & Shop
 */
@Controller
public class DanhGiaController {

    @Autowired
    private DanhGiaService danhGiaService;

    @Autowired
    private com.example.demo.service.TuongTacKhachHangService tuongTacKhachHangService;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DanhGiaSanPhamRepository danhGiaSanPhamRepository;

    /**
     * Lấy ID khách hàng hiện tại (mặc định lấy tài khoản demo khách hàng)
     */
    private Long layMaKhachHangHienTai() {
        return nguoiDungRepository.findByEmail("khachhang@flexshop.vn")
                .map(NguoiDung::getMaNguoiDung)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .findFirst()
                        .map(NguoiDung::getMaNguoiDung)
                        .orElse(1L));
    }

    /**
     * 1. Hiển thị danh sách sản phẩm trong đơn hàng để khách hàng chọn đánh giá
     */
    @GetMapping("/khach-hang/danh-gia/don-hang/{maDonHangShop}")
    public String hienThiDanhSachDanhGiaDonHang(
            @PathVariable("maDonHangShop") Long maDonHangShop,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long maKhachHang = layMaKhachHangHienTai();

        DonHangShop donHang = donHangShopRepository.findById(maDonHangShop).orElse(null);
        if (donHang == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Không tìm thấy đơn hàng mã #" + maDonHangShop);
            return "redirect:/khach-hang/danh-gia/cua-toi";
        }

        try {
            List<DonHangDanhGiaItemDTO> dsItem = danhGiaService.layDanhSachSanPhamDonHangDeDanhGia(maDonHangShop, maKhachHang);
            model.addAttribute("donHang", donHang);
            model.addAttribute("danhSachItem", dsItem);
            return "danh-gia/don-hang-danh-gia";
        } catch (SecurityException se) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", se.getMessage());
            return "redirect:/khach-hang/danh-gia/cua-toi";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/khach-hang/danh-gia/cua-toi";
        }
    }

    /**
     * 2. Mở giao diện viết đánh giá chi tiết cho 1 sản phẩm trong đơn
     */
    @GetMapping("/khach-hang/danh-gia/viet/{maChiTietDon}")
    public String hienThiFormVietDanhGia(
            @PathVariable("maChiTietDon") Long maChiTietDon,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long maKhachHang = layMaKhachHangHienTai();

        ChiTietDonHang chiTiet = chiTietDonHangRepository.findById(maChiTietDon).orElse(null);
        if (chiTiet == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Không tìm thấy sản phẩm đơn hàng mã #" + maChiTietDon);
            return "redirect:/khach-hang/danh-gia/cua-toi";
        }

        DonHangShop donHang = chiTiet.getDonHangShop();
        if (donHang == null || !"DA_GIAO".equalsIgnoreCase(donHang.getTrangThai())) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã hoàn tất giao hàng (ĐÃ GIAO)!");
            return "redirect:/khach-hang/danh-gia/don-hang/" + (donHang != null ? donHang.getMaDonHangShop() : 1);
        }

        // Kiểm tra xem đã đánh giá chưa
        if (danhGiaSanPhamRepository.existsByChiTietDonHang_MaChiTietDon(maChiTietDon)) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Sản phẩm này quý khách đã gửi đánh giá trước đó rồi.");
            return "redirect:/khach-hang/danh-gia/don-hang/" + donHang.getMaDonHangShop();
        }

        DanhGiaSanPhamForm form = new DanhGiaSanPhamForm();
        form.setMaChiTietDon(maChiTietDon);
        form.setSoSao(5); // Mặc định 5 sao
        form.setAnDanh(false);

        napThongTinSanPhamVaoModel(model, chiTiet);
        model.addAttribute("form", form);

        return "danh-gia/viet-danh-gia";
    }

    /**
     * 3. Tiếp nhận và lưu trữ đánh giá sản phẩm (POST)
     */
    @PostMapping("/khach-hang/danh-gia/gui")
    public String guiDanhGia(
            @Valid @ModelAttribute("form") DanhGiaSanPhamForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long maKhachHang = layMaKhachHangHienTai();

        if (bindingResult.hasErrors()) {
            if (form.getMaChiTietDon() != null) {
                chiTietDonHangRepository.findById(form.getMaChiTietDon())
                        .ifPresent(chiTiet -> napThongTinSanPhamVaoModel(model, chiTiet));
            }
            return "danh-gia/viet-danh-gia";
        }

        try {
            DanhGiaSanPham danhGia = danhGiaService.guiDanhGiaSanPham(form, maKhachHang);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Cảm ơn quý khách đã gửi đánh giá! Điểm đánh giá của sản phẩm và Shop đã được cập nhật thành công.");

            if (danhGia.getSanPham() != null) {
                return "redirect:/san-pham/" + danhGia.getSanPham().getMaSanPham() + "/danh-gia";
            }
            return "redirect:/khach-hang/danh-gia/cua-toi";
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            model.addAttribute("thongBaoLoi", e.getMessage());
            if (form.getMaChiTietDon() != null) {
                chiTietDonHangRepository.findById(form.getMaChiTietDon())
                        .ifPresent(chiTiet -> napThongTinSanPhamVaoModel(model, chiTiet));
            }
            return "danh-gia/viet-danh-gia";
        } catch (Exception e) {
            model.addAttribute("thongBaoLoi", "Có lỗi xảy ra trong quá trình gửi đánh giá: " + e.getMessage());
            if (form.getMaChiTietDon() != null) {
                chiTietDonHangRepository.findById(form.getMaChiTietDon())
                        .ifPresent(chiTiet -> napThongTinSanPhamVaoModel(model, chiTiet));
            }
            return "danh-gia/viet-danh-gia";
        }
    }

    /**
     * 4. Giao diện Xem đánh giá của Sản Phẩm (Public E-commerce view)
     * Kèm bảng phân tích số sao, bộ lọc theo sao / hình ảnh và phân trang
     */
    @GetMapping("/san-pham/{maSanPham}/danh-gia")
    public String xemDanhGiaSanPham(
            @PathVariable("maSanPham") Long maSanPham,
            @RequestParam(value = "soSao", required = false) Integer soSao,
            @RequestParam(value = "coHinhAnh", required = false) Boolean coHinhAnh,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        SanPham sanPham = sanPhamRepository.findById(maSanPham).orElse(null);
        if (sanPham == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Không tìm thấy sản phẩm mã #" + maSanPham);
            return "redirect:/khach-hang/danh-gia/cua-toi";
        }

        // Lấy link ảnh từ biến thể sản phẩm nếu có
        List<BienTheSanPham> dsBienThe = bienTheSanPhamRepository.findBySanPham_MaSanPham(maSanPham);
        dsBienThe.stream()
                .filter(bt -> bt.getLinkAnh() != null && !bt.getLinkAnh().isBlank())
                .findFirst()
                .ifPresent(bt -> sanPham.setLinkAnh(bt.getLinkAnh()));

        // Lấy thống kê tổng quan (phân bổ số sao)
        ThongKeDanhGiaDTO thongKe = danhGiaService.layThongKeDanhGiaSanPham(maSanPham);

        // Lấy danh sách đánh giá lọc theo điều kiện
        Page<DanhGiaSanPham> pageDanhGia = danhGiaService.layDanhSachDanhGiaSanPham(maSanPham, soSao, coHinhAnh, page, size);

        Long maKhachHang = layMaKhachHangHienTai();
        boolean daYeuThich = tuongTacKhachHangService.kiemTraDaYeuThich(maKhachHang, maSanPham);
        boolean daTheoDoiShop = (sanPham.getGianHang() != null)
                && tuongTacKhachHangService.kiemTraDaTheoDoi(maKhachHang, sanPham.getGianHang().getMaGianHang());
        long tongFollowShop = (sanPham.getGianHang() != null)
                ? tuongTacKhachHangService.demLuotTheoDoiGianHang(sanPham.getGianHang().getMaGianHang()) : 0;
        long tongLuotThich = tuongTacKhachHangService.demLuotYeuThichSanPham(maSanPham);
        long tongHoiDap = tuongTacKhachHangService.layThongKeHoiDap(maSanPham).getTongSoCauHoi();

        model.addAttribute("sanPham", sanPham);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageDanhGia", pageDanhGia);
        model.addAttribute("danhSachDanhGia", pageDanhGia.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", pageDanhGia.getTotalPages());
        model.addAttribute("totalElements", pageDanhGia.getTotalElements());
        model.addAttribute("soSao", soSao);
        model.addAttribute("coHinhAnh", coHinhAnh);
        model.addAttribute("daYeuThich", daYeuThich);
        model.addAttribute("daTheoDoiShop", daTheoDoiShop);
        model.addAttribute("tongFollowShop", tongFollowShop);
        model.addAttribute("tongLuotThich", tongLuotThich);
        model.addAttribute("tongHoiDap", tongHoiDap);

        return "danh-gia/san-pham-danh-gia";
    }

    /**
     * 5. Lịch sử đánh giá của Khách hàng hiện tại
     */
    @GetMapping("/khach-hang/danh-gia/cua-toi")
    public String lichSuDanhGiaCuaToi(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model
    ) {
        Long maKhachHang = layMaKhachHangHienTai();
        Page<DanhGiaSanPham> pageDanhGia = danhGiaService.layDanhSachDanhGiaCuaToi(maKhachHang, page, size);

        model.addAttribute("pageDanhGia", pageDanhGia);
        model.addAttribute("danhSachDanhGia", pageDanhGia.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", pageDanhGia.getTotalPages());
        model.addAttribute("totalElements", pageDanhGia.getTotalElements());

        return "danh-gia/danh-gia-cua-toi";
    }

    private void napThongTinSanPhamVaoModel(Model model, ChiTietDonHang chiTiet) {
        model.addAttribute("chiTiet", chiTiet);
        model.addAttribute("donHangShop", chiTiet.getDonHangShop());

        String tenSanPham = "Sản phẩm FlexShop";
        String linkAnh = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=200";
        String tenPhanLoai = "";

        if (chiTiet.getMaBienThe() != null) {
            Optional<BienTheSanPham> optBt = bienTheSanPhamRepository.findById(chiTiet.getMaBienThe());
            if (optBt.isPresent()) {
                BienTheSanPham bt = optBt.get();
                if (bt.getSanPham() != null) {
                    tenSanPham = bt.getSanPham().getTenSanPham();
                    model.addAttribute("maSanPhamGoc", bt.getSanPham().getMaSanPham());
                }
                if (bt.getTenBienThe() != null) {
                    tenPhanLoai = bt.getTenBienThe();
                }
                if (bt.getLinkAnh() != null && !bt.getLinkAnh().isBlank()) {
                    linkAnh = bt.getLinkAnh();
                }
            }
        }

        model.addAttribute("tenSanPham", tenSanPham);
        model.addAttribute("linkAnh", linkAnh);
        model.addAttribute("tenPhanLoai", tenPhanLoai);
    }
}
