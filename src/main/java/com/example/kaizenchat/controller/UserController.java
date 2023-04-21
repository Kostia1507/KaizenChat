package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.UserUpdateRequest;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            userService.updateUser(request.getId(), request.getNickname(), request.getAvatar(), request.getBio());
            return ResponseEntity.ok().body(Map.of("message","user updated"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(Map.of("message","wrong id"));
        }
    }
}
