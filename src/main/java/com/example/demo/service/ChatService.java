package com.example.demo.service;

import com.example.demo.config.ChatWebSocketHandler;
import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service xử lý toàn bộ logic nghiệp vụ Live Chat & Đề xuất Trả giá sản phẩm (US-59)
 */
@Slf4j
@Service
public class ChatService {

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

    @Autowired
    private BienTheSanPhamRepository bienTheSanPhamRepository;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DecimalFormat df = new DecimalFormat("#,###");

    /**
     * 1. Lấy hoặc tạo mới cuộc trò chuyện giữa Khách hàng và Shop
     */
    @Transactional
    public CuocTroChuyen layHoacTaoCuocTroChuyen(Long maKhachHang, Long maGianHang) {
        return cuocTroChuyenRepository.findByKhachHang_MaNguoiDungAndGianHang_MaGianHang(maKhachHang, maGianHang)
                .orElseGet(() -> {
                    NguoiDung khachHang = nguoiDungRepository.findById(maKhachHang)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng ID #" + maKhachHang));
                    GianHang gianHang = gianHangRepository.findById(maGianHang)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gian hàng ID #" + maGianHang));

                    CuocTroChuyen ctc = new CuocTroChuyen();
                    ctc.setKhachHang(khachHang);
                    ctc.setGianHang(gianHang);
                    ctc.setTinNhanCuoiCung("Bắt đầu cuộc trò chuyện");
                    ctc.setThoiGianTinCuoi(LocalDateTime.now());
                    ctc.setSoTinChuaDocKhach(0);
                    ctc.setSoTinChuaDocShop(0);
                    ctc.setNgayTao(LocalDateTime.now());

                    CuocTroChuyen saved = cuocTroChuyenRepository.save(ctc);

                    // Gửi tin nhắn chào mừng tự động từ Shop
                    TinNhan chaoMung = new TinNhan();
                    chaoMung.setCuocTroChuyen(saved);
                    chaoMung.setLoaiNguoiGui("SHOP");
                    chaoMung.setMaNguoiGui(gianHang.getChuSoHuu() != null ? gianHang.getChuSoHuu().getMaNguoiDung() : 0L);
                    chaoMung.setLoaiTinNhan("VAN_BAN");
                    chaoMung.setNoiDung("Xin chào quý khách! " + gianHang.getTenGianHang() + " có thể hỗ trợ gì cho bạn hôm nay ạ?");
                    chaoMung.setDaXem(false);
                    chaoMung.setNgayTao(LocalDateTime.now());
                    tinNhanRepository.save(chaoMung);

                    return saved;
                });
    }

    /**
     * 2. Gửi tin nhắn văn bản thông thường
     */
    @Transactional
    public TinNhan guiTinNhan(Long maCuocTroChuyen, Long maNguoiGui, String loaiNguoiGui, String noiDung) {
        if (noiDung == null || noiDung.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống!");
        }

        CuocTroChuyen ctc = cuocTroChuyenRepository.findById(maCuocTroChuyen)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cuộc trò chuyện #" + maCuocTroChuyen));

        TinNhan tinNhan = new TinNhan();
        tinNhan.setCuocTroChuyen(ctc);
        tinNhan.setLoaiNguoiGui(loaiNguoiGui);
        tinNhan.setMaNguoiGui(maNguoiGui);
        tinNhan.setLoaiTinNhan("VAN_BAN");
        tinNhan.setNoiDung(noiDung.trim());
        tinNhan.setDaXem(false);
        tinNhan.setNgayTao(LocalDateTime.now());

        TinNhan saved = tinNhanRepository.save(tinNhan);

        // Cập nhật thông tin cuộc trò chuyện
        ctc.setTinNhanCuoiCung(saved.getNoiDung());
        ctc.setThoiGianTinCuoi(saved.getNgayTao());
        if ("KHACH_HANG".equalsIgnoreCase(loaiNguoiGui)) {
            ctc.setSoTinChuaDocShop((ctc.getSoTinChuaDocShop() == null ? 0 : ctc.getSoTinChuaDocShop()) + 1);
        } else {
            ctc.setSoTinChuaDocKhach((ctc.getSoTinChuaDocKhach() == null ? 0 : ctc.getSoTinChuaDocKhach()) + 1);
        }
        cuocTroChuyenRepository.save(ctc);

        // Phát sóng WebSocket
        phatSongWebSocket(maCuocTroChuyen, "TIN_NHAN_MOI", saved, null);

        return saved;
    }

