package com.example.api.security.pwdreset;

import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface TokenPasswordResetRepository extends CrudRepository<TokenPasswordReset, Long> {
    Optional<TokenPasswordReset> findByToken(String token);
}
