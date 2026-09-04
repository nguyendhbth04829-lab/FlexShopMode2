package com.example.demo.controller;

import com.example.demo.dto.ThongKeKhieuNaiDTO;
import com.example.demo.dto.YeuCauKhieuNaiForm;
import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.PhieuKhieuNai;
import com.example.demo.repository.ChiTietDonHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.PhieuKhieuNaiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/khieu-nai")
public class KhieuNaiController {

    @Autowired
    private PhieuKhieuNaiService phieuKhieuNaiService;

    @Autowired
    private ChiTietDonHangRepository chiTietDonHangRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    private Long layMaKhachHangHienTai() {
        return nguoiDungRepository.findByEmail("khachhang@flexshop.vn")
                .map(NguoiDung::getMaNguoiDung)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .findFirst()
                        .map(NguoiDung::getMaNguoiDung)
                        .orElse(1L));
    }

    @GetMapping({"", "/", "/danh-sach"})
    public String danhSach(
            Model model,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "loaiKhieuNai", required = false) String loaiKhieuNai,
            @RequestParam(value = "mucDoUuTien", required = false) String mucDoUuTien,
            @RequestParam(value = "tuNgay", required = false) String tuNgay,
            @RequestParam(value = "denNgay", required = false) String denNgay,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size
    ) {
        Long maKhachHang = layMaKhachHangHienTai();

        // 1. Thống kê đồng bộ trực tiếp từ Database
        ThongKeKhieuNaiDTO thongKe = phieuKhieuNaiService.layThongKeKhieuNai(maKhachHang);
        model.addAttribute("thongKe", thongKe);

        // 2. Truy vấn danh sách có phân trang & tìm kiếm lọc đa tiêu chí
        Page<PhieuKhieuNai> pagePhieu = phieuKhieuNaiService.layDanhSachKhieuNaiNangCao(
                maKhachHang, tuKhoa, trangThai, loaiKhieuNai, mucDoUuTien, tuNgay, denNgay, page, size
        );

        model.addAttribute("danhSachPhieu", pagePhieu.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", pagePhieu.getTotalPages());
        model.addAttribute("totalElements", pagePhieu.getTotalElements());

        // Giữ lại tham số tìm kiếm trên thanh lọc
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("loaiKhieuNai", loaiKhieuNai);
        model.addAttribute("mucDoUuTien", mucDoUuTien);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);

        return "khieu-nai/danh-sach";
    }

    @GetMapping("/tao-phieu")
    public String hienThiFormTaoPhieu(
            Model model,
            @RequestParam(value = "maDonHangShop", required = false) Long maDonHangShop
    ) {
        Long maKhachHang = layMaKhachHangHienTai();
        List<DonHangShop> listDonHang = phieuKhieuNaiService.layDanhSachDonHangKhaDung(maKhachHang);

        YeuCauKhieuNaiForm form = new YeuCauKhieuNaiForm();
        if (maDonHangShop != null) {
            form.setMaDonHangShop(maDonHangShop);
        }

        model.addAttribute("formKhieuNai", form);
        model.addAttribute("listDonHang", listDonHang);

        return "khieu-nai/tao-phieu";
    }

    @PostMapping("/tao-phieu")
    public String xuLyTaoPhieu(
            @Valid @ModelAttribute("formKhieuNai") YeuCauKhieuNaiForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long maKhachHang = layMaKhachHangHienTai();

        // 1. Kiểm tra validation Bean Validation (@NotNull, @NotBlank, @Size,...)
        if (bindingResult.hasErrors()) {
            model.addAttribute("listDonHang", phieuKhieuNaiService.layDanhSachDonHangKhaDung(maKhachHang));
            return "khieu-nai/tao-phieu";
        }

        // 2. Thực thi nghiệp vụ và validation chuyên sâu
        try {
            PhieuKhieuNai phieuMoi = phieuKhieuNaiService.taoPhieuKhieuNaiTuForm(maKhachHang, form);
            redirectAttributes.addFlashAttribute("thongBaoThanhCong",
                    "Gửi yêu cầu khiếu nại thành công! Mã số phiếu: " + phieuMoi.getMaCodePhieu());
            return "redirect:/khieu-nai/chi-tiet?id=" + phieuMoi.getMaPhieu();
        } catch (Exception e) {
            model.addAttribute("thongBaoLoi", e.getMessage());
            model.addAttribute("listDonHang", phieuKhieuNaiService.layDanhSachDonHangKhaDung(maKhachHang));
            return "khieu-nai/tao-phieu";
        }
    }

    @GetMapping("/chi-tiet")
    public String chiTiet(Model model, @RequestParam("id") Long id) {
        PhieuKhieuNai phieu = phieuKhieuNaiService.layChiTietPhieu(id);
        model.addAttribute("phieu", phieu);
        model.addAttribute("danhSachBangChung", phieu.getDanhSachBangChung());
        model.addAttribute("donHangShop", phieu.getDonHangShop());
        model.addAttribute("danhSachSanPham", chiTietDonHangRepository.findAllByDonHangShop_MaDonHangShop(phieu.getDonHangShop().getMaDonHangShop()));

        return "khieu-nai/chi-tiet";
    }

    @PostMapping("/huy")
    public String huyPhieu(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Long maKhachHang = layMaKhachHangHienTai();
        try {
            boolean ketQua = phieuKhieuNaiService.huyKhieuNai(id, maKhachHang);
            if (ketQua) {
                redirectAttributes.addFlashAttribute("thongBaoThanhCong", "Đã hủy yêu cầu khiếu nại thành công!");
            } else {
                redirectAttributes.addFlashAttribute("thongBaoLoi", "Không thể hủy khiếu nại do phiếu đã được nhân viên CSKH tiếp nhận xử lý!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("thongBaoLoi", "Lỗi: " + e.getMessage());
        }
        return "redirect:/khieu-nai/chi-tiet?id=" + id;
    }
}
