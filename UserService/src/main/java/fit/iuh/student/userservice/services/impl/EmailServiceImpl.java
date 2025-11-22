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
        return validateOTPWithOption(email, otp, false); // Mặc định không xóa
    }

    public boolean validateOTPForPasswordReset(String email, String otp) {
        return validateOTPWithOption(email, otp, true); // Xóa sau khi validate thành công
    }

    /**
     * Validate OTP với tùy chọn xóa sau khi thành công
     * @param deleteAfterSuccess true để xóa OTP sau khi validate thành công
     */
    private boolean validateOTPWithOption(String email, String otp, boolean deleteAfterSuccess) {
        String action = deleteAfterSuccess ? "PASSWORD_RESET" : "VALIDATE_ONLY";
        logger.info("VALIDATE OTP [{}] -> email={}, otp={}", action, email, otp);

        // Ưu tiên tìm OTP reset password trước (key: {email}-reset-pwd)
        String resetPwdKey = email + "-reset-pwd";
        logger.info("Trying to find reset password OTP with key: {}", resetPwdKey);

        String storedOtp = redisTemplate.opsForValue().get(resetPwdKey);
        String usedKey = resetPwdKey;

        // Kiểm tra TTL cho reset password key
        Long ttl = redisTemplate.getExpire(usedKey, TimeUnit.SECONDS);
        logger.info("Reset password key check: key={}, ttlSeconds={}, storedOtp={}",
                   usedKey, ttl, storedOtp);

        // Nếu không tìm thấy reset password OTP hoặc đã expire, thử tìm register OTP
        if (storedOtp == null || ttl <= 0) {
            logger.info("Reset password OTP not found or expired, trying register OTP key: {}", email);
            storedOtp = redisTemplate.opsForValue().get(email);
            usedKey = email;
            ttl = redisTemplate.getExpire(usedKey, TimeUnit.SECONDS);
            logger.info("Register OTP key check: key={}, ttlSeconds={}, storedOtp={}",
                       usedKey, ttl, storedOtp);
        }

        logger.info("Final validation [{}]: redisKey={}, ttlSeconds={}, storedOtp={}, inputOtp={}",
                   action, usedKey, ttl, storedOtp, otp);

        if (storedOtp != null && ttl > 0 && storedOtp.equals(otp)) {
            if (deleteAfterSuccess) {
                redisTemplate.delete(usedKey);
                logger.info("OTP validated and deleted for {} - email={}", action, email);
            } else {
                logger.info("OTP validation SUCCESS for {} - email={} (OTP preserved)", action, email);
            }
            return true;
        }

        // Enhanced error logging with detailed comparison
        if (storedOtp != null && !storedOtp.equals(otp)) {
            logger.warn("OTP MISMATCH for {} - email={}, expected={}, received={}, ttlSeconds={}",
                       action, email, storedOtp, otp, ttl);
        } else if (storedOtp == null) {
            logger.warn("OTP NOT FOUND for {} - email={}, key={} (may be expired or never sent)",
                       action, email, usedKey);
        } else if (ttl <= 0) {
            logger.warn("OTP EXPIRED for {} - email={}, storedOtp={}, ttlSeconds={}",
                       action, email, storedOtp, ttl);
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
            String redisKey = payload.getEmail() + "-reset-pwd";

            // Kiểm tra và xóa OTP cũ nếu có
            String existingOtp = redisTemplate.opsForValue().get(redisKey);
            if (existingOtp != null) {
                logger.info("Removing existing OTP for email: {}", payload.getEmail());
                redisTemplate.delete(redisKey);
            }

            logger.info("SEND OTP RESET PASSWORD -> email={}, redisKey={}, otp={}, previousOtp={}",
                       payload.getEmail(), redisKey, otp, existingOtp);

            // Lưu vào Redis với thời gian sống là 5 phút
            redisTemplate.opsForValue().set(redisKey, String.valueOf(otp), 5 * 60L, TimeUnit.SECONDS);
            payload.setOtp(String.valueOf(otp));
            payload.setReceiptId(user.get().getUserId());
            payload.setEventType(UserEvent.OTP_RESET_PASSWORD);
            // Gửi sự kiện OTP reset password
            userEventPublisher.publishOtpResetPasswordEvent(payload);

            logger.info("OTP RESET PASSWORD sent successfully for email={}", payload.getEmail());
        }
        catch (Exception e){
            logger.error("Failed to send reset password email to: {}", payload.getEmail(), e);
        }
        return new ResetPasswordResponse(HttpStatus.OK.value(), "Send email reset password successfully !", payload.getEmail());
    }
}
