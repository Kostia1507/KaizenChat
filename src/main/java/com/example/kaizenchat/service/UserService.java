package com.example.kaizenchat.service;

import com.example.kaizenchat.dto.AvatarDTO;
import com.example.kaizenchat.dto.UserLoginRequest;
import com.example.kaizenchat.dto.UserRegistrationRequest;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.AvatarNotExistsException;
import com.example.kaizenchat.exception.InvalidRequestDataException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.model.Avatar;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {

    UserEntity findUserById(Long id) throws UserNotFoundException;

    UserEntity findUserByPhoneNumber(String phoneNumber) throws UserNotFoundException;

    Map<String, String> register(UserRegistrationRequest request);

    Map<String, String> login(UserLoginRequest request) throws InvalidRequestDataException;

    Map<String, String> refreshTokens(String refreshToken) throws InvalidRequestDataException;

    void updateUser(Long userId, String nickname, String avatar, String bio) throws UserNotFoundException;

    boolean updateAvatar(Long userId, String encodedAvatar);

    AvatarDTO downloadAvatar(Long userId) throws AvatarNotExistsException, UserNotFoundException;

}