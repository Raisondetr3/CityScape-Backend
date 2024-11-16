package ru.itmo.cs.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.cs.entity.User;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HumanDTO {
    private Long id;
    @NotNull
    @Size(min = 1)
    private String name;

    @Min(1)
    private int age;

    @Min(1)
    private int height;

    private ZonedDateTime birthday;

    private User createdBy;
}

