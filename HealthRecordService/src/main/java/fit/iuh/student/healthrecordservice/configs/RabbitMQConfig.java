package fit.iuh.student.healthrecordservice.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public FanoutExchange HealthRecordExchange() {
        return new FanoutExchange("HEALTH_RECORD_NOTIFICATION_EXCHANGE");
    }

    @Bean
    public Queue HealthMedicalRecordQueue() {
        return QueueBuilder.durable("HEALTH_RECORD_NOTIFICATION_QUEUE").build();
    }

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

    @Bean
    public Binding binding(Queue HealthMedicalRecordQueue, FanoutExchange HealthRecordExchange) {
        return BindingBuilder.bind(HealthMedicalRecordQueue).to(HealthRecordExchange);
    }
}
