package com.example.serviceui.controller;

import com.example.serviceui.dto.AuthRequest;
import com.example.serviceui.dto.AuthResponse;
import com.example.serviceui.dto.UserDTO;
import com.example.serviceui.service.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Authentication API", description = "Endpoints for user registration and login")
public class AuthController {
    private final UserServiceClient userServiceClient;

    public AuthController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        log.info("Login attempt for email: {}", authRequest.getEmail());
        Optional<UserDTO> userDTO = userServiceClient.getUserByEmail(authRequest.getEmail());
        if (userDTO.isEmpty()) {
            log.warn("User not found for email: {}", authRequest.getEmail());
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("Пользователь с таким email не найден");
            return ResponseEntity.status(401).body(response);
        }

        String storedPassword = userDTO.get().getHashedPassword();
        String providedPassword = authRequest.getPassword();
        log.info("Stored password: '{}', Provided password: '{}'", storedPassword, providedPassword);
        if (storedPassword == null || !storedPassword.equals(providedPassword)) {
            log.warn("Invalid password for email: {}", authRequest.getEmail());
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("Неверный пароль");
            return ResponseEntity.status(401).body(response);
        }

        log.debug("Successful login for user: {}", authRequest.getEmail());
        AuthResponse response = new AuthResponse();
        response.setId(userDTO.get().getId());
        response.setEmail(userDTO.get().getEmail());
        response.setName(userDTO.get().getName());
        response.setSuccess(true);
        response.setMessage("Вход выполнен успешно");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Registers a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user data or email already exists")
    })
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
        log.info("Registering user with email: {}", userDTO.getEmail());
        Optional<UserDTO> existingUser = userServiceClient.getUserByEmail(userDTO.getEmail());
        if (existingUser.isPresent()) {
            log.warn("Email {} already registered", userDTO.getEmail());
            return ResponseEntity.badRequest().build();
        }
        if (userDTO.getEmail() == null || userDTO.getHashedPassword() == null || userDTO.getName() == null) {
            log.warn("Invalid registration data for email: {}", userDTO.getEmail());
            return ResponseEntity.badRequest().build();
        }
        UserDTO createdUser = userServiceClient.createUser(userDTO);
        log.debug("Registered user with ID: {}", createdUser.getId());
        return ResponseEntity.ok(createdUser);
    }
}