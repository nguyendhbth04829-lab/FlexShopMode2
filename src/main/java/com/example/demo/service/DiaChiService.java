package com.example.demo.service;

import com.example.demo.dto.request.CapNhatDiaChiRequest;
import com.example.demo.dto.request.TaoDiaChiRequest;
import com.example.demo.dto.response.DiaChiResponse;
import com.example.demo.dto.response.ThongKeDiaChiResponse;
import com.example.demo.entity.DiaChiNguoiDung;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.DiaChiNguoiDungRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaChiService {

    public static final int GIOI_HAN_DIA_CHI_TOI_DA = 20;

    private final DiaChiNguoiDungRepository diaChiRepository;

    /**
     * US-05: Thêm mới địa chỉ nhận hàng
     * - Giới hạn tối đa 20 địa chỉ
     * - Địa chỉ đầu tiên tự động thành mặc định
     * - Nếu set la_mac_dinh = true: Tự động chuyển tất cả địa chỉ khác về false trong cùng 1 Transaction
     */
    @Transactional
    public DiaChiResponse taoDiaChi(TaoDiaChiRequest yeuCau, Long maNguoiDung) {
        // 1. Kiểm tra giới hạn tối đa 20 địa chỉ
        long soLuongHienTai = diaChiRepository.countByMaNguoiDungAndDaXoaFalse(maNguoiDung);
        if (soLuongHienTai >= GIOI_HAN_DIA_CHI_TOI_DA) {
            throw new NgoaiLeUngDung(
                    "Bạn đã đạt giới hạn tối đa " + GIOI_HAN_DIA_CHI_TOI_DA + " địa chỉ giao hàng. Vui lòng xóa bớt địa chỉ không dùng trước khi thêm mới!",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 2. Tự động set mặc định nếu là địa chỉ đầu tiên của người dùng
        boolean datLamMacDinh = (soLuongHienTai == 0) || Boolean.TRUE.equals(yeuCau.getLaMacDinh());

        DiaChiNguoiDung diaChi = DiaChiNguoiDung.builder()
                .maNguoiDung(maNguoiDung)
                .tenNguoiNhan(yeuCau.getTenNguoiNhan().trim())
                .soDienThoai(yeuCau.getSoDienThoai().trim())
                .tinhThanh(yeuCau.getTinhThanh().trim())
                .quanHuyen(yeuCau.getQuanHuyen().trim())
                .xaPhuong(yeuCau.getXaPhuong().trim())
                .diaChiChiTiet(yeuCau.getDiaChiChiTiet().trim())
                .laMacDinh(datLamMacDinh)
                .daXoa(false)
                .ngayTao(LocalDateTime.now())
                .build();

        DiaChiNguoiDung diaChiDaLuu = diaChiRepository.save(diaChi);

        // 3. Nếu là mặc định -> Reset tất cả các địa chỉ khác của người dùng này về la_mac_dinh = false
        if (datLamMacDinh && soLuongHienTai > 0) {
            diaChiRepository.huyMacDinhCacDiaChiKhac(maNguoiDung, diaChiDaLuu.getMaDiaChi());
            log.info("Đã đồng bộ chuyển các địa chỉ khác về la_mac_dinh = 0 cho user ID: {}", maNguoiDung);
        }

        log.info("Tạo thành công địa chỉ mới ID: {} cho user ID: {}", diaChiDaLuu.getMaDiaChi(), maNguoiDung);
        return DiaChiResponse.fromEntity(diaChiDaLuu);
    }

    /**
     * US-05: Cập nhật địa chỉ nhận hàng
     */
    @Transactional
    public DiaChiResponse capNhatDiaChi(Long maDiaChi, CapNhatDiaChiRequest yeuCau, Long maNguoiDung) {
        DiaChiNguoiDung diaChi = timDiaChiTheoIdVaUser(maDiaChi, maNguoiDung);

        diaChi.setTenNguoiNhan(yeuCau.getTenNguoiNhan().trim());
        diaChi.setSoDienThoai(yeuCau.getSoDienThoai().trim());
        diaChi.setTinhThanh(yeuCau.getTinhThanh().trim());
        diaChi.setQuanHuyen(yeuCau.getQuanHuyen().trim());
        diaChi.setXaPhuong(yeuCau.getXaPhuong().trim());
        diaChi.setDiaChiChiTiet(yeuCau.getDiaChiChiTiet().trim());

        if (yeuCau.getLaMacDinh() != null) {
            if (Boolean.TRUE.equals(yeuCau.getLaMacDinh())) {
                diaChi.setLaMacDinh(true);
                diaChiRepository.huyMacDinhCacDiaChiKhac(maNguoiDung, maDiaChi);
            } else if (Boolean.TRUE.equals(diaChi.getLaMacDinh())) {
                // Nếu đang là mặc định mà muốn bỏ, kiểm tra xem có còn địa chỉ nào khác không
                long soLuongKhac = diaChiRepository.countByMaNguoiDungAndDaXoaFalse(maNguoiDung);
                if (soLuongKhac <= 1) {
                    throw new NgoaiLeUngDung("Không thể hủy mặc định vì đây là địa chỉ duy nhất của bạn!", HttpStatus.BAD_REQUEST);
                }
                diaChi.setLaMacDinh(false);
            }
        }

        DiaChiNguoiDung diaChiCapNhat = diaChiRepository.save(diaChi);
        log.info("Cập nhật thành công địa chỉ ID: {} cho user ID: {}", maDiaChi, maNguoiDung);
        return DiaChiResponse.fromEntity(diaChiCapNhat);
    }

    /**
     * US-05 Nghiệp vụ cốt lõi: Thiết lập địa chỉ mặc định một chạm
     * Chuyển tất cả địa chỉ khác về la_mac_dinh = 0 trong cùng 1 Transaction
     */
    @Transactional
    public DiaChiResponse datLamMacDinh(Long maDiaChi, Long maNguoiDung) {
        DiaChiNguoiDung diaChi = timDiaChiTheoIdVaUser(maDiaChi, maNguoiDung);

        if (Boolean.TRUE.equals(diaChi.getLaMacDinh())) {
            return DiaChiResponse.fromEntity(diaChi); // Đã là mặc định rồi
        }

        diaChi.setLaMacDinh(true);
        diaChiRepository.save(diaChi);

        // Đồng bộ chuyển tất cả địa chỉ khác của user này về 0
        diaChiRepository.huyMacDinhCacDiaChiKhac(maNguoiDung, maDiaChi);

        log.info("User ID: {} đã đặt địa chỉ ID: {} làm mặc định. Đã reset các địa chỉ khác về 0.", maNguoiDung, maDiaChi);
        return DiaChiResponse.fromEntity(diaChi);
    }

    /**
     * US-05: Xóa địa chỉ (Xóa mềm da_xoa = true)
     * Nếu xóa địa chỉ mặc định, tự động gán địa chỉ còn lại gần nhất làm mặc định
     */
    @Transactional
    public void xoaDiaChi(Long maDiaChi, Long maNguoiDung) {
        DiaChiNguoiDung diaChi = timDiaChiTheoIdVaUser(maDiaChi, maNguoiDung);

        boolean laMacDinh = Boolean.TRUE.equals(diaChi.getLaMacDinh());

        diaChi.setDaXoa(true);
        diaChi.setLaMacDinh(false);
        diaChiRepository.save(diaChi);

        log.info("User ID: {} đã xóa địa chỉ ID: {}", maNguoiDung, maDiaChi);

        // Nếu địa chỉ vừa xóa là mặc định, tự động gán địa chỉ còn lại kế tiếp làm mặc định thay thế
        if (laMacDinh) {
            Optional<DiaChiNguoiDung> diaChiKeTiep = diaChiRepository.findTopByMaNguoiDungAndDaXoaFalseOrderByNgayTaoDesc(maNguoiDung);
            if (diaChiKeTiep.isPresent()) {
                DiaChiNguoiDung thayThe = diaChiKeTiep.get();
                thayThe.setLaMacDinh(true);
                diaChiRepository.save(thayThe);
                log.info("Tự động gán địa chỉ ID: {} làm mặc định thay thế cho user ID: {}", thayThe.getMaDiaChi(), maNguoiDung);
            }
        }
    }

    /**
     * Lấy chi tiết 1 địa chỉ
     */
    @Transactional(readOnly = true)
    public DiaChiResponse layChiTietDiaChi(Long maDiaChi, Long maNguoiDung) {
        DiaChiNguoiDung diaChi = timDiaChiTheoIdVaUser(maDiaChi, maNguoiDung);
        return DiaChiResponse.fromEntity(diaChi);
    }

    /**
     * Lấy địa chỉ mặc định của người dùng
     */
    @Transactional(readOnly = true)
    public Optional<DiaChiResponse> layDiaChiMacDinh(Long maNguoiDung) {
        return diaChiRepository.findByMaNguoiDungAndLaMacDinhTrueAndDaXoaFalse(maNguoiDung)
                .map(DiaChiResponse::fromEntity);
    }

    /**
     * Tìm kiếm, phân trang và lọc địa chỉ
     */
    @Transactional(readOnly = true)
    public Page<DiaChiResponse> layDanhSachPhanTrang(Long maNguoiDung, String keyword, String tinhThanh, Boolean laMacDinh, Pageable pageable) {
        return diaChiRepository.timKiemPhanTrang(maNguoiDung, keyword, tinhThanh, laMacDinh, pageable)
                .map(DiaChiResponse::fromEntity);
    }

    /**
     * Lấy toàn bộ danh sách địa chỉ (ưu tiên địa chỉ mặc định lên trước)
     */
    @Transactional(readOnly = true)
    public List<DiaChiResponse> layToanBoDiaChi(Long maNguoiDung) {
        return diaChiRepository.findByMaNguoiDungAndDaXoaFalseOrderByLaMacDinhDescNgayTaoDesc(maNguoiDung)
                .stream()
                .map(DiaChiResponse::fromEntity)
                .toList();
    }

    /**
     * Thống kê sổ địa chỉ
     */
    @Transactional(readOnly = true)
    public ThongKeDiaChiResponse thongKeDiaChi(Long maNguoiDung) {
        long tongSo = diaChiRepository.countByMaNguoiDungAndDaXoaFalse(maNguoiDung);
        Optional<DiaChiResponse> macDinh = layDiaChiMacDinh(maNguoiDung);
        List<String> tinhThanh = diaChiRepository.layDanhSachTinhThanhCuaNguoiDung(maNguoiDung);

        return ThongKeDiaChiResponse.builder()
                .tongSoDiaChi(tongSo)
                .gioiHanToiDa(GIOI_HAN_DIA_CHI_TOI_DA)
                .soLuongConLai(Math.max(0, GIOI_HAN_DIA_CHI_TOI_DA - tongSo))
                .coDiaChiMacDinh(macDinh.isPresent())
                .diaChiMacDinh(macDinh.orElse(null))
                .danhSachTinhThanh(tinhThanh)
                .build();
    }

    /**
     * Kiểm tra quyền sở hữu và sự tồn tại của địa chỉ
     */
    private DiaChiNguoiDung timDiaChiTheoIdVaUser(Long maDiaChi, Long maNguoiDung) {
        return diaChiRepository.findByMaDiaChiAndMaNguoiDungAndDaXoaFalse(maDiaChi, maNguoiDung)
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy địa chỉ hoặc bạn không có quyền truy cập địa chỉ này!", HttpStatus.NOT_FOUND));
    }
}
