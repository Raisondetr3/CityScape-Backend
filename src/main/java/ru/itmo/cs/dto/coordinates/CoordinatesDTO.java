package ru.itmo.cs.dto.coordinates;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.itmo.cs.entity.User;

import java.util.Objects;

@Getter
@Setter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordinatesDTO that = (CoordinatesDTO) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}