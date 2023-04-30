package com.example.kaizenchat.controller;

import com.example.kaizenchat.dto.RefreshTokenRequest;
import com.example.kaizenchat.dto.UserLoginRequest;
import com.example.kaizenchat.dto.UserRegistrationRequest;
import com.example.kaizenchat.exception.InvalidRequestDataException;
import com.example.kaizenchat.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("IN AuthController register(): body {}", request);
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (BadCredentialsException e) {
            log.info("IN AuthController -> register(): invalid request data");
            return ResponseEntity.status(FORBIDDEN).body(Map.of("message", "invalid request"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            Map<String, String> responseBody = new HashMap<>(userService.login(request));
            responseBody.put("isRegistered", "true");
            return ResponseEntity.ok(responseBody);
        } catch (UsernameNotFoundException | InvalidRequestDataException e) {
            log.info("IN AuthController -> login(): invalid request data");
            return ResponseEntity.status(FORBIDDEN).body(Map.of("isRegistered", "false"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            return ResponseEntity.ok(userService.refreshTokens(request.getOldRefreshToken()));
        } catch (InvalidRequestDataException e) {
            log.info("IN AuthController -> refresh(): invalid request data");
            return ResponseEntity.status(FORBIDDEN).body(Map.of("message", "wrong refresh token"));
        }
    }
}