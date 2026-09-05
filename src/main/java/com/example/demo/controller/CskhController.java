package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.LenhHoanTienBoiThuong;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.PhieuKhieuNai;
import com.example.demo.repository.LenhHoanTienBoiThuongRepository;
import com.example.demo.service.CskhService;
import com.example.demo.service.PhanQuyetTranhChapService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cskh")
public class CskhController {

    // Nhân viên CSKH mặc định cho phiên làm việc Demo (Ngô Thị CSKH Hỗ Trợ - ID 6)
    private static final Long MA_CSKH_MAC_DINH = 6L;

    @Autowired
    private CskhService cskhService;

    @Autowired
    private PhanQuyetTranhChapService phanQuyetTranhChapService;

    @Autowired
    private LenhHoanTienBoiThuongRepository lenhHoanTienBoiThuongRepository;

    /**
     * Dashboard tiếp nhận & phân loại ticket CSKH (US-46)
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(name = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(name = "loaiKhieuNai", required = false) String loaiKhieuNai,
            @RequestParam(name = "mucDoUuTien", required = false) String mucDoUuTien,
            @RequestParam(name = "locPhuTrach", required = false, defaultValue = "TAT_CA") String locPhuTrach,
            @RequestParam(name = "tuNgay", required = false) String tuNgay,
            @RequestParam(name = "denNgay", required = false) String denNgay,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        ThongKeDashboardCskhDTO thongKe = cskhService.layThongKeDashboard();
        Page<PhieuKhieuNai> pageTicket = cskhService.layDanhSachTicketCskh(
                tuKhoa, trangThai, loaiKhieuNai, mucDoUuTien, locPhuTrach, MA_CSKH_MAC_DINH, tuNgay, denNgay, page, size
        );
        List<NguoiDung> dsNhanVien = cskhService.layDanhSachNhanVienCskh();

        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageTicket", pageTicket);
        model.addAttribute("dsNhanVien", dsNhanVien);
        model.addAttribute("maCskhHienTai", MA_CSKH_MAC_DINH);

        // Giữ lại tham số tìm lọc trên form giao diện
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("loaiKhieuNai", loaiKhieuNai);
        model.addAttribute("mucDoUuTien", mucDoUuTien);
        model.addAttribute("locPhuTrach", locPhuTrach);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("size", size);

        return "cskh/dashboard";
    }

    /**
     * Tra cứu đối chiếu 3 bên (Khách hàng - Shop - Shipper POD) và ghi chú nội bộ (US-46)
     */
    @GetMapping("/doi-chieu/{maPhieu}")
    public String manHinhDoiChieuBaBen(@PathVariable("maPhieu") Long maPhieu, Model model) {
        DoiChieuBaBenDTO doiChieu = cskhService.layDuLieuDoiChieuBaBen(maPhieu);
        List<NguoiDung> dsNhanVien = cskhService.layDanhSachNhanVienCskh();

        model.addAttribute("doiChieu", doiChieu);
        model.addAttribute("dsNhanVien", dsNhanVien);
        model.addAttribute("maCskhHienTai", MA_CSKH_MAC_DINH);

        if (!model.containsAttribute("formGhiChu")) {
            GhiChuNoiBoForm formGhiChu = new GhiChuNoiBoForm();
            formGhiChu.setMaNhanVien(MA_CSKH_MAC_DINH);
            model.addAttribute("formGhiChu", formGhiChu);
        }

        PhanLoaiTicketForm formPhanLoai = new PhanLoaiTicketForm();
        formPhanLoai.setLoaiKhieuNai(doiChieu.getPhieuKhieuNai().getLoaiKhieuNai());
        formPhanLoai.setMucDoUuTien(doiChieu.getPhieuKhieuNai().getMucDoUuTien());
        formPhanLoai.setTrangThai(doiChieu.getPhieuKhieuNai().getTrangThai());
        if (doiChieu.getPhieuKhieuNai().getCskhXuLy() != null) {
            formPhanLoai.setMaCskhPhuTrach(doiChieu.getPhieuKhieuNai().getCskhXuLy().getMaNguoiDung());
        }
        model.addAttribute("formPhanLoai", formPhanLoai);

        // US-47: Lấy lệnh hoàn tiền / bồi thường (nếu đã có phán quyết)
        Optional<LenhHoanTienBoiThuong> optLenh = lenhHoanTienBoiThuongRepository.findByPhieuKhieuNai_MaPhieu(maPhieu);
        model.addAttribute("lenhHoanTien", optLenh.orElse(null));

        // Form Phán quyết tranh chấp (nếu chưa có trong model từ flash attribute)
        if (!model.containsAttribute("formPhanQuyet")) {
            PhanQuyetTranhChapForm formPhanQuyet = new PhanQuyetTranhChapForm();
            formPhanQuyet.setMaPhieu(maPhieu);
            formPhanQuyet.setQuyetDinh("DUYET_HOAN_TIEN_KHACH");
            formPhanQuyet.setBenChiuPhi("NGUOI_BAN");
            BigDecimal tienMacDinh = (doiChieu.getPhieuKhieuNai().getSoTienHoanTra() != null &&
                    doiChieu.getPhieuKhieuNai().getSoTienHoanTra().compareTo(BigDecimal.ZERO) > 0)
                    ? doiChieu.getPhieuKhieuNai().getSoTienHoanTra()
                    : doiChieu.getPhieuKhieuNai().getDonHangShop().getTongTienShopNhan();
            formPhanQuyet.setSoTien(tienMacDinh);
            model.addAttribute("formPhanQuyet", formPhanQuyet);
        }

        return "cskh/doi-chieu-ba-ben";
    }