    /**
     * 3. Gửi thẻ sản phẩm (Product Card) vào khung chat
     */
    @Transactional
    public TinNhan guiTheSanPham(Long maCuocTroChuyen, Long maNguoiGui, String loaiNguoiGui, Long maSanPham, Long maBienThe) {
        CuocTroChuyen ctc = cuocTroChuyenRepository.findById(maCuocTroChuyen)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cuộc trò chuyện #" + maCuocTroChuyen));

        SanPham sanPham = sanPhamRepository.findById(maSanPham)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm #" + maSanPham));

        BienTheSanPham bienThe = null;
        if (maBienThe != null) {
            bienThe = bienTheSanPhamRepository.findById(maBienThe).orElse(null);
        } else {
            List<BienTheSanPham> danhSachBienThe = bienTheSanPhamRepository.findBySanPham_MaSanPhamAndDaXoaFalse(maSanPham);
            if (!danhSachBienThe.isEmpty()) {
                bienThe = danhSachBienThe.get(0);
            }
        }

        BigDecimal giaBan = (bienThe != null && bienThe.getGiaBan() != null) ? bienThe.getGiaBan() : sanPham.getGiaCoBan();
        BigDecimal giaGoc = (bienThe != null && bienThe.getGiaGoc() != null) ? bienThe.getGiaGoc() : giaBan;
        String linkAnh = (bienThe != null && bienThe.getLinkAnh() != null) ? bienThe.getLinkAnh() : "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500";

        TheSanPhamDTO theSp = TheSanPhamDTO.builder()
                .maSanPham(sanPham.getMaSanPham())
                .tenSanPham(sanPham.getTenSanPham())
                .duongDanSlug(sanPham.getDuongDanSlug())
                .giaBan(giaBan)
                .giaGoc(giaGoc)
                .linkAnh(linkAnh)
                .maBienThe(bienThe != null ? bienThe.getMaBienThe() : null)
                .tenBienThe(bienThe != null ? bienThe.getTenBienThe() : "Mặc định")
                .maGianHang(sanPham.getGianHang().getMaGianHang())
                .tenGianHang(sanPham.getGianHang().getTenGianHang())
                .build();

        String json;
        try {
            json = objectMapper.writeValueAsString(theSp);
        } catch (Exception e) {
            json = "{}";
        }

        TinNhan tinNhan = new TinNhan();
        tinNhan.setCuocTroChuyen(ctc);
        tinNhan.setLoaiNguoiGui(loaiNguoiGui);
        tinNhan.setMaNguoiGui(maNguoiGui);
        tinNhan.setLoaiTinNhan("THE_SAN_PHAM");
        tinNhan.setNoiDung("Đã gửi thẻ sản phẩm: " + sanPham.getTenSanPham());
        tinNhan.setDuLieuDinhKemJson(json);
        tinNhan.setDaXem(false);
        tinNhan.setNgayTao(LocalDateTime.now());

        TinNhan saved = tinNhanRepository.save(tinNhan);

        ctc.setTinNhanCuoiCung(saved.getNoiDung());
        ctc.setThoiGianTinCuoi(saved.getNgayTao());
        if ("KHACH_HANG".equalsIgnoreCase(loaiNguoiGui)) {
            ctc.setSoTinChuaDocShop((ctc.getSoTinChuaDocShop() == null ? 0 : ctc.getSoTinChuaDocShop()) + 1);
        } else {
            ctc.setSoTinChuaDocKhach((ctc.getSoTinChuaDocKhach() == null ? 0 : ctc.getSoTinChuaDocKhach()) + 1);
        }
        cuocTroChuyenRepository.save(ctc);

        phatSongWebSocket(maCuocTroChuyen, "THE_SAN_PHAM", saved, theSp);

        return saved;
    }

