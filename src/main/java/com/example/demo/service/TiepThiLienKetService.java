package com.example.demo.service;

import com.example.demo.dto.AffiliateThongKeDTO;
import com.example.demo.dto.GhiNhanDonAffiliateRequestDTO;
import com.example.demo.dto.TaoLinkAffiliateRequestDTO;
import com.example.demo.entity.DonHangShop;
import com.example.demo.entity.DonHangTiepThi;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.SanPham;
import com.example.demo.entity.TiepThiLienKet;
import com.example.demo.repository.DonHangShopRepository;
import com.example.demo.repository.DonHangTiepThiRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.SanPhamRepository;
import com.example.demo.repository.TiepThiLienKetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & TIẾP THỊ NỘI SÀN (DEV 5 - MINH)
 * USER STORY: US-65 - Service Xử Lý Nghiệp Vụ Tiếp Thị Liên Kết (Affiliate)
 * =====================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TiepThiLienKetService {

    private final TiepThiLienKetRepository tiepThiLienKetRepository;
    private final DonHangTiepThiRepository donHangTiepThiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final SanPhamRepository sanPhamRepository;
    private final DonHangShopRepository donHangShopRepository;

    /**
     * US-65: Khởi tạo Liên Kết Tiếp Thị Độc Quyền cho KOC
     */
    @Transactional
    public TiepThiLienKet taoMoiLinkAffiliate(TaoLinkAffiliateRequestDTO dto) {
        dto.validate();

        NguoiDung koc = nguoiDungRepository.findById(dto.getMaKoc())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin KOC với ID: " + dto.getMaKoc()));

        SanPham sanPham = sanPhamRepository.findById(dto.getMaSanPham())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + dto.getMaSanPham()));

        // Sinh mã link ngẫu nhiên duy nhất
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String maLink = "AFF-KOC" + koc.getMaNguoiDung() + "-SP" + sanPham.getMaSanPham() + "-" + randomSuffix;

        // Đảm bảo không trùng lặp mã link
        while (tiepThiLienKetRepository.existsByMaLinkAffiliate(maLink)) {
            randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            maLink = "AFF-KOC" + koc.getMaNguoiDung() + "-SP" + sanPham.getMaSanPham() + "-" + randomSuffix;
        }

        TiepThiLienKet tiepThi = new TiepThiLienKet();
        tiepThi.setKoc(koc);
        tiepThi.setSanPham(sanPham);
        tiepThi.setMaLinkAffiliate(maLink);
        tiepThi.setTyLeHoaHongPhanTram(dto.getTyLeHoaHongPhanTram());
        tiepThi.setTongHoaHongKiemDuoc(BigDecimal.ZERO);
        tiepThi.setNgayTao(LocalDateTime.now());

        TiepThiLienKet saved = tiepThiLienKetRepository.save(tiepThi);
        log.info("[US-65] Đã tạo thành công Link Affiliate: {} cho KOC: {} - Sản phẩm: {}", 
                maLink, koc.getHoVaTen(), sanPham.getTenSanPham());
        return saved;
    }

    /**
     * US-65: Ghi Nhận Đơn Hàng Tiếp Thị (Attribution) & Tính Toán Hoa Hồng
     */
    @Transactional
    public DonHangTiepThi ghiNhanDonHangTiepThi(GhiNhanDonAffiliateRequestDTO dto) {
        dto.validate();

        TiepThiLienKet link = tiepThiLienKetRepository.findByMaLinkAffiliate(dto.getMaLinkAffiliate())
                .orElseThrow(() -> new IllegalArgumentException("Mã link tiếp thị [" + dto.getMaLinkAffiliate() + "] không tồn tại!"));

        DonHangShop donHangShop = donHangShopRepository.findById(dto.getMaDonHangShop())
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng shop ID [" + dto.getMaDonHangShop() + "] không tồn tại!"));

        if (donHangTiepThiRepository.existsByDonHangShop_MaDonHangShop(dto.getMaDonHangShop())) {
            throw new IllegalStateException("Đơn hàng [" + donHangShop.getMaCodeDonShop() + "] đã được ghi nhận tiếp thị liên kết trước đó!");
        }

        // Tính hoa hồng = Tiền hàng shop * (Tỷ lệ hoa hồng / 100)
        BigDecimal tienHang = donHangShop.getTienHangShop();
        BigDecimal tyLe = link.getTyLeHoaHongPhanTram();
        BigDecimal hoaHong = tienHang.multiply(tyLe).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        DonHangTiepThi donAffiliate = new DonHangTiepThi();
        donAffiliate.setTiepThiLienKet(link);
        donAffiliate.setDonHangShop(donHangShop);
        donAffiliate.setHoaHongDuocNhan(hoaHong);
        donAffiliate.setTrangThai("CHO_DOI_SOAT");
        donAffiliate.setNgayGhiNhan(LocalDateTime.now());

        DonHangTiepThi saved = donHangTiepThiRepository.save(donAffiliate);
        log.info("[US-65] Đã ghi nhận đơn tiếp thị: Mã đơn shop = {}, Hoa hồng = {} VNĐ, Trạng thái = CHO_DOI_SOAT",
                donHangShop.getMaCodeDonShop(), hoaHong);
        return saved;
    }

    /**
     * US-65: Duyệt Đối Soát & Giải Ngân Hoa Hồng Cho KOC
     */
    @Transactional
    public DonHangTiepThi duyetHoaHong(Long maDonAffiliate) {
        DonHangTiepThi don = donHangTiepThiRepository.findById(maDonAffiliate)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn tiếp thị ID: " + maDonAffiliate));

        if (!"CHO_DOI_SOAT".equalsIgnoreCase(don.getTrangThai())) {
            throw new IllegalStateException("Đơn tiếp thị đang ở trạng thái [" + don.getTenTrangThaiTiengViet() + 
                    "], chỉ có thể duyệt đối soát khi ở trạng thái 'Chờ đối soát'!");
        }

        don.setTrangThai("DA_DUYET");

        // Cộng dồn hoa hồng vào link tiếp thị
        TiepThiLienKet link = don.getTiepThiLienKet();
        BigDecimal tongMoi = link.getTongHoaHongKiemDuoc().add(don.getHoaHongDuocNhan());
        link.setTongHoaHongKiemDuoc(tongMoi);
        tiepThiLienKetRepository.save(link);

        DonHangTiepThi saved = donHangTiepThiRepository.save(don);
        log.info("[US-65] Đã duyệt đối soát đơn tiếp thị ID: {}, Cộng {} VNĐ hoa hồng cho KOC: {}",
                maDonAffiliate, don.getHoaHongDuocNhan(), link.getKoc().getHoVaTen());
        return saved;
    }

    /**
     * US-65: Hủy Ghi Nhận Hoa Hồng (Khi đơn hàng bị hủy hoặc hoàn trả/gian lận)
     */
    @Transactional
    public DonHangTiepThi huyHoaHong(Long maDonAffiliate) {
        DonHangTiepThi don = donHangTiepThiRepository.findById(maDonAffiliate)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn tiếp thị ID: " + maDonAffiliate));

        if ("DA_HUY".equalsIgnoreCase(don.getTrangThai())) {
            throw new IllegalStateException("Đơn tiếp thị này đã bị hủy trước đó!");
        }

        // Nếu trước đó đã duyệt thì phải trừ lại tổng hoa hồng của link
        if ("DA_DUYET".equalsIgnoreCase(don.getTrangThai())) {
            TiepThiLienKet link = don.getTiepThiLienKet();
            BigDecimal tongMoi = link.getTongHoaHongKiemDuoc().subtract(don.getHoaHongDuocNhan());
            if (tongMoi.compareTo(BigDecimal.ZERO) < 0) {
                tongMoi = BigDecimal.ZERO;
            }
            link.setTongHoaHongKiemDuoc(tongMoi);
            tiepThiLienKetRepository.save(link);
        }

        don.setTrangThai("DA_HUY");
        DonHangTiepThi saved = donHangTiepThiRepository.save(don);
        log.warn("[US-65] Đã hủy hoa hồng đơn tiếp thị ID: {}", maDonAffiliate);
        return saved;
    }

    /**
     * US-65: Lấy Thống Kê Tổng Hợp Cho KOC hoặc Toàn Hệ Thống
     */
    @Transactional(readOnly = true)
    public AffiliateThongKeDTO getThongKeAffiliate(Long maKoc) {
        if (maKoc != null && maKoc > 0) {
            int soLink = tiepThiLienKetRepository.countByKoc_MaNguoiDung(maKoc);
            int soDon = donHangTiepThiRepository.countByTiepThiLienKet_Koc_MaNguoiDung(maKoc);
            BigDecimal choDoiSoat = donHangTiepThiRepository.tinhTongHoaHongTheoTrangThaiVaKoc(maKoc, "CHO_DOI_SOAT");
            BigDecimal daDuyet = donHangTiepThiRepository.tinhTongHoaHongTheoTrangThaiVaKoc(maKoc, "DA_DUYET");
            BigDecimal doanhThu = donHangTiepThiRepository.tinhTongDoanhThuDonHangCuaKoc(maKoc);

            return AffiliateThongKeDTO.builder()
                    .tongSoLink(soLink)
                    .tongDonHang(soDon)
                    .tongHoaHongChoDoiSoat(choDoiSoat != null ? choDoiSoat : BigDecimal.ZERO)
                    .tongHoaHongDaDuyet(daDuyet != null ? daDuyet : BigDecimal.ZERO)
                    .tongDoanhThuDonHang(doanhThu != null ? doanhThu : BigDecimal.ZERO)
                    .build();
        } else {
            List<TiepThiLienKet> allLinks = tiepThiLienKetRepository.findAll();
            List<DonHangTiepThi> allDons = donHangTiepThiRepository.findAll();

            BigDecimal choDoiSoat = donHangTiepThiRepository.tinhTongHoaHongHeThongTheoTrangThai("CHO_DOI_SOAT");
            BigDecimal daDuyet = donHangTiepThiRepository.tinhTongHoaHongHeThongTheoTrangThai("DA_DUYET");

            BigDecimal doanhThu = allDons.stream()
                    .filter(d -> !"DA_HUY".equalsIgnoreCase(d.getTrangThai()))
                    .map(d -> d.getDonHangShop().getTienHangShop())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return AffiliateThongKeDTO.builder()
                    .tongSoLink(allLinks.size())
                    .tongDonHang(allDons.size())
                    .tongHoaHongChoDoiSoat(choDoiSoat != null ? choDoiSoat : BigDecimal.ZERO)
                    .tongHoaHongDaDuyet(daDuyet != null ? daDuyet : BigDecimal.ZERO)
                    .tongDoanhThuDonHang(doanhThu)
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public List<TiepThiLienKet> getDanhSachLink(Long maKoc) {
        if (maKoc != null && maKoc > 0) {
            return tiepThiLienKetRepository.findByKoc_MaNguoiDungOrderByNgayTaoDesc(maKoc);
        }
        return tiepThiLienKetRepository.findAllByOrderByNgayTaoDesc();
    }

    @Transactional(readOnly = true)
    public List<DonHangTiepThi> getDanhSachDonHang(Long maKoc, String trangThai) {
        if (trangThai != null && !trangThai.trim().isEmpty() && !"ALL".equalsIgnoreCase(trangThai)) {
            if (maKoc != null && maKoc > 0) {
                return donHangTiepThiRepository.findByTiepThiLienKet_Koc_MaNguoiDungAndTrangThaiOrderByNgayGhiNhanDesc(maKoc, trangThai);
            }
            return donHangTiepThiRepository.findByTrangThaiOrderByNgayGhiNhanDesc(trangThai);
        }

        if (maKoc != null && maKoc > 0) {
            return donHangTiepThiRepository.findByTiepThiLienKet_Koc_MaNguoiDungOrderByNgayGhiNhanDesc(maKoc);
        }
        return donHangTiepThiRepository.findAllByOrderByNgayGhiNhanDesc();
    }

    @Transactional(readOnly = true)
    public TiepThiLienKet getChiTietLink(Long maAffiliate) {
        return tiepThiLienKetRepository.findById(maAffiliate)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy liên kết tiếp thị với ID: " + maAffiliate));
    }
}
