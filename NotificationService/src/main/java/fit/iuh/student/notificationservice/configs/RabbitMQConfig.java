package fit.iuh.student.notificationservice.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue cho appointment notifications (giữ nguyên nếu cần)
    @Bean
    public Queue appointmentNotificationQueue() {
        return QueueBuilder.durable("APPOINTMENT_NOTIFICATION_QUEUE").build();
    }

    // Queue riêng cho Notification Service nhận medical record events
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable("NOTIFICATION_HEALTH_RECORD_QUEUE").build();
    }

    // Khai báo FanoutExchange cho medical record events
    @Bean
    public FanoutExchange healthRecordExchange() {
        return new FanoutExchange("HEALTH_RECORD_NOTIFICATION_EXCHANGE");
    }

    // Binding queue với exchange
    @Bean
    public Binding medicalRecordBinding(Queue notificationQueue,
                                        FanoutExchange healthRecordExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(healthRecordExchange);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}