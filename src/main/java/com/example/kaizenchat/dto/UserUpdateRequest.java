package com.example.kaizenchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class UserUpdateRequest {

    @NotNull(message = "should not be null")
    private Long id;

    @NotBlank(message = "should not be blank")
    @Size(min = 4, message = "length should be 4 or longer")
    private String nickname;

    private String bio;

}