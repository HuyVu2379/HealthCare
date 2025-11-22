package fit.iuh.student.schedulingservice.enums;

public enum AppointmentStatus {
    PAYMENT_PENDING,  // Chờ thanh toán - chưa gửi thông báo cho bác sĩ
    PENDING,          // Chờ bác sĩ xác nhận - đã thanh toán thành công
    CONFIRMED,        
    CANCELED,         
    REJECTED,         
    COMPLETED,        
    NO_SHOW,          
    RESCHEDULED       
}