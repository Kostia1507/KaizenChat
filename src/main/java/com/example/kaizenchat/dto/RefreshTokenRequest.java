package com.example.kaizenchat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RefreshTokenRequest {

    @NotBlank(message = "should not be blank")
    private String oldRefreshToken;

}