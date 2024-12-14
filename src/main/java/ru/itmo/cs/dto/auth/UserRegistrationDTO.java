package ru.itmo.cs.dto.auth;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {
    @NotBlank
    @Size(min = 1)
    private String username;

    @NotBlank
    @Size(min = 6)
    private String password;
}