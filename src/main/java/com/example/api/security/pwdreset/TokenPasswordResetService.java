package com.example.api.security.pwdreset;

import com.example.api.user.User;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenPasswordResetService {

    private final TokenPasswordResetRepository tokenPasswordResetRepository;

    public TokenPasswordResetService(TokenPasswordResetRepository tokenPasswordResetRepository) {
        this.tokenPasswordResetRepository = tokenPasswordResetRepository;
    }

    public void createPasswordResetToken (User user, String token) {
        TokenPasswordReset tokenPasswordReset = new TokenPasswordReset();
        tokenPasswordReset.setUser(user);
        tokenPasswordReset.setToken(token);
        tokenPasswordReset.setExpirationDate(new Date(new Date().getTime() + 86400000L));
        tokenPasswordResetRepository.save(tokenPasswordReset);
    }

    public Optional<User> getUser (String token) {
        final Optional<TokenPasswordReset> passToken = tokenPasswordResetRepository.findByToken(token);
        Optional<User> user;
        if (passToken.isPresent()) {
            if (!isTokenExpired(passToken.get().getExpirationDate())) {
                user = Optional.of(passToken.get().getUser());
                return user;
            }
        }
        user = Optional.empty();
        return user;
    }

    public String validatePasswordResetToken (String token) {
        final Optional<TokenPasswordReset> passToken = tokenPasswordResetRepository.findByToken(token);
        return passToken.map(tokenPasswordReset ->
                isTokenExpired(tokenPasswordReset.getExpirationDate()) ? "expired"
                : "tokenIsValid").orElse("invalidToken");
    }

    protected boolean isTokenExpired (Date expirationDate) {
        long duration = new Date().getTime() - expirationDate.getTime();
        return duration > 0;
    }
}