    /**
     * 4. Tạo đề xuất Trả Giá Sản Phẩm (Make an Offer) & Validate chi tiết
     */
    @Transactional
    public DeXuatTraGia taoDeXuatTraGia(DeXuatTraGiaForm form, Long maKhachHang) {
        CuocTroChuyen ctc = cuocTroChuyenRepository.findById(form.getMaCuocTroChuyen())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cuộc trò chuyện #" + form.getMaCuocTroChuyen()));

        NguoiDung khachHang = nguoiDungRepository.findById(maKhachHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản khách hàng!"));

        SanPham sanPham = sanPhamRepository.findById(form.getMaSanPham())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm #" + form.getMaSanPham()));

        // Chặn người dùng tự trả giá sản phẩm của chính gian hàng mình sở hữu
        if (sanPham.getGianHang().getChuSoHuu() != null &&
                sanPham.getGianHang().getChuSoHuu().getMaNguoiDung().equals(maKhachHang)) {
            throw new IllegalArgumentException("Bạn không thể trả giá sản phẩm do chính bạn sở hữu!");
        }

        BienTheSanPham bienThe = null;
        if (form.getMaBienThe() != null) {
            bienThe = bienTheSanPhamRepository.findById(form.getMaBienThe()).orElse(null);
        } else {
            List<BienTheSanPham> danhSachBienThe = bienTheSanPhamRepository.findBySanPham_MaSanPhamAndDaXoaFalse(sanPham.getMaSanPham());
            if (!danhSachBienThe.isEmpty()) {
                bienThe = danhSachBienThe.get(0);
            }
        }

        BigDecimal giaGoc = (bienThe != null && bienThe.getGiaBan() != null) ? bienThe.getGiaBan() : sanPham.getGiaCoBan();
        BigDecimal giaDeXuat = form.getGiaDeXuat();

        // 1. Kiểm tra giá đề xuất phải < giá gốc niêm yết
        if (giaDeXuat.compareTo(giaGoc) >= 0) {
            throw new IllegalArgumentException("Giá đề xuất trả giá (" + df.format(giaDeXuat) + "đ) phải nhỏ hơn giá niêm yết hiện tại (" + df.format(giaGoc) + "đ)!");
        }

        // 2. Kiểm tra ngưỡng sàn an toàn: Không được giảm sâu quá 50%
        BigDecimal nguongToiThieu = giaGoc.multiply(new BigDecimal("0.50")).setScale(0, RoundingMode.HALF_UP);
        if (giaDeXuat.compareTo(nguongToiThieu) < 0) {
            throw new IllegalArgumentException("Để đảm bảo quyền lợi của Shop, mức trả giá tối đa được giảm là 50% (Tối thiểu " + df.format(nguongToiThieu) + "đ)!");
        }

        // 3. Chặn tạo đề xuất trùng lặp khi đang có đề xuất cùng sản phẩm chờ duyệt
        Optional<DeXuatTraGia> deXuatCu = deXuatTraGiaRepository.findTopByKhachHang_MaNguoiDungAndSanPham_MaSanPhamAndTrangThai(
                maKhachHang, sanPham.getMaSanPham(), "CHO_DUYET"
        );
        if (deXuatCu.isPresent()) {
            throw new IllegalArgumentException("Bạn đang có một đề xuất trả giá cho sản phẩm này đang chờ Shop xét duyệt. Vui lòng đợi Shop phản hồi trước khi gửi đề xuất mới!");
        }

        // Tạo bản ghi đề xuất trả giá
        DeXuatTraGia deXuat = new DeXuatTraGia();
        deXuat.setCuocTroChuyen(ctc);
        deXuat.setKhachHang(khachHang);
        deXuat.setGianHang(sanPham.getGianHang());
        deXuat.setSanPham(sanPham);
        deXuat.setBienThe(bienThe);
        deXuat.setSoLuong(form.getSoLuong() != null ? form.getSoLuong() : 1);
        deXuat.setGiaGoc(giaGoc);
        deXuat.setGiaDeXuat(giaDeXuat);
        deXuat.setTrangThai("CHO_DUYET");
        deXuat.setGhiChuKhach(form.getGhiChuKhach() != null ? form.getGhiChuKhach().trim() : null);
        deXuat.setNgayTao(LocalDateTime.now());
        deXuat.setNgayHetHan(LocalDateTime.now().plusDays(1)); // Hiệu lực 24 giờ

        DeXuatTraGia savedDeXuat = deXuatTraGiaRepository.save(deXuat);

        // Chuẩn bị payload JSON đính kèm tin nhắn
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("maDeXuat", savedDeXuat.getMaDeXuat());
        payloadMap.put("maSanPham", sanPham.getMaSanPham());
        payloadMap.put("tenSanPham", sanPham.getTenSanPham());
        payloadMap.put("giaGoc", giaGoc);
        payloadMap.put("giaDeXuat", giaDeXuat);
        payloadMap.put("soLuong", savedDeXuat.getSoLuong());
        payloadMap.put("trangThai", "CHO_DUYET");
        payloadMap.put("ghiChuKhach", savedDeXuat.getGhiChuKhach());
        payloadMap.put("tenBienThe", bienThe != null ? bienThe.getTenBienThe() : "Mặc định");
        payloadMap.put("linkAnh", (bienThe != null && bienThe.getLinkAnh() != null) ? bienThe.getLinkAnh() : "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500");

        String json;
        try {
            json = objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            json = "{}";
        }

        // Tạo tin nhắn đính kèm đề xuất trả giá
        TinNhan tinNhan = new TinNhan();
        tinNhan.setCuocTroChuyen(ctc);
        tinNhan.setLoaiNguoiGui("KHACH_HANG");
        tinNhan.setMaNguoiGui(maKhachHang);
        tinNhan.setLoaiTinNhan("DE_XUAT_TRA_GIA");
        tinNhan.setNoiDung("Đề xuất trả giá " + df.format(giaDeXuat) + "đ cho sản phẩm " + sanPham.getTenSanPham());
        tinNhan.setDuLieuDinhKemJson(json);
        tinNhan.setDeXuatTraGia(savedDeXuat);
        tinNhan.setDaXem(false);
        tinNhan.setNgayTao(LocalDateTime.now());

        TinNhan savedTinNhan = tinNhanRepository.save(tinNhan);

        ctc.setTinNhanCuoiCung(savedTinNhan.getNoiDung());
        ctc.setThoiGianTinCuoi(savedTinNhan.getNgayTao());
        ctc.setSoTinChuaDocShop((ctc.getSoTinChuaDocShop() == null ? 0 : ctc.getSoTinChuaDocShop()) + 1);
        cuocTroChuyenRepository.save(ctc);

        phatSongWebSocket(ctc.getMaCuocTroChuyen(), "DE_XUAT_TRA_GIA", savedTinNhan, savedDeXuat);

        return savedDeXuat;
    }

    /**
     * 5. Shop xử lý duyệt Đề xuất Trả Giá (Đồng ý / Từ chối / Phản hồi giá đối ứng)
     */
    @Transactional
    public DeXuatTraGia xuLyDuyetTraGia(XuLyTraGiaForm form, Long maSeller) {
        DeXuatTraGia deXuat = deXuatTraGiaRepository.findById(form.getMaDeXuat())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đề xuất trả giá #" + form.getMaDeXuat()));

        if (!"CHO_DUYET".equalsIgnoreCase(deXuat.getTrangThai())) {
            throw new IllegalStateException("Đề xuất này đã được xử lý trước đó với trạng thái: " + deXuat.getTrangThai());
        }

        CuocTroChuyen ctc = deXuat.getCuocTroChuyen();
        String hanhDong = form.getHanhDong();
        String noiDungTinNhan;

        if ("DONG_Y".equalsIgnoreCase(hanhDong)) {
            deXuat.setTrangThai("DONG_Y");
            deXuat.setNgayCapNhat(LocalDateTime.now());
            deXuat.setPhanHoiShop(form.getPhanHoiShop());

            noiDungTinNhan = "🎉 Shop đã ĐỒNG Ý mức giá đề xuất " + df.format(deXuat.getGiaDeXuat()) +
                    "đ cho sản phẩm [" + deXuat.getSanPham().getTenSanPham() + "]! Bạn có thể đặt mua ngay với giá ưu đãi này.";
        } else if ("TU_CHOI".equalsIgnoreCase(hanhDong)) {
            deXuat.setTrangThai("TU_CHOI");
            deXuat.setNgayCapNhat(LocalDateTime.now());
            deXuat.setPhanHoiShop(form.getPhanHoiShop() != null ? form.getPhanHoiShop().trim() : "Mức giá chưa phù hợp với chính sách bán lẻ");

            noiDungTinNhan = "❌ Shop đã TỪ CHỐI đề xuất trả giá " + df.format(deXuat.getGiaDeXuat()) +
                    "đ cho sản phẩm [" + deXuat.getSanPham().getTenSanPham() + "]. Lý do: " + deXuat.getPhanHoiShop();
        } else if ("PHAN_HOI_LAI".equalsIgnoreCase(hanhDong)) {
            if (form.getGiaShopPhanHoi() == null || form.getGiaShopPhanHoi().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Vui lòng nhập mức giá đối ứng hợp lệ lớn hơn 0!");
            }
            if (form.getGiaShopPhanHoi().compareTo(deXuat.getGiaGoc()) >= 0) {
                throw new IllegalArgumentException("Mức giá đối ứng phải thấp hơn giá gốc niêm yết!");
            }

            deXuat.setTrangThai("PHAN_HOI_LAI");
            deXuat.setGiaShopPhanHoi(form.getGiaShopPhanHoi());
            deXuat.setPhanHoiShop(form.getPhanHoiShop());
            deXuat.setNgayCapNhat(LocalDateTime.now());

            noiDungTinNhan = "💬 Shop phản hồi mức giá ưu đãi đối ứng: " + df.format(form.getGiaShopPhanHoi()) +
                    "đ cho sản phẩm [" + deXuat.getSanPham().getTenSanPham() + "] kèm lời nhắn: " +
                    (form.getPhanHoiShop() != null ? form.getPhanHoiShop() : "Mức giá tốt nhất Shop có thể hỗ trợ!");
        } else {
            throw new IllegalArgumentException("Hành động xử lý không hợp lệ: " + hanhDong);
        }

        DeXuatTraGia updated = deXuatTraGiaRepository.save(deXuat);

        // Sinh tin nhắn hệ thống thông báo trạng thái mới vào luồng hội thoại
        TinNhan tinNhanHeThong = new TinNhan();
        tinNhanHeThong.setCuocTroChuyen(ctc);
        tinNhanHeThong.setLoaiNguoiGui("SHOP");
        tinNhanHeThong.setMaNguoiGui(maSeller != null ? maSeller : 0L);
        tinNhanHeThong.setLoaiTinNhan("HE_THONG");
        tinNhanHeThong.setNoiDung(noiDungTinNhan);
        tinNhanHeThong.setDaXem(false);
        tinNhanHeThong.setNgayTao(LocalDateTime.now());

        tinNhanRepository.save(tinNhanHeThong);

        // Cập nhật cuộc trò chuyện
        ctc.setTinNhanCuoiCung(tinNhanHeThong.getNoiDung());
        ctc.setThoiGianTinCuoi(tinNhanHeThong.getNgayTao());
        ctc.setSoTinChuaDocKhach((ctc.getSoTinChuaDocKhach() == null ? 0 : ctc.getSoTinChuaDocKhach()) + 1);
        cuocTroChuyenRepository.save(ctc);

        phatSongWebSocket(ctc.getMaCuocTroChuyen(), "CAP_NHAT_TRA_GIA", tinNhanHeThong, updated);

        return updated;
    }

    /**
     * 6. Đánh dấu đã đọc tin nhắn
     */
    @Transactional
    public void danhDauDaXem(Long maCuocTroChuyen, String loaiNguoiXem) {
        cuocTroChuyenRepository.findById(maCuocTroChuyen).ifPresent(ctc -> {
            if ("KHACH_HANG".equalsIgnoreCase(loaiNguoiXem)) {
                ctc.setSoTinChuaDocKhach(0);
                tinNhanRepository.danhDauDaXem(maCuocTroChuyen, "SHOP");
            } else {
                ctc.setSoTinChuaDocShop(0);
                tinNhanRepository.danhDauDaXem(maCuocTroChuyen, "KHACH_HANG");
            }
            cuocTroChuyenRepository.save(ctc);
        });
    }

    /**
     * 7. Thống kê chat và đề xuất trả giá cho Shop (Dashboard)
     */
    @Transactional(readOnly = true)
    public ThongKeChatDTO layThongKeChatShop(Long maGianHang) {
        long tongHoiThoai = cuocTroChuyenRepository.countByGianHang_MaGianHang(maGianHang);
        long tongTinChuaDoc = cuocTroChuyenRepository.countByGianHang_MaGianHangAndSoTinChuaDocShopGreaterThan(maGianHang, 0);

        long tongDeXuat = deXuatTraGiaRepository.countByGianHang_MaGianHang(maGianHang);
        long choDuyet = deXuatTraGiaRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "CHO_DUYET");
        long daDongY = deXuatTraGiaRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "DONG_Y");
        long tuChoi = deXuatTraGiaRepository.countByGianHang_MaGianHangAndTrangThai(maGianHang, "TU_CHOI");

