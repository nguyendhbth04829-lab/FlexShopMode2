package com.example.demo.controller;

import com.example.demo.dto.DangKyTraSauRequestDTO;
import com.example.demo.dto.ThanhToanKyTraSauRequestDTO;
import com.example.demo.dto.TraSauThongKeDTO;
import com.example.demo.dto.VayTraSauRequestDTO;
import com.example.demo.entity.DonHangTong;
import com.example.demo.entity.HopDongTraSau;
import com.example.demo.entity.KyThanhToanTraSau;
import com.example.demo.entity.TaiKhoanTraSau;
import com.example.demo.repository.DonHangTongRepository;
import com.example.demo.service.TraSauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TÍN DỤNG TIÊU DÙNG (DEV 5 - MINH)
 * USER STORY: US-63 - Controller Dịch Vụ Mua Trước Trả Sau (SPayLater / BNPL)
 * =====================================================================
 */
@Controller
@RequestMapping("/tra-sau")
public class TraSauController {

    @Autowired
    private TraSauService traSauService;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    /**
     * US-63: Màn hình Quản Lý Ví SPayLater Cá Nhân (Hạn mức, Dư nợ, Điểm tín dụng)
     */
    @GetMapping("/tai-khoan")
    public String taiKhoan(
            Model model,
            @RequestParam(value = "maNguoiDung", defaultValue = "1") Long maNguoiDung
    ) {
        TraSauThongKeDTO thongKe = traSauService.layThongKeTraSau(maNguoiDung);
        TaiKhoanTraSau taiKhoan = traSauService.layHoacTaoTaiKhoan(maNguoiDung);

        model.addAttribute("thongKe", thongKe);
        model.addAttribute("taiKhoan", taiKhoan);

        // Form đăng ký kích hoạt
        DangKyTraSauRequestDTO dangKyDTO = new DangKyTraSauRequestDTO();
        dangKyDTO.setMaNguoiDung(maNguoiDung);
        dangKyDTO.setHoVaTen(taiKhoan.getNguoiDung().getHoVaTen());
        dangKyDTO.setSoDienThoai(taiKhoan.getNguoiDung().getSoDienThoai());
        model.addAttribute("dangKyDTO", dangKyDTO);

        return "tra-sau/tai-khoan";
    }

