package com.example.kaizenchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Chat {

    private long id;
    private String name;
    private Long userId;
    private String username;
    private String lastMessage;
    private ZonedDateTime lastMessageTime;

}