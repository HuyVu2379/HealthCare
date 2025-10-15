package fit.iuh.student.schedulingservice.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue appointmentQueue() {
        return QueueBuilder.durable("APPOINTMENT_NOTIFICATION_QUEUE").build();
    }

    @Bean
    public Queue scheduleQueue() {
        return QueueBuilder.durable("SCHEDULE_HEALTH_RECORD_QUEUE").build();
    }

    /**
     * Queue for receiving appointment schedule requests from CommunicationService
     * CommunicationService publishes ScheduleEventMessage to this queue
     * SchedulingService (this service) consumes from this queue to process appointment bookings
     */
    @Bean
    public Queue scheduleSocketRequestQueue() {
        return QueueBuilder.durable("SCHEDULE_SOCKET_REQUEST_QUEUE").build();
    }

    /**
     * Queue for sending appointment notification responses to CommunicationService
     * SchedulingService (this service) publishes AppointmentEventMessage (with AppointmentData) to this queue
     * CommunicationService consumes from this queue to send WebSocket notifications
     */
    @Bean
    public Queue scheduleSocketResponseQueue() {
        return QueueBuilder.durable("SCHEDULE_SOCKET_RESPONSE_QUEUE").build();
    }

    @Bean
    public FanoutExchange healthRecordExchange() {
        return new FanoutExchange("HEALTH_RECORD_NOTIFICATION_EXCHANGE");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Binding medicalRecordBinding(Queue scheduleQueue,
                                        FanoutExchange healthRecordExchange) {
        return BindingBuilder.bind(scheduleQueue)
                .to(healthRecordExchange);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
