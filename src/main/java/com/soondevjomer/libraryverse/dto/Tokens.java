package com.soondevjomer.libraryverse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tokens {
    private String accessToken;
    private String refreshToken;
}
