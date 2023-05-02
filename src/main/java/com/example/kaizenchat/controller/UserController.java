package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.UserUpdateRequest;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.AvatarNotExistsException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.model.Avatar;
import com.example.kaizenchat.security.jwt.UserDetailsImpl;
import com.example.kaizenchat.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static com.example.kaizenchat.utils.MultipartFileUtils.isNotValidFileSize;
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

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<Map<String, UserEntity>> getUserByPhoneNumber(@PathVariable String phoneNumber)
            throws UserNotFoundException {

        log.info("IN UserController -> getUserByPhoneNumber(): {}", phoneNumber);
        UserEntity user = userService.findUserByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(of("user", user));
    }

    @ResponseStatus(NO_CONTENT)
    @GetMapping("/exists/{phoneNumber}")
    public void checkPhoneNumberExistence(@PathVariable String phoneNumber) throws UserNotFoundException {
        log.info("IN UserController -> checkPhoneNumberExistence(): {}", phoneNumber);
        userService.findUserByPhoneNumber(phoneNumber);
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<Map<String, UserEntity>> getUserById(@PathVariable Long userId)
            throws UserNotFoundException {

        log.info("IN UserController -> getUserById(): {}", userId);
        UserEntity user = userService.findUserById(userId);
        return ResponseEntity.ok(of("user", user));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, String>> updateUser(@Valid @RequestBody UserUpdateRequest request)
            throws UserNotFoundException {

        log.info("IN UserController -> updateUser(): id={}", request.getId());
        userService.updateUser(request.getId(), request.getNickname(), null, request.getBio());
        return ResponseEntity.ok().body(of("message", "user updated"));
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
    public ResponseEntity<byte[]> downloadAvatar(@PathVariable long userId)
            throws UserNotFoundException, AvatarNotExistsException {

        log.info("IN UserController -> downloadAvatar(): user-id={}", userId);
        Avatar avatar = userService.downloadAvatar(userId);
        return ResponseEntity.ok()
                .contentType(avatar.contentType())
                .body(avatar.bytes());
    }

}