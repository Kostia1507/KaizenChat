package com.example.kaizenchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddMemberToChatRequest {
    private Long chatId;
    private boolean privacyMode;
    private String password;

    public boolean hasEmptyField() {
        if (chatId == null) {
            return false;
        }
        return privacyMode && !(password == null || password.isEmpty());
    }
}