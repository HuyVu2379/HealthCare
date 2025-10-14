package fit.iuh.student.communicationservice.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    /**
     * Queue for sending appointment schedule requests from CommunicationService to SchedulingService
     * CommunicationService publishes ScheduleEventMessage to this queue
     * SchedulingService consumes from this queue
     */
    @Bean
    public Queue scheduleSocketRequestQueue() {
        return QueueBuilder.durable("SCHEDULE_SOCKET_REQUEST_QUEUE").build();
    }

    /**
     * Queue for receiving appointment notification responses from SchedulingService to CommunicationService
     * SchedulingService publishes AppointmentEventMessage (with AppointmentData) to this queue
     * CommunicationService consumes from this queue to send WebSocket notifications
     */
    @Bean
    public Queue scheduleSocketResponseQueue() {
        return QueueBuilder.durable("SCHEDULE_SOCKET_RESPONSE_QUEUE").build();
    }
}
