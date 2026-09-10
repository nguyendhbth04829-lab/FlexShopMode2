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
            long dangLam = shipperService.demCuocDangLam(tx.getMaTaiXe());
            model.addAttribute("soCuocDangLam", dangLam);
            // Canh bao thuong truc: Offline ma van con cuoc do -> refresh van con
            if (!model.containsAttribute("canhBao")
                    && !Boolean.TRUE.equals(tx.getDangTrucTuyen()) && dangLam > 0) {
                model.addAttribute("canhBao",
                        "Bạn đang Offline nhưng vẫn còn " + dangLam + " cuốc đang làm dở.");
            }
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

    /** US-35: Danh sach cuoc cho nhan (mobile), 2 tab + phan trang + sap xep. */
    @GetMapping("/don-cho-nhan")
    public String donChoNhan(
            Model model,
            @RequestParam(value = "tab", defaultValue = "cho-nhan") String tab,
            @RequestParam(value = "sort", defaultValue = "moi-nhat") String sort,
            @RequestParam(value = "pageCN", defaultValue = "0") int pageCN,
            @RequestParam(value = "pageDN", defaultValue = "0") int pageDN) {
        try {
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            napModelDonChoNhan(model, tx, tab, sort, pageCN, pageDN);
        } catch (IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
        }
        return "shipper/don-cho-nhan";
    }

    private void napModelDonChoNhan(Model model, TaiXeGiaoHang tx,
                                    String tab, String sort, int pageCN, int pageDN) {
        boolean cuNhat = "cu-nhat".equals(sort);
        model.addAttribute("shipper", tx);
        model.addAttribute("duocNhanDon", shipperService.duocNhanNhiemVu(tx));
        model.addAttribute("tab", tab);
        model.addAttribute("sort", sort);
        org.springframework.data.domain.Page<com.example.demo.entity.DonHangShop> trangChoNhan =
                shipperService.layDonChoNhan(pageCN, 10, cuNhat);
        model.addAttribute("trangChoNhan", trangChoNhan);
        model.addAttribute("listDon", trangChoNhan.getContent());
        model.addAttribute("pageCN", pageCN);
        model.addAttribute("tongTrangCN", trangChoNhan.getTotalPages());
        org.springframework.data.domain.Page<com.example.demo.entity.NhiemVuGiaoHang> trangDaNhan =
                shipperService.layCuocCuaToi(tx.getMaTaiXe(), pageDN, 10, cuNhat);
        model.addAttribute("trangDaNhan", trangDaNhan);
        model.addAttribute("cuocCuaToi", trangDaNhan.getContent());
        model.addAttribute("pageDN", pageDN);
        model.addAttribute("tongTrangDN", trangDaNhan.getTotalPages());
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
                napModelDonChoNhan(model, tx, "cho-nhan", "moi-nhat", 0, 0);
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
            java.util.List<com.example.demo.entity.NhiemVuGiaoHang> cuoc =
                    shipperService.layCuocCuaToi(tx.getMaTaiXe());
            model.addAttribute("cuocCuaToi", cuoc);
            // US-40: map maDon -> yeu cau chuyen hoan (neu co)
            java.util.Map<Long, com.example.demo.entity.YeuCauChuyenHoan> mapChuyenHoan =
                    new java.util.HashMap<>();
            for (com.example.demo.entity.NhiemVuGiaoHang n : cuoc) {
                if (n.getDonHangShop() != null) {
                    shipperService.layChuyenHoan(n.getDonHangShop().getMaDonHangShop())
                            .ifPresent(yc -> mapChuyenHoan.put(n.getMaNhiemVu(), yc));
                }
            }
            model.addAttribute("mapChuyenHoan", mapChuyenHoan);
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

    /** US-37: Form xac nhan giao thanh cong POD (mobile). */
    @GetMapping("/pod/{maNhiemVu}")
    public String hienThiPod(@PathVariable("maNhiemVu") Long maNhiemVu, Model model) {
        try {
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            com.example.demo.entity.NhiemVuGiaoHang nv =
                    shipperService.layCuocCuaToi(tx.getMaTaiXe()).stream()
                            .filter(n -> n.getMaNhiemVu().equals(maNhiemVu)).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cuốc " + maNhiemVu));
            if (!"DANG_GIAO".equals(nv.getTrangThai())) {
                model.addAttribute("loiNghiepVu", "Cuốc đang " + nv.getTrangThaiDisplay() + ", không thể POD.");
            }
            model.addAttribute("nhiemVu", nv);
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", new com.example.demo.dto.XacNhanPodForm());
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
        }
        return "shipper/pod";
    }

    @PostMapping("/pod/{maNhiemVu}")
    public String xuLyPod(
            @PathVariable("maNhiemVu") Long maNhiemVu,
            @Valid @ModelAttribute("form") com.example.demo.dto.XacNhanPodForm form,
            BindingResult bindingResult,
            @RequestParam(value = "anhPod", required = false) org.springframework.web.multipart.MultipartFile anhPod,
            Model model) {
        TaiXeGiaoHang tx = shipperService.layShipperHienTai();
        com.example.demo.entity.NhiemVuGiaoHang nv = shipperService.layCuocCuaToi(tx.getMaTaiXe()).stream()
                .filter(n -> n.getMaNhiemVu().equals(maNhiemVu)).findFirst().orElse(null);
        model.addAttribute("nhiemVu", nv);
        if (bindingResult.hasErrors()) {
            return "shipper/pod";
        }
        if (anhPod == null || anhPod.isEmpty()) {
            model.addAttribute("loiNghiepVu", "Bắt buộc chụp/upload ảnh bằng chứng giao hàng (POD)!");
            return "shipper/pod";
        }
        try {
            shipperService.xacNhanGiaoThanhCong(tx.getMaTaiXe(), maNhiemVu, anhPod,
                    form.getViDoGiaoHang(), form.getKinhDoGiaoHang());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
            return "shipper/pod";
        }
        return "redirect:/shipper/cuoc-cua-toi?pod=ok";
    }

    /** US-38: Form bao giao that bai + hen giao lai (mobile). */
    @GetMapping("/that-bai/{maNhiemVu}")
    public String hienThiThatBai(@PathVariable("maNhiemVu") Long maNhiemVu, Model model) {
        try {
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            com.example.demo.entity.NhiemVuGiaoHang nv =
                    shipperService.layCuocCuaToi(tx.getMaTaiXe()).stream()
                            .filter(n -> n.getMaNhiemVu().equals(maNhiemVu)).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cuốc " + maNhiemVu));
            if (!"DANG_GIAO".equals(nv.getTrangThai())) {
                model.addAttribute("loiNghiepVu", "Cuốc đang " + nv.getTrangThaiDisplay() + ", không thể báo thất bại.");
            }
            model.addAttribute("nhiemVu", nv);
            if (!model.containsAttribute("form")) {
                model.addAttribute("form", new com.example.demo.dto.BaoThatBaiForm());
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
        }
        return "shipper/that-bai";
    }

    @PostMapping("/that-bai/{maNhiemVu}")
    public String xuLyThatBai(
            @PathVariable("maNhiemVu") Long maNhiemVu,
            @Valid @ModelAttribute("form") com.example.demo.dto.BaoThatBaiForm form,
            BindingResult bindingResult,
            Model model) {
        TaiXeGiaoHang tx = shipperService.layShipperHienTai();
        com.example.demo.entity.NhiemVuGiaoHang nv = shipperService.layCuocCuaToi(tx.getMaTaiXe()).stream()
                .filter(n -> n.getMaNhiemVu().equals(maNhiemVu)).findFirst().orElse(null);
        model.addAttribute("nhiemVu", nv);
        if (bindingResult.hasErrors()) {
            return "shipper/that-bai";
        }
        try {
            shipperService.baoThatBai(tx.getMaTaiXe(), maNhiemVu, form);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
            return "shipper/that-bai";
        }
        return "redirect:/shipper/cuoc-cua-toi?thatbai=ok";
    }

    /** US-39: Dashboard lich su + thong ke COD theo ngay (mobile). */
    @GetMapping("/cod")
    public String dashboardCod(
            Model model,
            @RequestParam(value = "ngay", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate ngay) {
        try {
            if (ngay == null) ngay = java.time.LocalDate.now();
            TaiXeGiaoHang tx = shipperService.layShipperHienTai();
            model.addAttribute("shipper", tx);
            model.addAttribute("ngayXem",
                    ngay.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            model.addAttribute("ngayValue", ngay.toString());
            model.addAttribute("thongKe", shipperService.thongKeShipper(tx.getMaTaiXe(), ngay));
            model.addAttribute("lichSu", shipperService.layCuocTrongNgay(tx.getMaTaiXe(), ngay));
        } catch (IllegalStateException ex) {
            model.addAttribute("loiNghiepVu", ex.getMessage());
        }
        return "shipper/cod";
    }
}