        double tyLe = 0.0;
        if (tongDeXuat > 0) {
            tyLe = Math.round(((double) daDongY / tongDeXuat * 100.0) * 10.0) / 10.0;
        }

        return ThongKeChatDTO.builder()
                .tongHoiThoai(tongHoiThoai)
                .tongTinChuaDoc(tongTinChuaDoc)
                .tongDeXuatTraGia(tongDeXuat)
                .deXuatChoDuyet(choDuyet)
                .deXuatDaDongY(daDongY)
                .deXuatTuChoi(tuChoi)
                .tyLeDongYPhanTram(tyLe)
                .build();
    }

    /**
     * Phát sóng gói tin qua WebSocket Handler
     */
    private void phatSongWebSocket(Long maCuocTroChuyen, String suKien, TinNhan tinNhan, Object duLieuThem) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("suKien", suKien);
            message.put("maCuocTroChuyen", maCuocTroChuyen);
            message.put("maTinNhan", tinNhan.getMaTinNhan());
            message.put("loaiNguoiGui", tinNhan.getLoaiNguoiGui());
            message.put("loaiTinNhan", tinNhan.getLoaiTinNhan());
            message.put("noiDung", tinNhan.getNoiDung());
            message.put("duLieuDinhKemJson", tinNhan.getDuLieuDinhKemJson());
            message.put("thoiGian", tinNhan.getNgayTao().toString());
            message.put("duLieuThem", duLieuThem);

            chatWebSocketHandler.broadcast(maCuocTroChuyen, message);
        } catch (Exception e) {
            log.error("Lỗi khi phát sóng WebSocket cho cuộc trò chuyện #{}: ", maCuocTroChuyen, e);
        }
    }
}
