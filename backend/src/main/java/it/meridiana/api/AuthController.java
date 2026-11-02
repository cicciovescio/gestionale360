package it.meridiana.api;

import it.meridiana.domain.AppUser;
import it.meridiana.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/auth/login")
    public Api.LoginResponse login(@RequestBody Api.LoginRequest req) {
        return auth.login(req);
    }

    @GetMapping("/me")
    public Api.UserView me(@AuthenticationPrincipal AppUser user) {
        return Mapper.user(user);
    }
}
