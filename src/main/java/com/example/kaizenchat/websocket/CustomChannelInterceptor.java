package com.example.kaizenchat.websocket;

import com.example.kaizenchat.entity.ChatEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.JWTInvalidException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.exception.UserViolationPermissionsException;
import com.example.kaizenchat.security.jwt.JWTProvider;
import com.example.kaizenchat.security.jwt.JWTType;
import com.example.kaizenchat.security.jwt.UserDetailsImpl;
import com.example.kaizenchat.service.UserService;
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
import java.util.Objects;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.springframework.messaging.simp.stomp.StompCommand.*;

@Slf4j
public class CustomChannelInterceptor implements ChannelInterceptor {

    private final JWTProvider jwtProvider;
    private final UserService userService;

    public CustomChannelInterceptor(JWTProvider jwtProvider, UserService userService) {
        this.jwtProvider = jwtProvider;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        MessageHeaders messageHeaders = message.getHeaders();
        SimpMessageType messageType = (SimpMessageType) messageHeaders.get("simpMessageType");
        String commandType = messageType.name();

        log.info("IN CustomChannelInterceptor -> preSend(): message-type: {}", commandType);

        // just pass through without jwt verifying when disconnection
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
                log.error("CustomChannelInterceptor(): {}", errorMessage);
                throw new JWTInvalidException(errorMessage);
            } else {
                Authentication authentication = jwtProvider.getAuthentication(token);
                if (!commandType.equals(CONNECT.name())) {
                    if (commandType.equals(SUBSCRIBE.name())) {
                        validateSubscription(headerAccessor, authentication);
                    }
                    headerAccessor.setUser(authentication);
                }
            }
        } else {
            throw new JWTInvalidException("JWT is not present");
        }

        log.info("OUT CustomChannelInterceptor -> preSend(): message-type: {}", messageType);

        headerAccessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(message.getPayload(), headerAccessor.getMessageHeaders());
    }

    private void validateSubscription(StompHeaderAccessor accessor, Authentication auth) {
        var userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long id = userDetails.getId();
        String destination = accessor.getDestination();
        log.info("CustomChannelInterceptor -> validateSubscription(): userId={} dest={}",
                id,
                destination
        );

        try {
            UserEntity user = userService.findUserById(id);

            // parse destination
            // ex. dest=/duo-chat/3 => split by "/" => [, duo-chat, 3]
            String[] parts = Objects.requireNonNull(destination).split("/");
            Long subId = Long.parseLong(parts[2]);

            if (parts[1].equals("user") && !user.getId().equals(subId)) {

                throw new UserViolationPermissionsException(
                        format("user=%d tried to get messages of user=%d", id, subId)
                );

            } else if (parts[1].equals("chatroom") || parts[1].equals("duo-chat")) {

                Predicate<ChatEntity> byId = chat -> chat.getId().equals(subId);

                // find chat with subId in user's group chats
                long count = user.getGroupChats().stream()
                        .filter(byId)
                        .count();

                if (count != 0) {
                    return;
                }

                // find chat with subId in user's duo chats
                count = user.getDuoChats().stream()
                        .filter(byId)
                        .count();

                // if user does not belong to neither group nor duo chat with subId
                if (count == 0) {
                    throw new UserViolationPermissionsException(
                            format("user=%d tried to get data from a chat=%d that does not belong to him",
                                    id, subId
                            )
                    );
                }
            }
        } catch (UserNotFoundException | UserViolationPermissionsException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}