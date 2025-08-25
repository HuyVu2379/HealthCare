package fit.iuh.student.notificationservice.services.Impl;

import fit.iuh.student.notificationservice.consumer.payload.AppointmentEventPayload;
import fit.iuh.student.notificationservice.consumer.payload.UserEventPayload;
import fit.iuh.student.notificationservice.entities.Notification;
import fit.iuh.student.notificationservice.enums.NotificationType;
import fit.iuh.student.notificationservice.repositories.NotificationRepository;
import fit.iuh.student.notificationservice.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;


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

    @Override
    public void sendEmailBookingAppointment(AppointmentEventPayload payload) {
        try {
            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"vi\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"UTF-8\" />\n" +
                    "    <title>Xác nhận đặt lịch khám</title>\n" +
                    "    <style>\n" +
                    "      body { font-family: Arial, sans-serif; background-color: #f6f6f6; margin: 0; padding: 0; }\n" +
                    "      .container { max-width: 650px; margin: 40px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05); }\n" +
                    "      .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eee; }\n" +
                    "      .header h1 { color: #1e3a8a; margin: 0; }\n" +
                    "      .content { padding: 20px 0; font-size: 16px; color: #333; }\n" +
                    "      .appointment-details { background-color: #f0f8ff; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #1e3a8a; }\n" +
                    "      .detail-row { margin: 8px 0; }\n" +
                    "      .detail-label { font-weight: bold; color: #1e3a8a; display: inline-block; width: 150px; }\n" +
                    "      .highlight { background-color: #e6f3ff; padding: 15px; border-radius: 5px; margin: 15px 0; text-align: center; }\n" +
                    "      .success-badge { background-color: #d4edda; color: #155724; padding: 8px 15px; border-radius: 20px; font-weight: bold; display: inline-block; }\n" +
                    "      .footer { font-size: 13px; color: #777; text-align: center; margin-top: 30px; border-top: 1px solid #eee; padding-top: 15px; }\n" +
                    "    </style>\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"container\">\n" +
                    "      <div class=\"header\">\n" +
                    "        <h1>Đặt lịch khám thành công</h1>\n" +
                    "        <div class=\"success-badge\">Đã xác nhận</div>\n" +
                    "      </div>\n" +
                    "      <div class=\"content\">\n" +
                    "        <p>Xin chào <strong>{{patientName}}</strong>,</p>\n" +
                    "        <p>Chúng tôi xác nhận rằng lịch khám của bạn đã được đặt thành công. Dưới đây là thông tin chi tiết:</p>\n" +
                    "        \n" +
                    "        <div class=\"appointment-details\">\n" +
                    "          <h3 style=\"margin-top: 0; color: #1e3a8a;\">Thông tin lịch khám</h3>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Mã lịch khám:</span>\n" +
                    "            <span>{{appointmentId}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Bác sĩ:</span>\n" +
                    "            <span>{{doctorName}} - {{specialty}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Ngày khám:</span>\n" +
                    "            <span>{{appointmentDate}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Thời gian:</span>\n" +
                    "            <span>{{timeSlot}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Hình thức:</span>\n" +
                    "            <span>{{consultationType}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Địa chỉ:</span>\n" +
                    "            <span>{{clinicAddress}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Triệu chứng:</span>\n" +
                    "            <span>{{symptoms}}</span>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"highlight\">\n" +
                    "          <strong>Vui lòng có mặt trước 15 phút so với giờ hẹn</strong>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <p><strong>Lưu ý quan trọng:</strong></p>\n" +
                    "        <ul>\n" +
                    "          <li>Mang theo CCCD/CMND và các giấy tờ y tế liên quan</li>\n" +
                    "          <li>Nếu cần hủy lịch, vui lòng thông báo trước ít nhất 2 giờ</li>\n" +
                    "          <li>Liên hệ hotline: 1900-xxxx nếu cần hỗ trợ</li>\n" +
                    "        </ul>\n" +
                    "        \n" +
                    "        <p>Cảm ơn bạn đã tin tưởng dịch vụ của chúng tôi!</p>\n" +
                    "        <p>Trân trọng,<br /><strong>Đội ngũ chăm sóc khách hàng</strong></p>\n" +
                    "      </div>\n" +
                    "      <div class=\"footer\">\n" +
                    "        © 2025 Hệ thống chăm sóc sức khỏe Health Care. Mọi quyền được bảo lưu.\n" +
                    "      </div>\n" +
                    "    </div>\n" +
                    "  </body>\n" +
                    "</html>";

            // Format consultation type
            String consultationTypeText = payload.getConsultationType() != null ? 
                (payload.getConsultationType().toString().equals("ONLINE") ? "Khám trực tuyến" : "Khám tại phòng khám") : 
                "Không xác định";

            // Format time slot
            String timeSlotText = payload.getTimeSlot() != null ? 
                (payload.getTimeSlot().getStartTime() != null && payload.getTimeSlot().getEndTime() != null ? 
                    payload.getTimeSlot().getStartTime() + " - " + payload.getTimeSlot().getEndTime() : 
                    "Không xác định") : 
                "Không xác định";

            // Format appointment date
            String appointmentDateText = payload.getAppointmentDate() != null ? 
                payload.getAppointmentDate().toString() : 
                "Không xác định";

            // Replace placeholders with actual values
            htmlBody = htmlBody.replace("{{patientName}}", payload.getPatient() != null && payload.getPatient().getFullName() != null ? payload.getPatient().getFullName() : "Bệnh nhân")
                    .replace("{{appointmentId}}", payload.getAppointmentId() != null ? payload.getAppointmentId() : "")
                    .replace("{{doctorName}}", payload.getDoctor() != null && payload.getDoctor().getFullName() != null ? payload.getDoctor().getFullName() : "")
                    .replace("{{specialty}}", payload.getDoctor() != null && payload.getDoctor().getSpecialty() != null ? payload.getDoctor().getSpecialty() : "")
                    .replace("{{appointmentDate}}", appointmentDateText)
                    .replace("{{timeSlot}}", timeSlotText)
                    .replace("{{consultationType}}", consultationTypeText)
                    .replace("{{clinicAddress}}", payload.getDoctor() != null && payload.getDoctor().getClinicAddress() != null ? payload.getDoctor().getClinicAddress() : "Không có")
                    .replace("{{symptoms}}", payload.getSymptoms() != null ? payload.getSymptoms() : "Không có");

            // Check if patient and email are not null before setting recipient
            if (payload.getPatient() != null && payload.getPatient().getEmail() != null) {
                helper.setTo(payload.getPatient().getEmail());
                helper.setSubject("✅ Xác nhận đặt lịch khám - Mã: " + (payload.getAppointmentId() != null ? payload.getAppointmentId() : ""));
                helper.setText(htmlBody, true);
                
                mailSender.send(message);
            } else {
                logger.error("Cannot send email: patient or email is null");
            }
        } catch (Exception e) {
            String email = payload.getPatient() != null && payload.getPatient().getEmail() != null ? 
                payload.getPatient().getEmail() : "unknown";
            logger.error("Failed to send booking confirmation email to: {}", email, e);
        } finally {
            try {
                if (payload.getPatient() != null && payload.getPatient().getUserId() != null) {
                    Notification notification = Notification.builder()
                            .recipient_id(payload.getPatient().getUserId())
                            .type(NotificationType.EMAIL)
                            .message("Xác nhận đặt lịch khám - Mã: " + 
                                (payload.getAppointmentId() != null ? payload.getAppointmentId() : ""))
                            .build();
                    notificationRepository.save(notification);
                    
                    String email = payload.getPatient().getEmail() != null ? 
                        payload.getPatient().getEmail() : "unknown";
                    logger.info("Booking confirmation email sent to: {}", email);
                }
            } catch (Exception ex) {
                logger.error("Failed to save notification", ex);
            }
        }
    }

    @Override
    public void sendEmailCancelAppointment(AppointmentEventPayload payload) {
        try {
            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"vi\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"UTF-8\" />\n" +
                    "    <title>Thông báo hủy lịch khám</title>\n" +
                    "    <style>\n" +
                    "      body { font-family: Arial, sans-serif; background-color: #f6f6f6; margin: 0; padding: 0; }\n" +
                    "      .container { max-width: 650px; margin: 40px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05); }\n" +
                    "      .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eee; }\n" +
                    "      .header h1 { color: #dc3545; margin: 0; }\n" +
                    "      .content { padding: 20px 0; font-size: 16px; color: #333; }\n" +
                    "      .appointment-details { background-color: #fff5f5; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545; }\n" +
                    "      .detail-row { margin: 8px 0; }\n" +
                    "      .detail-label { font-weight: bold; color: #dc3545; display: inline-block; width: 150px; }\n" +
                    "      .highlight { background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 15px 0; border: 1px solid #ffeaa7; }\n" +
                    "      .cancel-badge { background-color: #f8d7da; color: #721c24; padding: 8px 15px; border-radius: 20px; font-weight: bold; display: inline-block; }\n" +
                    "      .action-buttons { text-align: center; margin: 25px 0; }\n" +
                    "      .btn { display: inline-block; padding: 12px 25px; margin: 0 10px; text-decoration: none; border-radius: 5px; font-weight: bold; }\n" +
                    "      .btn-primary { background-color: #007bff; color: white; }\n" +
                    "      .footer { font-size: 13px; color: #777; text-align: center; margin-top: 30px; border-top: 1px solid #eee; padding-top: 15px; }\n" +
                    "    </style>\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"container\">\n" +
                    "      <div class=\"header\">\n" +
                    "        <h1>Thông báo hủy lịch khám</h1>\n" +
                    "        <div class=\"cancel-badge\">Đã hủy</div>\n" +
                    "      </div>\n" +
                    "      <div class=\"content\">\n" +
                    "        <p>Xin chào <strong>{{patientName}}</strong>,</p>\n" +
                    "        <p>Chúng tôi xin thông báo rằng lịch khám của bạn đã được hủy. Dưới đây là thông tin chi tiết:</p>\n" +
                    "        \n" +
                    "        <div class=\"appointment-details\">\n" +
                    "          <h3 style=\"margin-top: 0; color: #dc3545;\">📋 Thông tin lịch khám đã hủy</h3>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Mã lịch khám:</span>\n" +
                    "            <span>{{appointmentId}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Bác sĩ:</span>\n" +
                    "            <span>{{doctorName}} - {{specialty}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Ngày khám:</span>\n" +
                    "            <span>{{appointmentDate}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Thời gian:</span>\n" +
                    "            <span>{{timeSlot}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Hình thức:</span>\n" +
                    "            <span>{{consultationType}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Lý do hủy:</span>\n" +
                    "            <span>{{cancelReason}}</span>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"highlight\">\n" +
                    "          <strong>Bạn có thể đặt lại lịch khám mới bất kỳ lúc nào!</strong>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"action-buttons\">\n" +
                    "          <a href=\"#\" class=\"btn btn-primary\">Đặt lịch khám mới</a>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <p><strong>Thông tin hỗ trợ:</strong></p>\n" +
                    "        <ul>\n" +
                    "          <li>Hotline: 1900-xxxx (24/7)</li>\n" +
                    "          <li>Email: support@healthcare.com</li>\n" +
                    "          <li>Website: www.healthcare.com</li>\n" +
                    "        </ul>\n" +
                    "        \n" +
                    "        <p>Chúng tôi rất tiếc vì sự bất tiện này và mong được phục vụ bạn trong tương lai.</p>\n" +
                    "        <p>Trân trọng,<br /><strong>Đội ngũ chăm sóc khách hàng</strong></p>\n" +
                    "      </div>\n" +
                    "      <div class=\"footer\">\n" +
                    "        © 2025 Hệ thống chăm sóc sức khỏe Health Care. Mọi quyền được bảo lưu.\n" +
                    "      </div>\n" +
                    "    </div>\n" +
                    "  </body>\n" +
                    "</html>";

            // Format consultation type
            String consultationTypeText = payload.getConsultationType() != null ? 
                (payload.getConsultationType().toString().equals("ONLINE") ? "Khám trực tuyến" : "Khám tại phòng khám") : 
                "Không xác định";

            // Format time slot
            String timeSlotText = payload.getTimeSlot() != null ? 
                (payload.getTimeSlot().getStartTime() != null && payload.getTimeSlot().getEndTime() != null ? 
                    payload.getTimeSlot().getStartTime() + " - " + payload.getTimeSlot().getEndTime() : 
                    "Không xác định") : 
                "Không xác định";

            // Format appointment date
            String appointmentDateText = payload.getAppointmentDate() != null ? 
                payload.getAppointmentDate().toString() : 
                "Không xác định";

            // Determine cancel reason
            String cancelReason = payload.getNote() != null && !payload.getNote().trim().isEmpty()
                    ? payload.getNote()
                    : "Theo yêu cầu của bệnh nhân";

            // Replace placeholders with actual values
            htmlBody = htmlBody.replace("{{patientName}}", payload.getPatient() != null && payload.getPatient().getFullName() != null ? payload.getPatient().getFullName() : "Bệnh nhân")
                    .replace("{{appointmentId}}", payload.getAppointmentId() != null ? payload.getAppointmentId() : "")
                    .replace("{{doctorName}}", payload.getDoctor() != null && payload.getDoctor().getFullName() != null ? payload.getDoctor().getFullName() : "")
                    .replace("{{specialty}}", payload.getDoctor() != null && payload.getDoctor().getSpecialty() != null ? payload.getDoctor().getSpecialty() : "")
                    .replace("{{appointmentDate}}", appointmentDateText)
                    .replace("{{timeSlot}}", timeSlotText)
                    .replace("{{consultationType}}", consultationTypeText)
                    .replace("{{cancelReason}}", cancelReason);

            // Check if patient and email are not null before setting recipient
            if (payload.getPatient() != null && payload.getPatient().getEmail() != null) {
                helper.setTo(payload.getPatient().getEmail());
                helper.setSubject("❌ Thông báo hủy lịch khám - Mã: " + (payload.getAppointmentId() != null ? payload.getAppointmentId() : ""));
                helper.setText(htmlBody, true);
                
                mailSender.send(message);
            } else {
                logger.error("Cannot send cancellation email: patient or email is null");
            }
        } catch (Exception e) {
            String email = payload.getPatient() != null && payload.getPatient().getEmail() != null ? 
                payload.getPatient().getEmail() : "unknown";
            logger.error("Failed to send cancellation email to: {}", email, e);
        } finally {
            try {
                if (payload.getPatient() != null && payload.getPatient().getUserId() != null) {
                    Notification notification = Notification.builder()
                            .recipient_id(payload.getPatient().getUserId())
                            .type(NotificationType.EMAIL)
                            .message("Thông báo hủy lịch khám - Mã: " + 
                                (payload.getAppointmentId() != null ? payload.getAppointmentId() : ""))
                            .build();
                    notificationRepository.save(notification);
                    
                    String email = payload.getPatient().getEmail() != null ? 
                        payload.getPatient().getEmail() : "unknown";
                    logger.info("Cancellation email sent to: {}", email);
                }
            } catch (Exception ex) {
                logger.error("Failed to save notification", ex);
            }
        }
    }
}
