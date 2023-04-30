package com.example.kaizenchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class UserLoginRequest {

    @NotBlank(message = "should not be blank")
    @Size(min = 13, max = 13, message = "length should be 13")
    private String phoneNumber;

    @NotBlank(message = "should not be blank")
    @Size(min = 8, message = "length should be 8 or longer")
    private String password;

}