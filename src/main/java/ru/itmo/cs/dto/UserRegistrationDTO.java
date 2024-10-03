package ru.itmo.cs.dto;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UserRegistrationDTO {
    @NotBlank
    @Size(min = 1)
    private String username;

    @NotBlank
    @Size(min = 6)
    private String password;
}
