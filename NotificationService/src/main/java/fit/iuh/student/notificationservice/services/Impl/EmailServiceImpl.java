package fit.iuh.student.notificationservice.services.Impl;

import fit.iuh.student.notificationservice.consumer.payload.UserEventPayload;
import fit.iuh.student.notificationservice.entities.Notification;
import fit.iuh.student.notificationservice.enums.NotificationType;
import fit.iuh.student.notificationservice.repositories.NotificationRepository;
import fit.iuh.student.notificationservice.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final MimeMessage message;
    private final MimeMessageHelper helper;
    private final NotificationRepository notificationRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    public EmailServiceImpl(JavaMailSender mailSender,NotificationRepository notificationRepository) throws MessagingException {
        this.mailSender = mailSender;
        message = mailSender.createMimeMessage();
        helper = new MimeMessageHelper(message, true, "UTF-8");
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void sendOtpRegisterEmail(UserEventPayload payload) {
        try{
            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"vi\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"UTF-8\" />\n" +
                    "    <title>Xác minh tài khoản</title>\n" +
                    "    <style>\n" +
                    "      body { font-family: Arial, sans-serif; background-color: #f6f6f6; margin: 0; padding: 0; }\n" +
                    "      .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05); }\n" +
                    "      .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eee; }\n" +
                    "      .header h1 { color: #1e3a8a; }\n" +
                    "      .content { padding: 20px 0; font-size: 16px; color: #333; }\n" +
                    "      .otp-box { font-size: 32px; font-weight: bold; background-color: #f0f4ff; color: #1e3a8a; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0; letter-spacing: 5px; }\n" +
                    "      .footer { font-size: 13px; color: #777; text-align: center; margin-top: 30px; border-top: 1px solid #eee; padding-top: 15px; }\n" +
                    "    </style>\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"container\">\n" +
                    "      <div class=\"header\">\n" +
                    "        <h1>Xác minh tài khoản</h1>\n" +
                    "      </div>\n" +
                    "      <div class=\"content\">\n" +
                    "        <p>Xin chào ! Cảm ơn bạn đã đăng ký tài khoản với chúng tôi.</p>\n" +
                    "        <p>Vui lòng sử dụng mã OTP dưới đây để xác minh địa chỉ email của bạn:</p>\n" +
                    "        <div class=\"otp-box\">{{otpCode}}</div>\n" +
                    "        <p>Mã OTP này sẽ hết hạn sau <strong>5 phút</strong>. Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>\n" +
                    "        <p>Trân trọng,<br /><strong>Đội ngũ hỗ trợ</strong></p>\n" +
                    "      </div>\n" +
                    "      <div class=\"footer\">\n" +
                    "        © 2025 Hệ thống chăm sóc sức khỏe Health Care.\n" +
                    "      </div>\n" +
                    "    </div>\n" +
                    "  </body>\n" +
                    "</html>";

            // Gán giá trị thực vào HTML
            htmlBody = htmlBody.replace("{{otpCode}}", String.valueOf(payload.getOtp()));

            helper.setTo(payload.getEmail());
            helper.setSubject(payload.getSubject());
            helper.setText(htmlBody, true);

            mailSender.send(message);
        }
        catch (Exception e){
            logger.error("Failed to send email to: {}", payload.getEmail(), e);
        }finally {
            Notification notification = Notification.builder()
                    .recipient_id(payload.getReceiptId())
                    .type(NotificationType.EMAIL)
                    .message(payload.getSubject())
                    .build();
            notificationRepository.save(notification);
            logger.info("Email sent to: {}", payload.getEmail());
        }
    }

    @Override
    public void sendOtpResetPasswordEmail(UserEventPayload payload) {
        try{
            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"vi\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"UTF-8\" />\n" +
                    "    <title>Đặt lại mật khẩu</title>\n" +
                    "    <style>\n" +
                    "      body {\n" +
                    "        font-family: Arial, sans-serif;\n" +
                    "        background-color: #f4f6f8;\n" +
                    "        margin: 0;\n" +
                    "        padding: 0;\n" +
                    "      }\n" +
                    "      .container {\n" +
                    "        max-width: 600px;\n" +
                    "        margin: 40px auto;\n" +
                    "        background-color: #ffffff;\n" +
                    "        padding: 30px;\n" +
                    "        border-radius: 10px;\n" +
                    "        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);\n" +
                    "      }\n" +
                    "      .header {\n" +
                    "        text-align: center;\n" +
                    "        margin-bottom: 30px;\n" +
                    "      }\n" +
                    "      .header h2 {\n" +
                    "        color: #1e3a8a;\n" +
                    "        margin: 0;\n" +
                    "      }\n" +
                    "      .content {\n" +
                    "        font-size: 16px;\n" +
                    "        color: #333333;\n" +
                    "      }\n" +
                    "      .otp-box {\n" +
                    "        font-size: 32px;\n" +
                    "        font-weight: bold;\n" +
                    "        background-color: #eaf1ff;\n" +
                    "        color: #1e3a8a;\n" +
                    "        padding: 20px;\n" +
                    "        text-align: center;\n" +
                    "        border-radius: 8px;\n" +
                    "        margin: 20px auto;\n" +
                    "        letter-spacing: 5px;\n" +
                    "        width: fit-content;\n" +
                    "      }\n" +
                    "      .footer {\n" +
                    "        font-size: 13px;\n" +
                    "        color: #777777;\n" +
                    "        text-align: center;\n" +
                    "        margin-top: 40px;\n" +
                    "        border-top: 1px solid #eeeeee;\n" +
                    "        padding-top: 15px;\n" +
                    "      }\n" +
                    "    </style>\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"container\">\n" +
                    "      <div class=\"header\">\n" +
                    "        <h2>Xác thực đặt lại mật khẩu</h2>\n" +
                    "      </div>\n" +
                    "      <div class=\"content\">\n" +
                    "        <p>Xin chào <strong>{{userName}}</strong>,</p>\n" +
                    "        <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>\n" +
                    "        <p>Vui lòng sử dụng mã OTP bên dưới để xác minh và tiếp tục:</p>\n" +
                    "        <div class=\"otp-box\">{{otpCode}}</div>\n" +
                    "        <p>Mã OTP này có hiệu lực trong <strong>5 phút</strong>.</p>\n" +
                    "        <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>\n" +
                    "        <p>Trân trọng,<br />Đội ngũ hỗ trợ</p>\n" +
                    "      </div>\n" +
                    "      <div class=\"footer\">\n" +
                    "        © 2025 Hệ thống chăm sóc sức khỏe Health Care.\n" +
                    "      </div>\n" +
                    "    </div>\n" +
                    "  </body>\n" +
                    "</html>";

            htmlBody = htmlBody.replace("{{otpCode}}", String.valueOf(payload.getOtp()))
                    .replace("{{userName}}", "Bạn");
            helper.setTo(payload.getEmail());
            helper.setSubject(payload.getSubject());
            helper.setText(htmlBody, true);

            mailSender.send(message);
        }
        catch (Exception e){
            logger.error("Failed to send email to: {}", payload.getEmail(), e);
        }finally {
            Notification notification = Notification.builder()
                    .recipient_id(payload.getReceiptId())
                    .type(NotificationType.EMAIL)
                    .message(payload.getSubject())
                    .build();
            notificationRepository.save(notification);
            logger.info("Email sent to: {}", payload.getEmail());
        }

    }
}
