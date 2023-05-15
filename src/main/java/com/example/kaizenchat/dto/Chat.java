package com.example.kaizenchat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class Chat {

    private long id;
    private String name;
    private Long adminId;
    private Long userId;
    private String username;
    private String lastMessage;
    private ZonedDateTime lastMessageTime;

}