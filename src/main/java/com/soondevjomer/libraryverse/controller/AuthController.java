package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.dto.LoginRequest;
import com.soondevjomer.libraryverse.dto.RefreshTokenRequest;
import com.soondevjomer.libraryverse.dto.RegisterRequest;
import com.soondevjomer.libraryverse.dto.Tokens;
import com.soondevjomer.libraryverse.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        Tokens tokens = authService.register(registerRequest);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        Tokens tokens = authService.login(loginRequest);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        Tokens tokens = authService.refreshToken(request);
        return ResponseEntity.ok(tokens);
    }
}
