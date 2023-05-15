package com.example.kaizenchat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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