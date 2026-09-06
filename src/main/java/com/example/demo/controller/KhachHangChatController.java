package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

/**
 * Controller phục vụ Khách hàng Live Chat và Trả Giá Sản Phẩm với Shop (US-59)
 */
@Controller
@RequestMapping("/khach-hang/chat")
public class KhachHangChatController {

    private static final String EMAIL_KHACH_HANG_MAC_DINH = "khachhang@flexshop.vn";

    @Autowired
    private ChatService chatService;

    @Autowired
    private CuocTroChuyenRepository cuocTroChuyenRepository;

    @Autowired
    private TinNhanRepository tinNhanRepository;

    @Autowired
    private DeXuatTraGiaRepository deXuatTraGiaRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private GianHangRepository gianHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    private NguoiDung layKhachHangHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_KHACH_HANG_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().contains("khach"))
                        .findFirst()
                        .orElse(null));
    }

    @GetMapping
    public String trangChatKhachHang(
            @RequestParam(value = "shopId", required = false) Long shopId,
            @RequestParam(value = "sanPhamId", required = false) Long sanPhamId,
            @RequestParam(value = "cuocTroChuyenId", required = false) Long cuocTroChuyenId,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            Model model
    ) {
        NguoiDung khachHang = layKhachHangHienTai();
        if (khachHang == null) {
            model.addAttribute("thongBaoLoi", "Không tìm thấy thông tin tài khoản khách hàng!");
            return "khach-hang/chat";
        }

        // 1. Lấy danh sách cuộc trò chuyện của khách hàng
        List<CuocTroChuyen> danhSachCuocTroChuyen;
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            danhSachCuocTroChuyen = cuocTroChuyenRepository.timKiemHoiThoaiKhachHang(khachHang.getMaNguoiDung(), tuKhoa.trim());
        } else {
            danhSachCuocTroChuyen = cuocTroChuyenRepository.findByKhachHang_MaNguoiDungOrderByThoiGianTinCuoiDesc(khachHang.getMaNguoiDung());
        }

        // 2. Xác định cuộc trò chuyện hiện tại đang mở
        CuocTroChuyen cuocTroChuyenHienTai = null;

        if (cuocTroChuyenId != null) {
            cuocTroChuyenHienTai = cuocTroChuyenRepository.findById(cuocTroChuyenId).orElse(null);
        } else if (shopId != null) {
            cuocTroChuyenHienTai = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), shopId);
        } else if (!danhSachCuocTroChuyen.isEmpty()) {
            cuocTroChuyenHienTai = danhSachCuocTroChuyen.get(0);
        } else {
            // Mặc định tạo hoặc mở chat với Shop đầu tiên (TechZone Flagship Store)
            GianHang gianHangMacDinh = gianHangRepository.findAll().stream().findFirst().orElse(null);
            if (gianHangMacDinh != null) {
                cuocTroChuyenHienTai = chatService.layHoacTaoCuocTroChuyen(khachHang.getMaNguoiDung(), gianHangMacDinh.getMaGianHang());
                danhSachCuocTroChuyen = cuocTroChuyenRepository.findByKhachHang_MaNguoiDungOrderByThoiGianTinCuoiDesc(khachHang.getMaNguoiDung());
            }
        }

        // 3. Nếu có cuộc trò chuyện đang mở, lấy tin nhắn và danh sách sản phẩm của Shop
        List<TinNhan> danhSachTinNhan = Collections.emptyList();
        List<SanPham> danhSachSanPhamShop = Collections.emptyList();
        List<DeXuatTraGia> danhSachTraGia = Collections.emptyList();
        SanPham sanPhamDangChon = null;

        if (cuocTroChuyenHienTai != null) {
            danhSachTinNhan = tinNhanRepository.findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoAsc(cuocTroChuyenHienTai.getMaCuocTroChuyen());
            danhSachTraGia = deXuatTraGiaRepository.findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoDesc(cuocTroChuyenHienTai.getMaCuocTroChuyen());

            // Đánh dấu khách đã xem tin nhắn từ shop
            chatService.danhDauDaXem(cuocTroChuyenHienTai.getMaCuocTroChuyen(), "KHACH_HANG");

            // Lấy danh sách sản phẩm đang bán của shop này
            Long maGianHang = cuocTroChuyenHienTai.getGianHang().getMaGianHang();
            danhSachSanPhamShop = sanPhamRepository.findByGianHang_MaGianHangAndTrangThai(maGianHang, "HOAT_DONG");

            // Nếu truyền sanPhamId từ bên ngoài (ví dụ từ trang chi tiết sản phẩm)
            if (sanPhamId != null) {
                sanPhamDangChon = sanPhamRepository.findById(sanPhamId).orElse(null);
            } else if (!danhSachSanPhamShop.isEmpty()) {
                sanPhamDangChon = danhSachSanPhamShop.get(0);
            }
        }

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("danhSachCuocTroChuyen", danhSachCuocTroChuyen);
        model.addAttribute("cuocTroChuyenHienTai", cuocTroChuyenHienTai);
        model.addAttribute("danhSachTinNhan", danhSachTinNhan);
        model.addAttribute("danhSachSanPhamShop", danhSachSanPhamShop);
        model.addAttribute("sanPhamDangChon", sanPhamDangChon);
        model.addAttribute("danhSachTraGia", danhSachTraGia);
        model.addAttribute("tuKhoa", tuKhoa);

        return "khach-hang/chat";
    }
}
