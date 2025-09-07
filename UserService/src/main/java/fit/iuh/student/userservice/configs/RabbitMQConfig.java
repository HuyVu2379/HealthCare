package fit.iuh.student.userservice.configs;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable("USER_NOTIFICATION_QUEUE").build();
    }

    @Bean
    public Queue healthRecordUserQueue() {
        return QueueBuilder.durable("HEALTH_RECORD_USER_NOTIFICATION_QUEUE").build();
    }

    @Bean
    public FanoutExchange healthRecordExchange() {
        return new FanoutExchange("HEALTH_RECORD_NOTIFICATION_EXCHANGE");
    }

    @Bean
    public Binding medicalRecordBinding(Queue healthRecordUserQueue,
                                        FanoutExchange healthRecordExchange) {
        return BindingBuilder.bind(healthRecordUserQueue)
                .to(healthRecordExchange);
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
}
