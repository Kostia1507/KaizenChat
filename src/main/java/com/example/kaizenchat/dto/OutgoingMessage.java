package com.example.kaizenchat.dto;

import lombok.*;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class OutgoingMessage {
    private Action action;
    private Long senderId;
    private Long messageId;
    private String senderNickname;
    private Long chatId;
    private String body;
    private ZonedDateTime timeStamp;

    public OutgoingMessage(Action action, Long senderId, String senderNickname,
                           Long chatId, String body, ZonedDateTime timeStamp){
        this.action = action;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.chatId = chatId;
        this.body = body;
        this.timeStamp = timeStamp;
    }
}