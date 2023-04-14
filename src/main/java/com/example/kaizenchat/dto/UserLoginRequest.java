package com.example.kaizenchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class UserLoginRequest {
    private String phoneNumber;
    private String password;

    public static boolean hasEmptyField(UserLoginRequest request) {
        return request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty();
    }
}