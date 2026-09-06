package com.example.demo.controller;

import com.example.demo.dto.SuDungXuForm;
import com.example.demo.dto.ThongKeViXuDTO;
import com.example.demo.entity.DonHangTong;
import com.example.demo.entity.LichSuGiaoDichXu;
import com.example.demo.entity.NguoiDung;
import com.example.demo.repository.DonHangTongRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.ViXuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller phục vụ Khách hàng quản lý Ví Xu, Điểm danh nhận thưởng & Dùng xu trừ tiền (US-55 - Coin Reward System)
 */
@Controller
public class ViXuController {

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    @Autowired
    private ViXuService viXuService;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    private NguoiDung layKhachHangHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().contains("khach"))
                        .findFirst()
                        .orElse(null));
    }

    /**
     * 1. Màn hình Quản Lý Ví Xu & Lịch Sử Biến Động Xu
     */
    @GetMapping("/khach-hang/vi-xu")
    public String trangViXu(
            @RequestParam(value = "loaiGiaoDich", defaultValue = "TAT_CA") String loaiGiaoDich,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        if (khachHang == null) {
            model.addAttribute("thongBaoLoi", "Không tìm thấy thông tin tài khoản khách hàng!");
            return "khach-hang/vi-xu";
        }

        ThongKeViXuDTO thongKe = viXuService.layThongKeViXu(khachHang.getMaNguoiDung());
        Page<LichSuGiaoDichXu> pageLichSu = viXuService.layLichSuGiaoDichPhanTrang(
                khachHang.getMaNguoiDung(), loaiGiaoDich, page, size
        );

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("pageLichSu", pageLichSu);
        model.addAttribute("danhSachGiaoDich", pageLichSu.getContent());
        model.addAttribute("loaiHienTai", loaiGiaoDich);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageLichSu.getTotalPages());
        model.addAttribute("totalElements", pageLichSu.getTotalElements());

        return "khach-hang/vi-xu";
    }

    /**
     * 2. Điểm danh nhận thưởng xu hàng ngày (+1.000 Xu)
     */
    @PostMapping("/khach-hang/vi-xu/diem-danh")
    public String diemDanhNhanXu(RedirectAttributes redirectAttributes) {
        NguoiDung khachHang = layKhachHangHienTai();
        if (khachHang == null) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Không tìm thấy tài khoản người dùng!");
            return "redirect:/khach-hang/vi-xu";
        }

        try {
            Long soXuNhan = viXuService.diemDanhNhanXu(khachHang.getMaNguoiDung());
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Điểm danh thành công! Bạn vừa nhận được +" + soXuNhan + " FlexShop Xu.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/khach-hang/vi-xu";
    }

    /**
     * 3. Áp dụng Xu vào Đơn hàng để giảm trừ trực tiếp (Checkout)
     */
    @PostMapping("/khach-hang/thanh-toan/ap-dung-xu")
    public String apDungXu(
            @Valid @ModelAttribute SuDungXuForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung khachHang = layKhachHangHienTai();

        if (bindingResult.hasErrors()) {
            String err = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("thongBaoLoi", err);
            return "redirect:/khach-hang/thanh-toan/voucher";
        }

        try {
            DonHangTong donHang = donHangTongRepository.findByMaCodeDonTong(form.getMaCodeDonTong())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + form.getMaCodeDonTong()));

            Long soXuDung = form.getSoXuMuonDung();
            if (Boolean.TRUE.equals(form.getDungToiDa())) {
                soXuDung = viXuService.tinhSoXuToiDaChoPhep(khachHang.getMaNguoiDung(), donHang);
            }

            viXuService.apDungXuVaoDonHang(khachHang.getMaNguoiDung(), form.getMaCodeDonTong(), soXuDung);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Đã áp dụng thành công " + soXuDung + " Xu (Giảm " + soXuDung + " đ) vào đơn hàng!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/khach-hang/thanh-toan/voucher";
    }

    /**
     * 4. Hủy áp dụng Xu trên đơn hàng
     */
    @PostMapping("/khach-hang/thanh-toan/huy-xu")
    public String huyApDungXu(
            @RequestParam("maCodeDonTong") String maCodeDonTong,
            RedirectAttributes redirectAttributes
    ) {
        NguoiDung khachHang = layKhachHangHienTai();

        try {
            viXuService.huyApDungXu(khachHang.getMaNguoiDung(), maCodeDonTong);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã hủy áp dụng xu trên đơn hàng và hoàn lại số dư ví.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", ex.getMessage());
        }

        return "redirect:/khach-hang/thanh-toan/voucher";
    }

    /**
     * REST API lấy số dư và thông tin ví xu hiện tại
     */
    @GetMapping("/api/vi-xu/thong-tin")
    @ResponseBody
    public ResponseEntity<ThongKeViXuDTO> layThongTinViXuAjax() {
        NguoiDung khachHang = layKhachHangHienTai();
        if (khachHang == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(viXuService.layThongKeViXu(khachHang.getMaNguoiDung()));
    }
}
