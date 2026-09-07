package com.example.demo.controller;

import com.example.demo.dto.ThongKeVongQuayDTO;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.PhanThuongVongQuay;
import com.example.demo.entity.VongQuayMayMan;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.VongQuayMayManService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller giao diện Vòng Quay May Mắn (Shopee Lucky Wheel - US-66)
 * Khách hàng quay thưởng mỗi ngày nhận Xu và Voucher, xem lịch sử và quản trị thống kê.
 */
@Controller
public class VongQuayMayManController {

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    @Autowired
    private VongQuayMayManService vongQuayMayManService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * Xác định tài khoản khách hàng demo đang thao tác
     */
    private NguoiDung layKhachHangHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().contains("khach"))
                        .findFirst()
                        .orElseGet(() -> nguoiDungRepository.findAll().stream().findFirst().orElse(null)));
    }

    /**
     * 1. Màn hình Vòng Quay May Mắn chính của Khách Hàng
     */
    @GetMapping("/khach-hang/vong-quay-may-man")
    public String trangVongQuay(
            @RequestParam(value = "loaiPhanThuong", defaultValue = "TAT_CA") String loaiPhanThuong,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        Long maKhachHang = (khachHang != null) ? khachHang.getMaNguoiDung() : 4L;

        // Lấy danh sách 8 ô phần thưởng
        List<PhanThuongVongQuay> dsPhanThuong = vongQuayMayManService.layDanhSachPhanThuong();

        // Chuyển danh sách phần thưởng sang JSON an toàn (dùng Map sạch tránh lỗi Hibernate / LocalDateTime)
        String dsPhanThuongJson = "[]";
        try {
            List<java.util.Map<String, Object>> dsJson = dsPhanThuong.stream().map(pt -> {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("maPhanThuong", pt.getMaPhanThuong());
                m.put("thuTuO", pt.getThuTuO());
                m.put("tenPhanThuong", pt.getTenPhanThuong());
                m.put("loaiPhanThuong", pt.getLoaiPhanThuong());
                m.put("giaTriXu", pt.getGiaTriXu());
                m.put("mauSacO", pt.getMauSacO());
                m.put("mauChu", pt.getMauChu());
                m.put("icon", pt.getIcon());
                return m;
            }).collect(java.util.stream.Collectors.toList());
            dsPhanThuongJson = objectMapper.writeValueAsString(dsJson);
        } catch (Exception ex) {
            // fallback
        }

        // Lấy thống kê và số lượt quay còn lại
        ThongKeVongQuayDTO thongKe = vongQuayMayManService.layThongKeVongQuay(maKhachHang);

        // Lấy lịch sử quay cá nhân có phân trang và lọc
        Page<VongQuayMayMan> pageLichSu = vongQuayMayManService.layLichSuQuayCaNhan(maKhachHang, loaiPhanThuong, page, size);

        // Danh sách người chơi trúng thưởng mới nhất cho Live Ticker
        List<VongQuayMayMan> dsVinhDanh = vongQuayMayManService.layDanhSachVinhDanhMoiNhat();

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("dsPhanThuong", dsPhanThuong);
        model.addAttribute("dsPhanThuongJson", dsPhanThuongJson);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageLichSu", pageLichSu);
        model.addAttribute("dsLichSu", pageLichSu.getContent());
        model.addAttribute("loaiHienTai", loaiPhanThuong);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageLichSu.getTotalPages());
        model.addAttribute("totalElements", pageLichSu.getTotalElements());
        model.addAttribute("dsVinhDanh", dsVinhDanh);

        return "khach-hang/vong-quay-may-man";
    }

    /**
     * 2. Xử lý đổi 1.000 Xu lấy +1 lượt quay thêm
     */
    @PostMapping("/khach-hang/vong-quay-may-man/doi-luot")
    public String doiXuLayLuotQuay(RedirectAttributes redirectAttributes) {
        NguoiDung khachHang = layKhachHangHienTai();
        if (khachHang == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Không tìm thấy thông tin tài khoản!");
            return "redirect:/khach-hang/vong-quay-may-man";
        }

        try {
            Long soXuMoi = vongQuayMayManService.doiXuLayLuotQuay(khachHang.getMaNguoiDung());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Đổi 1.000 Xu lấy +1 lượt quay thành công! Số dư xu hiện tại: " + String.format("%,d", soXuMoi) + " Xu.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/khach-hang/vong-quay-may-man";
    }

    /**
     * 3. Màn hình Quản trị & Thống kê Vòng quay toàn sàn cho Quản trị viên / CSKH
     */
    @GetMapping({"/admin/vong-quay-may-man", "/quan-tri/vong-quay-may-man"})
    public String trangQuanTriVongQuay(
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "loaiPhanThuong", defaultValue = "TAT_CA") String loaiPhanThuong,
            @RequestParam(value = "tuNgay", required = false) String tuNgay,
            @RequestParam(value = "denNgay", required = false) String denNgay,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        ThongKeVongQuayDTO thongKe = vongQuayMayManService.layThongKeVongQuay(null);
        List<PhanThuongVongQuay> dsPhanThuong = vongQuayMayManService.layDanhSachPhanThuong();
        Page<VongQuayMayMan> pageLichSuToanSan = vongQuayMayManService.layLichSuQuayToanSan(
                tuKhoa, loaiPhanThuong, tuNgay, denNgay, page, size);

        model.addAttribute("thongKe", thongKe);
        model.addAttribute("dsPhanThuong", dsPhanThuong);
        model.addAttribute("pageLichSu", pageLichSuToanSan);
        model.addAttribute("dsLichSu", pageLichSuToanSan.getContent());
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("loaiHienTai", loaiPhanThuong);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageLichSuToanSan.getTotalPages());
        model.addAttribute("totalElements", pageLichSuToanSan.getTotalElements());

        return "khach-hang/thong-ke-vong-quay";
    }
}
