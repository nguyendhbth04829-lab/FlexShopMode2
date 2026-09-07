package com.example.demo.service;

import com.example.demo.dto.request.TuChoiGianHangRequest;
import com.example.demo.dto.response.GianHangResponse;
import com.example.demo.dto.response.PhanTrangResponse;
import com.example.demo.dto.response.ThongKeGianHangResponse;
import com.example.demo.entity.ChungChiGianHang;
import com.example.demo.entity.GianHang;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.VaiTro;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.ChungChiGianHangRepository;
import com.example.demo.repository.GianHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KiemDuyetGianHangService {

    private final GianHangRepository gianHangRepository;
    private final ChungChiGianHangRepository chungChiGianHangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository;

    /**
     * US-09: Phê duyệt (Approve) yêu cầu mở Shop của Seller
     * - Cập nhật trang_thai = HOAT_DONG trong gian_hang
     * - Cập nhật trang_thai_duyet = DA_DUYET trong chung_chi_gian_hang
     * - Thêm bản ghi gán ma_vai_tro (NGUOI_BAN) vào nguoi_dung_vai_tro cho chủ sở hữu
     */
    @Transactional
    public GianHangResponse pheDuyetGianHang(Long maGianHang) {
        GianHang gianHang = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy hồ sơ gian hàng mã: " + maGianHang, HttpStatus.NOT_FOUND));

        if ("HOAT_DONG".equalsIgnoreCase(gianHang.getTrangThai())) {
            throw new NgoaiLeUngDung("Gian hàng '" + gianHang.getTenGianHang() + "' đã được phê duyệt và đang hoạt động!", HttpStatus.BAD_REQUEST);
        }

        // 1. Cập nhật trạng thái gian hàng thành HOAT_DONG
        gianHang.setTrangThai("HOAT_DONG");
        gianHang.setLyDoTuChoi(null);
        GianHang gianHangSaved = gianHangRepository.save(gianHang);

        // 2. Cập nhật giấy phép kinh doanh đính kèm thành DA_DUYET
        ChungChiGianHang chungChi = chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(maGianHang)
                .orElse(null);
        if (chungChi != null) {
            chungChi.setTrangThaiDuyet("DA_DUYET");
            chungChi = chungChiGianHangRepository.save(chungChi);
        }

        // 3. Tìm chủ sở hữu và gán vai trò NGUOI_BAN vào nguoi_dung_vai_tro
        NguoiDung chuSoHuu = nguoiDungRepository.findById(gianHang.getMaChuSoHuu())
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy thông tin tài khoản chủ sở hữu!", HttpStatus.NOT_FOUND));

        VaiTro vaiTroNguoiBan = vaiTroRepository.findByTenVaiTro("NGUOI_BAN")
                .or(() -> vaiTroRepository.findByTenVaiTro("ROLE_NGUOI_BAN"))
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy vai trò NGUOI_BAN trong hệ thống!", HttpStatus.INTERNAL_SERVER_ERROR));

        chuSoHuu.getDanhSachVaiTro().add(vaiTroNguoiBan);
        nguoiDungRepository.save(chuSoHuu);

        log.info("Admin đã phê duyệt thành công gian hàng ID: {}, Tên: '{}'. Chủ sở hữu ID: {} đã được cấp vai trò NGUOI_BAN.",
                maGianHang, gianHangSaved.getTenGianHang(), chuSoHuu.getMaNguoiDung());

        return chuyenSangGianHangResponse(gianHangSaved, chungChi, chuSoHuu);
    }

    /**
     * US-09: Từ chối (Reject) yêu cầu mở Shop của Seller
     * - Cập nhật trang_thai = TU_CHOI trong gian_hang
     * - Ghi nhận bắt buộc ly_do_tu_choi từ Admin
     * - Cập nhật trang_thai_duyet = TU_CHOI trong chung_chi_gian_hang
     */
    @Transactional
    public GianHangResponse tuChoiGianHang(Long maGianHang, TuChoiGianHangRequest yeuCau) {
        if (yeuCau == null || yeuCau.getLyDoTuChoi() == null || yeuCau.getLyDoTuChoi().trim().length() < 5 || yeuCau.getLyDoTuChoi().trim().length() > 255) {
            throw new NgoaiLeUngDung("Lý do từ chối bắt buộc và phải có độ dài từ 5 đến 255 ký tự!", HttpStatus.BAD_REQUEST);
        }

        GianHang gianHang = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy hồ sơ gian hàng mã: " + maGianHang, HttpStatus.NOT_FOUND));

        if ("HOAT_DONG".equalsIgnoreCase(gianHang.getTrangThai())) {
            throw new NgoaiLeUngDung("Không thể từ chối gian hàng đang hoạt động chính thức!", HttpStatus.BAD_REQUEST);
        }

        String lyDo = yeuCau.getLyDoTuChoi().trim();

        // 1. Cập nhật trạng thái gian hàng thành TU_CHOI kèm lý do
        gianHang.setTrangThai("TU_CHOI");
        gianHang.setLyDoTuChoi(lyDo);
        GianHang gianHangSaved = gianHangRepository.save(gianHang);

        // 2. Cập nhật trạng thái giấy phép thành TU_CHOI
        ChungChiGianHang chungChi = chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(maGianHang)
                .orElse(null);
        if (chungChi != null) {
            chungChi.setTrangThaiDuyet("TU_CHOI");
            chungChi = chungChiGianHangRepository.save(chungChi);
        }

        NguoiDung chuSoHuu = nguoiDungRepository.findById(gianHang.getMaChuSoHuu()).orElse(null);

        log.info("Admin đã từ chối yêu cầu mở gian hàng ID: {}, Tên: '{}'. Lý do: '{}'",
                maGianHang, gianHangSaved.getTenGianHang(), lyDo);

        return chuyenSangGianHangResponse(gianHangSaved, chungChi, chuSoHuu);
    }

    /**
     * Xem chi tiết hồ sơ gian hàng
     */
    @Transactional(readOnly = true)
    public GianHangResponse layChiTietGianHang(Long maGianHang) {
        GianHang gianHang = gianHangRepository.findById(maGianHang)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy hồ sơ gian hàng mã: " + maGianHang, HttpStatus.NOT_FOUND));

        ChungChiGianHang chungChi = chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(maGianHang).orElse(null);
        NguoiDung chuSoHuu = nguoiDungRepository.findById(gianHang.getMaChuSoHuu()).orElse(null);

        return chuyenSangGianHangResponse(gianHang, chungChi, chuSoHuu);
    }

    /**
     * Lấy danh sách hồ sơ gian hàng có phân trang, tìm kiếm và lọc
     */
    @Transactional(readOnly = true)
    public PhanTrangResponse<GianHangResponse> danhSachGianHangPhanTrang(String keyword, String trangThai, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        Page<GianHang> pageGianHang = gianHangRepository.timKiemPhanTrang(keyword, trangThai, pageable);

        List<GianHangResponse> danhSach = pageGianHang.getContent().stream().map(g -> {
            ChungChiGianHang cc = chungChiGianHangRepository.findFirstByMaGianHangOrderByMaChungChiDesc(g.getMaGianHang()).orElse(null);
            NguoiDung chuSoHuu = nguoiDungRepository.findById(g.getMaChuSoHuu()).orElse(null);
            return chuyenSangGianHangResponse(g, cc, chuSoHuu);
        }).toList();

        return PhanTrangResponse.<GianHangResponse>builder()
                .content(danhSach)
                .number(pageGianHang.getNumber())
                .totalPages(pageGianHang.getTotalPages())
                .totalElements(pageGianHang.getTotalElements())
                .size(pageGianHang.getSize())
                .first(pageGianHang.isFirst())
                .last(pageGianHang.isLast())
                .empty(pageGianHang.isEmpty())
                .build();
    }

    /**
     * Lấy thống kê số lượng hồ sơ theo trạng thái
     */
    @Transactional(readOnly = true)
    public ThongKeGianHangResponse layThongKeKiemDuyet() {
        return ThongKeGianHangResponse.builder()
                .tongSo(gianHangRepository.countByDaXoaFalse())
                .choDuyet(gianHangRepository.countByTrangThaiAndDaXoaFalse("CHO_DUYET"))
                .hoatDong(gianHangRepository.countByTrangThaiAndDaXoaFalse("HOAT_DONG"))
                .tuChoi(gianHangRepository.countByTrangThaiAndDaXoaFalse("TU_CHOI"))
                .tamKhoa(gianHangRepository.countByTrangThaiAndDaXoaFalse("TAM_KHOA"))
                .build();
    }

    private GianHangResponse chuyenSangGianHangResponse(GianHang g, ChungChiGianHang cc, NguoiDung chuSoHuu) {
        if (g == null) return null;

        GianHangResponse.GianHangResponseBuilder builder = GianHangResponse.builder()
                .maGianHang(g.getMaGianHang())
                .maChuSoHuu(g.getMaChuSoHuu())
                .tenGianHang(g.getTenGianHang())
                .duongDanSlug(g.getDuongDanSlug())
                .moTa(g.getMoTa())
                .linkLogo(g.getLinkLogo())
                .linkBanner(g.getLinkBanner())
                .diaChiKho(g.getDiaChiKho())
                .sdtKho(g.getSdtKho())
                .trangThai(g.getTrangThai())
                .lyDoTuChoi(g.getLyDoTuChoi())
                .hangGianHang(g.getHangGianHang())
                .ngayTao(g.getNgayTao());

        if (chuSoHuu != null) {
            builder.tenChuSoHuu(chuSoHuu.getHoVaTen())
                    .emailChuSoHuu(chuSoHuu.getEmail())
                    .sdtChuSoHuu(chuSoHuu.getSoDienThoai());
        }

        if (cc != null) {
            builder.maChungChi(cc.getMaChungChi())
                    .loaiGiayTo(cc.getLoaiGiayTo())
                    .soGiayTo(cc.getSoGiayTo())
                    .linkAnhGiayTo(cc.getLinkAnhGiayTo())
                    .trangThaiGiayTo(cc.getTrangThaiDuyet());
        }

        return builder.build();
    }
}
