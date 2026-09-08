package com.example.demo.controller;

import com.example.demo.dto.AffiliateThongKeDTO;
import com.example.demo.dto.GhiNhanDonAffiliateRequestDTO;
import com.example.demo.dto.TaoLinkAffiliateRequestDTO;
import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.DonHangTiepThi;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.SanPham;
import com.example.demo.entity.TiepThiLienKet;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.SanPhamRepository;
import com.example.demo.service.TiepThiLienKetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - Controller Tiếp Thị Liên Kết & Hoa Hồng KOC
 * =====================================================================
 */
@Slf4j
@Controller
@RequestMapping("/tiep-thi-lien-ket")
@RequiredArgsConstructor
public class TiepThiLienKetController {

    private final TiepThiLienKetService tiepThiLienKetService;
    private final NguoiDungRepository nguoiDungRepository;
    private final SanPhamRepository sanPhamRepository;
    private final DonHangShopRepository donHangShopRepository;

    /**
     * US-65: Dashboard Tổng Quan KOC & Quản Lý Link Tiếp Thị
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "maKoc", required = false) Long maKoc,
            Model model
    ) {
        List<NguoiDung> danhSachKoc = nguoiDungRepository.findAll();
        AffiliateThongKeDTO thongKe = tiepThiLienKetService.getThongKeAffiliate(maKoc);
        List<TiepThiLienKet> danhSachLink = tiepThiLienKetService.getDanhSachLink(maKoc);

        model.addAttribute("thongKe", thongKe);
        model.addAttribute("danhSachLink", danhSachLink);
        model.addAttribute("danhSachKoc", danhSachKoc);
        model.addAttribute("maKocHienTai", maKoc);

        return "tiep-thi-lien-ket/dashboard";
    }

    /**
     * US-65: Giao Diện Tạo Mới Link Tiếp Thị Độc Quyền
     */
    @GetMapping("/tao-link")
    public String trangTaoLink(
            @RequestParam(value = "maKoc", required = false) Long maKoc,
            Model model
    ) {
        TaoLinkAffiliateRequestDTO dto = new TaoLinkAffiliateRequestDTO();
        if (maKoc != null) {
            dto.setMaKoc(maKoc);
        } else {
            dto.setMaKoc(1L); // Mặc định KOC Nguyễn Văn Khách
        }
        dto.setTyLeHoaHongPhanTram(new BigDecimal("10.00")); // Mặc định 10%

        List<NguoiDung> danhSachKoc = nguoiDungRepository.findAll();
        List<SanPham> danhSachSanPham = sanPhamRepository.findAll();

        model.addAttribute("taoDTO", dto);
        model.addAttribute("danhSachKoc", danhSachKoc);
        model.addAttribute("danhSachSanPham", danhSachSanPham);

        return "tiep-thi-lien-ket/tao-link";
    }

    /**
     * US-65: Xử Lý Tạo Mới Link Tiếp Thị
     */
    @PostMapping("/tao-link")
    public String xuLyTaoLink(
            @ModelAttribute("taoDTO") TaoLinkAffiliateRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            TiepThiLienKet saved = tiepThiLienKetService.taoMoiLinkAffiliate(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã tạo thành công liên kết tiếp thị [" + saved.getMaLinkAffiliate() + 
                    "] cho sản phẩm [" + saved.getSanPham().getTenSanPham() + "]!");
            return "redirect:/tiep-thi-lien-ket/dashboard?maKoc=" + dto.getMaKoc();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tiep-thi-lien-ket/tao-link?maKoc=" + dto.getMaKoc();
        }
    }

    /**
     * US-65: Quản Lý Đơn Hàng Tiếp Thị & Đối Soát Hoa Hồng
     */
    @GetMapping("/don-hang")
    public String danhSachDonHang(
            @RequestParam(value = "maKoc", required = false) Long maKoc,
            @RequestParam(value = "trangThai", defaultValue = "ALL") String trangThai,
            Model model
    ) {
        List<NguoiDung> danhSachKoc = nguoiDungRepository.findAll();
        List<DonHangTiepThi> danhSachDon = tiepThiLienKetService.getDanhSachDonHang(maKoc, trangThai);
        AffiliateThongKeDTO thongKe = tiepThiLienKetService.getThongKeAffiliate(maKoc);

        model.addAttribute("danhSachDon", danhSachDon);
        model.addAttribute("danhSachKoc", danhSachKoc);
        model.addAttribute("maKocHienTai", maKoc);
        model.addAttribute("trangThaiHienTai", trangThai);
        model.addAttribute("thongKe", thongKe);

        return "tiep-thi-lien-ket/danh-sach-don-hang";
    }

