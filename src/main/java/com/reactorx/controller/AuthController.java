package com.reactorx.controller;

import com.reactorx.dto.AuthRequestDTO;
import com.reactorx.dto.AuthResponseDTO;
import com.reactorx.dto.UserDTO;
import com.reactorx.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest) {
        // This calls AuthService.login, which handles AuthenticationManager usage.
        AuthResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthRequestDTO registerRequest) {
        try {
            // This handles validation, encoding, and saving.
            UserDTO user = authService.register(registerRequest);

            // Log in the user immediately after successful registration
            AuthResponseDTO loginResponse = authService.login(registerRequest);

            // Return the login response to the frontend to complete the flow
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            // Use ResponseEntity.badRequest() which is handled by GlobalExceptionHandler
            // This returns a standard JSON error message if registration fails (e.g., "Email already exists!")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}