    /**
     * CSKH tiếp nhận nhanh ticket
     */
    @PostMapping("/tiep-nhan/{maPhieu}")
    public String tiepNhanTicket(
            @PathVariable("maPhieu") Long maPhieu,
            @RequestParam(name = "redirectUrl", required = false, defaultValue = "/cskh/dashboard") String redirectUrl,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PhieuKhieuNai p = cskhService.tiepNhanTicket(maPhieu, MA_CSKH_MAC_DINH);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Đã tiếp nhận thành công Ticket [" + p.getMaCodePhieu() + "]. Trạng thái chuyển sang 'CSKH đang xử lý'.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi tiếp nhận ticket: " + e.getMessage());
        }
        return "redirect:" + redirectUrl;
    }

    /**
     * Phân loại ticket và cập nhật mức độ ưu tiên
     */
    @PostMapping("/phan-loai/{maPhieu}")
    public String phanLoaiTicket(
            @PathVariable("maPhieu") Long maPhieu,
            @Valid @ModelAttribute("formPhanLoai") PhanLoaiTicketForm form,
            BindingResult bindingResult,
            @RequestParam(name = "redirectUrl", required = false, defaultValue = "/cskh/dashboard") String redirectUrl,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String loiChiTiet = bindingResult.getFieldErrors().stream()
                    .map(fe -> fe.getDefaultMessage())
                    .reduce((m1, m2) -> m1 + "; " + m2)
                    .orElse("Dữ liệu phân loại chưa hợp lệ, vui lòng kiểm tra lại.");
            redirectAttributes.addFlashAttribute("thongBaoLoi", loiChiTiet);
            return "redirect:" + redirectUrl;
        }

        try {
            PhieuKhieuNai p = cskhService.phanLoaiTicket(maPhieu, form, MA_CSKH_MAC_DINH);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Đã cập nhật phân loại và mức độ ưu tiên cho Ticket [" + p.getMaCodePhieu() + "] thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi phân loại ticket: " + e.getMessage());
        }
        return "redirect:" + redirectUrl;
    }

    /**
     * Thêm ghi chú điều tra nội bộ
     */
    @PostMapping("/ghi-chu-noi-bo/{maPhieu}")
    public String themGhiChuNoiBo(
            @PathVariable("maPhieu") Long maPhieu,
            @Valid @ModelAttribute("formGhiChu") GhiChuNoiBoForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String loiChiTiet = bindingResult.getFieldErrors().stream()
                    .map(fe -> fe.getDefaultMessage())
                    .reduce((m1, m2) -> m1 + "; " + m2)
                    .orElse("Nội dung ghi chú điều tra nội bộ không hợp lệ.");
            redirectAttributes.addFlashAttribute("thongBaoLoi", loiChiTiet);
            return "redirect:/cskh/doi-chieu/" + maPhieu;
        }

        try {
            cskhService.themGhiChuNoiBo(maPhieu, form.getMaNhanVien(), form.getNoiDung());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã thêm ghi chú điều tra nội bộ thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi thêm ghi chú: " + e.getMessage());
        }
        return "redirect:/cskh/doi-chieu/" + maPhieu;
    }

    /**
     * Ra phán quyết tranh chấp (US-47)
     */
    @PostMapping("/phan-quyet/{maPhieu}")
    public String raPhanQuyet(
            @PathVariable("maPhieu") Long maPhieu,
            @Valid @ModelAttribute("formPhanQuyet") PhanQuyetTranhChapForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String loiChiTiet = bindingResult.getFieldErrors().stream()
                    .map(fe -> fe.getDefaultMessage())
                    .reduce((m1, m2) -> m1 + "; " + m2)
                    .orElse("Dữ liệu phán quyết không hợp lệ, vui lòng kiểm tra lại.");
            redirectAttributes.addFlashAttribute("thongBaoLoi", loiChiTiet);
            return "redirect:/cskh/doi-chieu/" + maPhieu;
        }

        try {
            form.setMaPhieu(maPhieu);
            LenhHoanTienBoiThuong lenh = phanQuyetTranhChapService.raPhanQuyet(form, MA_CSKH_MAC_DINH);
            String tb = "Đã ban hành phán quyết chính thức thành công cho Ticket ID: " + maPhieu + ". ";
            if (lenh != null) {
                tb += "Đã tự động khởi tạo Chứng từ bồi hoàn #" + lenh.getMaLenh() + " với số tiền " +
                        String.format("%,.0f", lenh.getSoTien()) + " VNĐ (" + lenh.getBenChiuPhiDisplay() + ").";
            } else {
                tb += "Đã bác bỏ khiếu nại của khách hàng và giải phóng tiền bán hàng cho Gian hàng.";
            }
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", tb);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi ban hành phán quyết: " + e.getMessage());
        }
        return "redirect:/cskh/doi-chieu/" + maPhieu;
    }

    /**
     * Quản lý danh sách phán quyết tranh chấp & lệnh hoàn tiền / bồi thường toàn sàn (US-47)
     */
    @GetMapping("/phan-quyet")
    public String danhSachPhanQuyet(
            @RequestParam(name = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(name = "benChiuPhi", required = false) String benChiuPhi,
            @RequestParam(name = "trangThai", required = false) String trangThai,
            @RequestParam(name = "tuNgay", required = false) String tuNgay,
            @RequestParam(name = "denNgay", required = false) String denNgay,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        ThongKePhanQuyetDTO thongKe = phanQuyetTranhChapService.layThongKePhanQuyet();
        Page<LenhHoanTienBoiThuong> pageLenh = phanQuyetTranhChapService.layDanhSachLenhHoanTien(
                tuKhoa, benChiuPhi, trangThai, tuNgay, denNgay, page, size
        );

        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageLenh", pageLenh);
        model.addAttribute("maCskhHienTai", MA_CSKH_MAC_DINH);

        // Giữ lại tham số tìm kiếm trên giao diện
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("benChiuPhi", benChiuPhi);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        model.addAttribute("size", size);

        return "cskh/danh-sach-phan-quyet";
    }
}
