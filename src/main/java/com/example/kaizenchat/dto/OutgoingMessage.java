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
    private String senderNickname;
    private Long chatId;
    private String body;
    private ZonedDateTime timeStamp;
}