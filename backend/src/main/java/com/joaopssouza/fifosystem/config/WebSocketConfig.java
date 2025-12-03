package com.joaopssouza.fifosystem.config;

import com.joaopssouza.fifosystem.domain.entity.User;
import com.joaopssouza.fifosystem.domain.repository.UserRepository;
import com.joaopssouza.fifosystem.security.JwtTokenProvider;
import com.joaopssouza.fifosystem.websocket.DashboardWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final DashboardWebSocketHandler dashboardWebSocketHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(dashboardWebSocketHandler, "/api/ws")
                .setAllowedOrigins("*") // Importante para o CORS do WebSocket
                .addInterceptors(new JwtHandshakeInterceptor());
    }

    // Valida o token que vem na URL: ws://...?token=...
    private class JwtHandshakeInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                     WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                String query = servletRequest.getServletRequest().getQueryString();
                if (query != null && query.contains("token=")) {
                    try {
                        String token = query.split("token=")[1].split("&")[0];
                        
                        // --- LOGS DE DEBUG ---
                        System.out.println("WS Handshake: Validando token...");
                        
                        if (jwtTokenProvider.validateToken(token)) {
                            String username = jwtTokenProvider.getUsername(token);
                            User user = userRepository.findByUsername(username).orElse(null);
                            if (user != null) {
                                attributes.put("user", user);
                                System.out.println("WS Handshake: Sucesso para " + username);
                                return true;
                            } else {
                                System.out.println("WS Handshake: Usuário não encontrado no banco.");
                            }
                        } else {
                            System.out.println("WS Handshake: Token inválido ou expirado.");
                        }
                    } catch (Exception e) {
                        System.out.println("WS Handshake Erro: " + e.getMessage());
                        return false;
                    }
                } else {
                    System.out.println("WS Handshake: Token não fornecido na URL.");
                }
            }
            return false;
        }
        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                 WebSocketHandler wsHandler, Exception exception) {}
    }
}