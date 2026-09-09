package com.example.demo.controller;

import com.example.demo.dto.KetQuaTinhPhiDTO;
import com.example.demo.dto.TinhPhiVanChuyenForm;
import com.example.demo.repository.DoiTacVanChuyenRepository;
import com.example.demo.service.PhiVanChuyenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * US-32 (lam lai): Form test tinh phi van chuyen theo khoi luong quy doi.
 * Giao dien test tong xanh duong: /logistics/phi-van-chuyen
 */
@Controller
@RequestMapping("/logistics/phi-van-chuyen")
public class PhiVanChuyenController {

    @Autowired
    private PhiVanChuyenService phiVanChuyenService;

    @Autowired
    private DoiTacVanChuyenRepository doiTacVanChuyenRepository;

    @GetMapping
    public String hienThiForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new TinhPhiVanChuyenForm());
        }
        model.addAttribute("listBangGia", phiVanChuyenService.layTatCaBangGia());
        model.addAttribute("listDoiTac", doiTacVanChuyenRepository.findAll());
        return "logistics/phi-van-chuyen";
    }

    @PostMapping("/tinh")
    public String tinhPhi(
            @Valid @ModelAttribute("form") TinhPhiVanChuyenForm form,
            BindingResult bindingResult,
            Model model) {
        model.addAttribute("listBangGia", phiVanChuchenServiceSafe());
        model.addAttribute("listDoiTac", doiTacVanChuyenRepository.findAll());
        if (bindingResult.hasErrors()) {
            return "logistics/phi-van-chuyen";
        }
        try {
            KetQuaTinhPhiDTO ketQua = phiVanChuyenService.tinhPhi(form);
            model.addAttribute("ketQua", ketQua);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
        }
        return "logistics/phi-van-chuyen";
    }

    private java.util.List<com.example.demo.entity.BangGiaVanChuyen> phiVanChuchenServiceSafe() {
        try {
            return phiVanChuyenService.layTatCaBangGia();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }
}
