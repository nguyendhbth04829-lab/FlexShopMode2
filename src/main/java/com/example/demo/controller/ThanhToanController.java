package com.example.demo.controller;

import com.example.demo.dto.ThanhToanRequestDTO;
import com.example.demo.entity.DonHangTong;
import com.example.demo.service.ThanhToanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/thanh-toan")
public class ThanhToanController {

    @Autowired
    private ThanhToanService thanhToanService;

    /**
     * US-26: Danh sách các đơn hàng cần thanh toán / theo dõi trạng thái thanh toán
     */
    @GetMapping("/danh-sach")
    public String danhSach(
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThaiThanhToan", required = false) String trangThaiThanhToan,
            @RequestParam(value = "page", defaultValue = "0") Integer page
    ) {
        int pageSize = 5;
        Page<DonHangTong> pageDonHang = thanhToanService.getDanhSachDonHang(keyword, trangThaiThanhToan, page, pageSize);

        model.addAttribute("listDonHang", pageDonHang.getContent());
        model.addAttribute("pageDonHang", pageDonHang);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageDonHang.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThaiThanhToan", trangThaiThanhToan);

        return "thanh-toan/danh-sach";
    }

    /**
     * US-26: Màn hình chọn phương thức thanh toán (COD hoặc Mock Online Payment)
     */
    @GetMapping("/chon-phuong-thuc")
    public String chonPhuongThuc(Model model, @RequestParam("id") Long id) {
        DonHangTong donHang = thanhToanService.getDonHangTong(id);
        model.addAttribute("donHang", donHang);

        ThanhToanRequestDTO dto = new ThanhToanRequestDTO();
        dto.setMaDonHangTong(id);
        dto.setPhuongThuc("COD");
        model.addAttribute("thanhToanDTO", dto);

        return "thanh-toan/chon-phuong-thuc";
    }

    /**
     * US-26: Xử lý chọn phương thức thanh toán từ form
     */
    @PostMapping("/xac-nhan")
    public String xacNhanThanhToan(
            @ModelAttribute("thanhToanDTO") ThanhToanRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if ("COD".equalsIgnoreCase(dto.getPhuongThuc())) {
                thanhToanService.xuLyChonCOD(dto.getMaDonHangTong());
                redirectAttributes.addFlashAttribute("successMessage", 
                        "Đã xác nhận phương thức COD! Đơn hàng đã được chuyển tới các Shop để đóng gói và giao hàng.");
                return "redirect:/thanh-toan/ket-qua?id=" + dto.getMaDonHangTong();
            } else if ("MOCK_ONLINE".equalsIgnoreCase(dto.getPhuongThuc())) {
                // Điều hướng sang cổng thanh toán trực tuyến giả lập Mock Gateway
                return "redirect:/thanh-toan/mock-gateway?id=" + dto.getMaDonHangTong();
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Phương thức thanh toán không hợp lệ!");
                return "redirect:/thanh-toan/chon-phuong-thuc?id=" + dto.getMaDonHangTong();
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/thanh-toan/chon-phuong-thuc?id=" + dto.getMaDonHangTong();
        }
    }

    /**
     * US-26: Cổng thanh toán trực tuyến giả lập (Mock Online Payment Gateway)
     */
    @GetMapping("/mock-gateway")
    public String mockGateway(Model model, @RequestParam("id") Long id) {
        DonHangTong donHang = thanhToanService.getDonHangTong(id);
        model.addAttribute("donHang", donHang);
        return "thanh-toan/mock-gateway";
    }

    /**
     * US-26: Xử lý kết quả từ Cổng giả lập (Thành công / Thất bại)
     */
    @PostMapping("/mock-process")
    public String mockProcess(
            @RequestParam("id") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "maNganHang", defaultValue = "FLEXPAY_BANK") String maNganHang,
            @RequestParam(value = "soThe", defaultValue = "9704-8888-9999-0001") String soThe,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if ("SUCCESS".equalsIgnoreCase(status)) {
                thanhToanService.xuLyMockOnlineThanhCong(id, maNganHang, soThe);
                redirectAttributes.addFlashAttribute("successMessage", 
                        "Thanh toán trực tuyến thành công qua cổng Mock Payment Gateway!");
            } else {
                thanhToanService.xuLyMockOnlineThatBai(id, "Giao dịch bị từ chối hoặc người dùng hủy");
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "Giao dịch thanh toán trực tuyến không thành công hoặc đã bị từ chối.");
            }
            return "redirect:/thanh-toan/ket-qua?id=" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/thanh-toan/danh-sach";
        }
    }

    /**
     * US-26: Màn hình kết quả thanh toán / Hóa đơn điện tử
     */
    @GetMapping("/ket-qua")
    public String ketQua(Model model, @RequestParam("id") Long id) {
        DonHangTong donHang = thanhToanService.getDonHangTong(id);
        model.addAttribute("donHang", donHang);
        return "thanh-toan/ket-qua";
    }
}
