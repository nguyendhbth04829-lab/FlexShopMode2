package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:flexshop.enterprise.otp@gmail.com}")
    private String fromEmail;

    /**
     * Gửi mã OTP xác thực khôi phục mật khẩu qua Gmail
     */
    public boolean guiEmailOtp(String toEmail, String maOtp, int thoiGianHieuLucGiay) {
        log.info("==================================================================");
        log.info(">>> [FLEXSHOP OTP] Gửi mã xác thực đến: {}", toEmail);
        log.info(">>> MÃ OTP KHÔI PHỤC (6 SỐ): [ {} ]", maOtp);
        log.info(">>> THỜI GIAN HIỆU LỰC: {} GIÂY (5 PHÚT)", thoiGianHieuLucGiay);
        log.info("==================================================================");

        if (mailSender == null) {
            log.warn("JavaMailSender chưa được khởi tạo (do server chưa được restart sau khi thêm dependency spring-boot-starter-mail). Bỏ qua gửi SMTP.");
            return false;
        }

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail, "FlexShop Enterprise");
                helper.setTo(toEmail);
                helper.setSubject("🔒 [" + maOtp + "] Mã OTP khôi phục mật khẩu FlexShop");

                String htmlContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 580px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 16px; background-color: #ffffff;">
                        <div style="text-align: center; margin-bottom: 24px;">
                            <h2 style="color: #4f46e5; margin: 0; font-weight: 800;">FlexShop Enterprise</h2>
                            <span style="color: #64748b; font-size: 13px;">Sàn Thương Mại Điện Tử Đa Gian Hàng V2</span>
                        </div>
                        <div style="padding: 20px; background-color: #f8fafc; border-radius: 12px; margin-bottom: 20px;">
                            <p style="color: #1e293b; font-size: 15px; margin-top: 0;">Xin chào,</p>
                            <p style="color: #475569; font-size: 14px; line-height: 1.6;">
                                Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với địa chỉ email <strong>%s</strong>.
                            </p>
                            <p style="color: #475569; font-size: 14px; margin-bottom: 8px;">Dưới đây là mã xác thực OTP của bạn:</p>
                            <div style="text-align: center; margin: 20px 0;">
                                <span style="display: inline-block; font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #4f46e5; background: #ede9fe; padding: 12px 28px; border-radius: 12px; border: 2px dashed #818cf8;">
                                    %s
                                </span>
                            </div>
                            <p style="color: #ef4444; font-size: 13px; margin-bottom: 0; text-align: center; font-weight: 600;">
                                ⏰ Mã có hiệu lực trong 5 phút (300 giây). Tuyệt đối không chia sẻ mã này cho bất kỳ ai!
                            </p>
                        </div>
                        <div style="font-size: 12px; color: #94a3b8; line-height: 1.5;">
                            <p>Lưu ý: Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này hoặc liên hệ ngay với đội ngũ hỗ trợ CSKH FlexShop.</p>
                            <p style="margin-bottom: 0;">© 2026 FlexShop Enterprise. Trân trọng cảm ơn.</p>
                        </div>
                    </div>
                """.formatted(toEmail, maOtp);

                helper.setText(htmlContent, true);
                mailSender.send(message);
                log.info(">>> [FLEXSHOP OTP] ĐÃ GỬI THÀNH CÔNG EMAIL ĐẾN: {}", toEmail);
            } catch (Exception e) {
                log.error(">>> [FLEXSHOP OTP] LỖI GỬI EMAIL QUA SMTP GMAIL: {}", e.getMessage());
            }
        });

        return true;
    }
}
