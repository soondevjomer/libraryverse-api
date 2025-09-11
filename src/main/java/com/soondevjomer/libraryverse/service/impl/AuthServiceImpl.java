package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.dto.LoginRequest;
import com.soondevjomer.libraryverse.dto.RefreshTokenRequest;
import com.soondevjomer.libraryverse.dto.RegisterRequest;
import com.soondevjomer.libraryverse.dto.Tokens;
import com.soondevjomer.libraryverse.model.User;
import com.soondevjomer.libraryverse.repository.UserRepository;
import com.soondevjomer.libraryverse.service.AuthService;
import com.soondevjomer.libraryverse.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

        @Transactional
        @Override
        public Tokens register(RegisterRequest registerRequest) {
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                throw new IllegalArgumentException("Username is already in use.");
            }

            User user = User.builder()
                    .name(registerRequest.getName())
                    .username(registerRequest.getUsername())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .role(registerRequest.getRole())
                    .build();
            userRepository.save(user);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            registerRequest.getUsername(),
                            registerRequest.getPassword()
                    )
            );

            return jwtService.generateTokens(authentication);
        }

        @Override
        public Tokens login(LoginRequest loginRequest) {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate Token Pair
            return jwtService.generateTokens(authentication);
        }

        public Tokens refreshToken(@Valid RefreshTokenRequest request) {

            String refreshToken = request.getRefreshToken();
            // check if it is valid refresh token
            if(!jwtService.isRefreshToken(refreshToken)) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            String user = jwtService.extractUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(user);

            if (userDetails == null) {
                throw new IllegalArgumentException("User not found");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            String accessToken = jwtService.generateAccessToken(authentication);
            return new Tokens(accessToken, refreshToken);
        }
}
