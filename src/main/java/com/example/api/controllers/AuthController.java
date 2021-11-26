package com.example.api.controllers;

import com.example.api.controllers.payload.JwtResponse;
import com.example.api.controllers.payload.LoginRequest;
import com.example.api.security.UserDetailsImpl;
import com.example.api.security.jwt.JwtUtils;
import com.example.api.security.pwdreset.TokenPasswordResetService;
import com.example.api.services.MailService;
import com.example.api.user.User;
import com.example.api.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Oleg Turchin
 */
@Log4j2
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication of users")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final TokenPasswordResetService tokenPasswordResetService;
    private final MailService mailService;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          TokenPasswordResetService tokenPasswordResetService,
                          MailService mailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.tokenPasswordResetService = tokenPasswordResetService;
        this.mailService = mailService;
    }

    @Operation(summary = "Authenticate user", security = {@SecurityRequirement(name = "none")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A successful response.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = { @Content(mediaType = "application/json") })
    })
    @PostMapping(path=  "/signin", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(
                authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @Operation(summary = "Forgot password. Password reset.", security = {@SecurityRequirement(name = "none")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A successful response.",
                    content = {@Content()}),
            @ApiResponse(responseCode = "404", description = "User with the specified email was not found.",
                    content = {@Content()})
    })
    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword (HttpServletRequest request,
                                            @RequestParam("email") String email) {
        Optional<User> findUser = userRepository.findByEmail(email);
        if (!findUser.isPresent()) {
            return new ResponseEntity<>("error", HttpStatus.NOT_FOUND);
        } else {
            String token = UUID.randomUUID().toString();
            tokenPasswordResetService.createPasswordResetToken(findUser.get(), token);
            mailService.sendEmailResetPassword(getAppUrl(request.getRequestURL().toString()),
                    token, findUser.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Operation(summary = "Change password with password reset token.", security = {@SecurityRequirement(name = "none")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A successful response.",
                    content = {@Content()}),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = {@Content()})
    })
    @SneakyThrows
    @GetMapping("/changePassword")
    public String changePassword(HttpServletRequest request,
                                 @RequestParam("token") String token) {

        String htmlDoc;
        String result = tokenPasswordResetService.validatePasswordResetToken(token);
        switch (result) {
            case "tokenIsValid":
                htmlDoc = new String(Files.readAllBytes(Paths.get("templates/new_password.html")));
                Optional<User> user = tokenPasswordResetService.getUser(token);
                JwtUtils jwtUtils = new JwtUtils();
                if (user.isPresent()) {
                    String jwtToken = jwtUtils.generateJwtTokenForResetPassword(user.get(),
                           jwtSecret);
                    htmlDoc = htmlDoc.replace("{{serverContext}}",
                                        getAppUrl(request.getRequestURL().toString()));
                    htmlDoc = htmlDoc.replace("{{userId}}", user.get().getId() + "")
                                    .replace("{{jwtToken}}", jwtToken);
                }
                return htmlDoc;
            case "expired":
                htmlDoc = new String(Files.readAllBytes(Paths.get("templates/expired_token_reset_password.html")));
                return htmlDoc;
            case "invalidToken":
            default:
                htmlDoc = new String(Files.readAllBytes(Paths.get("templates/invalid_token_reset_password.html")));
                return htmlDoc;
        }
    }

    @SneakyThrows
    private String getAppUrl (String urlRequest) {
        URL url = new URL(urlRequest);
        return url.getProtocol() + "://" + url.getAuthority();
    }
}
