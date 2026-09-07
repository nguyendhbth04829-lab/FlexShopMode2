package com.example.demo.service;

import com.example.demo.dto.request.CapNhatTrangThaiNguoiDungRequest;
import com.example.demo.dto.request.PhanQuyenNguoiDungRequest;
import com.example.demo.dto.request.TaoNhanVienRequest;
import com.example.demo.dto.response.QuanLyNguoiDungResponse;
import com.example.demo.dto.response.ThongKeNguoiDungResponse;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.VaiTro;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Dịch vụ xử lý nghiệp vụ Quản lý người dùng & Phân quyền nội bộ Admin (US-06)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuanLyNguoiDungService {

    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * US-06: Lấy danh sách người dùng có phân trang, tìm kiếm và lọc vai trò, trạng thái
     */
    @Transactional(readOnly = true)
    public Page<QuanLyNguoiDungResponse> layDanhSachNguoiDung(String keyword, String vaiTro, String trangThai, Pageable pageable) {
        String keywordChuan = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String vaiTroChuan = (vaiTro != null && !vaiTro.trim().isEmpty() && !"ALL".equalsIgnoreCase(vaiTro)) ? vaiTro.trim() : null;
        String trangThaiChuan = (trangThai != null && !trangThai.trim().isEmpty() && !"ALL".equalsIgnoreCase(trangThai)) ? trangThai.trim() : null;

        Page<NguoiDung> danhSach = nguoiDungRepository.timKiemPhanTrang(keywordChuan, vaiTroChuan, trangThaiChuan, pageable);
        return danhSach.map(QuanLyNguoiDungResponse::tuEntity);
    }

    /**
     * US-06: Thống kê tổng quan số lượng người dùng cho bảng điều khiển KPI
     */
    @Transactional(readOnly = true)
    public ThongKeNguoiDungResponse layThongKeNguoiDung() {
        long tongSo = nguoiDungRepository.countByDaXoaFalse();
        long soKhachHang = nguoiDungRepository.demSoLuongTheoVaiTro("KHACH_HANG");
        long soNguoiBan = nguoiDungRepository.demSoLuongTheoVaiTro("NGUOI_BAN");
        long soTaiXe = nguoiDungRepository.demSoLuongTheoVaiTro("TAI_XE");
        long soCskh = nguoiDungRepository.demSoLuongTheoVaiTro("CSKH");
        long soAdmin = nguoiDungRepository.demSoLuongTheoVaiTro("ADMIN");
        long soBiKhoa = nguoiDungRepository.countByTrangThaiAndDaXoaFalse("BI_KHOA");
        long soHoatDong = nguoiDungRepository.countByTrangThaiAndDaXoaFalse("HOAT_DONG");
        long soMoiHomNay = nguoiDungRepository.demSoNguoiDungMoiTu(LocalDate.now().atStartOfDay());

        return ThongKeNguoiDungResponse.builder()
                .tongSoNguoiDung(tongSo)
                .soKhachHang(soKhachHang)
                .soNguoiBan(soNguoiBan)
                .soTaiXe(soTaiXe)
                .soCskh(soCskh)
                .soAdmin(soAdmin)
                .soBiKhoa(soBiKhoa)
                .soHoatDong(soHoatDong)
                .soNguoiDungMoiHomNay(soMoiHomNay)
                .build();
    }

    /**
     * US-06: Lấy chi tiết thông tin một người dùng theo ID
     */
    @Transactional(readOnly = true)
    public QuanLyNguoiDungResponse layChiTietNguoiDung(Long id) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .filter(u -> u.getDaXoa() == null || !u.getDaXoa())
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy người dùng với mã ID: " + id, HttpStatus.NOT_FOUND));

        return QuanLyNguoiDungResponse.tuEntity(user);
    }

    /**
     * US-06: Cấp tài khoản nhân viên nội bộ (Shipper hoặc CSKH)
     */
    @Transactional
    public QuanLyNguoiDungResponse taoTaiKhoanNhanVien(TaoNhanVienRequest request, Long adminHienTaiId) {
        String emailChuan = request.getEmail().trim().toLowerCase();
        String sdtChuan = request.getSoDienThoai().trim();
        String vaiTroChon = request.getVaiTro().trim().toUpperCase();

        // 1. Kiểm tra tính duy nhất của Email
        if (nguoiDungRepository.existsByEmailIgnoreCase(emailChuan)) {
            throw new NgoaiLeUngDung("Email '" + emailChuan + "' đã tồn tại trong hệ thống. Vui lòng sử dụng email khác!", HttpStatus.CONFLICT);
        }

        // 2. Kiểm tra tính duy nhất của Số điện thoại
        if (nguoiDungRepository.existsBySoDienThoai(sdtChuan)) {
            throw new NgoaiLeUngDung("Số điện thoại '" + sdtChuan + "' đã được sử dụng bởi tài khoản khác!", HttpStatus.CONFLICT);
        }

        // 3. Kiểm tra vai trò nội bộ hợp lệ
        if (!"TAI_XE".equals(vaiTroChon) && !"CSKH".equals(vaiTroChon)) {
            throw new NgoaiLeUngDung("Chỉ được cấp tài khoản nhân viên với vai trò TAI_XE hoặc CSKH!", HttpStatus.BAD_REQUEST);
        }

        // 4. Lấy hoặc tạo vai trò tương ứng trong DB
        VaiTro vaiTroChinh = vaiTroRepository.findByTenVaiTro(vaiTroChon)
                .orElseGet(() -> vaiTroRepository.save(new VaiTro(vaiTroChon, "Vai trò nội bộ " + vaiTroChon)));

        Set<VaiTro> danhSachVaiTro = new HashSet<>();
        danhSachVaiTro.add(vaiTroChinh);

        // 5. Mã hóa mật khẩu khởi tạo bằng BCrypt salt 12
        String matKhauMaHoa = passwordEncoder.encode(request.getMatKhau());

        // 6. Tạo người dùng mới
        String bgColor = "TAI_XE".equals(vaiTroChon) ? "0284c7" : "10b981";
        String avatarUrl = "https://ui-avatars.com/api/?name=" + request.getHoVaTen().trim().replace(" ", "+") + "&background=" + bgColor + "&color=fff";

        NguoiDung nhanVienMoi = NguoiDung.builder()
                .hoVaTen(request.getHoVaTen().trim())
                .email(emailChuan)
                .soDienThoai(sdtChuan)
                .matKhauMaHoa(matKhauMaHoa)
                .anhDaiDien(avatarUrl)
                .trangThai("HOAT_DONG")
                .daXoa(false)
                .danhSachVaiTro(danhSachVaiTro)
                .ngayTao(LocalDateTime.now())
                .ngayCapNhat(LocalDateTime.now())
                .build();

        NguoiDung daLuu = nguoiDungRepository.save(nhanVienMoi);
        log.info("Admin ID: {} đã cấp thành công tài khoản nhân viên ID: {}, Email: {}, Vai trò: {}",
                adminHienTaiId, daLuu.getMaNguoiDung(), daLuu.getEmail(), vaiTroChon);

        return QuanLyNguoiDungResponse.tuEntity(daLuu);
    }

    /**
     * US-06: Khóa hoặc Mở khóa tài khoản người dùng
     * - Admin không được tự khóa chính mình
     * - Không được khóa tài khoản Quản trị viên tối cao
     */
    @Transactional
    public QuanLyNguoiDungResponse doiTrangThaiKhoa(Long id, CapNhatTrangThaiNguoiDungRequest request, Long adminHienTaiId) {
        // 1. Chặn Admin tự khóa tài khoản của chính mình
        if (id.equals(adminHienTaiId)) {
            throw new NgoaiLeUngDung("Bạn không thể tự khóa tài khoản Quản trị viên của chính mình!", HttpStatus.BAD_REQUEST);
        }

        NguoiDung user = nguoiDungRepository.findById(id)
                .filter(u -> u.getDaXoa() == null || !u.getDaXoa())
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy người dùng với mã ID: " + id, HttpStatus.NOT_FOUND));

        // 2. Chặn khóa tài khoản Quản trị viên tối cao
        if ("admin@flexshop.vn".equalsIgnoreCase(user.getEmail())) {
            throw new NgoaiLeUngDung("Không được phép khóa tài khoản Quản trị viên tối cao của hệ thống!", HttpStatus.BAD_REQUEST);
        }

        String trangThaiMoi = request.getTrangThai().trim().toUpperCase();
        if (!"HOAT_DONG".equals(trangThaiMoi) && !"BI_KHOA".equals(trangThaiMoi)) {
            throw new NgoaiLeUngDung("Trạng thái chỉ chấp nhận HOAT_DONG hoặc BI_KHOA!", HttpStatus.BAD_REQUEST);
        }

        user.setTrangThai(trangThaiMoi);
        if ("BI_KHOA".equals(trangThaiMoi)) {
            String lyDo = (request.getLyDo() != null && !request.getLyDo().trim().isEmpty())
                    ? request.getLyDo().trim()
                    : "Vi phạm chính sách tiêu chuẩn của hệ thống";
            user.setLyDoKhoa(lyDo);
        } else {
            user.setLyDoKhoa(null);
        }
        user.setNgayCapNhat(LocalDateTime.now());
        NguoiDung capNhat = nguoiDungRepository.save(user);

        log.info("Admin ID: {} đã thay đổi trạng thái user ID: {} sang '{}'. Lý do: {}",
                adminHienTaiId, id, trangThaiMoi, user.getLyDoKhoa());

        return QuanLyNguoiDungResponse.tuEntity(capNhat);
    }

    /**
     * US-06: Cập nhật phân quyền danh sách vai trò cho người dùng
     */
    @Transactional
    public QuanLyNguoiDungResponse phanQuyenNguoiDung(Long id, PhanQuyenNguoiDungRequest request, Long adminHienTaiId) {
        NguoiDung user = nguoiDungRepository.findById(id)
                .filter(u -> u.getDaXoa() == null || !u.getDaXoa())
                .orElseThrow(() -> new NgoaiLeUngDung("Không tìm thấy người dùng với mã ID: " + id, HttpStatus.NOT_FOUND));

        String vaiTroChon = request.layVaiTroDuyNhat();
        if (vaiTroChon == null || vaiTroChon.trim().isEmpty()) {
            throw new NgoaiLeUngDung("Vui lòng chọn 1 vai trò cho người dùng!", HttpStatus.BAD_REQUEST);
        }

        String chuanHoa = vaiTroChon.trim().toUpperCase().replace("ROLE_", "");

        // Nếu admin tự sửa chính mình, bắt buộc phải giữ lại quyền ADMIN
        if (id.equals(adminHienTaiId) && !"ADMIN".equals(chuanHoa)) {
            throw new NgoaiLeUngDung("Bạn không thể tự tước bỏ quyền Quản trị viên của chính mình!", HttpStatus.BAD_REQUEST);
        }

        VaiTro vaiTroMoi = vaiTroRepository.findByTenVaiTro(chuanHoa)
                .orElseGet(() -> vaiTroRepository.save(new VaiTro(chuanHoa, "Vai trò " + chuanHoa)));

        Set<VaiTro> danhSachVaiTroMoi = new HashSet<>();
        danhSachVaiTroMoi.add(vaiTroMoi);

        user.setDanhSachVaiTro(danhSachVaiTroMoi);
        user.setNgayCapNhat(LocalDateTime.now());
        NguoiDung capNhat = nguoiDungRepository.save(user);

        log.info("Admin ID: {} đã phân quyền user ID: {} sang 1 vai trò duy nhất: {}", adminHienTaiId, id, chuanHoa);
        return QuanLyNguoiDungResponse.tuEntity(capNhat);
    }
}
