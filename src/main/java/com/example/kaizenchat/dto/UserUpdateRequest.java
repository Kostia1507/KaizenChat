package com.example.kaizenchat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class UserUpdateRequest {
    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
}
