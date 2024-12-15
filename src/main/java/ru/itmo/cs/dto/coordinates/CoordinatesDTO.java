package ru.itmo.cs.dto.coordinates;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.cs.entity.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesDTO {

    private Long id;

    @NotNull
    @Min(1)
    @Max(820)
    private Long x;

    @NotNull
    private Double y;

    private User createdBy;
}