package fit.iuh.student.notificationservice.services.Impl;

import fit.iuh.student.notificationservice.consumers.payload.AppointmentEventPayload;
import fit.iuh.student.notificationservice.consumers.payload.MedicalRecordPayload;
import fit.iuh.student.notificationservice.consumers.payload.RescheduleAppointmentResponse;
import fit.iuh.student.notificationservice.consumers.payload.UserEventPayload;
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
                helper.setSubject("Thông báo hủy lịch khám - Mã: " + (payload.getAppointmentId() != null ? payload.getAppointmentId() : ""));
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
    
    @Override
    public void sendEmailRescheduleAppointment(RescheduleAppointmentResponse payload) {
        try {
            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"vi\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"UTF-8\" />\n" +
                    "    <title>Thông báo thay đổi lịch khám</title>\n" +
                    "    <style>\n" +
                    "      body { font-family: Arial, sans-serif; background-color: #f6f6f6; margin: 0; padding: 0; }\n" +
                    "      .container { max-width: 650px; margin: 40px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05); }\n" +
                    "      .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eee; }\n" +
                    "      .header h1 { color: #ff8c00; margin: 0; }\n" +
                    "      .content { padding: 20px 0; font-size: 16px; color: #333; }\n" +
                    "      .appointment-details { background-color: #fff8e1; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ff8c00; }\n" +
                    "      .old-appointment { background-color: #ffebee; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #f44336; }\n" +
                    "      .new-appointment { background-color: #e8f5e8; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #4caf50; }\n" +
                    "      .detail-row { margin: 8px 0; }\n" +
                    "      .detail-label { font-weight: bold; display: inline-block; width: 150px; }\n" +
                    "      .old-label { color: #f44336; }\n" +
                    "      .new-label { color: #4caf50; }\n" +
                    "      .highlight { background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 15px 0; border: 1px solid #ffeaa7; text-align: center; }\n" +
                    "      .reschedule-badge { background-color: #ffeaa7; color: #856404; padding: 8px 15px; border-radius: 20px; font-weight: bold; display: inline-block; }\n" +
                    "      .status-header { text-align: center; margin: 20px 0; }\n" +
                    "      .footer { font-size: 13px; color: #777; text-align: center; margin-top: 30px; border-top: 1px solid #eee; padding-top: 15px; }\n" +
                    "    </style>\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"container\">\n" +
                    "      <div class=\"header\">\n" +
                    "        <h1>Thông báo thay đổi lịch khám</h1>\n" +
                    "        <div class=\"reschedule-badge\">Đã thay đổi</div>\n" +
                    "      </div>\n" +
                    "      <div class=\"content\">\n" +
                    "        <p>Xin chào <strong>{{patientName}}</strong>,</p>\n" +
                    "        <p>Chúng tôi xin thông báo rằng lịch khám của bạn đã được thay đổi thời gian. Dưới đây là thông tin chi tiết:</p>\n" +
                    "        \n" +
                    "        <div class=\"appointment-details\">\n" +
                    "          <h3 style=\"margin-top: 0; color: #ff8c00;\">📋 Thông tin lịch khám</h3>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Mã lịch khám:</span>\n" +
                    "            <span>{{appointmentId}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Bác sĩ:</span>\n" +
                    "            <span>{{doctorName}} - {{specialty}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Hình thức:</span>\n" +
                    "            <span>{{consultationType}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Địa chỉ:</span>\n" +
                    "            <span>{{clinicAddress}}</span>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"status-header\">\n" +
                    "          <h3 style=\"color: #ff8c00; margin: 25px 0 15px 0;\">📅 Thay đổi thời gian</h3>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"old-appointment\">\n" +
                    "          <h4 style=\"margin-top: 0; color: #f44336;\">❌ Thời gian cũ</h4>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label old-label\">Ngày khám:</span>\n" +
                    "            <span>{{oldAppointmentDate}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label old-label\">Thời gian:</span>\n" +
                    "            <span>{{oldTimeSlot}}</span>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"new-appointment\">\n" +
                    "          <h4 style=\"margin-top: 0; color: #4caf50;\">✅ Thời gian mới</h4>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label new-label\">Ngày khám:</span>\n" +
                    "            <span>{{appointmentDate}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label new-label\">Thời gian:</span>\n" +
                    "            <span>{{timeSlot}}</span>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"appointment-details\">\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Lý do thay đổi:</span>\n" +
                    "            <span>{{rescheduleReason}}</span>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"highlight\">\n" +
                    "          <strong>Vui lòng có mặt trước 15 phút so với giờ hẹn mới</strong>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <p><strong>Lưu ý quan trọng:</strong></p>\n" +
                    "        <ul>\n" +
                    "          <li>Mang theo CCCD/CMND và các giấy tờ y tế liên quan</li>\n" +
                    "          <li>Nếu cần thay đổi thêm, vui lòng thông báo trước ít nhất 2 giờ</li>\n" +
                    "          <li>Liên hệ hotline: 1900-xxxx nếu có thắc mắc</li>\n" +
                    "        </ul>\n" +
                    "        \n" +
                    "        <p>Chúng tôi rất tiếc vì sự bất tiện này và cảm ơn sự thông cảm của bạn.</p>\n" +
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

            // Format time slot (new time)
            String timeSlotText = payload.getTimeSlot() != null ?
                (payload.getTimeSlot().getStartTime() != null && payload.getTimeSlot().getEndTime() != null ?
                    payload.getTimeSlot().getStartTime() + " - " + payload.getTimeSlot().getEndTime() :
                    "Không xác định") :
                "Không xác định";

            // Format appointment date (new date)
            String appointmentDateText = payload.getAppointmentDate() != null ?
                payload.getAppointmentDate().toString() :
                "Không xác định";

            // Format old appointment data (assuming these would be provided in payload or could be retrieved)
            String oldAppointmentDateText = "Không xác định";
            String oldTimeSlotText = "Không xác định";

            if (payload.getOldAppointment() != null) {
                oldAppointmentDateText = payload.getOldAppointment().getAppointmentDate() != null ?
                    payload.getOldAppointment().getAppointmentDate().toString() : "Không xác định";

                if (payload.getOldAppointment().getTimeSlot() != null &&
                    payload.getOldAppointment().getTimeSlot().getStartTime() != null &&
                    payload.getOldAppointment().getTimeSlot().getEndTime() != null) {
                    oldTimeSlotText = payload.getOldAppointment().getTimeSlot().getStartTime() + " - " +
                                    payload.getOldAppointment().getTimeSlot().getEndTime();
                }
            }

            // Determine reschedule reason
            String rescheduleReason = payload.getNote() != null && !payload.getNote().trim().isEmpty()
                    ? payload.getNote()
                    : "Theo yêu cầu của phòng khám";

            // Replace placeholders with actual values
            htmlBody = htmlBody.replace("{{patientName}}", payload.getPatient() != null && payload.getPatient().getFullName() != null ? payload.getPatient().getFullName() : "Bệnh nhân")
                    .replace("{{appointmentId}}", payload.getAppointmentId() != null ? payload.getAppointmentId() : "")
                    .replace("{{doctorName}}", payload.getDoctor() != null && payload.getDoctor().getFullName() != null ? payload.getDoctor().getFullName() : "")
                    .replace("{{specialty}}", payload.getDoctor() != null && payload.getDoctor().getSpecialty() != null ? payload.getDoctor().getSpecialty() : "")
                    .replace("{{appointmentDate}}", appointmentDateText)
                    .replace("{{timeSlot}}", timeSlotText)
                    .replace("{{oldAppointmentDate}}", oldAppointmentDateText)
                    .replace("{{oldTimeSlot}}", oldTimeSlotText)
                    .replace("{{consultationType}}", consultationTypeText)
                    .replace("{{clinicAddress}}", payload.getDoctor() != null && payload.getDoctor().getClinicAddress() != null ? payload.getDoctor().getClinicAddress() : "Không có")
                    .replace("{{rescheduleReason}}", rescheduleReason);

            // Check if patient and email are not null before setting recipient
            if (payload.getPatient() != null && payload.getPatient().getEmail() != null) {
                helper.setTo(payload.getPatient().getEmail());
                helper.setSubject("🔄 Thông báo thay đổi lịch khám - Mã: " + (payload.getAppointmentId() != null ? payload.getAppointmentId() : ""));
                helper.setText(htmlBody, true);

                mailSender.send(message);
            } else {
                logger.error("Cannot send reschedule email: patient or email is null");
            }
        } catch (Exception e) {
            String email = payload.getPatient() != null && payload.getPatient().getEmail() != null ?
                payload.getPatient().getEmail() : "unknown";
            logger.error("Failed to send reschedule email to: {}", email, e);
        } finally {
            try {
                if (payload.getPatient() != null && payload.getPatient().getUserId() != null) {
                    Notification notification = Notification.builder()
                            .recipient_id(payload.getPatient().getUserId())
                            .type(NotificationType.EMAIL)
                            .message("Thông báo thay đổi lịch khám - Mã: " +
                                (payload.getAppointmentId() != null ? payload.getAppointmentId() : ""))
                            .build();
                    notificationRepository.save(notification);

                    String email = payload.getPatient().getEmail() != null ?
                        payload.getPatient().getEmail() : "unknown";
                    logger.info("Reschedule email sent to: {}", email);
                }
            } catch (Exception ex) {
                logger.error("Failed to save notification", ex);
            }
        }
    }

    @Override
    public void sendEmailConfirmAppointmentStatus(AppointmentEventPayload payload) {
        // Implementation will be added in future updates
        logger.warn("sendEmailConfirmAppointmentStatus not implemented yet");
    }

    @Override
    public void sendEmailCompleteAppointmentStatus(MedicalRecordPayload payload) {
        try {
            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"vi\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"UTF-8\" />\n" +
                    "    <title>Thông báo hoàn thành khám bệnh</title>\n" +
                    "    <style>\n" +
                    "      body { font-family: Arial, sans-serif; background-color: #f6f6f6; margin: 0; padding: 0; }\n" +
                    "      .container { max-width: 650px; margin: 40px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05); }\n" +
                    "      .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #eee; }\n" +
                    "      .header h1 { color: #28a745; margin: 0; }\n" +
                    "      .content { padding: 20px 0; font-size: 16px; color: #333; }\n" +
                    "      .appointment-details { background-color: #f8fff8; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745; }\n" +
                    "      .medical-info { background-color: #e8f4ff; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007bff; }\n" +
                    "      .test-results-info { background-color: #fff3cd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107; }\n" +
                    "      .detail-row { margin: 8px 0; }\n" +
                    "      .detail-label { font-weight: bold; color: #28a745; display: inline-block; width: 150px; }\n" +
                    "      .medical-label { font-weight: bold; color: #007bff; display: inline-block; width: 150px; }\n" +
                    "      .test-label { font-weight: bold; color: #856404; display: inline-block; width: 150px; }\n" +
                    "      .highlight { background-color: #d1ecf1; padding: 15px; border-radius: 5px; margin: 15px 0; text-align: center; border: 1px solid #bee5eb; }\n" +
                    "      .complete-badge { background-color: #d4edda; color: #155724; padding: 8px 15px; border-radius: 20px; font-weight: bold; display: inline-block; }\n" +
                    "      .action-buttons { text-align: center; margin: 25px 0; }\n" +
                    "      .btn { display: inline-block; padding: 12px 25px; margin: 0 10px; text-decoration: none; border-radius: 5px; font-weight: bold; }\n" +
                    "      .btn-primary { background-color: #007bff; color: white; }\n" +
                    "      .prescription-note { background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; border: 1px solid #dee2e6; }\n" +
                    "      .footer { font-size: 13px; color: #777; text-align: center; margin-top: 30px; border-top: 1px solid #eee; padding-top: 15px; }\n" +
                    "    </style>\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"container\">\n" +
                    "      <div class=\"header\">\n" +
                    "        <h1>Khám bệnh hoàn thành</h1>\n" +
                    "        <div class=\"complete-badge\">✅ Đã hoàn thành</div>\n" +
                    "      </div>\n" +
                    "      <div class=\"content\">\n" +
                    "        <p>Xin chào,</p>\n" +
                    "        <p>Chúng tôi xin thông báo rằng ca khám của bạn đã được hoàn thành thành công. Dưới đây là thông tin chi tiết về buổi khám:</p>\n" +
                    "        \n" +
                    "        <div class=\"appointment-details\">\n" +
                    "          <h3 style=\"margin-top: 0; color: #28a745;\">📋 Thông tin ca khám</h3>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Mã lịch khám:</span>\n" +
                    "            <span>{{appointmentId}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"detail-label\">Ngày chẩn đoán:</span>\n" +
                    "            <span>{{dateDiagnosis}}</span>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"medical-info\">\n" +
                    "          <h3 style=\"margin-top: 0; color: #007bff;\">🏥 Thông tin y tế</h3>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"medical-label\">Chẩn đoán:</span>\n" +
                    "            <span>{{diagnosis}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"medical-label\">Triệu chứng:</span>\n" +
                    "            <span>{{symptoms}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"medical-label\">Ghi chú của bác sĩ:</span>\n" +
                    "            <span>{{doctorNote}}</span>\n" +
                    "          </div>\n" +
                    "          <div class=\"detail-row\">\n" +
                    "            <span class=\"medical-label\">Tình trạng sức khỏe:</span>\n" +
                    "            <span>{{statusHealth}}</span>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        {{prescriptionSection}}\n" +
                    "        \n" +
                    "        <div class=\"test-results-info\">\n" +
                    "          <h3 style=\"margin-top: 0; color: #856404;\">🧪 Thông tin xét nghiệm</h3>\n" +
                    "          <div class=\"highlight\" style=\"background-color: #fff3cd; border: 1px solid #ffeaa7;\">\n" +
                    "            <strong>⏰ Kết quả xét nghiệm sẽ được cập nhật lên hệ thống trong vòng 1-3 ngày làm việc</strong><br>\n" +
                    "            <small>Hệ thống sẽ cập nhật thông tin của bạn khi có kết quả xét nghiệm mới</small>\n" +
                    "          </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"highlight\">\n" +
                    "          <strong>🔔 Bạn có thể theo dõi hồ sơ y tế và kết quả xét nghiệm trên hệ thống của chúng tôi</strong>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"action-buttons\">\n" +
                    "          <a href=\"#\" class=\"btn btn-primary\">Xem hồ sơ y tế</a>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <p><strong>Hướng dẫn theo dõi:</strong></p>\n" +
                    "        <ul>\n" +
                    "          <li>Đăng nhập vào hệ thống để xem chi tiết hồ sơ y tế</li>\n" +
                    "          <li>Kết quả xét nghiệm sẽ được cập nhật trong vòng 1-3 ngày làm việc</li>\n" +
                    "          <li>Bạn sẽ nhận được thông báo khi có kết quả xét nghiệm mới</li>\n" +
                    "          <li>Liên hệ hotline: 1900-xxxx nếu có thắc mắc</li>\n" +
                    "        </ul>\n" +
                    "        \n" +
                    "        <p>Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi. Chúc bạn sớm hồi phục sức khỏe!</p>\n" +
                    "        <p>Trân trọng,<br /><strong>Đội ngũ y tế</strong></p>\n" +
                    "      </div>\n" +
                    "      <div class=\"footer\">\n" +
                    "        © 2025 Hệ thống chăm sóc sức khỏe Health Care. Mọi quyền được bảo lưu.\n" +
                    "      </div>\n" +
                    "    </div>\n" +
                    "  </body>\n" +
                    "</html>";

            // Build prescription section if available
            String prescriptionSection = "";
            if (payload.getTreatment() != null && !payload.getTreatment().trim().isEmpty()) {
                prescriptionSection = "<div class=\"prescription-note\">\n" +
                        "          <h4 style=\"margin-top: 0; color: #17a2b8;\">💊 Đơn thuốc và điều trị</h4>\n" +
                        "          <p>" + payload.getTreatment() + "</p>\n" +
                        "        </div>";
            }

            // Format date diagnosis
            String dateDiagnosisText = payload.getDateDiagnosis() != null ?
                payload.getDateDiagnosis().toString() : "Không xác định";

            // Replace placeholders with actual values from MedicalRecordPayload
            htmlBody = htmlBody.replace("{{appointmentId}}", payload.getAppointmentId() != null ? payload.getAppointmentId() : "")
                    .replace("{{dateDiagnosis}}", dateDiagnosisText)
                    .replace("{{diagnosis}}", payload.getDiagnosis() != null ? payload.getDiagnosis() : "Chưa có chẩn đoán")
                    .replace("{{symptoms}}", payload.getSymptoms() != null ? payload.getSymptoms() : "Không có")
                    .replace("{{doctorNote}}", payload.getDoctorNote() != null ? payload.getDoctorNote() : "Không có ghi chú")
                    .replace("{{statusHealth}}", payload.getStatusHealth() != null ? payload.getStatusHealth() : "Không xác định")
                    .replace("{{prescriptionSection}}", prescriptionSection);

            // Set email subject and content
            helper.setSubject("✅ Khám bệnh hoàn thành - Mã: " + (payload.getAppointmentId() != null ? payload.getAppointmentId() : ""));
            helper.setText(htmlBody, true);

            // Note: Since MedicalRecordPayload doesn't have patient email info,
            // this method would need to be called with additional patient info or
            // retrieve patient email separately. For now, log the completion.
            logger.info("Medical record completion email prepared for appointment: {}", payload.getAppointmentId());

        } catch (Exception e) {
            logger.error("Failed to prepare completion email for appointment: {}", payload.getAppointmentId(), e);
        } finally {
            try {
                // Save notification - Note: would need patient ID to save properly
                logger.info("Medical record processing completed for appointment: {}", payload.getAppointmentId());
            } catch (Exception ex) {
                logger.error("Failed to log medical record completion", ex);
            }
        }
    }

    @Override
    public void sendEmailRejectAppointmentStatus(AppointmentEventPayload payload) {
        // Implementation will be added in future updates
        logger.warn("sendEmailRejectAppointmentStatus not implemented yet");
    }

    @Override
    public void sendEmailRemindAppointment(AppointmentEventPayload payload) {
        // Implementation will be added in future updates
        logger.warn("sendEmailRemindAppointment not implemented yet");
    }
}
