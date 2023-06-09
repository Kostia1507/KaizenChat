package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.AvatarDTO;
import com.example.kaizenchat.dto.UserUpdateRequest;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.AvatarNotExistsException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.security.jwt.UserDetailsImpl;
import com.example.kaizenchat.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Map.of;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(OK)
    @GetMapping
    public Map<String, Long> getIdByAccessToken() {
        var userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return of("id", userDetails.getId());
    }

    @ResponseStatus(OK)
    @GetMapping("/phone/{phoneNumber}")
    public UserEntity getUserByPhoneNumber(@PathVariable String phoneNumber)
            throws UserNotFoundException {

        log.info("IN UserController -> getUserByPhoneNumber(): {}", phoneNumber);
        return userService.findUserByPhoneNumber(phoneNumber);
    }

    @ResponseStatus(OK)
    @GetMapping("/id/{userId}")
    public UserEntity getUserById(@PathVariable Long userId)
            throws UserNotFoundException {

        log.info("IN UserController -> getUserById(): {}", userId);
        return userService.findUserById(userId);
    }

    @ResponseStatus(OK)
    @PostMapping("/update")
    public Map<String, String> updateUser(@Valid @RequestBody UserUpdateRequest request)
            throws UserNotFoundException {

        log.info("IN UserController -> updateUser(): id={}", request.getId());
        userService.updateUser(request.getId(), request.getNickname(), null, request.getBio());
        return of("message", "user updated");
    }

    @ResponseStatus(OK)
    @PostMapping("/upload-avatar")
    public Map<String, String> uploadAvatar(@Valid @RequestBody AvatarDTO avatar)
            throws UserNotFoundException {

        var userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = userDetails.getId();

        log.info("IN UserController -> uploadAvatar(): userId={}", userId);

        boolean isUpdated = userService.updateAvatar(userId, avatar.getEncodedContent());
        if (isUpdated) {
            return of("message", "updated");
        }
        throw new UserNotFoundException("user is not defined");
    }

    @ResponseStatus(OK)
    @GetMapping("/{userId}/avatar")
    public AvatarDTO downloadAvatar(@PathVariable long userId)
            throws UserNotFoundException, AvatarNotExistsException {

        log.info("IN UserController -> downloadAvatar(): user-id={}", userId);
        return userService.downloadAvatar(userId);
    }

}