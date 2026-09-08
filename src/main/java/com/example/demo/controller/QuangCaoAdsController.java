package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.ChienDichQuangCao;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.service.QuangCaoAdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: QUẢNG CÁO & ĐẤU THẦU TỪ KHÓA CPC (DEV 5 - MINH)
 * USER STORY: US-64 - Controller Shopee Ads Đấu Thầu Từ Khóa CPC
 * =====================================================================
 */
@Controller
@RequestMapping("/quang-cao-ads")
public class QuangCaoAdsController {

    @Autowired
    private QuangCaoAdsService quangCaoAdsService;

    @Autowired
    private GianHangRepository gianHangRepository;

    /**
     * US-64: Danh Sách Chiến Dịch Quảng Cáo & Dashboard Hiệu Quả Ads của Shop
     */
    @GetMapping("/danh-sach")
    public String danhSach(
            Model model,
            @RequestParam(value = "maGianHang", defaultValue = "1") Long maGianHang,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "page", defaultValue = "0") Integer page
    ) {
        int pageSize = 6;
        Page<ChienDichQuangCao> pageChienDich = quangCaoAdsService.getDanhSachChienDichPhanTrang(keyword, trangThai, maGianHang, page, pageSize);
        AdsThongKeDTO thongKe = quangCaoAdsService.layThongKeAdsCuaShop(maGianHang);
        List<GianHang> listGianHang = gianHangRepository.findAll();

        model.addAttribute("listChienDich", pageChienDich.getContent());
        model.addAttribute("pageChienDich", pageChienDich);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageChienDich.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("maGianHang", maGianHang);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("listGianHang", listGianHang);

        return "quang-cao-ads/danh-sach-chien-dich";
    }

    /**
     * US-64: Màn hình Tạo Mới Chiến Dịch Quảng Cáo
     */
    @GetMapping("/tao-moi")
    public String taoMoi(
            Model model,
            @RequestParam(value = "maGianHang", defaultValue = "1") Long maGianHang
    ) {
        GianHang gianHang = gianHangRepository.findById(maGianHang).orElse(null);
        List<SanPham> dsSanPham = quangCaoAdsService.layDanhSachSanPhamCuaShop(maGianHang);
        List<GianHang> listGianHang = gianHangRepository.findAll();

        TaoChienDichAdsRequestDTO taoDTO = new TaoChienDichAdsRequestDTO();
        taoDTO.setMaGianHang(maGianHang);
        taoDTO.setNganSachNgay(new BigDecimal("100000.00"));
        taoDTO.setGiaThauMacDinh(new BigDecimal("1000.00"));

        model.addAttribute("gianHang", gianHang);
        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("listGianHang", listGianHang);
        model.addAttribute("taoDTO", taoDTO);

        return "quang-cao-ads/tao-chien-dich";
    }

    /**
     * US-64: Xử lý Tạo Mới Chiến Dịch
     */
    @PostMapping("/tao-moi")
    public String xuLyTaoMoi(
            @ModelAttribute("taoDTO") TaoChienDichAdsRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ChienDichQuangCao saved = quangCaoAdsService.taoMoiChienDich(dto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Khởi tạo thành công chiến dịch quảng cáo [" + saved.getTenChienDich() + "]!");
            return "redirect:/quang-cao-ads/chi-tiet/" + saved.getMaChienDich();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/quang-cao-ads/tao-moi?maGianHang=" + dto.getMaGianHang();
        }
    }

    /**
     * US-64: Chi Tiết Chiến Dịch & Quản Lý Danh Sách Từ Khóa Đấu Thầu
     */
    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(
            @PathVariable("id") Long id,
            Model model
    ) {
        ChienDichQuangCao chienDich = quangCaoAdsService.getChiTietChienDich(id);
        ThemTuKhoaAdsRequestDTO themTuKhoaDTO = new ThemTuKhoaAdsRequestDTO();
        themTuKhoaDTO.setMaChienDich(id);
        themTuKhoaDTO.setGiaThauMoiClickCpc(new BigDecimal("1000.00"));

        model.addAttribute("chienDich", chienDich);
        model.addAttribute("themTuKhoaDTO", themTuKhoaDTO);

        return "quang-cao-ads/chi-tiet-chien-dich";
    }

    /**
     * US-64: Thêm Từ Khóa Đấu Thầu Mới Vào Chiến Dịch
     */
    @PostMapping("/them-tu-khoa")
    public String themTuKhoa(
            @ModelAttribute("themTuKhoaDTO") ThemTuKhoaAdsRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            quangCaoAdsService.themTuKhoaVaoChienDich(dto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã thêm từ khóa đấu thầu [" + dto.getTuKhoa() + "] với giá CPC " + 
                    String.format("%,.0f", dto.getGiaThauMoiClickCpc()) + " VNĐ!");
            return "redirect:/quang-cao-ads/chi-tiet/" + dto.getMaChienDich();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/quang-cao-ads/chi-tiet/" + dto.getMaChienDich();
        }
    }

    /**
     * US-64: Đổi Trạng Thái Chiến Dịch (Bật / Tạm dừng)
     */
    @PostMapping("/doi-trang-thai")
    public String doiTrangThai(
            @RequestParam("maChienDich") Long maChienDich,
            @RequestParam("trangThaiMoi") String trangThaiMoi,
            @RequestParam(value = "redirectUrl", defaultValue = "danh-sach") String redirectUrl,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ChienDichQuangCao cd = quangCaoAdsService.thayDoiTrangThaiChienDich(maChienDich, trangThaiMoi);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Chiến dịch [" + cd.getTenChienDich() + "] đã chuyển sang: " + cd.getTenTrangThaiTiengViet());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if ("chi-tiet".equalsIgnoreCase(redirectUrl)) {
            return "redirect:/quang-cao-ads/chi-tiet/" + maChienDich;
        }
        return "redirect:/quang-cao-ads/danh-sach";
    }

    /**
     * US-64: Bật / Tắt trạng thái kích hoạt của từ khóa
     */
    @PostMapping("/bat-tat-tu-khoa")
    public String batTatTuKhoa(
            @RequestParam("maTuKhoa") Long maTuKhoa,
            @RequestParam("maChienDich") Long maChienDich,
            @RequestParam("dangKichHoat") boolean dangKichHoat,
            RedirectAttributes redirectAttributes
    ) {
        try {
            quangCaoAdsService.batTatTuKhoa(maTuKhoa, dangKichHoat);
            redirectAttributes.addFlashAttribute("successMessage", 
                    (dangKichHoat ? "Đã bật kích hoạt" : "Đã tạm dừng") + " từ khóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/quang-cao-ads/chi-tiet/" + maChienDich;
    }

    /**
     * US-64: Cập nhật giá thầu CPC của từ khóa
     */
    @PostMapping("/cap-nhat-cpc")
    public String capNhatCpc(
            @RequestParam("maTuKhoa") Long maTuKhoa,
            @RequestParam("maChienDich") Long maChienDich,
            @RequestParam("giaThauMoi") BigDecimal giaThauMoi,
            RedirectAttributes redirectAttributes
    ) {
        try {
            quangCaoAdsService.capNhatGiaThauTuKhoa(maTuKhoa, giaThauMoi);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã cập nhật giá thầu mới: " + String.format("%,.0f", giaThauMoi) + " VNĐ/click!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/quang-cao-ads/chi-tiet/" + maChienDich;
    }

    /**
     * US-64: Màn Hình Mô Phỏng Tìm Kiếm Khách Hàng (Search Simulation & Top Sponsored Rank)
     */
    @GetMapping("/mo-phong-tim-kiem")
    public String moPhongTimKiem(
            Model model,
            @RequestParam(value = "tuKhoa", defaultValue = "tai nghe") String tuKhoa
    ) {
        List<KetQuaTimKiemAdsDTO> danhSachKetQua = quangCaoAdsService.timKiemSanPhamDauThauAds(tuKhoa);

        long soLuongAds = danhSachKetQua.stream().filter(KetQuaTimKiemAdsDTO::isLaQuangCao).count();
        long soLuongTuNhien = danhSachKetQua.size() - soLuongAds;

        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("danhSachKetQua", danhSachKetQua);
        model.addAttribute("soLuongAds", soLuongAds);
        model.addAttribute("soLuongTuNhien", soLuongTuNhien);

        return "quang-cao-ads/mo-phong-tim-kiem";
    }

    /**
     * US-64: Mô Phỏng Lượt Click Quảng Cáo (Khấu Trừ Chi Phí CPC và Ví Người Bán Live)
     */
    @PostMapping("/mo-phong-click")
    public String moPhongClick(
            @RequestParam("maTuKhoa") Long maTuKhoa,
            @RequestParam(value = "tuKhoaTimKiem", defaultValue = "tai nghe") String tuKhoaTimKiem,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Map<String, Object> res = quangCaoAdsService.xuLyClickQuangCao(maTuKhoa);
            BigDecimal giaCpc = (BigDecimal) res.get("giaCpc");
            BigDecimal soDuMoi = (BigDecimal) res.get("soDuViConLai");
            boolean hetNganSach = (boolean) res.get("hetNganSach");

            String msg = "Ghi nhận 1 click hợp lệ! Đã trừ chi phí CPC: -" + 
                    String.format("%,.0f", giaCpc) + " VNĐ khỏi Ví Shop (Số dư ví còn: " + 
                    String.format("%,.0f", soDuMoi) + " VNĐ).";
            if (hetNganSach) {
                msg += " Chiến dịch đã đạt trần ngân sách ngày và tự động chuyển sang HẾT_NGÂN_SÁCH.";
            }

            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/quang-cao-ads/mo-phong-tim-kiem?tuKhoa=" + tuKhoaTimKiem;
    }
}
