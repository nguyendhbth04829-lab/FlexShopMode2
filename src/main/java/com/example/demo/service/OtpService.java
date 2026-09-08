package com.example.demo.service;

import com.example.demo.dto.request.DatLaiMatKhauRequest;
import com.example.demo.dto.request.GuiOtpDangKyRequest;
import com.example.demo.dto.request.GuiYeuCauOtpRequest;
import com.example.demo.dto.request.XacThucOtpRequest;
import com.example.demo.dto.response.OtpResponse;
import com.example.demo.entity.NguoiDung;
import com.example.demo.entity.XacThucOtp;
import com.example.demo.exception.NgoaiLeUngDung;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.XacThucOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final XacThucOtpRepository xacThucOtpRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public static final String LOAI_OTP_KHOI_PHUC = "KHOI_PHUC_MAT_KHAU";
    public static final String LOAI_OTP_DANG_KY = "DANG_KY";
    public static final int THOI_GIAN_HET_HAN_GIAY = 300; // 5 phút
    public static final int GIOI_HAN_LAN_GUI_MOI_GIO = 3;   // Tối đa 3 lần/1h
    public static final int SO_LAN_SAI_TOI_DA = 5;          // Tối đa 5 lần nhập sai
    public static final int THOI_GIAN_KHOA_GIO = 24;        // Khóa 24h

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * US-03: Gửi mã OTP khôi phục mật khẩu (Email / SMS)
     * - Sinh mã 6 số ngẫu nhiên
     * - Hết hạn sau 300 giây (5 phút)
     * - Rate Limit: Tối đa 3 lần trong 1 giờ
     * - Chặn khi đang bị khóa 24 giờ
     */
    @Transactional
    public OtpResponse guiOtpKhoiPhuc(GuiYeuCauOtpRequest yeuCau) {
        String dinhDanh = yeuCau.getTaiKhoan().trim();

        // 1. Kiểm tra tài khoản người dùng có tồn tại trên sàn hay không
        NguoiDung nguoiDung = nguoiDungRepository.findByIdentifier(dinhDanh)
                .orElseThrow(() -> new NgoaiLeUngDung(
                        "Không tìm thấy tài khoản liên kết với '" + dinhDanh + "'. Vui lòng kiểm tra lại!",
                        HttpStatus.NOT_FOUND, "taiKhoan"));

        if (Boolean.TRUE.equals(nguoiDung.getDaXoa())) {
            throw new NgoaiLeUngDung("Tài khoản đã bị xóa khỏi hệ thống!", HttpStatus.FORBIDDEN, "taiKhoan");
        }

        // Chuẩn hóa định danh gửi (ưu tiên email)
        String nguoiNhan = (dinhDanh.contains("@")) ? nguoiDung.getEmail().toLowerCase() : nguoiDung.getSoDienThoai();

        // 2. Kiểm tra tài khoản có đang bị khóa 24 giờ do nhập sai quá 5 lần hay không
        kiemTraTrangThaiKhoa(nguoiNhan);

        // 3. Kiểm tra Rate Limit: Tối đa 3 lần trong 1 giờ
        LocalDateTime motGioTruoc = LocalDateTime.now().minusHours(1);
        long soLanDaGui = xacThucOtpRepository.demSoLanGuiTrongKhoangThoiGian(nguoiNhan, LOAI_OTP_KHOI_PHUC, motGioTruoc);
        if (soLanDaGui >= GIOI_HAN_LAN_GUI_MOI_GIO) {
            throw new NgoaiLeUngDung(
                    "Bạn đã yêu cầu gửi mã OTP quá " + GIOI_HAN_LAN_GUI_MOI_GIO + " lần trong vòng 1 giờ. Vui lòng thử lại sau 1 giờ!",
                    HttpStatus.TOO_MANY_REQUESTS,
                    "taiKhoan"
            );
        }

        // 4. Sinh mã OTP gồm 6 chữ số ngẫu nhiên (000000 - 999999)
        int soNgauNhien = secureRandom.nextInt(1_000_000);
        String maOtp = String.format("%06d", soNgauNhien);

        // 5. Lưu bản ghi OTP vào CSDL với thời gian hết hạn đúng 300 giây
        LocalDateTime thoiGianHetHan = LocalDateTime.now().plusSeconds(THOI_GIAN_HET_HAN_GIAY);

        XacThucOtp otp = XacThucOtp.builder()
                .nguoiNhan(nguoiNhan)
                .maXacThuc(maOtp)
                .loaiOtp(LOAI_OTP_KHOI_PHUC)
                .thoiGianHetHan(thoiGianHetHan)
                .daSuDung(false)
                .soLanNhapSai(0)
                .khoaDenThoiGian(null)
                .ngayTao(LocalDateTime.now())
                .build();

        xacThucOtpRepository.save(otp);

        // 6. Gửi OTP qua Gmail (hoặc console logging)
        emailService.guiEmailOtp(nguoiDung.getEmail(), maOtp, THOI_GIAN_HET_HAN_GIAY);

        long soLanConLai = GIOI_HAN_LAN_GUI_MOI_GIO - (soLanDaGui + 1);

        log.info("Đã phát hành OTP khôi phục mật khẩu cho: {}, hết hạn: {}", nguoiNhan, thoiGianHetHan);

        return OtpResponse.builder()
                .thongBao("Mã OTP đã được gửi đến email " + cheEmail(nguoiDung.getEmail()) + ". Vui lòng kiểm tra hòm thư!")
                .nguoiNhan(nguoiNhan)
                .soGiayHieuLuc(THOI_GIAN_HET_HAN_GIAY)
                .soLanGuiConLai(soLanConLai)
                .maOtpDemo(maOtp) // Trả về để tiện kiểm thử tự động
                .build();
    }

    /**
     * US-03: Xác thực mã OTP
     * - Kiểm tra hạn 300s
     * - Kiểm tra số lần sai: quá 5 lần khóa 24h
     */
    @Transactional(noRollbackFor = NgoaiLeUngDung.class)
    public boolean xacThucOtp(XacThucOtpRequest yeuCau) {
        String dinhDanh = yeuCau.getTaiKhoan().trim();
        String maNhapVao = yeuCau.getMaOtp().trim();

        // Tìm người dùng
        NguoiDung nguoiDung = nguoiDungRepository.findByIdentifier(dinhDanh)
                .orElseThrow(() -> new NgoaiLeUngDung("Tài khoản không tồn tại!", HttpStatus.NOT_FOUND));

        String nguoiNhan = (dinhDanh.contains("@")) ? nguoiDung.getEmail().toLowerCase() : nguoiDung.getSoDienThoai();

        // Kiểm tra khóa 24h
        kiemTraTrangThaiKhoa(nguoiNhan);

        // Tìm OTP mới nhất chưa sử dụng
        XacThucOtp otp = xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc(
                nguoiNhan, LOAI_OTP_KHOI_PHUC)
                .orElseThrow(() -> new NgoaiLeUngDung(
                        "Không tìm thấy mã OTP hoặc mã đã được sử dụng. Vui lòng yêu cầu mã mới!",
                        HttpStatus.BAD_REQUEST, "maOtp"));

        // Kiểm tra hết hạn 300s
        if (otp.isHetHan()) {
            throw new NgoaiLeUngDung("Mã OTP đã hết hạn sau 300 giây (5 phút). Vui lòng yêu cầu gửi mã mới!", HttpStatus.BAD_REQUEST, "maOtp");
        }

        // So khớp mã OTP
        if (!otp.getMaXacThuc().equals(maNhapVao)) {
            int soLanSaiHienTai = (otp.getSoLanNhapSai() == null ? 0 : otp.getSoLanNhapSai()) + 1;
            otp.setSoLanNhapSai(soLanSaiHienTai);

            if (soLanSaiHienTai >= SO_LAN_SAI_TOI_DA) {
                // Khóa 24 giờ
                LocalDateTime thoiDiemMoKhoa = LocalDateTime.now().plusHours(THOI_GIAN_KHOA_GIO);
                otp.setKhoaDenThoiGian(thoiDiemMoKhoa);
                xacThucOtpRepository.saveAndFlush(otp);
                log.warn("Tài khoản {} đã nhập sai OTP quá {} lần -> BỊ KHÓA 24H đến {}", nguoiNhan, SO_LAN_SAI_TOI_DA, thoiDiemMoKhoa);
                throw new NgoaiLeUngDung(
                        "Bạn đã nhập sai mã OTP quá " + SO_LAN_SAI_TOI_DA + " lần! Chức năng khôi phục mật khẩu đã bị tạm khóa trong 24 giờ vì lý do bảo mật.",
                        HttpStatus.FORBIDDEN,
                        "maOtp"
                );
            } else {
                xacThucOtpRepository.saveAndFlush(otp);
                int conLai = SO_LAN_SAI_TOI_DA - soLanSaiHienTai;
                throw new NgoaiLeUngDung(
                        "Mã OTP không chính xác. Bạn còn " + conLai + " lần thử trước khi bị khóa chức năng trong 24 giờ!",
                        HttpStatus.BAD_REQUEST,
                        "maOtp"
                );
            }
        }

        // Nếu đúng: Reset bộ đếm số lần nhập sai
        otp.setSoLanNhapSai(0);
        xacThucOtpRepository.saveAndFlush(otp);
        return true;
    }

    /**
     * US-03: Đặt lại mật khẩu mới sau khi xác thực OTP thành công
     */
    @Transactional(noRollbackFor = NgoaiLeUngDung.class)
    public void datLaiMatKhau(DatLaiMatKhauRequest yeuCau) {
        // 1. Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!yeuCau.getMatKhauMoi().equals(yeuCau.getXacNhanMatKhau())) {
            throw new NgoaiLeUngDung("Mật khẩu mới và xác nhận mật khẩu không trùng khớp!", HttpStatus.BAD_REQUEST, "xacNhanMatKhau");
        }

        // 2. Xác thực mã OTP trước
        XacThucOtpRequest xacThucRequest = XacThucOtpRequest.builder()
                .taiKhoan(yeuCau.getTaiKhoan())
                .maOtp(yeuCau.getMaOtp())
                .build();
        xacThucOtp(xacThucRequest);

        String dinhDanh = yeuCau.getTaiKhoan().trim();
        NguoiDung nguoiDung = nguoiDungRepository.findByIdentifier(dinhDanh)
                .orElseThrow(() -> new NgoaiLeUngDung("Tài khoản không tồn tại!", HttpStatus.NOT_FOUND, "taiKhoan"));

        // 3. Kiểm tra mật khẩu mới không được trùng với mật khẩu cũ
        if (passwordEncoder.matches(yeuCau.getMatKhauMoi(), nguoiDung.getMatKhauMaHoa())) {
            throw new NgoaiLeUngDung("Mật khẩu mới không được trùng với mật khẩu hiện tại! Vui lòng chọn một mật khẩu khác.", HttpStatus.BAD_REQUEST, "matKhauMoi");
        }

        String nguoiNhan = (dinhDanh.contains("@")) ? nguoiDung.getEmail().toLowerCase() : nguoiDung.getSoDienThoai();

        XacThucOtp otp = xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc(
                nguoiNhan, LOAI_OTP_KHOI_PHUC)
                .orElseThrow(() -> new NgoaiLeUngDung("Mã OTP không hợp lệ!", HttpStatus.BAD_REQUEST));

        // 4. Cập nhật mật khẩu mới mã hóa BCrypt
        String matKhauMaHoaMoi = passwordEncoder.encode(yeuCau.getMatKhauMoi());
        nguoiDung.setMatKhauMaHoa(matKhauMaHoaMoi);
        nguoiDung.setNgayCapNhat(LocalDateTime.now());
        nguoiDungRepository.save(nguoiDung);

        // 4. Đánh dấu mã OTP là đã sử dụng
        otp.setDaSuDung(true);
        xacThucOtpRepository.saveAndFlush(otp);

        log.info("Đặt lại mật khẩu thành công cho tài khoản: {}", nguoiDung.getEmail());
    }

    /**
     * Gửi mã OTP xác thực đăng ký tài khoản qua Gmail
     * - Kiểm tra email và SĐT chưa tồn tại trên hệ thống
     * - Sinh mã 6 số ngẫu nhiên
     * - Hết hạn sau 300 giây (5 phút)
     * - Rate Limit: Tối đa 3 lần / 1 giờ
     * - Chặn khi bị khóa 24 giờ do nhập sai quá 5 lần
     */
    @Transactional
    public OtpResponse guiOtpDangKy(GuiOtpDangKyRequest yeuCau) {
        String email = yeuCau.getEmail().trim().toLowerCase();

        // 1. Kiểm tra Email chưa được đăng ký
        if (nguoiDungRepository.existsByEmailIgnoreCase(email)) {
            throw new NgoaiLeUngDung("Email '" + email + "' đã được đăng ký trong hệ thống. Vui lòng sử dụng email khác hoặc Đăng nhập!", HttpStatus.CONFLICT, "email");
        }

        // 2. Kiểm tra Số điện thoại nếu có
        if (StringUtils.hasText(yeuCau.getSoDienThoai())) {
            String sdt = yeuCau.getSoDienThoai().trim();
            if (nguoiDungRepository.existsBySoDienThoai(sdt)) {
                throw new NgoaiLeUngDung("Số điện thoại '" + sdt + "' đã được đăng ký trong hệ thống!", HttpStatus.CONFLICT, "soDienThoai");
            }
        }

        // 3. Kiểm tra tài khoản có đang bị khóa 24 giờ không
        kiemTraTrangThaiKhoa(email);

        // 4. Kiểm tra Rate Limit: Tối đa 3 lần / 1 giờ
        LocalDateTime motGioTruoc = LocalDateTime.now().minusHours(1);
        long soLanDaGui = xacThucOtpRepository.demSoLanGuiTrongKhoangThoiGian(email, LOAI_OTP_DANG_KY, motGioTruoc);
        if (soLanDaGui >= GIOI_HAN_LAN_GUI_MOI_GIO) {
            throw new NgoaiLeUngDung(
                    "Bạn đã yêu cầu gửi mã OTP quá " + GIOI_HAN_LAN_GUI_MOI_GIO + " lần trong vòng 1 giờ. Vui lòng thử lại sau 1 giờ!",
                    HttpStatus.TOO_MANY_REQUESTS,
                    "email"
            );
        }

        // 5. Sinh mã OTP gồm 6 chữ số ngẫu nhiên (000000 - 999999)
        int soNgauNhien = secureRandom.nextInt(1_000_000);
        String maOtp = String.format("%06d", soNgauNhien);

        // 6. Lưu bản ghi OTP vào CSDL với thời gian hết hạn 300 giây
        LocalDateTime thoiGianHetHan = LocalDateTime.now().plusSeconds(THOI_GIAN_HET_HAN_GIAY);

        XacThucOtp otp = XacThucOtp.builder()
                .nguoiNhan(email)
                .maXacThuc(maOtp)
                .loaiOtp(LOAI_OTP_DANG_KY)
                .thoiGianHetHan(thoiGianHetHan)
                .daSuDung(false)
                .soLanNhapSai(0)
                .khoaDenThoiGian(null)
                .ngayTao(LocalDateTime.now())
                .build();

        xacThucOtpRepository.save(otp);

        // 7. Gửi OTP qua Gmail
        emailService.guiEmailOtpDangKy(email, maOtp, THOI_GIAN_HET_HAN_GIAY, yeuCau.getHoVaTen());

        long soLanConLai = GIOI_HAN_LAN_GUI_MOI_GIO - (soLanDaGui + 1);

        log.info("Đã phát hành OTP đăng ký tài khoản cho: {}, hết hạn: {}", email, thoiGianHetHan);

        return OtpResponse.builder()
                .thongBao("Mã OTP xác thực đã được gửi đến email " + cheEmail(email) + ". Vui lòng kiểm tra hòm thư Gmail để hoàn tất đăng ký!")
                .nguoiNhan(email)
                .soGiayHieuLuc(THOI_GIAN_HET_HAN_GIAY)
                .soLanGuiConLai(soLanConLai)
                .maOtpDemo(maOtp)
                .build();
    }

    /**
     * Xác thực và đánh dấu đã sử dụng mã OTP đăng ký tài khoản
     */
    @Transactional(noRollbackFor = NgoaiLeUngDung.class)
    public boolean xacThucVaSuDungOtpDangKy(String email, String maOtp) {
        String nguoiNhan = email.trim().toLowerCase();
        String maNhapVao = (maOtp != null) ? maOtp.trim() : "";

        if (maNhapVao.isEmpty()) {
            throw new NgoaiLeUngDung("Vui lòng nhập mã OTP 6 số xác thực đã gửi đến Gmail của bạn!", HttpStatus.BAD_REQUEST, "maOtp");
        }

        // Kiểm tra khóa 24h
        kiemTraTrangThaiKhoa(nguoiNhan);

        // Tìm OTP mới nhất chưa sử dụng của loại DANG_KY
        XacThucOtp otp = xacThucOtpRepository.findTopByNguoiNhanAndLoaiOtpAndDaSuDungFalseOrderByNgayTaoDesc(
                nguoiNhan, LOAI_OTP_DANG_KY)
                .orElseThrow(() -> new NgoaiLeUngDung(
                        "Không tìm thấy mã OTP xác thực hoặc mã đã được sử dụng. Vui lòng bấm 'Gửi lại mã' để nhận OTP mới!",
                        HttpStatus.BAD_REQUEST, "maOtp"));

        // Kiểm tra hết hạn 300s
        if (otp.isHetHan()) {
            throw new NgoaiLeUngDung("Mã OTP đã hết hạn sau 300 giây (5 phút). Vui lòng yêu cầu gửi mã mới!", HttpStatus.BAD_REQUEST, "maOtp");
        }

        // So khớp mã OTP
        if (!otp.getMaXacThuc().equals(maNhapVao)) {
            int soLanSaiHienTai = (otp.getSoLanNhapSai() == null ? 0 : otp.getSoLanNhapSai()) + 1;
            otp.setSoLanNhapSai(soLanSaiHienTai);

            if (soLanSaiHienTai >= SO_LAN_SAI_TOI_DA) {
                // Khóa 24 giờ
                LocalDateTime thoiDiemMoKhoa = LocalDateTime.now().plusHours(THOI_GIAN_KHOA_GIO);
                otp.setKhoaDenThoiGian(thoiDiemMoKhoa);
                xacThucOtpRepository.saveAndFlush(otp);
                log.warn("Email {} đã nhập sai OTP đăng ký quá {} lần -> BỊ KHÓA 24H đến {}", nguoiNhan, SO_LAN_SAI_TOI_DA, thoiDiemMoKhoa);
                throw new NgoaiLeUngDung(
                        "Bạn đã nhập sai mã OTP quá " + SO_LAN_SAI_TOI_DA + " lần! Chức năng đăng ký với email này đã bị tạm khóa 24 giờ vì lý do an toàn.",
                        HttpStatus.FORBIDDEN,
                        "maOtp"
                );
            } else {
                xacThucOtpRepository.saveAndFlush(otp);
                int conLai = SO_LAN_SAI_TOI_DA - soLanSaiHienTai;
                throw new NgoaiLeUngDung(
                        "Mã OTP không chính xác. Bạn còn " + conLai + " lần thử trước khi bị khóa chức năng trong 24 giờ!",
                        HttpStatus.BAD_REQUEST,
                        "maOtp"
                );
            }
        }

        // Nếu đúng: Đánh dấu đã sử dụng và reset số lần sai
        otp.setSoLanNhapSai(0);
        otp.setDaSuDung(true);
        xacThucOtpRepository.saveAndFlush(otp);
        log.info("Xác thực OTP đăng ký thành công cho email: {}", nguoiNhan);
        return true;
    }

    /**
     * Kiểm tra tài khoản có đang bị khóa 24 giờ hay không
     */
    private void kiemTraTrangThaiKhoa(String nguoiNhan) {
        Optional<XacThucOtp> otpDangKhoa = xacThucOtpRepository
                .findTopByNguoiNhanAndKhoaDenThoiGianAfterOrderByKhoaDenThoiGianDesc(nguoiNhan, LocalDateTime.now());
        if (otpDangKhoa.isPresent()) {
            LocalDateTime thoiGianKhoa = otpDangKhoa.get().getKhoaDenThoiGian();
            throw new NgoaiLeUngDung(
                    "Chức năng khôi phục mật khẩu của tài khoản này đang bị tạm khóa trong 24 giờ (đến " +
                            thoiGianKhoa.getHour() + ":" + String.format("%02d", thoiGianKhoa.getMinute()) + " ngày " +
                            thoiGianKhoa.getDayOfMonth() + "/" + thoiGianKhoa.getMonthValue() + ") do nhập sai OTP quá 5 lần.",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private String cheEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 3) return "***@" + parts[1];
        return name.substring(0, 2) + "***" + name.substring(name.length() - 1) + "@" + parts[1];
    }
}
