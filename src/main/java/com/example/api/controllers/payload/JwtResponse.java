package com.example.api.controllers.payload;

import lombok.Getter;
import java.util.List;

public class JwtResponse {
    @Getter
    private final String token;
    @Getter
    private final Long id;
    @Getter
    private final String username;
    @Getter
    private final String email;
    @Getter
    private final List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, String email, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
