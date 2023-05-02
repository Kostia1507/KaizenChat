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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static java.time.ZonedDateTime.now;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(OK)
    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("IN AuthController register(): phone=[{}]", request.getPhoneNumber());
        return userService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            Map<String, String> responseBody = new HashMap<>(userService.login(request));
            responseBody.put("isRegistered", "true");
            return ResponseEntity.ok(responseBody);
        } catch (UsernameNotFoundException | InvalidRequestDataException e) {
            log.info("IN AuthController -> login(): invalid request data");
            return ResponseEntity.status(FORBIDDEN).body(Map.of("isLoggedIn", "false"));
        }
    }

    @ResponseStatus(OK)
    @PostMapping("/refresh")
    public Map<String, String> refresh(@Valid @RequestBody RefreshTokenRequest request)
            throws InvalidRequestDataException {

        log.info("IN AuthController -> refresh(): {}", now());
        return userService.refreshTokens(request.getOldRefreshToken());
    }
}