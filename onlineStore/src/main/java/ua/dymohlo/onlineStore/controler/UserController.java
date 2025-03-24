package ua.dymohlo.onlineStore.controler;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.onlineStore.dto.response.UserDetailedResponse;
import ua.dymohlo.onlineStore.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile/{email}")
    public ResponseEntity<UserDetailedResponse> getUserProfile(@PathVariable String email, Authentication authentication) {
        UserDetailedResponse profile = userService.getUserProfile(email, authentication);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/profile/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email, Authentication authentication) {
        userService.deleteUser(email, authentication);
        return ResponseEntity.noContent().build();
    }
}