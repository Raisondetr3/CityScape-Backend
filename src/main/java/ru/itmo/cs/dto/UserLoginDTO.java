package ru.itmo.cs.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class UserLoginDTO {
    @NotBlank(message = "username cannot be empty")
    private String username;

    @NotBlank(message = "password cannot be empty")
    private String password;
}
