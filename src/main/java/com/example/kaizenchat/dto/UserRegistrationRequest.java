package com.example.kaizenchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class UserRegistrationRequest {
    private String phoneNumber;
    private String nickname;
    private String userPhoto;
    private String password;

    public static boolean hasEmptyField(UserRegistrationRequest request) {
        return request.phoneNumber == null || request.phoneNumber.isEmpty() ||
                request.nickname == null || request.nickname.isEmpty() ||
                request.userPhoto == null || request.userPhoto.isEmpty() ||
                request.password == null || request.password.isEmpty();
    }
}