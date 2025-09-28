package fit.iuh.student.communicationservice.configs;

import fit.iuh.student.communicationservice.handlers.CustomWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final CustomWebSocketHandler customWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebSocketHandler, "/ws/communication")
                .setAllowedOrigins("*"); // Cho phép tất cả origins, có thể thay đổi theo yêu cầu bảo mật
//                .withSockJS(); // Hỗ trợ SockJS fallback

    }
}