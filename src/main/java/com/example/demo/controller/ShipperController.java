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
            Model model) {
        TaiXeGiaoHang tx = shipperService.layShipperHienTai();
        if (bindingResult.hasErrors()) {
            model.addAttribute("shipper", tx);
            model.addAttribute("duocNhanDon", shipperService.duocNhanNhiemVu(tx));
            return "shipper/trang-thai";
        }
        try {
            shipperService.doiTrangThai(tx.getMaTaiXe(), form);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("shipper", tx);
            model.addAttribute("duocNhanDon", shipperService.duocNhanNhiemVu(tx));
            model.addAttribute("loiNghiepVu", ex.getMessage());
            return "shipper/trang-thai";
        }
        return "redirect:/shipper/trang-thai";
    }
}
