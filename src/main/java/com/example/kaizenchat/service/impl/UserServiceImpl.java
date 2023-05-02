package com.example.kaizenchat.service.impl;

import com.example.kaizenchat.dto.UserLoginRequest;
import com.example.kaizenchat.dto.UserRegistrationRequest;
import com.example.kaizenchat.entity.RoleEntity;
import com.example.kaizenchat.entity.UserEntity;
import com.example.kaizenchat.exception.AvatarNotExistsException;
import com.example.kaizenchat.exception.InvalidRequestDataException;
import com.example.kaizenchat.exception.UserNotFoundException;
import com.example.kaizenchat.model.Avatar;
import com.example.kaizenchat.repository.RoleRepository;
import com.example.kaizenchat.repository.UserRepository;
import com.example.kaizenchat.security.jwt.JWTProvider;
import com.example.kaizenchat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.example.kaizenchat.security.jwt.JWTType.ACCESS;
import static com.example.kaizenchat.security.jwt.JWTType.REFRESH;
import static com.example.kaizenchat.utils.MultipartFileUtils.getFileExtension;
import static java.lang.String.format;

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
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(format("user with id:%d not found", id)));
    }

    @Override
    public UserEntity findUserByPhoneNumber(String phoneNumber) throws UserNotFoundException {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException(format("user with phone-number:[%s] not found", phoneNumber)));
    }

    @Override
    public Map<String, String> refreshTokens(String oldRefreshToken) throws InvalidRequestDataException {
        log.info("IN UserService -> refreshTokens()");

        UserEntity user = userRepository
                .findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new InvalidRequestDataException("refresh token was not found"));

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

    @Override
    public void updateUser(Long userId, String nickname, String avatar, String bio) throws UserNotFoundException {
        UserEntity user = findUserById(userId);
        user.setNickname(nickname != null ? nickname : user.getNickname());
        user.setAvatar(avatar != null ? avatar : user.getAvatar());
        user.setBio(bio != null ? bio : user.getBio());
        userRepository.save(user);
    }

    @Override
    public boolean updateAvatar(MultipartFile avatar, Long userId) {
        log.info("IN UserService -> updateAvatar()");

        String filename = String.format("img-%d_%d.%s",
                userId, Instant.now().toEpochMilli(), getFileExtension(avatar));

        Path destination = getImageDestination(filename);

        try {
            avatar.transferTo(destination);
            updateUser(userId, null, destination.toString(), null);
            return true;
        } catch (IOException | UserNotFoundException e) {
            log.error("IN UserService -> updateAvatar(): {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Avatar downloadAvatar(Long userId) throws UserNotFoundException, AvatarNotExistsException {
        log.info("IN UserService -> downloadAvatar()");
        UserEntity user = findUserById(userId);
        try {
            Path path = Path.of(user.getAvatar());
            String pathString = path.toString();

            String ext = pathString.substring(pathString.lastIndexOf(".") + 1);
            MediaType type = MediaType.valueOf("images/" + ext);
            byte[] bytes = Files.readAllBytes(path);

            return new Avatar(pathString, type, bytes);
        } catch (IOException e) {
            log.error("IN UserService -> downloadAvatar(): {}", e.getMessage());
            throw new AvatarNotExistsException(format("avatar for user:%d was not found", userId), e);
        }
    }

    private Path getImageDestination(String filename) {
        return Path.of("src", "main", "resources", "images", filename);
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