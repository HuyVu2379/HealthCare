package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.responses.ResetPasswordResponse;
import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.publisher.UserEventPublisher;
import fit.iuh.student.userservice.repositories.UserRepository;
import fit.iuh.student.userservice.services.EmailService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class EmailServiceImpl implements EmailService {
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    public EmailServiceImpl(RedisTemplate<String, String> redisTemplate, UserRepository userRepository, UserEventPublisher userEventPublisher) throws MessagingException {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
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
            userEventPublisher.publishOtpRegistrationEvent(to, subject);
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

            // Gửi sự kiện OTP reset password
            userEventPublisher.publishOtpResetPasswordEvent(to, subject);
        }
        catch (Exception e){
            logger.error("Failed to send email to: {}", to, e);
        }
        return new ResetPasswordResponse(HttpStatus.OK.value(), "Send email reset password successfully !", to);
    }
}
