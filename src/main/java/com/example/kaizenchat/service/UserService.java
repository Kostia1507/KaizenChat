package com.example.kaizenchat.service;

import com.example.kaizenchat.dto.UserLoginRequest;
import com.example.kaizenchat.dto.UserRegistrationRequest;
import com.example.kaizenchat.exception.InvalidRequestDataException;

import java.util.Map;

public interface UserService {
    Map<String, String> register(UserRegistrationRequest request);

    Map<String, String> login(UserLoginRequest request) throws InvalidRequestDataException;

    Map<String, String> refreshTokens(String refreshToken) throws InvalidRequestDataException;
}