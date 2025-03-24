package ua.dymohlo.onlineStore.controler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.onlineStore.dto.request.UserLoginInRequest;
import ua.dymohlo.onlineStore.dto.request.UserRegistrationRequest;
import ua.dymohlo.onlineStore.dto.response.UserProfileResponse;
import ua.dymohlo.onlineStore.service.AuthService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse register(@RequestBody UserRegistrationRequest registrationRequest,
                                        HttpServletResponse response) {
        return authService.register(registrationRequest, response);
    }

    @PostMapping("/login")
    public UserProfileResponse login(@RequestBody UserLoginInRequest userLoginInRequest,
                                     HttpServletResponse response) {
        return authService.loginIn(userLoginInRequest, response);
    }
}