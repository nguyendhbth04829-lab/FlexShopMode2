package com.example.demo.service;

import com.example.demo.dto.ApDungVoucherRequestDTO;
import com.example.demo.dto.KetQuaApDungVoucherDTO;
import com.example.demo.dto.KetQuaPhanBoShopDTO;
import com.example.demo.dto.ThongKeVoucherDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.DonHangTongRepository;
import com.example.demo.repository.LichSuDungMaGiamGiaRepository;
import com.example.demo.repository.MaGiamGiaRepository;
import com.example.demo.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service xử lý Thuật toán Voucher lồng nhau (Stackable Vouchers)
 * Áp dụng đồng thời 3 tầng: Freeship Sàn + Voucher Sàn + Voucher Shop (US-52 - VOUCHER).
 * Bảo toàn dòng tiền tài chính 100% giữa Sàn FlexShop, Gian hàng (Shop) và Khách hàng.
 */
@Service
public class VoucherService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale LOCALE_VN = new Locale("vi", "VN");

    @Autowired
    private MaGiamGiaRepository maGiamGiaRepository;

    @Autowired
    private LichSuDungMaGiamGiaRepository lichSuDungMaGiamGiaRepository;

    @Autowired
    private DonHangTongRepository donHangTongRepository;

    @Autowired
    private DonHangShopRepository donHangShopRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    /**
     * Định dạng tiền VND đẹp mắt
     */
    public String dinhDangTien(BigDecimal soTien) {
        if (soTien == null) return "0 đ";
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_VN);
        return nf.format(soTien) + " đ";
    }

    /**
     * Thuật toán Voucher Lồng Nhau (Stackable Vouchers) 3 Tầng:
     * - Tầng 1: Voucher Shop (Từng gian hàng) -> 100% Shop tài trợ
     * - Tầng 2: Voucher Sàn (Toàn đơn hàng) -> 100% Sàn FlexShop tài trợ
     * - Tầng 3: Freeship Sàn (Giảm phí giao hàng) -> 100% Sàn FlexShop tài trợ
     *
     * @param request Yêu cầu áp dụng voucher kèm mã đơn hàng và các mã nhập vào
     * @param maKhachHang Mã khách hàng thực hiện checkout
     * @return Kết quả chi tiết tính toán và phân bổ tài chính
     */
    @Transactional(readOnly = true)
    public KetQuaApDungVoucherDTO tinhToanVoucherStackable(ApDungVoucherRequestDTO request, Long maKhachHang) {
        if (request == null || request.getMaDonHangTong() == null) {
            KetQuaApDungVoucherDTO ketQuaLoi = new KetQuaApDungVoucherDTO();
            ketQuaLoi.setHopLe(false);
            ketQuaLoi.getDanhSachLoi().add("Vui lòng chọn đơn hàng cần áp dụng mã giảm giá!");
            return ketQuaLoi;
        }

        DonHangTong donHangTong = donHangTongRepository.findById(request.getMaDonHangTong()).orElse(null);
        if (donHangTong == null) {
            KetQuaApDungVoucherDTO ketQuaLoi = new KetQuaApDungVoucherDTO();
            ketQuaLoi.setHopLe(false);
            ketQuaLoi.getDanhSachLoi().add("Không tìm thấy đơn hàng mã #" + request.getMaDonHangTong());
            return ketQuaLoi;
        }

        List<DonHangShop> danhSachDonShop = donHangShopRepository.findByDonHangTongMaDonHangTong(donHangTong.getMaDonHangTong());
        if (danhSachDonShop.isEmpty()) {
            KetQuaApDungVoucherDTO ketQuaLoi = new KetQuaApDungVoucherDTO();
            ketQuaLoi.setHopLe(false);
            ketQuaLoi.getDanhSachLoi().add("Đơn hàng tổng không chứa bất kỳ đơn hàng shop nào!");
            return ketQuaLoi;
        }

        LocalDateTime thoiDiemHienTai = LocalDateTime.now();
        List<String> danhSachLoi = new ArrayList<>();
        List<String> danhSachThongBao = new ArrayList<>();

        BigDecimal tongTienHangGoc = donHangTong.getTongTienHang();
        BigDecimal tongPhiVanChuyenGoc = donHangTong.getTongPhiVanChuyen();

        // ---------------------------------------------------------------------
        // TẦNG 1: VOUCHER GIAN HÀNG (SHOP VOUCHER)
        // ---------------------------------------------------------------------
        Map<Long, BigDecimal> giamGiaShopMap = new HashMap<>(); // maDonHangShop -> soTienGiam
        Map<Long, MaGiamGia> voucherShopMap = new HashMap<>();   // maDonHangShop -> MaGiamGia
        BigDecimal tongGiamGiaShop = BigDecimal.ZERO;

        for (DonHangShop donShop : danhSachDonShop) {
            Long maShop = (donShop.getGianHang() != null) ? donShop.getGianHang().getMaGianHang() : null;
            String maCode = null;
            if (request.getMaVoucherShopMap() != null) {
                // Hỗ trợ truyền theo maGianHang hoặc theo maDonHangShop
                if (maShop != null && request.getMaVoucherShopMap().containsKey(maShop)) {
                    maCode = request.getMaVoucherShopMap().get(maShop);
                } else if (request.getMaVoucherShopMap().containsKey(donShop.getMaDonHangShop())) {
                    maCode = request.getMaVoucherShopMap().get(donShop.getMaDonHangShop());
                }
            }

            if (StringUtils.hasText(maCode)) {
                String codeTrimmed = maCode.trim().toUpperCase();
                Optional<MaGiamGia> optVoucher = maGiamGiaRepository.findByMaCodeVoucherIgnoreCaseAndDaXoaFalse(codeTrimmed);

                if (optVoucher.isEmpty()) {
                    danhSachLoi.add("Mã voucher shop [" + codeTrimmed + "] không tồn tại trên hệ thống!");
                    continue;
                }

                MaGiamGia vShop = optVoucher.get();

                // Validate quyền gian hàng
                if (vShop.laVoucherSan()) {
                    danhSachLoi.add("Mã [" + codeTrimmed + "] là Voucher Sàn FlexShop, vui lòng nhập vào ô Voucher Sàn!");
                    continue;
                }
                if (vShop.getGianHang() == null || !vShop.getGianHang().getMaGianHang().equals(maShop)) {
                    String tenShopDung = (vShop.getGianHang() != null) ? vShop.getGianHang().getTenGianHang() : "Shop khác";
                    String tenShopDon = (donShop.getGianHang() != null) ? donShop.getGianHang().getTenGianHang() : "Shop hiện tại";
                    danhSachLoi.add("Mã [" + codeTrimmed + "] là voucher của " + tenShopDung + ", không thể áp dụng cho gian hàng " + tenShopDon + "!");
                    continue;
                }

                // Validate trạng thái hoạt động & thời hạn
                kiemTraHieuLucVoucher(vShop, thoiDiemHienTai, maKhachHang, danhSachLoi, "Voucher Shop [" + codeTrimmed + "]");

                // Validate giá trị đơn hàng tối thiểu của Shop
                if (donShop.getTienHangShop().compareTo(vShop.getGiaTriDonToiThieu()) < 0) {
                    danhSachLoi.add("Gian hàng " + donShop.getGianHang().getTenGianHang() + " chưa đạt giá trị tối thiểu " +
                            dinhDangTien(vShop.getGiaTriDonToiThieu()) + " để áp dụng mã [" + codeTrimmed + "] (Hiện có: " +
                            dinhDangTien(donShop.getTienHangShop()) + ")!");
                    continue;
                }

                // Tính giá trị giảm tầng 1 (Shop voucher)
                BigDecimal giamShop = tinhGiaTriGiamTheoLoai(vShop, donShop.getTienHangShop());
                // Chặn tối đa không vượt quá giá trị tiền hàng shop
                giamShop = giamShop.min(donShop.getTienHangShop());

                giamGiaShopMap.put(donShop.getMaDonHangShop(), giamShop);
                voucherShopMap.put(donShop.getMaDonHangShop(), vShop);
                tongGiamGiaShop = tongGiamGiaShop.add(giamShop);

                danhSachThongBao.add("Áp dụng thành công Voucher Shop [" + codeTrimmed + "] của gian hàng " +
                        donShop.getGianHang().getTenGianHang() + ": Giảm " + dinhDangTien(giamShop));
            }
        }

        // ---------------------------------------------------------------------
        // TẦNG 2: VOUCHER SÀN FLEXSHOP (PLATFORM CASH / PERCENTAGE VOUCHER)
        // ---------------------------------------------------------------------
        BigDecimal giamGiaVoucherSan = BigDecimal.ZERO;
        String codeVoucherSan = null;
        String tenVoucherSan = null;
        MaGiamGia voucherSanEntity = null;

        if (StringUtils.hasText(request.getMaVoucherSan())) {
            codeVoucherSan = request.getMaVoucherSan().trim().toUpperCase();
            Optional<MaGiamGia> optSan = maGiamGiaRepository.findByMaCodeVoucherIgnoreCaseAndDaXoaFalse(codeVoucherSan);

            if (optSan.isEmpty()) {
                danhSachLoi.add("Mã Voucher Sàn [" + codeVoucherSan + "] không tồn tại!");
            } else {
                MaGiamGia vSan = optSan.get();

                if (vSan.laVoucherShop()) {
                    danhSachLoi.add("Mã [" + codeVoucherSan + "] là voucher của gian hàng " +
                            vSan.getGianHang().getTenGianHang() + ", vui lòng nhập vào mục Voucher Shop!");
                } else if (vSan.laVoucherFreeship()) {
                    danhSachLoi.add("Mã [" + codeVoucherSan + "] là mã miễn phí vận chuyển, vui lòng nhập vào mục Mã Freeship Sàn!");
                } else {
                    kiemTraHieuLucVoucher(vSan, thoiDiemHienTai, maKhachHang, danhSachLoi, "Voucher Sàn [" + codeVoucherSan + "]");

                    // Validate đơn tối thiểu toàn sàn
                    if (tongTienHangGoc.compareTo(vSan.getGiaTriDonToiThieu()) < 0) {
                        danhSachLoi.add("Tổng đơn hàng chưa đạt giá trị tối thiểu " +
                                dinhDangTien(vSan.getGiaTriDonToiThieu()) + " để dùng Voucher Sàn [" + codeVoucherSan + "] (Hiện có: " +
                                dinhDangTien(tongTienHangGoc) + ")!");
                    } else {
                        // Tính giảm giá sàn (chặn tối đa bằng tổng tiền hàng còn lại sau khi trừ voucher shop)
                        BigDecimal tienHangConLaiSauShop = tongTienHangGoc.subtract(tongGiamGiaShop).max(BigDecimal.ZERO);
                        BigDecimal giamSan = tinhGiaTriGiamTheoLoai(vSan, tongTienHangGoc);
                        giamGiaVoucherSan = giamSan.min(tienHangConLaiSauShop);
                        tenVoucherSan = vSan.getTenVoucher();
                        voucherSanEntity = vSan;

                        danhSachThongBao.add("Áp dụng thành công Voucher Sàn FlexShop [" + codeVoucherSan + "]: Sàn tài trợ giảm " +
                                dinhDangTien(giamGiaVoucherSan));
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // TẦNG 3: MÃ FREESHIP SÀN (PLATFORM SHIPPING VOUCHER)
        // ---------------------------------------------------------------------
        BigDecimal giamGiaFreeshipSan = BigDecimal.ZERO;
        String codeFreeshipSan = null;
        String tenFreeshipSan = null;
        MaGiamGia voucherFreeshipEntity = null;

        if (StringUtils.hasText(request.getMaFreeshipSan())) {
            codeFreeshipSan = request.getMaFreeshipSan().trim().toUpperCase();
            Optional<MaGiamGia> optShip = maGiamGiaRepository.findByMaCodeVoucherIgnoreCaseAndDaXoaFalse(codeFreeshipSan);

            if (optShip.isEmpty()) {
                danhSachLoi.add("Mã Freeship Sàn [" + codeFreeshipSan + "] không tồn tại!");
            } else {
                MaGiamGia vShip = optShip.get();

                if (vShip.laVoucherShop()) {
                    danhSachLoi.add("Mã [" + codeFreeshipSan + "] là voucher của gian hàng " +
                            vShip.getGianHang().getTenGianHang() + ", không phải mã Freeship!");
                } else if (!vShip.laVoucherFreeship()) {
                    danhSachLoi.add("Mã [" + codeFreeshipSan + "] không phải là mã Freeship (là mã giảm giá tiền hàng), vui lòng nhập vào mục Voucher Sàn!");
                } else {
                    kiemTraHieuLucVoucher(vShip, thoiDiemHienTai, maKhachHang, danhSachLoi, "Mã Freeship Sàn [" + codeFreeshipSan + "]");

                    // Validate đơn tối thiểu toàn sàn cho Freeship
                    if (tongTienHangGoc.compareTo(vShip.getGiaTriDonToiThieu()) < 0) {
                        danhSachLoi.add("Tổng đơn hàng chưa đạt giá trị tối thiểu " +
                                dinhDangTien(vShip.getGiaTriDonToiThieu()) + " để hưởng Freeship [" + codeFreeshipSan + "] (Hiện có: " +
                                dinhDangTien(tongTienHangGoc) + ")!");
                    } else {
                        // Tính tiền Freeship giảm (chặn tối đa bằng tổng phí vận chuyển gốc)
                        BigDecimal giamShip = vShip.getGiaTriGiam();
                        if (vShip.getGiamToiDa() != null && giamShip.compareTo(vShip.getGiamToiDa()) > 0) {
                            giamShip = vShip.getGiamToiDa();
                        }
                        giamGiaFreeshipSan = giamShip.min(tongPhiVanChuyenGoc);
                        tenFreeshipSan = vShip.getTenVoucher();
                        voucherFreeshipEntity = vShip;

                        danhSachThongBao.add("Áp dụng thành công Mã Freeship Sàn [" + codeFreeshipSan + "]: Sàn FlexShop miễn giảm " +
                                dinhDangTien(giamGiaFreeshipSan) + " phí vận chuyển");
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // PHÂN BỔ TÀI CHÍNH CHI TIẾT TỪNG SHOP & BẢO TOÀN DÒNG TIỀN
        // ---------------------------------------------------------------------
        List<KetQuaPhanBoShopDTO> danhSachPhanBoShop = new ArrayList<>();
        BigDecimal tongGiamSanDaPhanBo = BigDecimal.ZERO;
        KetQuaPhanBoShopDTO shopLonNhat = null;
        BigDecimal tienHangLonNhat = BigDecimal.ZERO;

        for (DonHangShop donShop : danhSachDonShop) {
            BigDecimal giamShop = giamGiaShopMap.getOrDefault(donShop.getMaDonHangShop(), BigDecimal.ZERO);
            String codeShop = null;
            if (voucherShopMap.containsKey(donShop.getMaDonHangShop())) {
                codeShop = voucherShopMap.get(donShop.getMaDonHangShop()).getMaCodeVoucher();
            }

            // Phân bổ Voucher Sàn theo tỷ trọng tiền hàng của shop
            BigDecimal giamSanChoShop = BigDecimal.ZERO;
            if (giamGiaVoucherSan.compareTo(BigDecimal.ZERO) > 0 && tongTienHangGoc.compareTo(BigDecimal.ZERO) > 0) {
                giamSanChoShop = giamGiaVoucherSan.multiply(donShop.getTienHangShop())
                        .divide(tongTienHangGoc, 0, RoundingMode.HALF_UP);
            }
            tongGiamSanDaPhanBo = tongGiamSanDaPhanBo.add(giamSanChoShop);

            BigDecimal tienHangSauShop = donShop.getTienHangShop().subtract(giamShop).max(BigDecimal.ZERO);
            // Tiền shop nhận = Tiền hàng gốc - Voucher Shop (Voucher Sàn được Sàn tài trợ bù cho Shop)
            BigDecimal tongTienShopNhan = donShop.getTienHangShop().subtract(giamShop);

            KetQuaPhanBoShopDTO phanBoDTO = KetQuaPhanBoShopDTO.builder()
                    .maDonHangShop(donShop.getMaDonHangShop())
                    .maGianHang(donShop.getGianHang() != null ? donShop.getGianHang().getMaGianHang() : null)
                    .tenGianHang(donShop.getGianHang() != null ? donShop.getGianHang().getTenGianHang() : "Gian hàng")
                    .tienHangGoc(donShop.getTienHangShop())
                    .phiVanChuyen(donShop.getPhiVanChuyen())
                    .codeVoucherShop(codeShop)
                    .giamGiaVoucherShop(giamShop)
                    .giamGiaVoucherSan(giamSanChoShop)
                    .tienHangSauVoucherShop(tienHangSauShop)
                    .tongTienShopNhan(tongTienShopNhan)
                    .ghiChu("Shop tài trợ " + dinhDangTien(giamShop) + ", Sàn tài trợ bù " + dinhDangTien(giamSanChoShop))
                    .build();

            danhSachPhanBoShop.add(phanBoDTO);

            if (donShop.getTienHangShop().compareTo(tienHangLonNhat) > 0) {
                tienHangLonNhat = donShop.getTienHangShop();
                shopLonNhat = phanBoDTO;
            }
        }

        // Cân bằng sai số làm tròn phân bổ voucher sàn vào shop lớn nhất
        if (shopLonNhat != null && giamGiaVoucherSan.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal chenhLech = giamGiaVoucherSan.subtract(tongGiamSanDaPhanBo);
            if (chenhLech.compareTo(BigDecimal.ZERO) != 0) {
                shopLonNhat.setGiamGiaVoucherSan(shopLonNhat.getGiamGiaVoucherSan().add(chenhLech));
            }
        }

        // ---------------------------------------------------------------------
        // TỔNG KẾT VÀ KIỂM ĐỊNH BẢO TOÀN TÀI CHÍNH 100%
        // ---------------------------------------------------------------------
        BigDecimal tongTaiTroSan = giamGiaVoucherSan.add(giamGiaFreeshipSan);
        BigDecimal tongTaiTroShop = tongGiamGiaShop;
        BigDecimal tongTietKiem = tongTaiTroSan.add(tongTaiTroShop);

        BigDecimal phiVanChuyenKhachTra = tongPhiVanChuyenGoc.subtract(giamGiaFreeshipSan).max(BigDecimal.ZERO);
        BigDecimal tienHangKhachTra = tongTienHangGoc.subtract(tongGiamGiaShop).subtract(giamGiaVoucherSan).max(BigDecimal.ZERO);
        BigDecimal tongThanhToanCuoi = tienHangKhachTra.add(phiVanChuyenKhachTra);

        // Kiểm tra bảo toàn dòng tiền:
        // Khách thực trả + Sàn tài trợ + Shop tài trợ == Tổng tiền hàng gốc + Tổng phí ship gốc
        BigDecimal tongGoc = tongTienHangGoc.add(tongPhiVanChuyenGoc);
        BigDecimal tongKhachTraVaTroGia = tongThanhToanCuoi.add(tongTaiTroSan).add(tongTaiTroShop);
        boolean baoToanTaiChinh = (tongKhachTraVaTroGia.compareTo(tongGoc) == 0);

        boolean hopLe = danhSachLoi.isEmpty();

        return KetQuaApDungVoucherDTO.builder()
                .hopLe(hopLe)
                .danhSachLoi(danhSachLoi)
                .danhSachThongBao(danhSachThongBao)
                .tongTienHangGoc(tongTienHangGoc)
                .tongPhiVanChuyenGoc(tongPhiVanChuyenGoc)
                .codeFreeshipSan(codeFreeshipSan)
                .tenFreeshipSan(tenFreeshipSan)
                .giamGiaFreeshipSan(giamGiaFreeshipSan)
                .codeVoucherSan(codeVoucherSan)
                .tenVoucherSan(tenVoucherSan)
                .giamGiaVoucherSan(giamGiaVoucherSan)
                .tongGiamGiaShop(tongGiamGiaShop)
                .tongTaiTroSan(tongTaiTroSan)
                .tongTaiTroShop(tongTaiTroShop)
                .tongTietKiem(tongTietKiem)
                .phiVanChuyenKhachTra(phiVanChuyenKhachTra)
                .tongThanhToanCuoi(tongThanhToanCuoi)
                .baoToanTaiChinh(baoToanTaiChinh)
                .danhSachPhanBoShop(danhSachPhanBoShop)
                .build();
    }

    /**
     * Xác nhận chốt đơn & áp dụng đồng thời các tầng voucher:
     * - Cập nhật DonHangTong (tongGiamGiaSan, tongGiamGiaShop, tongThanhToanCuoi)
     * - Cập nhật DonHangShop (giamGiaVoucherShop, giamGiaVoucherSan, tongTienShopNhan)
     * - Lưu bảng lich_su_dung_ma_giam_gia cho từng voucher đã áp dụng
     * - Tăng so_luong_da_dung của từng voucher
     */
    @Transactional
    public KetQuaApDungVoucherDTO xacNhanApDungVoucherChoDonHang(ApDungVoucherRequestDTO request, Long maKhachHang) {
        KetQuaApDungVoucherDTO ketQua = tinhToanVoucherStackable(request, maKhachHang);
        if (!ketQua.isHopLe()) {
            throw new IllegalArgumentException(String.join("; ", ketQua.getDanhSachLoi()));
        }

        DonHangTong donHangTong = donHangTongRepository.findById(request.getMaDonHangTong())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + request.getMaDonHangTong()));
        NguoiDung khachHang = nguoiDungRepository.findById(maKhachHang)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng #" + maKhachHang));

        // 1. Cập nhật DonHangTong
        donHangTong.setTongGiamGiaSan(ketQua.getTongTaiTroSan());
        donHangTong.setTongGiamGiaShop(ketQua.getTongTaiTroShop());
        donHangTong.setTongThanhToanCuoi(ketQua.getTongThanhToanCuoi());
        donHangTongRepository.save(donHangTong);

        // 2. Cập nhật DonHangShop
        List<DonHangShop> danhSachDonShop = donHangShopRepository.findByDonHangTongMaDonHangTong(donHangTong.getMaDonHangTong());
        Map<Long, DonHangShop> donShopMap = new HashMap<>();
        for (DonHangShop ds : danhSachDonShop) {
            donShopMap.put(ds.getMaDonHangShop(), ds);
        }

        for (KetQuaPhanBoShopDTO phanBo : ketQua.getDanhSachPhanBoShop()) {
            DonHangShop ds = donShopMap.get(phanBo.getMaDonHangShop());
            if (ds != null) {
                ds.setGiamGiaVoucherShop(phanBo.getGiamGiaVoucherShop());
                ds.setGiamGiaVoucherSan(phanBo.getGiamGiaVoucherSan());
                ds.setTongTienShopNhan(phanBo.getTongTienShopNhan());
                donHangShopRepository.save(ds);

                // Nếu có voucher shop được dùng cho shop này -> ghi nhận lịch sử
                if (StringUtils.hasText(phanBo.getCodeVoucherShop()) && phanBo.getGiamGiaVoucherShop().compareTo(BigDecimal.ZERO) > 0) {
                    MaGiamGia vShop = maGiamGiaRepository.findByMaCodeVoucherIgnoreCaseAndDaXoaFalse(phanBo.getCodeVoucherShop()).orElse(null);
                    if (vShop != null) {
                        ghiNhanLichSuDung(vShop, khachHang, donHangTong, ds, phanBo.getGiamGiaVoucherShop());
                        tangLuotDaDungVoucher(vShop);
                    }
                }
            }
        }

        // 3. Ghi nhận lịch sử cho Voucher Sàn FlexShop (nếu có)
        if (StringUtils.hasText(ketQua.getCodeVoucherSan()) && ketQua.getGiamGiaVoucherSan().compareTo(BigDecimal.ZERO) > 0) {
            MaGiamGia vSan = maGiamGiaRepository.findByMaCodeVoucherIgnoreCaseAndDaXoaFalse(ketQua.getCodeVoucherSan()).orElse(null);
            if (vSan != null) {
                ghiNhanLichSuDung(vSan, khachHang, donHangTong, null, ketQua.getGiamGiaVoucherSan());
                tangLuotDaDungVoucher(vSan);
            }
        }

        // 4. Ghi nhận lịch sử cho Mã Freeship Sàn (nếu có)
        if (StringUtils.hasText(ketQua.getCodeFreeshipSan()) && ketQua.getGiamGiaFreeshipSan().compareTo(BigDecimal.ZERO) > 0) {
            MaGiamGia vShip = maGiamGiaRepository.findByMaCodeVoucherIgnoreCaseAndDaXoaFalse(ketQua.getCodeFreeshipSan()).orElse(null);
            if (vShip != null) {
                ghiNhanLichSuDung(vShip, khachHang, donHangTong, null, ketQua.getGiamGiaFreeshipSan());
                tangLuotDaDungVoucher(vShip);
            }
        }

        return ketQua;
    }

    /**
     * Ghi nhận 1 bản ghi vào bảng lich_su_dung_ma_giam_gia
     */
    private void ghiNhanLichSuDung(MaGiamGia voucher, NguoiDung khachHang, DonHangTong donTong, DonHangShop donShop, BigDecimal soTienDaGiam) {
        LichSuDungMaGiamGia ls = new LichSuDungMaGiamGia();
        ls.setVoucher(voucher);
        ls.setNguoiDung(khachHang);
        ls.setDonHangTong(donTong);
        ls.setDonHangShop(donShop);
        ls.setSoTienDaGiam(soTienDaGiam);
        ls.setNgaySuDung(LocalDateTime.now());
        lichSuDungMaGiamGiaRepository.save(ls);
    }

    /**
     * Tăng số lượng đã dùng của voucher
     */
    private void tangLuotDaDungVoucher(MaGiamGia voucher) {
        int hienTai = (voucher.getSoLuongDaDung() != null) ? voucher.getSoLuongDaDung() : 0;
        voucher.setSoLuongDaDung(hienTai + 1);
        maGiamGiaRepository.save(voucher);
    }

    /**
     * Hàm phụ trợ kiểm tra tính hiệu lực đa tầng của voucher
     */
    private void kiemTraHieuLucVoucher(MaGiamGia v, LocalDateTime thoiDiemHienTai, Long maKhachHang, List<String> danhSachLoi, String tenGoi) {
        // 1. Kiểm tra trạng thái hoạt động
        if (Boolean.FALSE.equals(v.getDangHoatDong()) || Boolean.TRUE.equals(v.getDaXoa())) {
            danhSachLoi.add(tenGoi + " hiện đang bị khóa hoặc tạm ngừng kích hoạt!");
            return;
        }

        // 2. Kiểm tra ngày bắt đầu
        if (thoiDiemHienTai.isBefore(v.getNgayBatDau())) {
            danhSachLoi.add(tenGoi + " chưa đến ngày mở áp dụng (Bắt đầu từ: " +
                    v.getNgayBatDau().format(DATE_FORMATTER) + ")!");
            return;
        }

        // 3. Kiểm tra ngày kết thúc
        if (thoiDiemHienTai.isAfter(v.getNgayKetThuc())) {
            danhSachLoi.add(tenGoi + " đã hết hạn sử dụng từ ngày " +
                    v.getNgayKetThuc().format(DATE_FORMATTER) + "!");
            return;
        }

        // 4. Kiểm tra tổng số lượng phát hành của hệ thống
        int soLuongDaDung = (v.getSoLuongDaDung() != null) ? v.getSoLuongDaDung() : 0;
        if (soLuongDaDung >= v.getTongSoLuongPhatHanh()) {
            danhSachLoi.add(tenGoi + " đã hết lượt sử dụng trên hệ thống!");
            return;
        }

        // 5. Kiểm tra giới hạn lượt dùng của khách hàng này
        if (maKhachHang != null) {
            long daDungCuaKhach = lichSuDungMaGiamGiaRepository.countByVoucherMaVoucherAndNguoiDungMaNguoiDung(v.getMaVoucher(), maKhachHang);
            int gioiHan = (v.getGioiHanMoiNguoi() != null && v.getGioiHanMoiNguoi() > 0) ? v.getGioiHanMoiNguoi() : 1;
            if (daDungCuaKhach >= gioiHan) {
                danhSachLoi.add("Bạn đã sử dụng hết số lượt cho phép (" + gioiHan + " lượt) của " + tenGoi + "!");
            }
        }
    }

    /**
     * Tính toán số tiền giảm theo loại voucher (GIAM_GIA hoặc PHAN_TRAM)
     */
    private BigDecimal tinhGiaTriGiamTheoLoai(MaGiamGia v, BigDecimal giaTriCoBan) {
        if (v == null || giaTriCoBan == null) return BigDecimal.ZERO;

        if ("GIAM_GIA".equalsIgnoreCase(v.getLoaiVoucher()) || "FREESHIP".equalsIgnoreCase(v.getLoaiVoucher())) {
            BigDecimal giam = v.getGiaTriGiam();
            if (v.getGiamToiDa() != null && giam.compareTo(v.getGiamToiDa()) > 0) {
                giam = v.getGiamToiDa();
            }
            return giam;
        } else if ("PHAN_TRAM".equalsIgnoreCase(v.getLoaiVoucher())) {
            BigDecimal phanTram = v.getGiaTriGiam();
            BigDecimal giam = giaTriCoBan.multiply(phanTram).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (v.getGiamToiDa() != null && giam.compareTo(v.getGiamToiDa()) > 0) {
                giam = v.getGiamToiDa();
            }
            return giam;
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy danh sách voucher trong Kho Voucher có phân trang, lọc theo tab và từ khóa
     */
    @Transactional(readOnly = true)
    public Page<MaGiamGia> layDanhSachKhoVoucher(String tab, String tuKhoa, int page, int size) {
        if (!StringUtils.hasText(tab)) tab = "TAT_CA";
        if (StringUtils.hasText(tuKhoa)) tuKhoa = tuKhoa.trim();
        Pageable pageable = PageRequest.of(page, size);
        return maGiamGiaRepository.timKiemKhoVoucher(tab, tuKhoa, pageable);
    }

    /**
     * Thống kê kho voucher và lịch sử tiết kiệm của khách hàng
     */
    @Transactional(readOnly = true)
    public ThongKeVoucherDTO layThongKeVoucher(Long maKhachHang) {
        long tongKhaDung = maGiamGiaRepository.countByDangHoatDongTrueAndDaXoaFalse();
        long tongSan = maGiamGiaRepository.demVoucherSanFlexShop();
        long tongShip = maGiamGiaRepository.demVoucherFreeshipSan();
        long tongShop = maGiamGiaRepository.demVoucherShop();

        BigDecimal tongTienTietKiem = (maKhachHang != null)
                ? lichSuDungMaGiamGiaRepository.tongTienTietKiemCuaKhachHang(maKhachHang)
                : BigDecimal.ZERO;
        long tongSoLan = (maKhachHang != null)
                ? lichSuDungMaGiamGiaRepository.countByNguoiDungMaNguoiDung(maKhachHang)
                : 0;

        return ThongKeVoucherDTO.builder()
                .tongVoucherKhaDung(tongKhaDung)
                .tongVoucherSan(tongSan)
                .tongVoucherFreeship(tongShip)
                .tongVoucherShop(tongShop)
                .tongTienTietKiemDuoc(tongTienTietKiem != null ? tongTienTietKiem : BigDecimal.ZERO)
                .tongSoLanSuDung(tongSoLan)
                .build();
    }

    /**
     * Lấy danh sách tất cả voucher khả dụng (Freeship, Sàn, Shop) để gợi ý chọn trên giao diện thanh toán
     */
    @Transactional(readOnly = true)
    public Map<String, List<MaGiamGia>> layDanhSachVoucherGoiYChoDonHang(Long maDonHangTong) {
        Map<String, List<MaGiamGia>> result = new HashMap<>();
        List<MaGiamGia> dsFreeship = new ArrayList<>();
        List<MaGiamGia> dsSan = new ArrayList<>();
        List<MaGiamGia> dsShop = new ArrayList<>();

        List<MaGiamGia> allActive = maGiamGiaRepository.findByDangHoatDongTrueAndDaXoaFalse();
        LocalDateTime now = LocalDateTime.now();

        for (MaGiamGia v : allActive) {
            int daDung = (v.getSoLuongDaDung() != null) ? v.getSoLuongDaDung() : 0;
            if (daDung < v.getTongSoLuongPhatHanh() && !now.isBefore(v.getNgayBatDau()) && !now.isAfter(v.getNgayKetThuc())) {
                if (v.laVoucherFreeship()) {
                    dsFreeship.add(v);
                } else if (v.laVoucherSan()) {
                    dsSan.add(v);
                } else {
                    dsShop.add(v);
                }
            }
        }

        result.put("FREESHIP", dsFreeship);
        result.put("SAN", dsSan);
        result.put("SHOP", dsShop);
        return result;
    }
}
