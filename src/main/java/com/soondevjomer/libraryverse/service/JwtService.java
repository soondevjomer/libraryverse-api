package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.constant.Role;
import com.soondevjomer.libraryverse.dto.Tokens;
import com.soondevjomer.libraryverse.exception.InvalidJwtException;
import com.soondevjomer.libraryverse.model.Customer;
import com.soondevjomer.libraryverse.model.User;
import com.soondevjomer.libraryverse.repository.CustomerRepository;
import com.soondevjomer.libraryverse.repository.LibraryRepository;
import com.soondevjomer.libraryverse.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;


    public Tokens generateTokens(Authentication authentication) {
        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);

        return new Tokens(accessToken, refreshToken);
    }

    // Generate access token
    public String generateAccessToken(Authentication authentication) {
        log.info("request for access token");
        return generateToken(authentication, jwtExpirationMs, new HashMap<>());
    }

    // Generate refresh token
    public String generateRefreshToken(Authentication authentication) {
        Map<String, String> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        log.info("request for refresh token");
        return generateToken(authentication, refreshExpirationMs, claims);
    }

    private String generateToken(Authentication authentication, long expirationInMs, Map<String, String> claims) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date(); // Time of token creation
        Date expiryDate = new Date(now.getTime() + expirationInMs); // Time of token expiration
        String role = userPrincipal.getAuthorities()
                .iterator().next().getAuthority();
        claims.put("name", authentication.getName());
        claims.put("username", userPrincipal.getUsername());
        claims.put("role", role);

        User currentUser = userRepository.findByUsername(userPrincipal.getUsername())
                        .orElseThrow(()->new NoSuchElementException("User not found"));
        if (currentUser!=null) {
            claims.put("email", currentUser.getEmail());
            claims.put("userId", currentUser.getId().toString());
            claims.put("image", currentUser.getImage());
        }

        if (role.equals(Role.LIBRARIAN.toString())) {
            libraryRepository.findByOwnerUsername(userPrincipal.getUsername())
                    .ifPresent(library -> {
                        claims.put("libraryId", String.valueOf(library.getId()));
                        claims.put("libraryName", String.valueOf(library.getName()));
            });
        }

        if (role.equals(Role.READER.toString())) {
            Optional<Customer> optionalCustomer = customerRepository.findByUser(currentUser);
            optionalCustomer.ifPresent((customer) -> {
                claims.put("customerId", String.valueOf(customer.getId()));
                claims.put("address", String.valueOf(customer.getAddress()));
                claims.put("contactNumber", String.valueOf(customer.getContactNumber()));
            });
        }

        log.info("generate token");
        return Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(userPrincipal.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    // Validate token
    public boolean validateTokenForUser(String token, UserDetails userDetails) {
        final String username = extractUsernameFromToken(token);
        return username != null
                && username.equals(userDetails.getUsername());
    }

    public boolean isValidToken(String token) {
        return extractAllClaims(token) != null;
    }

    public String extractUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);

        if(claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    // Validate if the token is refresh token
    public boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        if(claims == null) {
            return false;
        }
        return "refresh".equals(claims.get("tokenType"));
    }

    private Claims extractAllClaims(String token) {
        Claims claims = null;

        try {
            claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException("Invalid or JWT token expired", e);
        }

        return claims;
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
