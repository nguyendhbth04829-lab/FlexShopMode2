package com.example.demo.controller;

import com.example.demo.dto.CapNhatTrangThaiForm;
import com.example.demo.entity.TaiXeGiaoHang;
import com.example.demo.service.ShipperService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * US-34: Shipper bat/tat trang thai lam viec.
 * Giao dien mobile-first: /shipper/trang-thai
 */
@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @Autowired
    private ShipperService shipperService;

    @GetMapping({"", "/", "/trang-thai"})
    public String trangThai(Model model) {
        try {
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            model.addAttribute("shipper", tx);
            model.addAttribute("duocNhanDon", shipperService.duocNhanNhiemVu(tx));
            model.addAttribute("soCuocDangLam", shipperService.demCuocDangLam(tx.getMaTaiXe()));
            if (!model.containsAttribute("form")) {
                CapNhatTrangThaiForm form = new CapNhatTrangThaiForm();
                form.setDangTrucTuyen(!Boolean.TRUE.equals(tx.getDangTrucTuyen()));
                form.setViDoHienTai(tx.getViDoHienTai());
                form.setKinhDoHienTai(tx.getKinhDoHienTai());
                model.addAttribute("form", form);
            }
        } catch (IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
        }
        return "shipper/trang-thai";
    }

    @PostMapping("/trang-thai")
    public String doiTrangThai(
            @Valid @ModelAttribute("form") CapNhatTrangThaiForm form,
            BindingResult bindingResult,
            Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        TaiXeGiaoHang tx = shipperService.layShipperHienTai();
        if (bindingResult.hasErrors()) {
            model.addAttribute("shipper", tx);
            model.addAttribute("duocNhanDon", shipperService.duocNhanNhiemVu(tx));
            model.addAttribute("soCuocDangLam", shipperService.demCuocDangLam(tx.getMaTaiXe()));
            return "shipper/trang-thai";
        }
        try {
            shipperService.doiTrangThai(tx.getMaTaiXe(), form);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("shipper", tx);
            model.addAttribute("duocNhanDon", shipperService.duocNhanNhiemVu(tx));
            model.addAttribute("soCuocDangLam", shipperService.demCuocDangLam(tx.getMaTaiXe()));
            model.addAttribute("loiNghiepVu", ex.getMessage());
            return "shipper/trang-thai";
        }
        // Canh bao khi tat Online ma van con cuoc dang lam do
        if (Boolean.FALSE.equals(form.getDangTrucTuyen())) {
            long conLai = shipperService.demCuocDangLam(tx.getMaTaiXe());
            if (conLai > 0) {
                redirectAttributes.addFlashAttribute("canhBao",
                        "Bạn đã Offline nhưng vẫn còn " + conLai + " cuốc đang làm dở.");
            }
        }
        return "redirect:/shipper/trang-thai";
    }

    /** US-35: Danh sach cuoc cho nhan (mobile), 10 cuoc/trang. */
    @GetMapping("/don-cho-nhan")
    public String donChoNhan(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        try {
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            model.addAttribute("shipper", tx);
            model.addAttribute("duocNhanDon", shipperService.duocNhanNhiemVu(tx));
            org.springframework.data.domain.Page<com.example.demo.entity.DonHangShop> trang =
                    shipperService.layDonChoNhan(page, 10);
            model.addAttribute("trangDon", trang);
            model.addAttribute("listDon", trang.getContent());
            model.addAttribute("trangHienTai", page);
            model.addAttribute("tongTrang", trang.getTotalPages());
            model.addAttribute("cuocCuaToi", shipperService.layCuocCuaToi(tx.getMaTaiXe()));
        } catch (IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
        }
        return "shipper/don-cho-nhan";
    }

    @PostMapping("/nhan-don/{maDon}")
    public String nhanDon(@PathVariable("maDon") Long maDon, Model model) {
        try {
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            shipperService.nhanCuoc(tx.getMaTaiXe(), maDon);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
            try {
                TaiXeGiaoHang tx = shipperService.layShipperHienTai();
                model.addAttribute("shipper", tx);
                model.addAttribute("duocNhanDon", shipperService.duocNhanNhiemVu(tx));
                model.addAttribute("listDon", shipperService.layDonChoNhan());
                model.addAttribute("cuocCuaToi", shipperService.layCuocCuaToi(tx.getMaTaiXe()));
            } catch (IllegalStateException ignored) {
            }
            return "shipper/don-cho-nhan";
        }
        return "redirect:/shipper/don-cho-nhan?nhan=ok";
    }

    /** US-36: Cuoc cua toi + xac nhan da lay hang (mobile). */
    @GetMapping("/cuoc-cua-toi")
    public String cuocCuaToi(Model model) {
        try {
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            model.addAttribute("shipper", tx);
            model.addAttribute("cuocCuaToi", shipperService.layCuocCuaToi(tx.getMaTaiXe()));
        } catch (IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
        }
        return "shipper/cuoc-cua-toi";
    }

    @PostMapping("/lay-hang/{maNhiemVu}")
    public String layHang(@PathVariable("maNhiemVu") Long maNhiemVu, Model model) {
        try {
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            shipperService.xacNhanLayHang(tx.getMaTaiXe(), maNhiemVu);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
            try {
                TaiXeGiaoHang tx = shipperService.layShipperHienTai();
                model.addAttribute("shipper", tx);
                model.addAttribute("cuocCuaToi", shipperService.layCuocCuaToi(tx.getMaTaiXe()));
            } catch (IllegalStateException ignored) {
            }
            return "shipper/cuoc-cua-toi";
        }
        return "redirect:/shipper/cuoc-cua-toi?lay=ok";
    }
}
