package ua.dymohlo.onlineStore.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.dymohlo.onlineStore.dto.request.UserLoginInRequest;
import ua.dymohlo.onlineStore.dto.request.UserRegistrationRequest;
import ua.dymohlo.onlineStore.dto.response.UserProfileResponse;
import ua.dymohlo.onlineStore.entity.User;
import ua.dymohlo.onlineStore.exception.AuthenticationException;
import ua.dymohlo.onlineStore.exception.UserAlreadyExistsException;
import ua.dymohlo.onlineStore.model.Role;
import ua.dymohlo.onlineStore.repository.UserRepository;
import ua.dymohlo.onlineStore.security.JwtTokenService;
import jakarta.servlet.http.Cookie;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserProfileResponse register(UserRegistrationRequest request, HttpServletResponse response) {
        userRepository.findByEmailIgnoreCase(request.getEmail())
                .ifPresent(existingUser -> {
                    throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
                });

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullname(request.getFullName())
                .role(Role.USER.name())
                .build();

        User savedUser = userRepository.save(newUser);

        String token = jwtTokenService.generateToken(savedUser);

        addJwtCookie(response, token);

        return UserProfileResponse.builder()
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullname())
                .role(savedUser.getRole())
                .token(token)
                .build();
    }

    public UserProfileResponse loginIn(UserLoginInRequest userLoginInRequest, HttpServletResponse response) {
        User user = userRepository.findByEmailIgnoreCase(userLoginInRequest.getEmail())
                .orElseThrow(() -> new AuthenticationException("Email not found"));

        if (!passwordEncoder.matches(userLoginInRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Incorrect password");
        }

        String token = jwtTokenService.generateToken(user);

        addJwtCookie(response, token);

        return UserProfileResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullname())
                .role(user.getRole())
                .token(token)
                .build();
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
    }
}