    /**
     * US-65: Duyệt Đối Soát & Giải Ngân Hoa Hồng Cho KOC
     */
    @PostMapping("/duyet-hoa-hong")
    public String duyetHoaHong(
            @RequestParam("maDonAffiliate") Long maDonAffiliate,
            @RequestParam(value = "maKoc", required = false) Long maKoc,
            RedirectAttributes redirectAttributes
    ) {
        try {
            DonHangTiepThi don = tiepThiLienKetService.duyetHoaHong(maDonAffiliate);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã duyệt hoa hồng thành công: +" + String.format("%,.0f", don.getHoaHongDuocNhan()) + 
                    " VNĐ cho đối tác KOC [" + don.getTiepThiLienKet().getKoc().getHoVaTen() + "]!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (maKoc != null) {
            return "redirect:/tiep-thi-lien-ket/don-hang?maKoc=" + maKoc;
        }
        return "redirect:/tiep-thi-lien-ket/don-hang";
    }

    /**
     * US-65: Hủy Ghi Nhận Hoa Hồng (Khi đơn trả hàng / hoàn tiền / phát hiện gian lận)
     */
    @PostMapping("/huy-hoa-hong")
    public String huyHoaHong(
            @RequestParam("maDonAffiliate") Long maDonAffiliate,
            @RequestParam(value = "maKoc", required = false) Long maKoc,
            RedirectAttributes redirectAttributes
    ) {
        try {
            DonHangTiepThi don = tiepThiLienKetService.huyHoaHong(maDonAffiliate);
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Đã hủy ghi nhận hoa hồng đơn tiếp thị #" + don.getMaDonAffiliate() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (maKoc != null) {
            return "redirect:/tiep-thi-lien-ket/don-hang?maKoc=" + maKoc;
        }
        return "redirect:/tiep-thi-lien-ket/don-hang";
    }

    /**
     * US-65: Màn Hình Mô Phỏng Khách Mua Hàng Qua Link Tiếp Thị
     */
    @GetMapping("/mo-phong")
    public String trangMoPhong(Model model) {
        List<TiepThiLienKet> danhSachLink = tiepThiLienKetService.getDanhSachLink(null);
        List<DonHangShop> danhSachDonHangShop = donHangShopRepository.findAll();

        GhiNhanDonAffiliateRequestDTO dto = new GhiNhanDonAffiliateRequestDTO();
        if (!danhSachLink.isEmpty()) {
            dto.setMaLinkAffiliate(danhSachLink.get(0).getMaLinkAffiliate());
        }
        if (!danhSachDonHangShop.isEmpty()) {
            dto.setMaDonHangShop(danhSachDonHangShop.get(0).getMaDonHangShop());
        }

        model.addAttribute("ghiNhanDTO", dto);
        model.addAttribute("danhSachLink", danhSachLink);
        model.addAttribute("danhSachDonHangShop", danhSachDonHangShop);

        return "tiep-thi-lien-ket/mo-phong-mua-hang";
    }

    /**
     * US-65: Xử Lý Mô Phỏng Đơn Mua Tiếp Thị
     */
    @PostMapping("/mo-phong-mua")
    public String xuLyMoPhongMua(
            @ModelAttribute("ghiNhanDTO") GhiNhanDonAffiliateRequestDTO dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            DonHangTiepThi don = tiepThiLienKetService.ghiNhanDonHangTiepThi(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Mô phỏng mua hàng thành công qua Link [" + dto.getMaLinkAffiliate() + 
                    "]! Đã tự động tính hoa hồng: +" + String.format("%,.0f", don.getHoaHongDuocNhan()) + 
                    " VNĐ (Trạng thái: " + don.getTenTrangThaiTiengViet() + ").");
            return "redirect:/tiep-thi-lien-ket/don-hang";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tiep-thi-lien-ket/mo-phong";
        }
    }
}
