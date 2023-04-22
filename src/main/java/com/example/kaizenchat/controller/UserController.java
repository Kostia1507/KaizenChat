package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.UserUpdateRequest;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.AvatarNotExistsException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.model.Avatar;
import com.example.kaizenchat.security.jwt.UserDetailsImpl;
import com.example.kaizenchat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.example.kaizenchat.utils.MultipartFileUtils.isNotValidFileSize;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/phone/{phoneNumber}")
    public ResponseEntity<Map<String, UserEntity>> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        try {
            UserEntity user = userService.findUserByPhoneNumber(phoneNumber);
            return ResponseEntity.ok(Map.of("user", user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(Map.of("user", new UserEntity()));
        }
    }

    @GetMapping(path = "/id/{userId}")
    public ResponseEntity<Map<String, UserEntity>> getUserById(@PathVariable Long userId) {
        try {
            UserEntity user = userService.findUserById(userId);
            return ResponseEntity.ok(Map.of("user", user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(Map.of("user", new UserEntity()));
        }
    }

    @PostMapping(path = "/update")
    public ResponseEntity<Map<String, String>> updateUser(@RequestBody UserUpdateRequest request) {
        try {
            userService.updateUser(request.getId(), request.getNickname(), null, request.getBio());
            return ResponseEntity.ok().body(Map.of("message","user updated"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(Map.of("message","wrong id"));
        }
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        log.info("IN UserController -> uploadAvatar(): file-size={} bytes", file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    of("message", "file is not present")
            );
        } else if (isNotValidFileSize(file)) {
            return ResponseEntity.badRequest().body(
                    of("message", "file size is greater than 3MB")
            );
        } else if (isNotValidFileSize(file)) {
            return ResponseEntity.badRequest().body(
                    of("message", "uploaded file is not an image")
            );
        }

        var userDetails = (UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        boolean isUpdated = userService.updateAvatar(file, userDetails.getId());
        if (isUpdated) {
            return ResponseEntity.ok(of("message", "updated"));
        } else {
            return ResponseEntity.status(FORBIDDEN).body(
                    of("message", "user is not defined")
            );
        }
    }

    @GetMapping("/{userId}/download-avatar")
    public ResponseEntity<byte[]> downloadAvatar(@PathVariable long userId) {
        log.info("IN UserController -> downloadAvatar(): user-id={}", userId);
        try {
            Avatar avatar = userService.downloadAvatar(userId);
            return ResponseEntity.ok()
                    .contentType(avatar.contentType())
                    .body(avatar.bytes());
        } catch (UserNotFoundException | AvatarNotExistsException e) {
            log.info("IN UserController -> downloadAvatar(): {}", e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(new byte[0]);
        }
    }

}