package com.example.kaizenchat.service;

import com.example.kaizenchat.dto.UserLoginRequest;
import com.example.kaizenchat.dto.UserRegistrationRequest;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.InvalidRequestDataException;
import com.example.kaizenchat.exception.UserNotFoundException;

import java.util.Map;

public interface UserService {

    UserEntity findUserById(Long id) throws UserNotFoundException;

    Map<String, String> register(UserRegistrationRequest request);

    Map<String, String> login(UserLoginRequest request) throws InvalidRequestDataException;

    Map<String, String> refreshTokens(String refreshToken) throws InvalidRequestDataException;
}