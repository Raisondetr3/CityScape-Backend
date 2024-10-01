package ru.itmo.cs.entity;

import com.sun.istack.NotNull;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "coordinates")
@Getter
@Setter
@NoArgsConstructor
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Max(820)
    private Long x;

    @NotNull
    private Double y;
}
