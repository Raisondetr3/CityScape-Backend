package ru.itmo.cs.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthLoginResponseDTO {

    private String token;

    private long expiresIn;
}
