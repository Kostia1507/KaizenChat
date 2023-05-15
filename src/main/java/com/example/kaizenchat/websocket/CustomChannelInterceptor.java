package com.example.kaizenchat.websocket;

import com.example.kaizenchat.exception.JWTInvalidException;
import com.example.kaizenchat.security.jwt.JWTProvider;
import com.example.kaizenchat.security.jwt.JWTType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.springframework.messaging.simp.stomp.StompCommand.CONNECT;
import static org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT;

@Slf4j
public class CustomChannelInterceptor implements ChannelInterceptor {

    private final JWTProvider jwtProvider;

    public CustomChannelInterceptor(JWTProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        MessageHeaders messageHeaders = message.getHeaders();
        SimpMessageType messageType = (SimpMessageType) messageHeaders.get("simpMessageType");
        String commandType = messageType.name();

        log.info("IN ChannelInterceptor -> preSend(): message-type: {}", commandType);

        // just pass through without jwt verifying when it's disconnection or subscription
        if (commandType.equals(DISCONNECT.name())) {
            return message;
        }

        // validate authorization header
        String bearer;
        List<String> authList = headerAccessor.getNativeHeader("Authorization");
        if (authList == null || authList.isEmpty()) {
            log.error("IN ChannelInterceptor -> preSend(): authList is empty");
            throw new JWTInvalidException("Jwt token is not present");
        } else if ((bearer = authList.get(0)) == null) {
            log.error("IN ChannelInterceptor -> preSend(): token is not present");
            throw new JWTInvalidException("JWT token is not present");
        }

        // time to validate jwt token
        String token = jwtProvider.getToken(bearer);
        if (token != null) {
            if (!jwtProvider.isTokenValid(token, JWTType.ACCESS)) {
                String errorMessage = "Invalid token";
                log.error("ChannelInterceptor(): {}", errorMessage);
                throw new JWTInvalidException(errorMessage);
            } else {
                Authentication authentication = jwtProvider.getAuthentication(token);
                if (!commandType.equals(CONNECT.name())) {
                    headerAccessor.setUser(authentication);
                }
            }
        } else {
            throw new JWTInvalidException("JWT is not present");
        }

        log.info("OUT ChannelInterceptor -> preSend(): message-type: {}", messageType);

        headerAccessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(message.getPayload(), headerAccessor.getMessageHeaders());
    }
}
