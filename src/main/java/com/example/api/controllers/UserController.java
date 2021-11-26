package com.example.api.controllers;

import com.example.api.controllers.payload.UserList;
import com.example.api.user.User;
import com.example.api.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
@Tag(name = "Users", description = "The Users of Enterprise APP")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserController(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    // get user data, take into account the user's role
    @Operation(summary = "Get user details", tags = {"admins"}, security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Details of user.",
                    content = {@Content(schema = @Schema(implementation = User.class),
                            mediaType = "application/json")  }),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Forbidden. No required permissions to get the resource.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "User not Found.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = { @Content(mediaType = "application/json") })
    })
    @GetMapping("/users/{id}")
    //for owner and admin only
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<User> getUser(@PathVariable long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // create a user, use the user role, check the login, email is not empty and does not exist in the database
    @Operation(summary = "Add new user", tags = {"admins"}, security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created user.",
                    content = {@Content()  }),
            @ApiResponse(responseCode = "400", description = "Bad request. Login or email or password equal null or is empty.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "409", description = "Bad request. Login or email address already exists.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = { @Content(mediaType = "application/json") })
    })
    @PostMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        String request;
        if (user.getId() != null) {
            request = "{\"error\": \"id is not null.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        if (user.getLogin() == null || user.getLogin().length() == 0) {
            request = "{\"error\": \"login is not correct. login cannot be equal to null or empty.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        Optional<User> userExist = userRepository.findByLogin(user.getLogin());
        if (userExist.isPresent()) {
            request = "{\"error\": \"login is incorrect. such a login already exists.\"}";
            return new ResponseEntity<>(request, HttpStatus.CONFLICT);
        }
        if (user.getEmail() == null || user.getEmail().length() == 0) {
            request = "{\"error\": \"email is not correct. email cannot be equal to null or empty.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        userExist = userRepository.findByEmail(user.getEmail());
        if (userExist.isPresent()) {
            request = "{\"error\": \"email is incorrect. such a email already exists.\"}";
            return new ResponseEntity<>(request, HttpStatus.CONFLICT);
        }
        if (user.getPassword() == null || user.getPassword().length() == 0) {
            request = "{\"error\": \"password is incorrect. password cannot be equal to null or empty.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // change user, user role, check login, email address, password
    @Operation(summary = "Update existing user", tags = {"admins"}, security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated.",
                    content = {@Content()  }),
            @ApiResponse(responseCode = "400", description = "Bad request. Login or email or password equal null or is empty.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = { @Content(mediaType = "application/json") })
    })
    @PutMapping("/users")
    //for owner and admin only
    @PreAuthorize("hasRole('ROLE_ADMIN') or #user.id == authentication.principal.id")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        String request;
        if (user.getId() == null) {
            request = "{\"error\": \"id is equal to null.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        Optional<User> userExist = userRepository.findById(user.getId());
        if (!userExist.isPresent()) {
            request = "{\"error\": \"id is incorrect. no user with this id.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        if (user.getLogin() == null || user.getLogin().length() == 0) {
            request = "{\"error\": \"login is not correct. login cannot be equal to null or empty.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        if (user.getEmail() == null || user.getEmail().length() == 0) {
            request = "{\"error\": \"email is not correct. email cannot be equal to null or empty.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        if (user.getPassword() == null || user.getPassword().length() == 0) {
            request = "{\"error\": \"password is incorrect. password cannot be equal to null or empty.\"}";
            return new ResponseEntity<>(request, HttpStatus.BAD_REQUEST);
        }
        try {
            user.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // delete user, take into account user role
    @Operation(summary = "Delete existing user", tags = {"admins"}, security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted.",
                    content = {@Content()}),
            @ApiResponse(responseCode = "403", description = "Unauthorized.",
                    content = {@Content()}),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = { @Content(mediaType = "application/json") })
    })
    @DeleteMapping ("/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // change user password, take into account user and owner role
    @Operation(summary = "Update password ", tags = {"admins"}, security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated.",
                    content = {@Content()}),
            @ApiResponse(responseCode = "400", description = "Bad request. Password cannot be equal to null or empty.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = { @Content(mediaType = "application/json") })
    })
    @PutMapping ("/users/setPassword/{id}")
    //for owner and admin only
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<String> setUserPassword(@PathVariable long id,
                                                  @Parameter(description = "New password")
                                                  @RequestParam(required = false) String newPassword) {
        if (newPassword == null || newPassword.length() == 0) {
            return new ResponseEntity<>("{\"error\":\"Password cannot be equal to null or empty.\"}",
                    HttpStatus.BAD_REQUEST);
        }
        try {
            Optional<User> userExist = userRepository.findById(id);
            User user;
            if (userExist.isPresent()) {
                user = userExist.get();
                user.setPassword(encoder.encode(newPassword));
                userRepository.save(user);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // list of all users, take into account the user's role
    @Operation(summary = "Get a user list", tags = {"admins"}, security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List the users.",
                    content = {@Content(array = @ArraySchema(schema = @Schema(implementation = UserList.class)),
                            mediaType = "application/json")  }),
            @ApiResponse(responseCode = "401", description = "Unauthorized.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Forbidden. No required permissions to get the resource.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "204", description = "No content.",
                    content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = { @Content(mediaType = "application/json") })
    })

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserList>> getAllUsers() {
        try {
            List<User> users = new ArrayList<>();
            userRepository.findAll().forEach(users::add);
            List <UserList> userList = new ArrayList<>();
            users.forEach(user -> {
                UserList userList1 = new UserList();
                userList1.setId(user.getId());
                userList1.setLogin(user.getLogin());
                userList1.setFullName(user.getFullName());
                userList1.setUserRole(user.getUserRole());
                userList1.setUrl_avatar(user.getUrl_avatar());
                userList1.setActive(user.getActive());
                userList1.setCreatedBy(user.getCreatedBy());
                userList1.setCreationDate(user.getCreationDate());
                userList1.setLastModifiedBy(user.getLastModifiedBy());
                userList1.setLastModifiedDate(user.getLastModifiedDate());
                userList.add(userList1);
            });
            if (users.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(userList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
