package com.example.kaizenchat.service.impl;

import com.example.kaizenchat.dto.UserLoginRequest;
import com.example.kaizenchat.dto.UserRegistrationRequest;
import com.example.kaizenchat.entity.RoleEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.InvalidRequestDataException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.repository.RoleRepository;
import com.example.kaizenchat.repository.UserRepository;
import com.example.kaizenchat.security.jwt.JWTProvider;
import com.example.kaizenchat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.example.kaizenchat.security.jwt.JWTType.ACCESS;
import static com.example.kaizenchat.security.jwt.JWTType.REFRESH;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final JWTProvider jwtProvider;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(JWTProvider jwtProvider, UserRepository userRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity findUserById(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public Map<String, String> refreshTokens(String oldRefreshToken) throws InvalidRequestDataException {
        log.info("IN UserService -> refreshTokens()");

        UserEntity user = userRepository
                .findByRefreshToken(oldRefreshToken)
                .orElseThrow(InvalidRequestDataException::new);

        // update refresh token
        Map<String, String> tokens = generatesTokens(user.getNickname(), user.getPhoneNumber());
        user.setRefreshToken(tokens.get("refreshToken"));
        userRepository.save(user);
        return tokens;
    }

    @Override
    public Map<String, String> register(UserRegistrationRequest request) {
        log.info("IN UserService -> register()");
        UserEntity user = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);
        if (user != null) {
            throw new BadCredentialsException("Such phone-number already exists");
        }

        // add role to new user
        Set<RoleEntity> roles = new HashSet<>();
        roleRepository.findByName("ROLE_USER").ifPresent(roles::add);

        Map<String, String> tokenPair = generatesTokens(request.getNickname(), request.getPhoneNumber());

        // build user
        user = UserEntity.builder()
                .phoneNumber(request.getPhoneNumber())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .avatar(request.getUserPhoto())
                .roles(roles)
                .registration(ZonedDateTime.now())
                .refreshToken(tokenPair.get("refreshToken"))
                .build();

        userRepository.save(user);
        return tokenPair;
    }

    @Override
    public Map<String, String> login(UserLoginRequest request) throws UsernameNotFoundException, InvalidRequestDataException {
        log.info("IN UserService -> login()");

        UserEntity user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new UsernameNotFoundException("User is not registered"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestDataException();
        }

        // update refresh token
        Map<String, String> tokens = generatesTokens(user.getNickname(), user.getPhoneNumber());
        user.setRefreshToken(tokens.get("refreshToken"));
        userRepository.save(user);
        return tokens;
    }

    private Map<String, String> generatesTokens(String nickname, String phoneNumber) {
        log.info("IN UserService -> generatesTokens(): gen tokens for user[phone:{}]", phoneNumber);

        String accessToken = jwtProvider.generateToken(ACCESS, nickname, phoneNumber);
        String accessExpiration = jwtProvider.getExpirationDate(accessToken, ACCESS).toString();
        String refreshToken = jwtProvider.generateToken(REFRESH, nickname, phoneNumber);
        String refreshExpiration = jwtProvider.getExpirationDate(refreshToken, REFRESH).toString();

        return Map.of(
                "accessToken", accessToken,
                "accessTokenExpiration", accessExpiration.substring(0, accessExpiration.indexOf("[")),
                "refreshToken", refreshToken,
                "refreshTokenExpiration", refreshExpiration.substring(0, refreshExpiration.indexOf("["))
        );
    }
}