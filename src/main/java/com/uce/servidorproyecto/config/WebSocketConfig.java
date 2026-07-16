package com.uce.servidorproyecto.config;

import com.uce.servidorproyecto.model.Usuario;
import com.uce.servidorproyecto.repository.UsuarioRepository;
import com.uce.servidorproyecto.security.MobileJwtService;
import com.uce.servidorproyecto.security.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final MobileJwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final MobileAuthProperties mobileAuthProperties;

    public WebSocketConfig(MobileJwtService jwtService,
                           UsuarioRepository usuarioRepository,
                           MobileAuthProperties mobileAuthProperties) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.mobileAuthProperties = mobileAuthProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(mobileAuthProperties.getAllowedOrigins().toArray(String[]::new))
                .addInterceptors(sessionUserHandshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompPrincipalInterceptor());
    }

    private HandshakeInterceptor sessionUserHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes) {
                if (request instanceof ServletServerHttpRequest servletRequest) {
                    HttpSession session = servletRequest.getServletRequest().getSession(false);
                    if (session != null) {
                        Usuario usuario = (Usuario) session.getAttribute(SecurityUtils.SESSION_USUARIO);
                        if (usuario != null && usuario.getId() != null) {
                            attributes.put("userId", usuario.getId());
                        }
                    }
                }
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {
                // no-op
            }
        };
    }

    private ChannelInterceptor stompPrincipalInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
                    return message;
                }
                Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                Object sessionUserId = sessionAttrs == null ? null : sessionAttrs.get("userId");
                Long userId = sessionUserId == null ? bearerUserId(accessor) : asLong(sessionUserId);
                if (userId == null) {
                    throw new AccessDeniedException("STOMP CONNECT requiere sesión web o Authorization Bearer válido");
                }
                accessor.setUser((Principal) () -> String.valueOf(userId));
                return message;
            }
        };
    }

    private Long bearerUserId(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null) {
            authorization = accessor.getFirstNativeHeader("authorization");
        }
        if (authorization == null
                || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return jwtService.parseUserId(authorization.substring(7).trim())
                .flatMap(usuarioRepository::findById)
                .filter(usuario -> "ACTIVO".equals(usuario.getEstado()))
                .map(Usuario::getId)
                .orElse(null);
    }

    private Long asLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
