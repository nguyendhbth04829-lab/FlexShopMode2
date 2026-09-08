package com.example.demo.service;

import com.example.demo.dto.GianLanThongKeDTO;
import com.example.demo.dto.TaoCanhBaoRequestDTO;
import com.example.demo.dto.XuLyCanhBaoRequestDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * =====================================================================
 * DỰ ÁN: FLEXSHOP ENTERPRISE V2 - SÀN THƯƠNG MẠI ĐIỆN TỬ ĐA GIAN HÀNG
 * PHÂN HỆ: TÀI CHÍNH & AN NINH SÀN (DEV 5 - MINH)
 * USER STORY: US-68 - Service Phát Hiện & Cảnh Báo Gian Lận (Anti-Fraud Engine)
 * =====================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CanhBaoGianLanService {

    private final CanhBaoGianLanRepository canhBaoGianLanRepository;
    private final YeuCauRutTienRepository yeuCauRutTienRepository;
    private final DonHangTiepThiRepository donHangTiepThiRepository;
    private final ChienDichQuangCaoRepository chienDichQuangCaoRepository;
    private final TaiKhoanTraSauRepository taiKhoanTraSauRepository;
    private final NguoiDungRepository nguoiDungRepository;

    /**
     * US-68: Động Cơ Quét Gian Lận Tự Động Xuyên Phân Hệ (Automated Anti-Fraud Scanner)
     */
    @Transactional
    public int quetGianLanToanHeThong() {
        int soLuongCanhBaoMoi = 0;

        // 1. Quét gian lận Rút tiền người bán (US-44)
        List<YeuCauRutTien> danhSachRutTien = yeuCauRutTienRepository.findAll();
        for (YeuCauRutTien yc : danhSachRutTien) {
            // Cảnh báo rút tiền lớn >= 1,000,000 VNĐ ở trạng thái chờ duyệt
            if (yc.getSoTienRut().compareTo(new BigDecimal("1000000.00")) >= 0 && "CHO_DUYET".equalsIgnoreCase(yc.getTrangThai())) {
                if (!canhBaoGianLanRepository.existsByLoaiDoiTuongAndMaDoiTuongAndTrangThai("RUT_TIEN", yc.getMaYeuCau(), "CHO_DIEU_TRA")) {
                    taoCanhBaoTuDong("RUT_TIEN", yc.getMaYeuCau(), 85, 
                            "Yêu cầu rút tiền giá trị lớn đột biến (" + 
                            String.format("%,.0f", yc.getSoTienRut()) + " VNĐ) từ Shop ID: " + 
                            yc.getGianHang().getMaGianHang() + " - Cần kiểm tra đối soát số dư.");
                    soLuongCanhBaoMoi++;
                }
            }
        }

        // 2. Quét gian lận Tiếp thị liên kết (US-65)
        List<DonHangTiepThi> danhSachAffiliate = donHangTiepThiRepository.findAll();
        for (DonHangTiepThi don : danhSachAffiliate) {
            // Cảnh báo hoa hồng cao >= 100,000 VNĐ đang chờ đối soát
            if (don.getHoaHongDuocNhan().compareTo(new BigDecimal("100000.00")) >= 0 && "CHO_DOI_SOAT".equalsIgnoreCase(don.getTrangThai())) {
                if (!canhBaoGianLanRepository.existsByLoaiDoiTuongAndMaDoiTuongAndTrangThai("AFFILIATE", don.getMaDonAffiliate(), "CHO_DIEU_TRA")) {
                    taoCanhBaoTuDong("AFFILIATE", don.getMaDonAffiliate(), 75,
                            "Phát hiện đơn tiếp thị hoa hồng cao bất thường (" +
                            String.format("%,.0f", don.getHoaHongDuocNhan()) + " VNĐ) qua Link: " +
                            don.getTiepThiLienKet().getMaLinkAffiliate() + " - Nghi vấn tự mua hưởng hoa hồng.");
                    soLuongCanhBaoMoi++;
                }
            }
        }

        // 3. Quét gian lận Quảng cáo Shopee Ads (US-64)
        List<ChienDichQuangCao> danhSachAds = chienDichQuangCaoRepository.findAll();
        for (ChienDichQuangCao cd : danhSachAds) {
            if (cd.getTongChiPhiDaDung() != null && cd.getTongChiPhiDaDung().compareTo(new BigDecimal("2000.00")) >= 0) {
                if (!canhBaoGianLanRepository.existsByLoaiDoiTuongAndMaDoiTuongAndTrangThai("ADS", cd.getMaChienDich(), "CHO_DIEU_TRA")) {
                    taoCanhBaoTuDong("ADS", cd.getMaChienDich(), 80,
                            "Chiến dịch Ads [" + cd.getTenChienDich() + "] phát sinh chi phí nhanh - Nghi vấn click tặc / bot dìm tiền ví đối thủ.");
                    soLuongCanhBaoMoi++;
                }
            }
        }

        // 4. Quét rủi ro Mua trước trả sau SPayLater (US-63)
        List<TaiKhoanTraSau> danhSachTraSau = taiKhoanTraSauRepository.findAll();
        for (TaiKhoanTraSau tk : danhSachTraSau) {
            if (tk.getDiemTinDung() != null && tk.getDiemTinDung() < 600) {
                if (!canhBaoGianLanRepository.existsByLoaiDoiTuongAndMaDoiTuongAndTrangThai("TRA_SAU", tk.getMaTkTraSau(), "CHO_DIEU_TRA")) {
                    taoCanhBaoTuDong("TRA_SAU", tk.getMaTkTraSau(), 90,
                            "Tài khoản SPayLater của khách hàng ID [" + tk.getNguoiDung().getMaNguoiDung() + 
                            "] có điểm tín dụng thấp (" + tk.getDiemTinDung() + " điểm) - Nguy cơ bùng nợ quá hạn.");
                    soLuongCanhBaoMoi++;
                }
            }
        }

        log.info("[US-68 Anti-Fraud] Quét hoàn tất, phát hiện và tạo mới {} cảnh báo gian lận.", soLuongCanhBaoMoi);
        return soLuongCanhBaoMoi;
    }

    private void taoCanhBaoTuDong(String loaiDoiTuong, Long maDoiTuong, int diemRuiRo, String lyDo) {
        CanhBaoGianLan cb = new CanhBaoGianLan();
        cb.setLoaiDoiTuong(loaiDoiTuong);
        cb.setMaDoiTuong(maDoiTuong);
        cb.setDiemRuiRo(diemRuiRo);
        cb.setLyDoCanhBao(lyDo);
        cb.setTrangThai("CHO_DIEU_TRA");
        cb.setNgayTao(LocalDateTime.now());
        canhBaoGianLanRepository.save(cb);
    }

    /**
     * US-68: Tạo Cảnh Báo Thủ Công
     */
    @Transactional
    public CanhBaoGianLan taoCanhBaoThuCong(TaoCanhBaoRequestDTO dto) {
        dto.validate();

        CanhBaoGianLan cb = new CanhBaoGianLan();
        cb.setLoaiDoiTuong(dto.getLoaiDoiTuong().toUpperCase());
        cb.setMaDoiTuong(dto.getMaDoiTuong());
        cb.setDiemRuiRo(dto.getDiemRuiRo());
        cb.setLyDoCanhBao(dto.getLyDoCanhBao());
        cb.setTrangThai("CHO_DIEU_TRA");
        cb.setNgayTao(LocalDateTime.now());

        return canhBaoGianLanRepository.save(cb);
    }

    /**
     * US-68: Xử Lý Phán Quyết Cảnh Báo (Áp dụng chế tài hoặc Bỏ qua)
     */
    @Transactional
    public CanhBaoGianLan xuLyCanhBao(XuLyCanhBaoRequestDTO dto) {
        dto.validate();

        CanhBaoGianLan cb = canhBaoGianLanRepository.findById(dto.getMaCanhBao())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cảnh báo ID: " + dto.getMaCanhBao()));

        if ("KHOA_DOI_TUONG".equalsIgnoreCase(dto.getHanhDong())) {
            cb.setTrangThai("DA_XU_LY");
            String ghiChu = (dto.getGhiChuXuLy() != null && !dto.getGhiChuXuLy().trim().isEmpty()) 
                    ? dto.getGhiChuXuLy() : "Đã khóa/đóng băng đối tượng vi phạm";
            cb.setLyDoCanhBao(cb.getLyDoCanhBao() + " -> [ĐÃ XỬ LÝ CHẾ TÀI: " + ghiChu + "]");

            // Áp dụng chế tài thực tế tùy loại đối tượng
            thucHienCheTai(cb.getLoaiDoiTuong(), cb.getMaDoiTuong());

            log.warn("[US-68 Anti-Fraud] Đã xử phạt cảnh báo #{}: {}", cb.getMaCanhBao(), ghiChu);
        } else if ("BO_QUA_CANH_BAO".equalsIgnoreCase(dto.getHanhDong())) {
            cb.setTrangThai("BO_QUA_CANH_BAO");
            String ghiChu = (dto.getGhiChuXuLy() != null && !dto.getGhiChuXuLy().trim().isEmpty()) 
                    ? dto.getGhiChuXuLy() : "Xác nhận giao dịch trung thực, an toàn";
            cb.setLyDoCanhBao(cb.getLyDoCanhBao() + " -> [BỎ QUA / AN TOÀN: " + ghiChu + "]");
            log.info("[US-68 Anti-Fraud] Đã bỏ qua cảnh báo #{}: False Positive.", cb.getMaCanhBao());
        }

        return canhBaoGianLanRepository.save(cb);
    }

    /**
     * Thực thi các chế tài bảo vệ sàn khi xác nhận gian lận
     */
    private void thucHienCheTai(String loaiDoiTuong, Long maDoiTuong) {
        try {
            if ("RUT_TIEN".equalsIgnoreCase(loaiDoiTuong)) {
                yeuCauRutTienRepository.findById(maDoiTuong).ifPresent(yc -> {
                    yc.setTrangThai("TU_CHOI");
                    yc.setLyDoTuChoi("Tài khoản bị nghi vấn gian lận tài chính theo cảnh báo Anti-Fraud US-68.");
                    yeuCauRutTienRepository.save(yc);
                });
            } else if ("ADS".equalsIgnoreCase(loaiDoiTuong)) {
                chienDichQuangCaoRepository.findById(maDoiTuong).ifPresent(cd -> {
                    cd.setTrangThai("TAM_DUNG");
                    chienDichQuangCaoRepository.save(cd);
                });
            } else if ("AFFILIATE".equalsIgnoreCase(loaiDoiTuong)) {
                donHangTiepThiRepository.findById(maDoiTuong).ifPresent(don -> {
                    don.setTrangThai("DA_HUY");
                    donHangTiepThiRepository.save(don);
                });
            } else if ("TRA_SAU".equalsIgnoreCase(loaiDoiTuong)) {
                taiKhoanTraSauRepository.findById(maDoiTuong).ifPresent(tk -> {
                    tk.setTrangThai("TAM_KHOA");
                    taiKhoanTraSauRepository.save(tk);
                });
            }
        } catch (Exception e) {
            log.error("[US-68 Anti-Fraud] Lỗi khi thực thi chế tài: {}", e.getMessage());
        }
    }

    /**
     * US-68: Báo Cáo Thống Kê Giám Sát An Ninh & Rủi Ro
     */
    @Transactional(readOnly = true)
    public GianLanThongKeDTO getThongKe() {
        List<CanhBaoGianLan> all = canhBaoGianLanRepository.findAll();
        int choXuLy = canhBaoGianLanRepository.countByTrangThai("CHO_DIEU_TRA");
        int daXuLy = canhBaoGianLanRepository.countByTrangThai("DA_XU_LY");
        int daBoQua = canhBaoGianLanRepository.countByTrangThai("BO_QUA_CANH_BAO");
        int nguyHiem = canhBaoGianLanRepository.countByDiemRuiRoGreaterThanEqual(75);
        Double diemTb = canhBaoGianLanRepository.tinhDiemRuiRoTrungBinh();

        return GianLanThongKeDTO.builder()
                .tongCanhBao(all.size())
                .soCaChoDieuTra(choXuLy)
                .soCaNguyHiemRuiRoCao(nguyHiem)
                .soCaDaXuLy(daXuLy)
                .soCaDaBoQua(daBoQua)
                .diemRuiRoTrungBinh(diemTb != null ? Math.round(diemTb * 10.0) / 10.0 : 0.0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CanhBaoGianLan> getDanhSach(String trangThai, String loaiDoiTuong) {
        boolean coTrangThai = trangThai != null && !trangThai.trim().isEmpty() && !"ALL".equalsIgnoreCase(trangThai);
        boolean coLoai = loaiDoiTuong != null && !loaiDoiTuong.trim().isEmpty() && !"ALL".equalsIgnoreCase(loaiDoiTuong);

        if (coTrangThai && coLoai) {
            return canhBaoGianLanRepository.findByLoaiDoiTuongAndTrangThaiOrderByNgayTaoDesc(loaiDoiTuong, trangThai);
        } else if (coTrangThai) {
            return canhBaoGianLanRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai);
        } else if (coLoai) {
            return canhBaoGianLanRepository.findByLoaiDoiTuongOrderByNgayTaoDesc(loaiDoiTuong);
        }
        return canhBaoGianLanRepository.findAllByOrderByNgayTaoDesc();
    }

    @Transactional(readOnly = true)
    public CanhBaoGianLan getChiTiet(Long id) {
        return canhBaoGianLanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cảnh báo với ID: " + id));
    }
}
