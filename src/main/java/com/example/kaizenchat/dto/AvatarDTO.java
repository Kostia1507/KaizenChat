package com.example.kaizenchat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AvatarDTO {

    @NotBlank(message = "should be not blank")
    private String encodedContent;

}