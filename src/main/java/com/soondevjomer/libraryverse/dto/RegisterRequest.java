package com.soondevjomer.libraryverse.dto;

import com.soondevjomer.libraryverse.constant.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {

    private String name;

    private String username;

    private String password;

    private String email;

    private Role role;
}
