package ru.itmo.cs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.cs.entity.enums.AdminRequestStatus;
import ru.itmo.cs.entity.enums.UserRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;

    @NotBlank
    private String username;

    @NotNull
    private UserRole role;

    private AdminRequestStatus adminRequestStatus;
}

