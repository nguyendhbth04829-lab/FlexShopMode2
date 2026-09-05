package com.example.demo.controller;

import com.example.demo.dto.ApDungVoucherRequestDTO;
import com.example.demo.dto.KetQuaApDungVoucherDTO;
import com.example.demo.dto.ThongKeVoucherDTO;
import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.DonHangTong;
import com.example.demo.entity.LichSuDungMaGiamGia;
import com.example.demo.entity.MaGiamGia;
import com.example.demo.entity.NguoiDung;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.DonHangTongRepository;
import com.example.demo.repository.LichSuDungMaGiamGiaRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý nghiệp vụ Voucher & Thanh toán lồng nhau (US-52 - VOUCHER)
 * Khách hàng áp dụng đồng thời: Freeship Sàn + Voucher Sàn + Voucher Shop.
 */
@Controller
public class VoucherController {

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private LichSuDungMaGiamGiaRepository lichSuDungMaGiamGiaRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    /**
     * Xác định tài khoản khách hàng đang đăng nhập
     */
    private NguoiDung layKhachHangHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().contains("khach"))
                        .findFirst()
                        .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElse(null)));
    }

    // =========================================================================
    // 1. GIAO DIỆN CHECKOUT & ÁP DỤNG ĐỒNG THỜI 3 TẦNG VOUCHER
    // =========================================================================

    /**
     * Trang thanh toán / checkout hiển thị form áp dụng voucher 3 tầng lồng nhau
     */
    @GetMapping("/khach-hang/thanh-toan/voucher")
    public String trangThanhToanVoucher(
            @RequestParam(value = "maDonHangTong", required = false) Long maDonHangTong,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

        // Lấy danh sách đơn hàng gần đây của khách hàng để người dùng dễ dàng chọn test
        List<DonHangTong> danhSachDonHang = donHangTongRepository.findByKhachHangMaNguoiDungOrderByNgayTaoDesc(maKhachHang);
        if (danhSachDonHang.isEmpty()) {
            danhSachDonHang = donHangTongRepository.findAll();
        }

        DonHangTong donHangDuocChon = null;
        if (maDonHangTong != null) {
            donHangDuocChon = donHangTongRepository.findById(maDonHangTong).orElse(null);
        }
        if (donHangDuocChon == null && !danhSachDonHang.isEmpty()) {
            donHangDuocChon = danhSachDonHang.get(0);
        }

        List<DonHangShop> danhSachDonShop = List.of();
        KetQuaApDungVoucherDTO ketQuaBanDau = null;

        if (donHangDuocChon != null) {
            danhSachDonShop = donHangShopRepository.findByDonHangTongMaDonHangTong(donHangDuocChon.getMaDonHangTong());

            // Tính toán mặc định khi chưa nhập mã
            ApDungVoucherRequestDTO initRequest = new ApDungVoucherRequestDTO();
            initRequest.setMaDonHangTong(donHangDuocChon.getMaDonHangTong());
            ketQuaBanDau = voucherService.tinhToanVoucherStackable(initRequest, maKhachHang);
        }

        // Lấy danh sách voucher gợi ý (khả dụng)
        Map<String, List<MaGiamGia>> voucherGoiY = voucherService.layDanhSachVoucherGoiYChoDonHang(
                donHangDuocChon != null ? donHangDuocChon.getMaDonHangTong() : null
        );

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("danhSachDonHang", danhSachDonHang);
        model.addAttribute("donHangHienTai", donHangDuocChon);
        model.addAttribute("danhSachDonShop", danhSachDonShop);
        model.addAttribute("ketQuaVoucher", ketQuaBanDau);
        model.addAttribute("voucherGoiY", voucherGoiY);
        model.addAttribute("requestVoucher", new ApDungVoucherRequestDTO());

        return "khach-hang/thanh-toan-voucher";
    }

    /**
     * API AJAX tính toán phân bổ voucher thời gian thực (trả JSON)
     */
    @PostMapping("/khach-hang/thanh-toan/voucher/tinh-toan-ajax")
    @ResponseBody
    public ResponseEntity<KetQuaApDungVoucherDTO> tinhToanVoucherAjax(
            @RequestBody ApDungVoucherRequestDTO request
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, maKhachHang);
        return ResponseEntity.ok(ketQua);
    }

    /**
     * Xử lý Form POST tính toán phân bổ voucher (Hiển thị ngay trên View)
     */
    @PostMapping("/khach-hang/thanh-toan/voucher/tinh-toan")
    public String tinhToanVoucherForm(
            @ModelAttribute("requestVoucher") ApDungVoucherRequestDTO request,
            @RequestParam Map<String, String> allParams,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

        // Trích xuất mã voucher shop từ form parameters (maVoucherShop_1, maVoucherShop_2, ...)
        Map<Long, String> shopMap = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("maVoucherShop_")) {
                try {
                    Long maShop = Long.parseLong(entry.getKey().replace("maVoucherShop_", ""));
                    if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                        shopMap.put(maShop, entry.getValue().trim());
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        request.setMaVoucherShopMap(shopMap);

        KetQuaApDungVoucherDTO ketQua = voucherService.tinhToanVoucherStackable(request, maKhachHang);

        DonHangTong donHangDuocChon = donHangTongRepository.findById(request.getMaDonHangTong()).orElse(null);
        List<DonHangShop> danhSachDonShop = (donHangDuocChon != null)
                ? donHangShopRepository.findByDonHangTongMaDonHangTong(donHangDuocChon.getMaDonHangTong())
                : List.of();

        List<DonHangTong> danhSachDonHang = donHangTongRepository.findByKhachHangMaNguoiDungOrderByNgayTaoDesc(maKhachHang);
        if (danhSachDonHang.isEmpty()) danhSachDonHang = donHangTongRepository.findAll();

        Map<String, List<MaGiamGia>> voucherGoiY = voucherService.layDanhSachVoucherGoiYChoDonHang(
                donHangDuocChon != null ? donHangDuocChon.getMaDonHangTong() : null
        );

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("danhSachDonHang", danhSachDonHang);
        model.addAttribute("donHangHienTai", donHangDuocChon);
        model.addAttribute("danhSachDonShop", danhSachDonShop);
        model.addAttribute("ketQuaVoucher", ketQua);
        model.addAttribute("voucherGoiY", voucherGoiY);
        model.addAttribute("requestVoucher", request);

        return "khach-hang/thanh-toan-voucher";
    }

    /**
     * Xác nhận chốt đơn hàng & áp dụng đồng thời các tầng voucher:
     * Cập nhật DonHangTong, DonHangShop, lưu LichSuDungMaGiamGia
     */
    @PostMapping("/khach-hang/thanh-toan/voucher/xac-nhan")
    public String xacNhanDonHang(
            @ModelAttribute("requestVoucher") ApDungVoucherRequestDTO request,
            @RequestParam Map<String, String> allParams,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NguoiDung khachHang = layKhachHangHienTai();
            Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

            // Thu thập mã voucher shop
            Map<Long, String> shopMap = new HashMap<>();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("maVoucherShop_")) {
                    try {
                        Long maShop = Long.parseLong(entry.getKey().replace("maVoucherShop_", ""));
                        if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                            shopMap.put(maShop, entry.getValue().trim());
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            request.setMaVoucherShopMap(shopMap);

            KetQuaApDungVoucherDTO ketQua = voucherService.xacNhanApDungVoucherChoDonHang(request, maKhachHang);

            String msg = "Áp dụng thành công đồng thời các voucher! Đơn hàng được giảm tổng cộng " +
                    voucherService.dinhDangTien(ketQua.getTongTietKiem()) +
                    " (Sàn tài trợ: " + voucherService.dinhDangTien(ketQua.getTongTaiTroSan()) +
                    ", Shop tài trợ: " + voucherService.dinhDangTien(ketQua.getTongTaiTroShop()) +
                    "). Số tiền cuối cùng cần thanh toán: " + voucherService.dinhDangTien(ketQua.getTongThanhToanCuoi()) + "!";

            redirectAttributes.addFlashAttribute("thongBaoThanhCong", msg);
            return "redirect:/khach-hang/thanh-toan/voucher?maDonHangTong=" + request.getMaDonHangTong();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
            return "redirect:/khach-hang/thanh-toan/voucher?maDonHangTong=" + request.getMaDonHangTong();
        }
    }

    // =========================================================================
    // 2. KHO VOUCHER CỦA KHÁCH HÀNG (VOUCHER WALLET)
    // =========================================================================

    /**
     * Trang xem danh sách Kho Voucher của khách hàng
     */
    @GetMapping("/khach-hang/kho-voucher")
    public String xemKhoVoucher(
            @RequestParam(value = "tab", defaultValue = "TAT_CA") String tab,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

        Page<MaGiamGia> pageVoucher = voucherService.layDanhSachKhoVoucher(tab, tuKhoa, page, size);
        ThongKeVoucherDTO thongKe = voucherService.layThongKeVoucher(maKhachHang);

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("pageVoucher", pageVoucher);
        model.addAttribute("danhSachVoucher", pageVoucher.getContent());
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("tabHienTai", tab);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageVoucher.getTotalPages());
        model.addAttribute("totalElements", pageVoucher.getTotalElements());

        return "khach-hang/kho-voucher";
    }

    // =========================================================================
    // 3. LỊCH SỬ SỬ DỤNG VOUCHER
    // =========================================================================

    /**
     * Xem danh sách lịch sử áp dụng voucher của tài khoản khách hàng
     */
    @GetMapping("/khach-hang/voucher/lich-su")
    public String xemLichSuVoucher(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 1L;

        Page<LichSuDungMaGiamGia> pageLichSu = lichSuDungMaGiamGiaRepository.findByNguoiDungMaNguoiDungOrderByNgaySuDungDesc(
                maKhachHang, PageRequest.of(page, size)
        );
        ThongKeVoucherDTO thongKe = voucherService.layThongKeVoucher(maKhachHang);

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("pageLichSu", pageLichSu);
        model.addAttribute("danhSachLichSu", pageLichSu.getContent());
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageLichSu.getTotalPages());
        model.addAttribute("totalElements", pageLichSu.getTotalElements());

        return "khach-hang/lich-su-voucher";
    }
}
