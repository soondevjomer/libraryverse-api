package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.LoginRequest;
import com.soondevjomer.libraryverse.dto.RefreshTokenRequest;
import com.soondevjomer.libraryverse.dto.RegisterRequest;
import com.soondevjomer.libraryverse.dto.Tokens;

public interface AuthService {

    Tokens register(RegisterRequest registerRequest);

    Tokens login(LoginRequest loginRequest);

    Tokens refreshToken(RefreshTokenRequest request);
}
