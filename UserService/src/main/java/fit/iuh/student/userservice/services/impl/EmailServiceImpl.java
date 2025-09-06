package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.responses.ResetPasswordResponse;
import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.publishers.UserEventPublisher;
import fit.iuh.student.userservice.publishers.events.UserEvent;
import fit.iuh.student.userservice.publishers.payload.UserEventPayload;
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
    public void sendOTPEmail(UserEventPayload payload) {
        try{
            Optional<User> user = userRepository.findByEmail(payload.getEmail());
            String existingOtp = redisTemplate.opsForValue().get(payload.getEmail());
            if (existingOtp != null) {
                redisTemplate.delete(payload.getEmail());
            }
            // Tạo OTP ngẫu nhiên
            int otp = (int) (Math.random() * 900000) + 100000;
            // Lưu vào Redis với thời gian sống là 5 phút
            redisTemplate.opsForValue().set(payload.getEmail(), String.valueOf(otp), 5 * 60L, TimeUnit.SECONDS);
            payload.setOtp(String.valueOf(otp));
            user.ifPresent(value -> payload.setReceiptId(value.getUserId()));
            payload.setEventType(UserEvent.OTP_REGISTER);
            userEventPublisher.publishOtpRegistrationEvent(payload);
        }
        catch (Exception e){
            logger.error("Failed to send email to: {}", payload.getEmail(), e);
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
    public ResetPasswordResponse sendOTPResetPassword(UserEventPayload payload) {
        try{
            Optional<User> user = userRepository.findByEmail(payload.getEmail());
            if (user.isEmpty()) {
                return new ResetPasswordResponse(HttpStatus.NOT_FOUND.value(), "Email not exist !", payload.getEmail());
            }
            // Tạo OTP ngẫu nhiên
            int otp = (int) (Math.random() * 900000) + 100000;

            // Lưu vào Redis với thời gian sống là 5 phút
            redisTemplate.opsForValue().set(payload.getEmail()+"-reset-pwd", String.valueOf(otp), 5 * 60L, TimeUnit.SECONDS);
            payload.setOtp(String.valueOf(otp));
            payload.setReceiptId(user.get().getUserId());
            payload.setEventType(UserEvent.OTP_RESET_PASSWORD);
            // Gửi sự kiện OTP reset password
            userEventPublisher.publishOtpResetPasswordEvent(payload);
        }
        catch (Exception e){
            logger.error("Failed to send email to: {}", payload.getEmail(), e);
        }
        return new ResetPasswordResponse(HttpStatus.OK.value(), "Send email reset password successfully !", payload.getEmail());
    }
}
