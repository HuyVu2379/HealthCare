package fit.iuh.student.communicationservice.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cho phép một bộ môi giới tin nhắn dựa trên bộ nhớ đơn giản để truyền tin nhắn trở lại máy khách
        config.enableSimpleBroker("/topic", "/queue");
        // Đặt tiền tố đích của ứng dụng cho các tin nhắn được liên kết với các phương thức được chú thích bằng @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký điểm cuối "/ws", kích hoạt tùy chọn dự phòng SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
