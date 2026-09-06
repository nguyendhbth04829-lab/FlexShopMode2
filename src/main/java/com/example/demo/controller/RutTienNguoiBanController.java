package com.example.demo.controller;

import com.example.demo.dto.RutTienNguoiBanRequestDTO;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.ViNguoiBan;
import com.example.demo.entity.YeuCauRutTien;
import com.example.demo.service.KyQuyService;
import com.example.demo.service.RutTienNguoiBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH, VÍ ESCROW & RÚT TIỀN (DEV 5 - MINH)
 * USER STORY: US-44 - Controller Rút Tiền Người Bán Về Ngân Hàng
 * =====================================================================
 * Tên Controller: RutTienNguoiBanController (Đặt tên tiếng Việt dễ hiểu theo yêu cầu)
 * Chức năng:
 *   1. Xem danh sách yêu cầu rút tiền (phân trang, bộ lọc tìm kiếm nâng cao).
 *   2. Thống kê số liệu rút tiền trực tiếp từ Database.
 *   3. Màn hình tạo phiếu rút tiền mới với validate số dư khả dụng tức thì.
 *   4. Quản trị viên duyệt chi tiền cho Shop hoặc từ chối kèm hoàn tiền tự động.
 * =====================================================================
 */
@Controller
@RequestMapping("/rut-tien-nguoi-ban")
public class RutTienNguoiBanController {

    @Autowired
    private RutTienNguoiBanService rutTienNguoiBanService;

    @Autowired
    private KyQuyService kyQuyService;

    /**
     * [US-44] Màn hình danh sách yêu cầu rút tiền người bán
     */
    @GetMapping("/danh-sach")
    public String danhSach(
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "maGianHang", required = false) Long maGianHang,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "6") Integer size
    ) {
        Page<YeuCauRutTien> pageYeuCau = rutTienNguoiBanService.getDanhSachPhanTrang(
                keyword, trangThai, maGianHang, page, size
        );
        Map<String, Object> thongKe = rutTienNguoiBanService.getThongKeRutTienDongBo();
        List<GianHang> listGianHang = kyQuyService.getDanhSachGianHang();

        model.addAttribute("listYeuCau", pageYeuCau.getContent());
        model.addAttribute("pageYeuCau", pageYeuCau);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", pageYeuCau.getTotalPages());
        model.addAttribute("totalElements", pageYeuCau.getTotalElements());

        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("maGianHang", maGianHang);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("listGianHang", listGianHang);

        return "rut-tien-nguoi-ban/danh-sach";
    }

    /**
     * [US-44] Màn hình form tạo yêu cầu rút tiền người bán
     */
    @GetMapping("/tao-yeu-cau")
    public String hienThiFormTao(
            Model model,
            @RequestParam(value = "maGianHang", required = false) Long maGianHang
    ) {
        List<GianHang> listGianHang = kyQuyService.getDanhSachGianHang();
        model.addAttribute("listGianHang", listGianHang);
        model.addAttribute("hanMucToiThieu", RutTienNguoiBanService.HAN_MUC_RUT_TOI_THIEU);

        RutTienNguoiBanRequestDTO dto = new RutTienNguoiBanRequestDTO();
        if (maGianHang != null) {
            dto.setMaGianHang(maGianHang);
            try {
                ViNguoiBan vi = kyQuyService.getViNguoiBanByGianHangId(maGianHang);
                model.addAttribute("vi", vi);
            } catch (Exception ignored) {}
        } else if (!listGianHang.isEmpty()) {
            Long defaultShopId = listGianHang.get(0).getMaGianHang();
            dto.setMaGianHang(defaultShopId);
            try {
                ViNguoiBan vi = kyQuyService.getViNguoiBanByGianHangId(defaultShopId);
                model.addAttribute("vi", vi);
            } catch (Exception ignored) {}
        }

        model.addAttribute("dto", dto);
        return "rut-tien-nguoi-ban/tao-yeu-cau";
    }

    /**
     * [US-44] Xử lý gửi yêu cầu rút tiền từ form
     */
    @PostMapping("/tao-yeu-cau")
    public String xuLyTaoYeuCau(
            @ModelAttribute("dto") RutTienNguoiBanRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            YeuCauRutTien saved = rutTienNguoiBanService.taoYeuCauRutTien(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã tạo yêu cầu rút tiền thành công! Mã yêu cầu: #" + saved.getMaYeuCau() +
                    ". Số tiền " + String.format("%,.0f VNĐ", saved.getSoTienRut()) +
                    " đã được trừ khỏi số dư khả dụng và đang chờ Admin duyệt chuyển khoản.");
            return "redirect:/rut-tien-nguoi-ban/danh-sach";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/rut-tien-nguoi-ban/tao-yeu-cau" + (dto.getMaGianHang() != null ? "?maGianHang=" + dto.getMaGianHang() : "");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo yêu cầu: " + e.getMessage());
            return "redirect:/rut-tien-nguoi-ban/tao-yeu-cau";
        }
    }

    /**
     * [US-44] Xem chi tiết yêu cầu rút tiền
     */
    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(Model model, @PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            YeuCauRutTien yeuCau = rutTienNguoiBanService.getChiTiet(id);
            ViNguoiBan vi = kyQuyService.getViNguoiBanByGianHangId(yeuCau.getGianHang().getMaGianHang());

            model.addAttribute("yeuCau", yeuCau);
            model.addAttribute("vi", vi);
            return "rut-tien-nguoi-ban/chi-tiet";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/rut-tien-nguoi-ban/danh-sach";
        }
    }

    /**
     * [US-44] Admin phê duyệt yêu cầu rút tiền
     */
    @PostMapping("/duyet/{id}")
    public String duyetYeuCau(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            YeuCauRutTien yeuCau = rutTienNguoiBanService.duyetYeuCauRutTien(id, 1L); // Giả lập Admin ID = 1
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã phê duyệt thành công yêu cầu rút tiền #" + id + " (" +
                    String.format("%,.0f VNĐ", yeuCau.getSoTienRut()) +
                    ") cho gian hàng [" + yeuCau.getGianHang().getTenGianHang() + "]!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi phê duyệt: " + e.getMessage());
        }
        return "redirect:/rut-tien-nguoi-ban/danh-sach";
    }

    /**
     * [US-44] Admin từ chối yêu cầu rút tiền kèm lý do -> Tự động hoàn tiền vào ví shop
     */
    @PostMapping("/tu-choi/{id}")
    public String tuChoiYeuCau(
            @PathVariable("id") Long id,
            @RequestParam("lyDoTuChoi") String lyDoTuChoi,
            RedirectAttributes redirectAttributes
    ) {
        try {
            YeuCauRutTien yeuCau = rutTienNguoiBanService.tuChoiYeuCauRutTien(id, 1L, lyDoTuChoi);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã từ chối yêu cầu rút tiền #" + id + ". Số tiền " +
                    String.format("%,.0f VNĐ", yeuCau.getSoTienRut()) +
                    " đã được tự động HOÀN TRẢ lại vào số dư khả dụng của gian hàng [" +
                    yeuCau.getGianHang().getTenGianHang() + "]!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi từ chối: " + e.getMessage());
        }
        return "redirect:/rut-tien-nguoi-ban/danh-sach";
    }
}
