package com.example.kaizenchat.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Slf4j
public class CustomStompErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, @NonNull Throwable ex) {
        Throwable causeEx = ex.getCause();
        log.error("CustomStompErrorHandler -> handleClientMessage(): {}", causeEx.getMessage());
        return super.handleClientMessageProcessingError(clientMessage, causeEx);
    }

}