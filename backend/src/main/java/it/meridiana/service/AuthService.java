package it.meridiana.service;

import it.meridiana.api.Api;
import it.meridiana.api.Mapper;
import it.meridiana.common.ApiException;
import it.meridiana.domain.AppUser;
import it.meridiana.repo.AppUserRepository;
import it.meridiana.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(AppUserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public Api.LoginResponse login(Api.LoginRequest req) {
        AppUser user = users.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenziali non valide"));
        if (!user.isActive() || !encoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
        }
        return new Api.LoginResponse(jwt.issue(user), Mapper.user(user));
    }
}
