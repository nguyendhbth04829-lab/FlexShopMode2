package com.example.demo.controller;

import com.example.demo.dto.GianLanThongKeDTO;
import com.example.demo.dto.TaoCanhBaoRequestDTO;
import com.example.demo.dto.XuLyCanhBaoRequestDTO;
import com.example.demo.entity.CanhBaoGianLan;
import com.example.demo.service.CanhBaoGianLanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & AN NINH SÀN (DEV 5 - MINH)
 * USER STORY: US-68 - Controller Trung Tâm Cảnh Báo & Xử Lý Gian Lận
 * =====================================================================
 */
@Slf4j
@Controller
@RequestMapping("/canh-bao-gian-lan")
@RequiredArgsConstructor
public class CanhBaoGianLanController {

    private final CanhBaoGianLanService canhBaoGianLanService;

    /**
     * US-68: Bảng Điều Khiển An Ninh & Danh Sách Cảnh Báo
     */
    @GetMapping("/danh-sach")
    public String danhSach(
            @RequestParam(value = "trangThai", defaultValue = "ALL") String trangThai,
            @RequestParam(value = "loaiDoiTuong", defaultValue = "ALL") String loaiDoiTuong,
            Model model
    ) {
        GianLanThongKeDTO thongKe = canhBaoGianLanService.getThongKe();
        List<CanhBaoGianLan> danhSachCanhBao = canhBaoGianLanService.getDanhSach(trangThai, loaiDoiTuong);

        model.addAttribute("thongKe", thongKe);
        model.addAttribute("danhSachCanhBao", danhSachCanhBao);
        model.addAttribute("trangThaiHienTai", trangThai);
        model.addAttribute("loaiDoiTuongHienTai", loaiDoiTuong);

        return "canh-bao-gian-lan/danh-sach";
    }

    /**
     * US-68: Chi Tiết Cảnh Báo & Xem Xét Hồ Sơ
     */
    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(
            @PathVariable("id") Long id,
            Model model
    ) {
        CanhBaoGianLan canhBao = canhBaoGianLanService.getChiTiet(id);
        XuLyCanhBaoRequestDTO xuLyDTO = new XuLyCanhBaoRequestDTO();
        xuLyDTO.setMaCanhBao(id);
        xuLyDTO.setHanhDong("KHOA_DOI_TUONG");

        model.addAttribute("canhBao", canhBao);
        model.addAttribute("xuLyDTO", xuLyDTO);

        return "canh-bao-gian-lan/chi-tiet-canh-bao";
    }

    /**
     * US-68: Thực Thi Phán Quyết Xử Lý Cảnh Báo (Khóa / Bỏ qua)
     */
    @PostMapping("/xu-ly")
    public String xuLy(
            @ModelAttribute("xuLyDTO") XuLyCanhBaoRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            CanhBaoGianLan cb = canhBaoGianLanService.xuLyCanhBao(dto);
            if ("DA_XU_LY".equalsIgnoreCase(cb.getTrangThai())) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Đã áp dụng chế tài xử phạt thành công cho cảnh báo #" + cb.getMaCanhBao() + 
                        "! Đối tượng liên quan đã bị khóa/đóng băng.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã xác nhận an toàn (False Positive) cho cảnh báo #" + cb.getMaCanhBao() + ".");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/canh-bao-gian-lan/danh-sach";
    }

    /**
     * US-68: Màn Hình Kích Hoạt Quét Gian Lận Tự Động
     */
    @GetMapping("/mo-phong-quet")
    public String trangMoPhong(Model model) {
        GianLanThongKeDTO thongKe = canhBaoGianLanService.getThongKe();
        TaoCanhBaoRequestDTO taoDTO = new TaoCanhBaoRequestDTO();
        taoDTO.setLoaiDoiTuong("RUT_TIEN");
        taoDTO.setMaDoiTuong(1L);
        taoDTO.setDiemRuiRo(85);
        taoDTO.setLyDoCanhBao("Phát hiện địa chỉ IP đăng nhập rút tiền bất thường từ mạng ẩn danh VPN/Tor.");

        model.addAttribute("thongKe", thongKe);
        model.addAttribute("taoDTO", taoDTO);

        return "canh-bao-gian-lan/mo-phong-quet-gian-lan";
    }

    /**
     * US-68: Kích Hoạt Động Cơ Quét Gian Lận Toàn Hệ Thống
     */
    @PostMapping("/kich-hoat-quet")
    public String kichHoatQuet(RedirectAttributes redirectAttributes) {
        try {
            int soCaMoi = canhBaoGianLanService.quetGianLanToanHeThong();
            if (soCaMoi > 0) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Hệ thống Anti-Fraud đã quét xong và phát hiện [" + soCaMoi + 
                        "] giao dịch nghi vấn có dấu hiệu gian lận tài chính!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Quét an ninh hoàn tất! Không phát hiện thêm hành vi gian lận mới nào.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/canh-bao-gian-lan/danh-sach";
    }

    /**
     * US-68: Tạo Cảnh Báo Gian Lận Thủ Công (Test case)
     */
    @PostMapping("/tao-thu-cong")
    public String taoThuCong(
            @ModelAttribute("taoDTO") TaoCanhBaoRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            CanhBaoGianLan cb = canhBaoGianLanService.taoCanhBaoThuCong(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã tạo thủ công cảnh báo #" + cb.getMaCanhBao() + " với điểm rủi ro: " + cb.getDiemRuiRo() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/canh-bao-gian-lan/danh-sach";
    }
}
