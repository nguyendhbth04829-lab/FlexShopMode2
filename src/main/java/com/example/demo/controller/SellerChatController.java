package com.example.demo.controller;

import com.example.demo.dto.ThongKeChatDTO;
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
import java.util.stream.Collectors;

/**
 * Controller phục vụ Kênh Người Bán (Seller) Live Chat và Duyệt Trả Giá (US-59)
 */
@Controller
@RequestMapping("/seller/chat")
public class SellerChatController {

    private static final String EMAIL_SELLER_MAC_DINH = "techzone@flexshop.vn";

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

    private NguoiDung laySellerHienTai() {
        return nguoiDungRepository.findByEmail(EMAIL_SELLER_MAC_DINH)
                .orElseGet(() -> nguoiDungRepository.findAll().stream()
                        .filter(u -> gianHangRepository.findByChuSoHuu_MaNguoiDung(u.getMaNguoiDung()).isPresent())
                        .findFirst()
                        .orElse(null));
    }

    private GianHang layGianHangCuaSeller(NguoiDung seller) {
        if (seller == null) {
            return gianHangRepository.findAll().stream().findFirst().orElse(null);
        }
        return gianHangRepository.findByChuSoHuu_MaNguoiDung(seller.getMaNguoiDung())
                .orElseGet(() -> gianHangRepository.findAll().stream().findFirst().orElse(null));
    }

    @GetMapping
    public String trangChatSeller(
            @RequestParam(value = "tab", defaultValue = "TAT_CA") String tab,
            @RequestParam(value = "cuocTroChuyenId", required = false) Long cuocTroChuyenId,
            @RequestParam(value = "tuKhoa", required = false) String tuKhoa,
            Model model
    ) {
        NguoiDung seller = laySellerHienTai();
        GianHang gianHang = layGianHangCuaSeller(seller);

        if (gianHang == null) {
            model.addAttribute("thongBaoLoi", "Không tìm thấy gian hàng của tài khoản người bán!");
            return "seller/chat";
        }

        // 1. Thống kê chat và đề xuất trả giá
        ThongKeChatDTO thongKe = chatService.layThongKeChatShop(gianHang.getMaGianHang());

        // 2. Lấy danh sách cuộc trò chuyện theo bộ lọc
        List<CuocTroChuyen> danhSachCuocTroChuyen;
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            danhSachCuocTroChuyen = cuocTroChuyenRepository.timKiemHoiThoaiShop(gianHang.getMaGianHang(), tuKhoa.trim());
        } else {
            danhSachCuocTroChuyen = cuocTroChuyenRepository.findByGianHang_MaGianHangOrderByThoiGianTinCuoiDesc(gianHang.getMaGianHang());
        }

        // Lọc theo tab
        if ("CHUA_DOC".equalsIgnoreCase(tab)) {
            danhSachCuocTroChuyen = danhSachCuocTroChuyen.stream()
                    .filter(c -> c.getSoTinChuaDocShop() != null && c.getSoTinChuaDocShop() > 0)
                    .collect(Collectors.toList());
        } else if ("CO_TRA_GIA".equalsIgnoreCase(tab)) {
            List<Long> maCoTraGia = deXuatTraGiaRepository.findMaCuocTroChuyenCoTraGiaChoDuyet(gianHang.getMaGianHang());
            danhSachCuocTroChuyen = danhSachCuocTroChuyen.stream()
                    .filter(c -> maCoTraGia.contains(c.getMaCuocTroChuyen()))
                    .collect(Collectors.toList());
        }

        // 3. Xác định cuộc trò chuyện hiện tại đang mở
        CuocTroChuyen cuocTroChuyenHienTai = null;
        if (cuocTroChuyenId != null) {
            cuocTroChuyenHienTai = cuocTroChuyenRepository.findById(cuocTroChuyenId).orElse(null);
        } else if (!danhSachCuocTroChuyen.isEmpty()) {
            cuocTroChuyenHienTai = danhSachCuocTroChuyen.get(0);
        }

        // 4. Nếu có cuộc trò chuyện đang mở, lấy tin nhắn và đề xuất trả giá
        List<TinNhan> danhSachTinNhan = Collections.emptyList();
        List<DeXuatTraGia> danhSachTraGia = Collections.emptyList();
        List<SanPham> danhSachSanPhamShop = sanPhamRepository.findByGianHang_MaGianHangAndTrangThai(gianHang.getMaGianHang(), "HOAT_DONG");

        if (cuocTroChuyenHienTai != null) {
            danhSachTinNhan = tinNhanRepository.findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoAsc(cuocTroChuyenHienTai.getMaCuocTroChuyen());
            danhSachTraGia = deXuatTraGiaRepository.findByCuocTroChuyen_MaCuocTroChuyenOrderByNgayTaoDesc(cuocTroChuyenHienTai.getMaCuocTroChuyen());

            // Đánh dấu Shop đã đọc tin nhắn từ khách
            chatService.danhDauDaXem(cuocTroChuyenHienTai.getMaCuocTroChuyen(), "SHOP");
        }

        model.addAttribute("seller", seller);
        model.addAttribute("gianHang", gianHang);
        model.addAttribute("thongKe", thongKe);
        model.addAttribute("tab", tab);
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("danhSachCuocTroChuyen", danhSachCuocTroChuyen);
        model.addAttribute("cuocTroChuyenHienTai", cuocTroChuyenHienTai);
        model.addAttribute("danhSachTinNhan", danhSachTinNhan);
        model.addAttribute("danhSachTraGia", danhSachTraGia);
        model.addAttribute("danhSachSanPhamShop", danhSachSanPhamShop);

        return "seller/chat";
    }
}
