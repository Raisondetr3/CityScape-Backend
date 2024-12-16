package ru.itmo.cs.dto.human;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.itmo.cs.entity.User;

import java.time.ZonedDateTime;

@Getter
@Setter
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

