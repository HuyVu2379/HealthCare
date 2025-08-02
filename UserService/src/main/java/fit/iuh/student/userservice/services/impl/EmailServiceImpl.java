package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.responses.ResetPasswordResponse;
import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.repositories.UserRepository;
import fit.iuh.student.userservice.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final MimeMessage message;
    private final MimeMessageHelper helper;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    public EmailServiceImpl(JavaMailSender mailSender, RedisTemplate<String, String> redisTemplate, UserRepository userRepository) throws MessagingException {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        message = mailSender.createMimeMessage();
        this.userRepository = userRepository;
        helper = new MimeMessageHelper(message, true, "UTF-8");
    }
    @Override
    public void sendOTPEmail(String to, String subject) {
        try{
            String existingOtp = redisTemplate.opsForValue().get(to);
            if (existingOtp != null) {
                redisTemplate.delete(to);
            }
            // Tạo OTP ngẫu nhiên
            int otp = (int) (Math.random() * 900000) + 100000;

            // Lưu vào Redis với thời gian sống là 5 phút
            redisTemplate.opsForValue().set(to, String.valueOf(otp), 5 * 60L, TimeUnit.SECONDS);

            // HTML mẫu
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
            htmlBody = htmlBody.replace("{{otpCode}}", String.valueOf(otp));

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
        }
        catch (Exception e){
            logger.error("Failed to send email to: {}", to, e);
        }
    }
    public boolean validateOTP(String email, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(email); // Xóa OTP sau khi xác thực thành công
            return true;
        }
        return false;
    }

    @Override
    public ResetPasswordResponse sendOTPResetPassword(String to, String subject) {
        try{
            Optional<User> user = userRepository.findByEmail(to);
            if (user.isEmpty()) {
                return new ResetPasswordResponse(HttpStatus.NOT_FOUND.value(), "Email not exist !", to);
            }
            // Tạo OTP ngẫu nhiên
            int otp = (int) (Math.random() * 900000) + 100000;

            // Lưu vào Redis với thời gian sống là 5 phút
            redisTemplate.opsForValue().set(to+"-reset-pwd", String.valueOf(otp), 5 * 60L, TimeUnit.SECONDS);

            // HTML mẫu
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

            String userName = user.isPresent() && user.get().getFullname() != null && !user.get().getFullname().isEmpty()
                    ? user.get().getFullname()
                    : "Bạn";
            htmlBody = htmlBody.replace("{{otpCode}}", String.valueOf(otp))
                    .replace("{{userName}}", userName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
        }
        catch (Exception e){
            logger.error("Failed to send email to: {}", to, e);
        }
        return new ResetPasswordResponse(HttpStatus.OK.value(), "Send email reset password successfully !", to);
    }
}
