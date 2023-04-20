package com.example.kaizenchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class GroupChatCreationRequest {
    private String name;
    private boolean privacyMode;
    private String password;

    public static boolean hasEmptyField(GroupChatCreationRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            return true;
        }
        if (request.isPrivacyMode()) {
            return request.getPassword() == null || request.getPassword().isEmpty();
        }
        return false;
    }
}