    /**
     * US-63: Xử lý Đăng ký / Kích hoạt Ví SPayLater
     */
    @PostMapping("/kich-hoat")
    public String kichHoat(
            @ModelAttribute("dangKyDTO") DangKyTraSauRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dto.validate();
            TaiKhoanTraSau tk = traSauService.kichHoatTaiKhoan(dto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Kích hoạt thành công Ví SPayLater! Hạn mức được cấp: " + 
                    String.format("%,.0f", tk.getHanMucDuocCap()) + " VNĐ.");
            return "redirect:/tra-sau/tai-khoan?maNguoiDung=" + dto.getMaNguoiDung();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tra-sau/tai-khoan?maNguoiDung=" + (dto.getMaNguoiDung() != null ? dto.getMaNguoiDung() : 1);
        }
    }

    /**
     * US-63: Màn hình Xác Nhận Gói Trả Sau Cho Đơn Hàng Tổng
     */
    @GetMapping("/xac-nhan-vay")
    public String xacNhanVay(
            Model model,
            @RequestParam(value = "maDonHangTong", required = false) Long maDonHangTong,
            RedirectAttributes redirectAttributes
    ) {
        if (maDonHangTong == null) {
            List<DonHangTong> all = donHangTongRepository.findAll();
            if (all.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng nào để thanh toán SPayLater!");
                return "redirect:/thanh-toan/danh-sach";
            }
            maDonHangTong = all.get(all.size() - 1).getMaDonHangTong();
        }

        DonHangTong donHang = donHangTongRepository.findById(maDonHangTong).orElse(null);
        if (donHang == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng tổng #" + maDonHangTong);
            return "redirect:/thanh-toan/danh-sach";
        }

        Long maKhachHang = donHang.getKhachHang().getMaNguoiDung();
        TaiKhoanTraSau taiKhoan = traSauService.layHoacTaoTaiKhoan(maKhachHang);

        try {
            traSauService.kiemTraDieuKienVay(maKhachHang, donHang.getTongThanhToanCuoi());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/thanh-toan/chon-phuong-thuc?id=" + maDonHangTong;
        }

        // Tạo trước bảng tính các gói: 1 kỳ, 3 kỳ, 6 kỳ, 12 kỳ
        BigDecimal tongTien = donHang.getTongThanhToanCuoi();
        List<KyThanhToanTraSau> duTinh1Ky = traSauService.tinhDuTinhKyTraGop(tongTien, 1);
        List<KyThanhToanTraSau> duTinh3Ky = traSauService.tinhDuTinhKyTraGop(tongTien, 3);
        List<KyThanhToanTraSau> duTinh6Ky = traSauService.tinhDuTinhKyTraGop(tongTien, 6);
        List<KyThanhToanTraSau> duTinh12Ky = traSauService.tinhDuTinhKyTraGop(tongTien, 12);

        VayTraSauRequestDTO vayDTO = new VayTraSauRequestDTO();
        vayDTO.setMaDonHangTong(maDonHangTong);
        vayDTO.setMaNguoiDung(maKhachHang);
        vayDTO.setSoKyTraGop(3);

        model.addAttribute("donHang", donHang);
        model.addAttribute("taiKhoan", taiKhoan);
        model.addAttribute("vayDTO", vayDTO);
        model.addAttribute("duTinh1Ky", duTinh1Ky);
        model.addAttribute("duTinh3Ky", duTinh3Ky);
        model.addAttribute("duTinh6Ky", duTinh6Ky);
        model.addAttribute("duTinh12Ky", duTinh12Ky);

        return "tra-sau/xac-nhan-vay";
    }

    /**
     * US-63: Xử lý Vay Trả Sau SPayLater & Thanh Toán Đơn Hàng
     */
    @PostMapping("/xac-nhan-vay")
    public String xuLyVay(
            @ModelAttribute("vayDTO") VayTraSauRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dto.validate();
            HopDongTraSau savedHopDong = traSauService.taoHopDongVaGiaiNgan(dto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Chúc mừng! Hợp đồng SPayLater #" + savedHopDong.getMaHopDong() + 
                    " đã được phê duyệt thành công. Đơn hàng đã được thanh toán và chuyển cho Shop chuẩn bị hàng!");
            return "redirect:/tra-sau/hop-dong/" + savedHopDong.getMaHopDong();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tra-sau/xac-nhan-vay?maDonHangTong=" + dto.getMaDonHangTong();
        }
    }

    /**
     * US-63: Danh Sách Hợp Đồng Trả Sau (Phân trang, Tìm kiếm, Lọc)
     */
    @GetMapping("/danh-sach-hop-dong")
    public String danhSachHopDong(
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "maTkTraSau", required = false) Long maTkTraSau,
            @RequestParam(value = "page", defaultValue = "0") Integer page
    ) {
        int pageSize = 6;
        Page<HopDongTraSau> pageHopDong = traSauService.getDanhSachHopDongPhanTrang(keyword, maTkTraSau, page, pageSize);

        model.addAttribute("listHopDong", pageHopDong.getContent());
        model.addAttribute("pageHopDong", pageHopDong);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageHopDong.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("maTkTraSau", maTkTraSau);

        return "tra-sau/danh-sach-hop-dong";
    }

    /**
     * US-63: Chi Tiết Hợp Đồng & Lịch Trả Nợ Từng Kỳ
     */
    @GetMapping("/hop-dong/{id}")
    public String chiTietHopDong(
            @PathVariable("id") Long id,
            Model model
    ) {
        HopDongTraSau hopDong = traSauService.getChiTietHopDong(id);
        model.addAttribute("hopDong", hopDong);

        ThanhToanKyTraSauRequestDTO traNoDTO = new ThanhToanKyTraSauRequestDTO();
        traNoDTO.setMaHopDong(id);
        model.addAttribute("traNoDTO", traNoDTO);

        return "tra-sau/chi-tiet-hop-dong";
    }

    /**
     * US-63: Trả Nợ Kỳ Trả Góp (Thanh toán nợ & Khôi phục hạn mức)
     */
    @PostMapping("/tra-no-ky")
    public String traNoKy(
            @ModelAttribute("traNoDTO") ThanhToanKyTraSauRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dto.validate();
            KyThanhToanTraSau ky = traSauService.thanhToanKyTraGop(dto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Thanh toán thành công Kỳ số " + ky.getKySo() + " với số tiền " + 
                    String.format("%,.0f", dto.getSoTienThanhToan()) + " VNĐ! Hạn mức khả dụng đã được khôi phục.");
            return "redirect:/tra-sau/hop-dong/" + dto.getMaHopDong();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tra-sau/hop-dong/" + dto.getMaHopDong();
        }
    }

    /**
     * US-63: Quét kiểm tra nợ quá hạn
     */
    @PostMapping("/quet-qua-han")
    public String quetQuaHan(RedirectAttributes redirectAttributes) {
        int count = traSauService.kiemTraVaCapNhatNoQuaHan();
        if (count > 0) {
            redirectAttributes.addFlashAttribute("warningMessage", 
                    "Đã quét và phát hiện " + count + " kỳ nợ quá hạn! Trạng thái tài khoản tương ứng đã được tạm khóa.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Quét hoàn tất: Không có kỳ nợ nào bị quá hạn mới.");
        }
        return "redirect:/tra-sau/tai-khoan";
    }
}
